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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        var nextWatering = intent.getLongExtra("PLANT_NEXT", 0L)

        // 2. Setup
        val db = WaterMeDatabase.getDatabase(this)
        val repo = PlantRepository(db.plantDao(), db.plantLogDao(), applicationContext)

        // 3. Preencher UI
        binding.tvDetailName.text = name
        binding.tvDetailFreq.text = "Rega a cada $freq dias"

        // Função auxiliar para mostrar a data bonita
        fun updateDateDisplay(dateMillis: Long) {
            val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
            val dateStr = sdf.format(Date(dateMillis))
            binding.tvDetailNext.text = "Próxima: $dateStr"
        }

        // Mostrar a data inicial
        if (nextWatering > 0) updateDateDisplay(nextWatering)

        // Descrição
        binding.tvDetailDesc.text = if (desc.isNullOrEmpty()) "Sem descrição." else desc

        // Luz
        binding.tvDetailLight.text = if (light >= 0) "$light lx" else "N/A"

        // Foto
        if (!photoUri.isNullOrEmpty()) {
            try {
                binding.ivDetailPhoto.setImageURI(Uri.parse(photoUri))
            } catch (e: Exception) {
                binding.ivDetailPhoto.setImageResource(android.R.drawable.ic_menu_report_image)
            }
        }

        // 4. Botão "Reguei Agora"
        binding.btnWaterNow.setOnClickListener {
            if (plantId == -1) return@setOnClickListener

            lifecycleScope.launch(Dispatchers.IO) {
                // A. Atualizar na BD e API
                repo.waterPlant(plantId)

                // B. Calcular a NOVA data para mostrar logo no ecrã sem ter de sair e entrar
                val now = System.currentTimeMillis()
                val newNextDate = now + (freq.toLong() * 86400000L) // dias -> ms

                runOnUiThread {
                    Toast.makeText(this@PlantDetailsActivity, "Planta regada! 💧", Toast.LENGTH_SHORT).show()
                    // Atualiza o texto imediatamente
                    updateDateDisplay(newNextDate)
                }
            }
        }

        // 5. Botão "Editar"
        binding.btnEditPlant.setOnClickListener {
            val intent = android.content.Intent(this, AddPlantActivity::class.java)
            intent.putExtra("IS_EDIT_MODE", true)
            intent.putExtra("PLANT_ID", plantId)
            intent.putExtra("PLANT_NAME", name)
            intent.putExtra("PLANT_DESC", desc)
            intent.putExtra("PLANT_FREQ", freq)
            intent.putExtra("PLANT_PHOTO", photoUri)
            intent.putExtra("PLANT_LIGHT", light)
            startActivity(intent)
            finish()
        }

        // 6. Botão "Apagar"
        binding.btnDeletePlant.setOnClickListener {
            if (plantId == -1) return@setOnClickListener
            AlertDialog.Builder(this)
                .setTitle("Apagar Planta")
                .setMessage("Tem a certeza?")
                .setPositiveButton("Sim") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        repo.deleteById(plantId)
                        runOnUiThread {
                            finish()
                        }
                    }
                }
                .setNegativeButton("Não", null)
                .show()
        }
    }
}