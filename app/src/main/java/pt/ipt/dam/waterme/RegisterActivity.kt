package pt.ipt.dam.waterme

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pt.ipt.dam.waterme.data.model.RegisterRequest
import pt.ipt.dam.waterme.data.network.RetrofitClient

/**
 * Activity responsável pelo registo de novos utilizadores.
 * Recolhe os dados do formulário e envia-os para a API backend para criar uma conta na base de dados remota.
 */
class RegisterActivity : AppCompatActivity() {

    /**
     * Método de criação da Activity.
     * Inicializa a interface e define a lógica do botão de registo.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar os elementos da UI
        val etName = findViewById<EditText>(R.id.etRegName)
        val etEmail = findViewById<EditText>(R.id.etRegEmail)
        val etPass = findViewById<EditText>(R.id.etRegPassword)
        val btnReg = findViewById<Button>(R.id.btnDoRegister)

        // Configurar o clique no botão "Registar"
        btnReg.setOnClickListener {
            // Obter os valores introduzidos
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val pass = etPass.text.toString()

            // Validação simples: verificar se os campos estão vazios
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Preenche tudo!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Chamada à API
            lifecycleScope.launch {
                try {
                    // Criar o objeto de pedido (DTO) com os dados
                    val request = RegisterRequest(name, email, pass)

                    // Enviar para o servidor através do Retrofit
                    // Esta linha suspende a execução até receber resposta do servidor
                    val response = RetrofitClient.api.register(request)

                    // Se chegar aqui, o registo foi bem-sucedido (sem exceções)
                    Toast.makeText(this@RegisterActivity, "Conta criada! Faz Login.", Toast.LENGTH_LONG).show()

                    // Fecha a RegisterActivity e regressa à activity anterior na pilha (o LoginActivity)
                    finish()

                } catch (e: Exception) {
                    // Tratamento de erros (ex: email já existe, servidor em baixo, sem internet)
                    Toast.makeText(this@RegisterActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
    }
}