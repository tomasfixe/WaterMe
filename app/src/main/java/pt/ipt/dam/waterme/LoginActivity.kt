package pt.ipt.dam.waterme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pt.ipt.dam.waterme.data.database.WaterMeDatabase // Import necessário
import pt.ipt.dam.waterme.data.model.LoginRequest
import pt.ipt.dam.waterme.data.network.RetrofitClient
import pt.ipt.dam.waterme.data.session.SessionManager

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = SessionManager(this)
        if (session.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.editTextEmail)
        val etPass = findViewById<EditText>(R.id.editTextPassword)
        val btnLogin = findViewById<Button>(R.id.buttonLogin)
        val btnRegister = findViewById<Button>(R.id.buttonRegister)

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPass.text.toString()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Preencha os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val request = LoginRequest(email, pass)
                    val response = RetrofitClient.api.login(request)

                    // 1. Guardar na sessão
                    session.saveUserSession(response.user_id, response.name)

                    // Apagar dados antigos do utilizador anterior
                    val db = WaterMeDatabase.getDatabase(this@LoginActivity)


                    db.plantDao().deleteAll()

                    // 3. Ir para a App
                    Toast.makeText(this@LoginActivity, "Bem-vindo, ${response.name}!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()

                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Login falhou! Verifica os dados.", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
    }
}