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

    // Variáveis da Foto
    private var latestTmpUri: Uri? = null
    private var finalPhotoPath: String? = null

    // Variáveis do Sensor de Luz
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private var currentLightLevel: Float? = null
    private var isReadingLight = false // Para controlar se estamos a ler ou não

    // Referências UI
    private lateinit var ivPlantPhoto: ImageView
    private lateinit var tvLightInfo: TextView
    private lateinit var btnLight: Button

    // Contratos da Câmara (Mantém-se igual)
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

        // 1. Configurar o Sensor Manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        // Referências UI
        val etName = findViewById<EditText>(R.id.etPlantName)
        val etDesc = findViewById<EditText>(R.id.etPlantDesc)
        val etFreq = findViewById<EditText>(R.id.etWaterFreq)
        val btnSave = findViewById<Button>(R.id.btnSavePlant)
        val btnCamera = findViewById<Button>(R.id.btnTakePhoto)

        // componentes de Luz
        btnLight = findViewById(R.id.btnMeasureLight)
        tvLightInfo = findViewById(R.id.tvLightLevel)
        ivPlantPhoto = findViewById(R.id.imageViewPlant)

        val database = WaterMeDatabase.getDatabase(this)
        val repository = PlantRepository(database.plantDao())

        //  Câmara
        btnCamera.setOnClickListener {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        //  Medir Luz
        btnLight.setOnClickListener {
            if (lightSensor == null) {
                Toast.makeText(this, "Este dispositivo não tem sensor de luz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isReadingLight) {
                // Começar a ler
                sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
                isReadingLight = true
                btnLight.text = "Parar Leitura"
                Toast.makeText(this, "A ler luz ambiente...", Toast.LENGTH_SHORT).show()
            } else {
                // Parar de ler (fixar o valor)
                stopLightSensor()
                Toast.makeText(this, "Valor de luz fixado!", Toast.LENGTH_SHORT).show()
            }
        }

        // guardar
        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val freqStr = etFreq.text.toString()

            if (name.isEmpty() || freqStr.isEmpty()) {
                Toast.makeText(this, "Preencha nome e frequência!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newPlant = Plant(
                name = name,
                description = etDesc.text.toString(),
                waterFrequency = freqStr.toInt(),
                photoUri = finalPhotoPath,
                latitude = null,  // GPS fica para depois
                longitude = null, // GPS fica para depois
                lightLevel = currentLightLevel, // <--- Guardamos o nível de luz!
                lastWateredDate = null,
                nextWateringDate = System.currentTimeMillis() + (freqStr.toLong() * 86400000)
            )

            lifecycleScope.launch(Dispatchers.IO) {
                repository.insert(newPlant)
                runOnUiThread {
                    Toast.makeText(this@AddPlantActivity, "Planta guardada!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    // --- Métodos do SensorEventListener ---

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val luxValue = event.values[0]
            currentLightLevel = luxValue

            // Atualiza a UI em tempo real
            tvLightInfo.text = "Luz: $luxValue lx"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    // Função auxiliar para parar o sensor
    private fun stopLightSensor() {
        if (isReadingLight) {
            sensorManager.unregisterListener(this)
            isReadingLight = false
            btnLight.text = "Medir Luz Novamente"
        }
    }

    // Parar o sensor se sairmos da aplicação para não gastar bateria
    override fun onPause() {
        super.onPause()
        stopLightSensor()
    }

    // --- Métodos Auxiliares da Câmara ---
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