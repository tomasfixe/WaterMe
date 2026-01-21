package pt.ipt.dam.waterme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pt.ipt.dam.waterme.data.model.LoginRequest
import pt.ipt.dam.waterme.data.network.RetrofitClient
import pt.ipt.dam.waterme.data.session.SessionManager

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar se já está logado antes de mostrar o ecrã
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

        // Botão para ir para o ecrã de Registo
        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Botão de Login
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

                    // SUCESSO
                    // 1. Guardar na sessão
                    session.saveUserSession(response.user_id, response.name)

                    // 2. Ir para a App
                    Toast.makeText(this@LoginActivity, "Bem-vindo, ${response.name}!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()

                } catch (e: Exception) {
                    // ERRO (Senha errada ou erro de rede)
                    Toast.makeText(this@LoginActivity, "Login falhou! Verifica os dados.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}