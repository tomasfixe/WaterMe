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

/**
 * Activity responsável por apresentar os detalhes completos de uma planta específica.
 * Aqui o utilizador pode ver informações, registar uma rega, consultar o histórico,
 * editar os dados ou apagar a planta.
 */
class PlantDetailsActivity : AppCompatActivity() {

    // ViewBinding para aceder aos elementos do layout (XML) de forma segura e rápida
    private lateinit var binding: ActivityPlantDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialização do binding
        binding = ActivityPlantDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Receber dados
        // Recuperamos os dados que foram enviados pela Activity anterior (lista de plantas) através do Intent.
        // Definimos valores por defeito (-1, null, etc.) para evitar crashes se faltar algum dado.
        val plantId = intent.getIntExtra("PLANT_ID", -1)
        val name = intent.getStringExtra("PLANT_NAME") ?: "Sem Nome"
        val desc = intent.getStringExtra("PLANT_DESC")
        val freq = intent.getIntExtra("PLANT_FREQ", 1)
        val light = intent.getFloatExtra("PLANT_LIGHT", -1f)
        val photoUri = intent.getStringExtra("PLANT_PHOTO")
        var nextWatering = intent.getLongExtra("PLANT_NEXT", 0L)

        // 2. Setup DB e Repo
        // Instanciamos a base de dados e o repositório para podermos fazer operações (regar, apagar, ler logs)
        val db = WaterMeDatabase.getDatabase(this)
        val repo = PlantRepository(db.plantDao(), db.plantLogDao(), applicationContext)

        // 3. Preencher UI
        // Colocamos os dados recebidos nos TextViews respetivos
        binding.tvDetailName.text = name
        binding.tvDetailFreq.text = "Rega a cada $freq dias"

        /**
         * Função local auxiliar para formatar datas (Timestamp Long -> String legível).
         * Usada tanto na inicialização como após clicar em "Regar Agora".
         */
        fun updateDateDisplay(dateMillis: Long) {
            val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
            val dateStr = sdf.format(Date(dateMillis))
            binding.tvDetailNext.text = "Próxima: $dateStr"
        }

        // Se houver uma data válida, atualiza o texto
        if (nextWatering > 0) updateDateDisplay(nextWatering)

        // Descrição
        binding.tvDetailDesc.text = if (desc.isNullOrEmpty()) "Sem descrição." else desc

        // Luz (Mostrar em escala)
        if (light >= 0) {
            // Convertemos para inteiro
            val level = light.toInt()
            binding.tvDetailLight.text = "Nível $level/10"
        } else {
            binding.tvDetailLight.text = "Não medida"
        }

        // Carregar Foto
        if (!photoUri.isNullOrEmpty()) {
            try {
                // Tenta carregar a imagem do URI. Se o ficheiro tiver sido apagado, entra no catch.
                binding.ivDetailPhoto.setImageURI(Uri.parse(photoUri))
            } catch (e: Exception) {
                // Mostra ícone de erro se não conseguir carregar a imagem
                binding.ivDetailPhoto.setImageResource(android.R.drawable.ic_menu_report_image)
            }
        }

        // 4. Botão "Reguei Agora"
        binding.btnWaterNow.setOnClickListener {
            if (plantId == -1) return@setOnClickListener

            lifecycleScope.launch(Dispatchers.IO) {
                // A. Rega e CRIA O LOG
                repo.waterPlant(plantId)

                // Calcula a nova data localmente apenas para atualizar a UI instantaneamente
                val now = System.currentTimeMillis()
                val newNextDate = now + (freq.toLong() * 86400000L)

                // Volta à thread principal (UI) para mostrar o Toast e atualizar a data
                runOnUiThread {
                    Toast.makeText(this@PlantDetailsActivity, "Planta regada! 💧", Toast.LENGTH_SHORT).show()
                    updateDateDisplay(newNextDate)
                }
            }
        }

        // 5. Botão "Ver Logs"
        binding.btnViewLogs.setOnClickListener {
            if (plantId == -1) return@setOnClickListener

            lifecycleScope.launch(Dispatchers.IO) {
                // Busca a lista de logs (histórico) ao repositório
                val logs = repo.getPlantLogs(plantId)

                // Mostra o popup na thread principal
                runOnUiThread {
                    showLogsDialog(logs)
                }
            }
        }

        // 6. Botão Editar
        binding.btnEditPlant.setOnClickListener {
            // Cria um Intent para abrir a AddPlantActivity em "Modo de Edição"
            val intent = android.content.Intent(this, AddPlantActivity::class.java)
            intent.putExtra("IS_EDIT_MODE", true)
            // Passamos todos os dados atuais para preencher o formulário
            intent.putExtra("PLANT_ID", plantId)
            intent.putExtra("PLANT_NAME", name)
            intent.putExtra("PLANT_DESC", desc)
            intent.putExtra("PLANT_FREQ", freq)
            intent.putExtra("PLANT_PHOTO", photoUri)
            intent.putExtra("PLANT_LIGHT", light)

            startActivity(intent)
            finish() // Fecha esta atividade para que, ao gravar, o utilizador volte à lista
        }

        // 7. Botão Apagar
        binding.btnDeletePlant.setOnClickListener {
            if (plantId == -1) return@setOnClickListener

            // Cria um diálogo de confirmação para evitar cliques acidentais
            AlertDialog.Builder(this)
                .setTitle("Apagar Planta")
                .setMessage("Tem a certeza?")
                .setPositiveButton("Sim") { _, _ ->
                    // Se confirmar, apaga em background
                    lifecycleScope.launch(Dispatchers.IO) {
                        repo.deleteById(plantId)
                        runOnUiThread { finish() } // Fecha o ecrã e volta à lista
                    }
                }
                .setNegativeButton("Não", null) // Se cancelar, não faz nada
                .show()
        }
    }

    /**
     * Função auxiliar para construir e mostrar o popup (AlertDialog) com o histórico de regas.
     * @param logs A lista de objetos PlantLog recuperada da base de dados.
     */
    private fun showLogsDialog(logs: List<PlantLog>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Histórico de Regas 📅")

        if (logs.isEmpty()) {
            builder.setMessage("Esta planta ainda não foi regada.")
        } else {
            // Formata cada log para uma String bonita (ex: "💧 12/05/2023 às 14:30")
            val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
            val logsAsStrings = logs.map { log ->
                "💧 ${dateFormat.format(Date(log.date))}"
            }.toTypedArray()

            // Define a lista de itens no diálogo
            builder.setItems(logsAsStrings, null)
        }
        builder.setPositiveButton("Fechar", null)
        builder.show()
    }
}