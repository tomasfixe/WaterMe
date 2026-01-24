package pt.ipt.dam.waterme

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.ipt.dam.waterme.data.database.WaterMeDatabase
import pt.ipt.dam.waterme.data.model.Plant
import pt.ipt.dam.waterme.data.repository.PlantRepository
import java.io.File

/**
 * Activity responsável por Adicionar (Criar) ou Editar uma Planta.
 *
 * Funcionalidades principais:
 * 1. Formulário de dados (Nome, Descrição, Frequência).
 * 2. Integração com a Câmara para tirar fotos.
 * 3. Integração com o Sensor de Luz para medir a luminosidade ambiente.
 * 4. Lógica dual: serve tanto para criar nova planta como para atualizar uma existente.
 */
class AddPlantActivity : AppCompatActivity(), SensorEventListener {

    // Variáveis para gerir a captura de imagem
    private var latestTmpUri: Uri? = null   // URI temporário antes de tirar a foto
    private var finalPhotoPath: String? = null // Caminho final da imagem guardada

    // Variáveis de Controlo de Edição
    private var isEditMode = false // Flag para saber se estamos a criar ou editar
    private var editPlantId = 0    // ID da planta a editar
    private var oldPhotoPath: String? = null // Para não perder a foto antiga se o user não tirar nova

    // Variáveis do Hardware (Sensor)
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private var currentLightLevel: Float? = null // Valor lido pelo sensor
    private var isReadingLight = false // Estado do botão (Lendo vs Parado)

    // Elementos da Interface Gráfica (UI)
    private lateinit var ivPlantPhoto: ImageView
    private lateinit var tvLightInfo: TextView
    private lateinit var btnLight: Button
    private lateinit var etName: EditText
    private lateinit var etDesc: EditText
    private lateinit var etFreq: EditText
    private lateinit var btnSave: Button

    /**
     * Contrato para tirar uma foto.
     * Recebe um boolean (sucesso/falha) após a câmara fechar.
     * Se for sucesso, define a imagem na ImageView.
     */
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            latestTmpUri?.let { uri ->
                ivPlantPhoto.setImageURI(uri) // Mostra a foto no ecrã
                finalPhotoPath = uri.toString() // Guarda o caminho para salvar na BD
            }
        }
    }

    /**
     * Contrato para pedir permissão de uso da câmara em tempo de execução.
     */
    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) launchCamera() else Toast.makeText(this, "Sem permissão de câmara", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_plant)

        // Inicializar Views (ligar XML ao Kotlin)
        etName = findViewById(R.id.etPlantName)
        etDesc = findViewById(R.id.etPlantDesc)
        etFreq = findViewById(R.id.etWaterFreq)
        btnSave = findViewById(R.id.btnSavePlant)
        val btnCamera = findViewById<Button>(R.id.btnTakePhoto)
        btnLight = findViewById(R.id.btnMeasureLight)
        tvLightInfo = findViewById(R.id.tvLightLevel)
        ivPlantPhoto = findViewById(R.id.imageViewPlant)

        // Inicializar Sensores e Base de Dados
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) // Obtém sensor de luz

        val database = WaterMeDatabase.getDatabase(this)
        // Instancia o repositório para gerir os dados
        val repository = PlantRepository(database.plantDao(), database.plantLogDao(), applicationContext)

        // --- LÓGICA DE EDIÇÃO ---
        // Verificar se chegámos aqui via "Editar Planta" ou "Adicionar Nova"
        checkEditMode()

        // Configurar Botões
        btnCamera.setOnClickListener {
            // Pede permissão antes de abrir a câmara
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        btnLight.setOnClickListener {
            handleLightSensor() // Inicia ou para a leitura de luz
        }

        btnSave.setOnClickListener {
            saveOrUpdatePlant(repository) // Grava na BD
        }
    }

    /**
     * Verifica os extras do Intent para saber se estamos em modo de edição.
     * Se sim, preenche os campos com os dados existentes da planta.
     */
    // Função para verificar se estamos a editar
    private fun checkEditMode() {
        if (intent.getBooleanExtra("IS_EDIT_MODE", false)) {
            isEditMode = true
            editPlantId = intent.getIntExtra("PLANT_ID", 0)

            // Mudar título do botão para refletir a ação correta
            btnSave.text = "Atualizar Planta"

            // Preencher campos de texto
            etName.setText(intent.getStringExtra("PLANT_NAME"))
            etDesc.setText(intent.getStringExtra("PLANT_DESC"))
            etFreq.setText(intent.getIntExtra("PLANT_FREQ", 1).toString())

            // Recuperar a foto antiga
            oldPhotoPath = intent.getStringExtra("PLANT_PHOTO")
            if (oldPhotoPath != null) {
                ivPlantPhoto.setImageURI(Uri.parse(oldPhotoPath))
                finalPhotoPath = oldPhotoPath // Se não tirar nova, usa a antiga
            }
        }
    }

    /**
     * Valida os dados de entrada e decide se faz um INSERT ou um UPDATE na base de dados.
     */
    private fun saveOrUpdatePlant(repository: PlantRepository) {
        val name = etName.text.toString()
        val freqStr = etFreq.text.toString()

        // Validação simples
        if (name.isEmpty() || freqStr.isEmpty()) {
            Toast.makeText(this, "Preencha nome e frequência!", Toast.LENGTH_SHORT).show()
            return
        }

        // Cálculo da próxima rega
        // Se estiver em modo edição, mantém as datas antigas. Se for novo, cria datas a partir de hoje.
        // 86400000 ms = 1 dia
        val nextDate = System.currentTimeMillis() + (freqStr.toLong() * 86400000)

        // Criar o objeto Planta
        val plantToSave = Plant(
            id = if (isEditMode) editPlantId else 0, // ID 0 faz trigger ao AutoGenerate do Room (Insert)
            name = name,
            description = etDesc.text.toString(),
            waterFrequency = freqStr.toInt(),
            photoUri = finalPhotoPath,
            lightLevel = currentLightLevel, // Atualiza a luz se tiver medido de novo
            lastWateredDate = null,
            nextWateringDate = nextDate
        )

        // Executar a operação de IO em background
        lifecycleScope.launch(Dispatchers.IO) {
            if (isEditMode) {
                repository.update(plantToSave)
            } else {
                repository.insert(plantToSave)
            }

            // Voltar à thread principal para mostrar feedback e fechar a janela
            runOnUiThread {
                Toast.makeText(this@AddPlantActivity, if(isEditMode) "Planta atualizada!" else "Planta criada!", Toast.LENGTH_SHORT).show()
                finish() // Fecha a Activity e volta à lista
            }
        }
    }

    /**
     * Gere o estado do sensor de luz (Ligar/Desligar).
     * Alterna entre registar o listener e cancelar o registo.
     */
    private fun handleLightSensor() {
        if (lightSensor == null) {
            Toast.makeText(this, "Sem sensor de luz!", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isReadingLight) {
            // Começa a ouvir o sensor
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
            isReadingLight = true
            btnLight.text = "Parar Leitura"
            Toast.makeText(this, "A ler...", Toast.LENGTH_SHORT).show()
        } else {
            stopLightSensor() // Para a leitura
            Toast.makeText(this, "Valor fixado!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Callback chamado sempre que o sensor deteta uma mudança na luminosidade.
     * Aqui convertemos o valor bruto (Lux) para uma escala de 1 a 10.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val luxValue = event.values[0]

            // --- ESCALA 1-10 ---
            // Consideramos que 1000 lux já é "Brilho Máximo" (10) para interior
            // A função coerceIn(1, 10) garante que o valor nunca é inferior a 1 nem superior a 10
            val scaleValue = (luxValue / 100).toInt().coerceIn(1, 10)

            // Guardamos o valor da escala (1 a 10) em vez do valor bruto
            currentLightLevel = scaleValue.toFloat()

            // Atualizar texto no ecrã para o utilizador perceber o valor lido
            tvLightInfo.text = "Luminosidade: Nível $scaleValue/10"

            if (scaleValue <= 2) {
                // Se for muito escuro, usar vermelho para alertar
                tvLightInfo.setTextColor(android.graphics.Color.RED)
            } else {
                // Se houver luz suficiente, usar branco
                tvLightInfo.setTextColor(android.graphics.Color.WHITE)
            }
        }
    }

    // Não precisamos deste método para o sensor de luz, mas é obrigatório pela interface
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * Remove o listener do sensor.
     * Importante chamar no onPause ou quando o user clica em "Parar".
     */
    private fun stopLightSensor() {
        if (isReadingLight) {
            sensorManager.unregisterListener(this)
            isReadingLight = false
            btnLight.text = "Medir Luz"
        }
    }

    /**
     * Chamado quando a aplicação vai para background.
     * Paramos o sensor para não gastar bateria desnecessariamente.
     */
    override fun onPause() {
        super.onPause()
        stopLightSensor()
    }

    /**
     * Prepara o ficheiro temporário e lança o Intent da câmara.
     * Usa 'lifecycleScope' para garantir que a Activity está num estado válido.
     */
    private fun launchCamera() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takePictureLauncher.launch(uri) // Abre a app da câmara
            }
        }
    }

    /**
     * Cria um ficheiro temporário na cache da app para guardar a foto tirada.
     * Usa o FileProvider para partilhar o acesso ao ficheiro com a app da câmara de forma segura.
     * @return O URI (Content URI) do ficheiro criado.
     */
    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit() // Garante que o ficheiro é limpo se a app fechar
        }
        // O 'authority' deve coincidir com o definido no AndroidManifest.xml
        return FileProvider.getUriForFile(applicationContext, "pt.ipt.dam.waterme.provider", tmpFile)
    }
}