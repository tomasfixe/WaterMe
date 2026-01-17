package pt.ipt.dam.waterme

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.ipt.dam.waterme.data.database.WaterMeDatabase
import pt.ipt.dam.waterme.data.repository.PlantRepository

class PlantDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plant_details)

        // 1. Receber dados do Intent
        // NOTA: Os nomes das chaves ("PLANT_NAME", etc) têm de ser IGUAIS ao que puseste no PlantsFragment
        val plantId = intent.getIntExtra("PLANT_ID", -1)
        val name = intent.getStringExtra("PLANT_NAME") ?: "Sem Nome"
        val desc = intent.getStringExtra("PLANT_DESC")
        val freq = intent.getIntExtra("PLANT_FREQ", 0)
        val light = intent.getFloatExtra("PLANT_LIGHT", -1f)
        val photoUri = intent.getStringExtra("PLANT_PHOTO")

        // 2. Referências UI
        val tvName = findViewById<TextView>(R.id.tvDetailName)
        val tvDesc = findViewById<TextView>(R.id.tvDetailDesc)
        val tvFreq = findViewById<TextView>(R.id.tvDetailFreq)
        val tvLight = findViewById<TextView>(R.id.tvDetailLight)
        val ivPhoto = findViewById<ImageView>(R.id.ivDetailPhoto)
        val btnDelete = findViewById<Button>(R.id.btnDeletePlant)

        // 3. Mostrar Dados
        tvName.text = name
        tvFreq.text = "$freq dias"

        // Tratamento da Descrição (para não aparecer "null")
        if (desc.isNullOrEmpty()) {
            tvDesc.text = "Sem descrição disponível."
        } else {
            tvDesc.text = desc
        }

        // Tratamento da Luz
        if (light >= 0) {
            tvLight.text = "$light lx"
        } else {
            tvLight.text = "N/A"
        }

        // Tratamento da Foto
        if (!photoUri.isNullOrEmpty()) {
            try {
                ivPhoto.setImageURI(Uri.parse(photoUri))
            } catch (e: Exception) {
                ivPhoto.setImageResource(android.R.drawable.ic_menu_report_image)
            }
        }

        // 4. Lógica do Botão Apagar
        btnDelete.setOnClickListener {
            if (plantId == -1) {
                Toast.makeText(this, "Erro: ID inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Diálogo de confirmação
            AlertDialog.Builder(this)
                .setTitle("Apagar Planta")
                .setMessage("Tem a certeza que quer eliminar a planta '$name'?")
                .setPositiveButton("Sim, apagar") { _, _ ->
                    deletePlant(plantId)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun deletePlant(id: Int) {
        val db = WaterMeDatabase.getDatabase(this)
        val repo = PlantRepository(db.plantDao())

        lifecycleScope.launch(Dispatchers.IO) {
            repo.deleteById(id)

            runOnUiThread {
                Toast.makeText(this@PlantDetailsActivity, "Planta eliminada.", Toast.LENGTH_SHORT).show()
                finish() // Fecha esta página e volta à lista
            }
        }
    }
}