package pt.ipt.dam.waterme // Confirma se este package é igual ao do MainActivity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        val btnLogin = findViewById<Button>(R.id.buttonLogin)

        btnLogin.setOnClickListener {
            // Simula login e vai para a app principal
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}