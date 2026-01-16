package pt.ipt.dam.waterme

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.ipt.dam.waterme.data.database.WaterMeDatabase
import pt.ipt.dam.waterme.data.model.Plant
import pt.ipt.dam.waterme.data.repository.PlantRepository

class AddPlantActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_plant)

        // Referências aos elementos do ecrã
        val etName = findViewById<EditText>(R.id.etPlantName)
        val etDesc = findViewById<EditText>(R.id.etPlantDesc)
        val etFreq = findViewById<EditText>(R.id.etWaterFreq)
        val btnSave = findViewById<Button>(R.id.btnSavePlant)

        // Inicializar Repositório (acesso direto rápido, sem ViewModel por simplificação aqui)
        val database = WaterMeDatabase.getDatabase(this)
        val repository = PlantRepository(database.plantDao())

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val desc = etDesc.text.toString()
            val freqStr = etFreq.text.toString()

            if (name.isEmpty() || freqStr.isEmpty()) {
                Toast.makeText(this, "Preencha o nome e a frequência!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Criar o objeto Planta
            val newPlant = Plant(
                name = name,
                description = desc,
                waterFrequency = freqStr.toInt(),
                photoUri = null, // Implementaremos a seguir
                latitude = null, // Implementaremos a seguir
                longitude = null,// Implementaremos a seguir
                lastWateredDate = null,
                nextWateringDate = System.currentTimeMillis() + (freqStr.toLong() * 86400000) // Calcula data futura
            )

            // Guardar em Background
            lifecycleScope.launch(Dispatchers.IO) {
                repository.insert(newPlant)
                runOnUiThread {
                    Toast.makeText(this@AddPlantActivity, "Planta guardada!", Toast.LENGTH_SHORT).show()
                    finish() // Fecha a atividade e volta à lista
                }
            }
        }
    }
}