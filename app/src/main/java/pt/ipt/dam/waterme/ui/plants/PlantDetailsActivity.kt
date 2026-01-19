package pt.ipt.dam.waterme

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.ipt.dam.waterme.data.database.WaterMeDatabase
import pt.ipt.dam.waterme.data.repository.PlantRepository
import pt.ipt.dam.waterme.databinding.ActivityPlantDetailsBinding

class PlantDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlantDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlantDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Receber dados
        val plantId = intent.getIntExtra("PLANT_ID", -1)
        val name = intent.getStringExtra("PLANT_NAME") ?: "Sem Nome"
        val desc = intent.getStringExtra("PLANT_DESC")
        val freq = intent.getIntExtra("PLANT_FREQ", 1)
        val light = intent.getFloatExtra("PLANT_LIGHT", -1f)
        val photoUri = intent.getStringExtra("PLANT_PHOTO")

        // 2. Base de Dados
        val db = WaterMeDatabase.getDatabase(this)
        val repo = PlantRepository(db.plantDao(), db.plantLogDao())

        // 3. Preencher UI
        binding.tvDetailName.text = name
        binding.tvDetailFreq.text = "$freq dias"

        if (desc.isNullOrEmpty()) {
            binding.tvDetailDesc.text = "Sem descrição disponível."
        } else {
            binding.tvDetailDesc.text = desc
        }

        if (light >= 0) {
            binding.tvDetailLight.text = "$light lx"
        } else {
            binding.tvDetailLight.text = "N/A"
        }

        if (!photoUri.isNullOrEmpty()) {
            try {
                binding.ivDetailPhoto.setImageURI(Uri.parse(photoUri))
            } catch (e: Exception) {
                binding.ivDetailPhoto.setImageResource(android.R.drawable.ic_menu_report_image)
            }
        }

        // 4. Botão "Reguei"
        binding.btnWaterNow.setOnClickListener {
            if (plantId == -1) return@setOnClickListener

            lifecycleScope.launch(Dispatchers.IO) {
                repo.waterPlant(plantId, freq)
                runOnUiThread {
                    Toast.makeText(this@PlantDetailsActivity, "Planta regada! 💧", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 5. Botão "Editar" (Atualizado)
        binding.btnEditPlant.setOnClickListener {
            val intent = android.content.Intent(this, AddPlantActivity::class.java)
            // Passar dados para edição
            intent.putExtra("IS_EDIT_MODE", true)
            intent.putExtra("PLANT_ID", plantId)
            intent.putExtra("PLANT_NAME", name)
            intent.putExtra("PLANT_DESC", desc)
            intent.putExtra("PLANT_FREQ", freq)
            intent.putExtra("PLANT_PHOTO", photoUri)

            startActivity(intent)
            finish() // Fecha esta página para forçar recarregamento ao voltar
        }

        // 6. Botão "Apagar"
        binding.btnDeletePlant.setOnClickListener {
            if (plantId == -1) {
                Toast.makeText(this, "Erro: ID inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setTitle("Apagar Planta")
                .setMessage("Tem a certeza que quer eliminar a planta '$name'?")
                .setPositiveButton("Sim, apagar") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        repo.deleteById(plantId)
                        runOnUiThread {
                            Toast.makeText(this@PlantDetailsActivity, "Planta eliminada.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}