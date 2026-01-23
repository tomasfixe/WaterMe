package pt.ipt.dam.waterme

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.ipt.dam.waterme.data.database.WaterMeDatabase
import pt.ipt.dam.waterme.data.model.PlantLog
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

        // 2. Setup DB e Repo
        val db = WaterMeDatabase.getDatabase(this)
        val repo = PlantRepository(db.plantDao(), db.plantLogDao(), applicationContext)

        // 3. Preencher UI
        binding.tvDetailName.text = name
        binding.tvDetailFreq.text = "Rega a cada $freq dias"

        fun updateDateDisplay(dateMillis: Long) {
            val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
            val dateStr = sdf.format(Date(dateMillis))
            binding.tvDetailNext.text = "Próxima: $dateStr"
        }
        if (nextWatering > 0) updateDateDisplay(nextWatering)

        binding.tvDetailDesc.text = if (desc.isNullOrEmpty()) "Sem descrição." else desc
        // Luz (Mostrar em escala)
        if (light >= 0) {
            // Convertemos para inteiro para não aparecer "5.0/10"
            val level = light.toInt()
            binding.tvDetailLight.text = "Nível $level/10"
        } else {
            binding.tvDetailLight.text = "Não medida"
        }
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
                // A. Rega e CRIA O LOG (Agora já funciona!)
                repo.waterPlant(plantId)

                val now = System.currentTimeMillis()
                val newNextDate = now + (freq.toLong() * 86400000L)

                runOnUiThread {
                    Toast.makeText(this@PlantDetailsActivity, "Planta regada! 💧", Toast.LENGTH_SHORT).show()
                    updateDateDisplay(newNextDate)
                }
            }
        }

        // 5. Botão "Ver Logs" (Adicionei o botão no layout XML ou assume que já lá está)
        binding.btnViewLogs.setOnClickListener {
            if (plantId == -1) return@setOnClickListener

            lifecycleScope.launch(Dispatchers.IO) {
                // Busca a lista de logs
                val logs = repo.getPlantLogs(plantId)

                runOnUiThread {
                    showLogsDialog(logs)
                }
            }
        }

        // 6. Botão Editar
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

        // 7. Botão Apagar
        binding.btnDeletePlant.setOnClickListener {
            if (plantId == -1) return@setOnClickListener
            AlertDialog.Builder(this)
                .setTitle("Apagar Planta")
                .setMessage("Tem a certeza?")
                .setPositiveButton("Sim") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        repo.deleteById(plantId)
                        runOnUiThread { finish() }
                    }
                }
                .setNegativeButton("Não", null)
                .show()
        }
    }

    // Função para mostrar o popup com a lista
    private fun showLogsDialog(logs: List<PlantLog>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Histórico de Regas 📅")

        if (logs.isEmpty()) {
            builder.setMessage("Esta planta ainda não foi regada.")
        } else {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
            val logsAsStrings = logs.map { log ->
                "💧 ${dateFormat.format(Date(log.date))}"
            }.toTypedArray()

            builder.setItems(logsAsStrings, null)
        }
        builder.setPositiveButton("Fechar", null)
        builder.show()
    }
}