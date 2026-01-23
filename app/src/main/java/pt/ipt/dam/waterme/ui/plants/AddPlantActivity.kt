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

class AddPlantActivity : AppCompatActivity(), SensorEventListener {

    private var latestTmpUri: Uri? = null
    private var finalPhotoPath: String? = null

    // Variáveis de Controlo de Edição
    private var isEditMode = false
    private var editPlantId = 0
    private var oldPhotoPath: String? = null // Para não perder a foto se o user não tirar nova

    // Variáveis Sensor
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private var currentLightLevel: Float? = null
    private var isReadingLight = false

    private lateinit var ivPlantPhoto: ImageView
    private lateinit var tvLightInfo: TextView
    private lateinit var btnLight: Button
    private lateinit var etName: EditText
    private lateinit var etDesc: EditText
    private lateinit var etFreq: EditText
    private lateinit var btnSave: Button

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            latestTmpUri?.let { uri ->
                ivPlantPhoto.setImageURI(uri)
                finalPhotoPath = uri.toString()
            }
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) launchCamera() else Toast.makeText(this, "Sem permissão de câmara", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_plant)

        // Inicializar Views
        etName = findViewById(R.id.etPlantName)
        etDesc = findViewById(R.id.etPlantDesc)
        etFreq = findViewById(R.id.etWaterFreq)
        btnSave = findViewById(R.id.btnSavePlant)
        val btnCamera = findViewById<Button>(R.id.btnTakePhoto)
        btnLight = findViewById(R.id.btnMeasureLight)
        tvLightInfo = findViewById(R.id.tvLightLevel)
        ivPlantPhoto = findViewById(R.id.imageViewPlant)

        // Inicializar Sensores e DB
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        val database = WaterMeDatabase.getDatabase(this)
        // Adicionámos 'applicationContext' como 3º argumento
        val repository = PlantRepository(database.plantDao(), database.plantLogDao(), applicationContext)

        // --- LÓGICA DE EDIÇÃO ---
        checkEditMode()

        btnCamera.setOnClickListener {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        btnLight.setOnClickListener {
            handleLightSensor()
        }

        btnSave.setOnClickListener {
            saveOrUpdatePlant(repository)
        }
    }

    // Função para verificar se estamos a editar
    private fun checkEditMode() {
        if (intent.getBooleanExtra("IS_EDIT_MODE", false)) {
            isEditMode = true
            editPlantId = intent.getIntExtra("PLANT_ID", 0)

            // Mudar título do botão
            btnSave.text = "Atualizar Planta"

            // Preencher campos
            etName.setText(intent.getStringExtra("PLANT_NAME"))
            etDesc.setText(intent.getStringExtra("PLANT_DESC"))
            etFreq.setText(intent.getIntExtra("PLANT_FREQ", 1).toString())

            // Foto antiga
            oldPhotoPath = intent.getStringExtra("PLANT_PHOTO")
            if (oldPhotoPath != null) {
                ivPlantPhoto.setImageURI(Uri.parse(oldPhotoPath))
                finalPhotoPath = oldPhotoPath // Se não tirar nova, usa a antiga
            }
        }
    }

    // Função separada para salvar/atualizar
    private fun saveOrUpdatePlant(repository: PlantRepository) {
        val name = etName.text.toString()
        val freqStr = etFreq.text.toString()

        if (name.isEmpty() || freqStr.isEmpty()) {
            Toast.makeText(this, "Preencha nome e frequência!", Toast.LENGTH_SHORT).show()
            return
        }

        // Se estiver em modo edição, mantém as datas antigas. Se for novo, cria datas.
        val nextDate = System.currentTimeMillis() + (freqStr.toLong() * 86400000)

        val plantToSave = Plant(
            id = if (isEditMode) editPlantId else 0, // ID 0 = Insert, ID > 0 = Update
            name = name,
            description = etDesc.text.toString(),
            waterFrequency = freqStr.toInt(),
            photoUri = finalPhotoPath,
            lightLevel = currentLightLevel, // Atualiza a luz se tiver medido de novo
            lastWateredDate = null,
            nextWateringDate = nextDate
        )

        lifecycleScope.launch(Dispatchers.IO) {
            if (isEditMode) {
                repository.update(plantToSave)
            } else {
                repository.insert(plantToSave)
            }

            runOnUiThread {
                Toast.makeText(this@AddPlantActivity, if(isEditMode) "Planta atualizada!" else "Planta criada!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun handleLightSensor() {
        if (lightSensor == null) {
            Toast.makeText(this, "Sem sensor de luz!", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isReadingLight) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
            isReadingLight = true
            btnLight.text = "Parar Leitura"
            Toast.makeText(this, "A ler...", Toast.LENGTH_SHORT).show()
        } else {
            stopLightSensor()
            Toast.makeText(this, "Valor fixado!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val luxValue = event.values[0]

            // --- ESCALA 1-10 ---
            // Consideramos que 1000 lux já é "Brilho Máximo" (10) para interior
            // A função coerceIn(1, 10) garante que o valor nunca é inferior a 1 nem superior a 10
            val scaleValue = (luxValue / 100).toInt().coerceIn(1, 10)

            // Guardamos o valor da escala (1 a 10) em vez do valor bruto
            currentLightLevel = scaleValue.toFloat()

            // Atualizar texto no ecrã para o utilizador perceber
            tvLightInfo.text = "Luminosidade: Nível $scaleValue/10"

            // Dica visual (opcional): mudar a cor se for muito escuro
            if (scaleValue <= 2) {
                tvLightInfo.setTextColor(android.graphics.Color.RED)
            } else {
                tvLightInfo.setTextColor(android.graphics.Color.WHITE)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun stopLightSensor() {
        if (isReadingLight) {
            sensorManager.unregisterListener(this)
            isReadingLight = false
            btnLight.text = "Medir Luz"
        }
    }

    override fun onPause() {
        super.onPause()
        stopLightSensor()
    }

    private fun launchCamera() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takePictureLauncher.launch(uri)
            }
        }
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(applicationContext, "pt.ipt.dam.waterme.provider", tmpFile)
    }
}