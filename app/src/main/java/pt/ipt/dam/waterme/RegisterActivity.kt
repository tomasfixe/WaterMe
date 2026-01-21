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

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<EditText>(R.id.etRegName)
        val etEmail = findViewById<EditText>(R.id.etRegEmail)
        val etPass = findViewById<EditText>(R.id.etRegPassword)
        val btnReg = findViewById<Button>(R.id.btnDoRegister)

        btnReg.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val pass = etPass.text.toString()

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Preenche tudo!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Chamada à API
            lifecycleScope.launch {
                try {
                    val request = RegisterRequest(name, email, pass)
                    val response = RetrofitClient.api.register(request)

                    Toast.makeText(this@RegisterActivity, "Conta criada! Faz Login.", Toast.LENGTH_LONG).show()
                    finish() // Volta para o Login

                } catch (e: Exception) {
                    Toast.makeText(this@RegisterActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
    }
}