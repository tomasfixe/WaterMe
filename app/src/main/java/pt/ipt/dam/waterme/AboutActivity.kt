package pt.ipt.dam.waterme

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity simples para o ecrã "Sobre Nós".
 * Este ecrã é acessível a partir do Login (antes da autenticação) e mostra
 * informações estáticas sobre o projeto e a equipa.
 */
class AboutActivity : AppCompatActivity() {

    /**
     * Método chamado quando a Activity é criada.
     * Define o layout visual e configura o comportamento dos botões.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define o ficheiro XML que contém o design desta página
        setContentView(R.layout.activity_about)

        // Configuração do botão "Voltar"
        val btnBack = findViewById<Button>(R.id.btnBackFromAbout)

        btnBack.setOnClickListener {
            // O utilizador regressa automaticamente ao ecrã anterior (neste caso, o LoginActivity).
            finish()
        }
    }
}