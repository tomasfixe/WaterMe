package pt.ipt.dam.waterme

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Botão para voltar para trás
        val btnBack = findViewById<Button>(R.id.btnBackFromAbout)
        btnBack.setOnClickListener {
            finish() // Fecha esta atividade e volta ao Login
        }
    }
}