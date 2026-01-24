package pt.ipt.dam.waterme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pt.ipt.dam.waterme.data.model.LoginRequest
import pt.ipt.dam.waterme.data.network.RetrofitClient
import pt.ipt.dam.waterme.data.session.SessionManager

/**
 * Activity responsável pelo ecrã de Login.
 * É o ponto de entrada da aplicação (definido no Manifesto) e gere a autenticação do utilizador.
 *
 * Funcionalidades principais:
 * 1. Verifica se já existe uma sessão ativa (Auto-Login).
 * 2. Valida credenciais (Email/Password) junto da API.
 * 3. Guarda os dados da sessão localmente.
 * 4. Permite navegação para o Registo ou Sobre Nós.
 */
class LoginActivity : AppCompatActivity() {

    /**
     * Método chamado quando a Activity é criada.
     * Configura a UI e define os listeners para os botões.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- VERIFICAÇÃO DE SESSÃO ---
        // Antes de mostrar o layout de login, verificamos se o utilizador já está logado.
        // Se estiver, saltamos diretamente para a MainActivity.
        val session = SessionManager(this)
        if (session.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Impede que o botão "Voltar" regresse a este ecrã vazio
            return
        }

        // Se não houver sessão, carregamos o layout de Login
        setContentView(R.layout.activity_login)

        // Inicializar Views
        val etEmail = findViewById<EditText>(R.id.editTextEmail)
        val etPass = findViewById<EditText>(R.id.editTextPassword)
        val btnLogin = findViewById<Button>(R.id.buttonLogin)
        val btnRegister = findViewById<Button>(R.id.buttonRegister)

        // Botão para ir para o ecrã de Registo
        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // --- LÓGICA DE LOGIN ---
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPass.text.toString()

            // Validação local básica: impede chamadas à API se os campos estiverem vazios
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Preencha os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            lifecycleScope.launch {
                try {
                    // Prepara o objeto de pedido (DTO)
                    val request = LoginRequest(email, pass)

                    // Chama a API (operação suspensa que aguarda resposta do servidor)
                    val response = RetrofitClient.api.login(request)

                    // 1. Guardar na sessão (SharedPreferences)
                    // define o "currentUserId" usado em toda a app
                    session.saveUserSession(response.user_id, response.name)


                    // 2. Navegar para a Aplicação Principal
                    Toast.makeText(this@LoginActivity, "Bem-vindo, ${response.name}!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))

                    // Fecha a LoginActivity para que não fique na pilha de navegação
                    finish()

                } catch (e: Exception) {
                    // Tratamento de erros
                    Toast.makeText(this@LoginActivity, "Login falhou! Verifica os dados.", Toast.LENGTH_SHORT).show()
                    e.printStackTrace() // Log do erro para debugging
                }
            }
        }

        // Link para o ecrã "Sobre Nós"
        val tvAbout = findViewById<TextView>(R.id.tvAboutUs)
        tvAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }
}