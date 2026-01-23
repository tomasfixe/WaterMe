package pt.ipt.dam.waterme.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ipt.dam.waterme.LoginActivity
import pt.ipt.dam.waterme.R
import pt.ipt.dam.waterme.data.database.WaterMeDatabase
import pt.ipt.dam.waterme.data.model.ChangePasswordRequest
import pt.ipt.dam.waterme.data.network.RetrofitClient
import pt.ipt.dam.waterme.data.session.SessionManager

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar o layout
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        val context = requireContext()
        val session = SessionManager(context)

        // 1. Preencher o "Olá [Nome]"
        val tvHello = root.findViewById<TextView>(R.id.tvProfileHello)
        val userName = session.fetchUserName() ?: "Utilizador"
        tvHello.text = "Olá, $userName"

        // 2. Lógica de "Mostrar/Esconder" as caixas de texto
        val btnToggle = root.findViewById<Button>(R.id.btnToggleChangePass)
        val containerPass = root.findViewById<LinearLayout>(R.id.layoutChangePassContainer)

        btnToggle.setOnClickListener {
            if (containerPass.visibility == View.VISIBLE) {
                containerPass.visibility = View.GONE
                btnToggle.text = "Alterar Palavra-Passe"
            } else {
                containerPass.visibility = View.VISIBLE
                btnToggle.text = "Cancelar Alteração"
            }
        }

        // 3. Lógica de Gravar a Nova Senha
        val etCurrent = root.findViewById<EditText>(R.id.etCurrentPass)
        val etNew = root.findViewById<EditText>(R.id.etNewPass)
        val etConfirm = root.findViewById<EditText>(R.id.etConfirmNewPass)
        val btnSave = root.findViewById<Button>(R.id.btnSavePassword)

        btnSave.setOnClickListener {
            val currentPass = etCurrent.text.toString()
            val newPass = etNew.text.toString()
            val confirmPass = etConfirm.text.toString()

            if (currentPass.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(context, "Preenche todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirmPass) {
                Toast.makeText(context, "As novas senhas não são iguais!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Chamar API
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val userId = session.fetchUserId()
                    val request = ChangePasswordRequest(userId, currentPass, newPass)
                    val response = RetrofitClient.api.changePassword(request)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Senha alterada com sucesso!", Toast.LENGTH_LONG).show()
                            // Limpar e esconder
                            etCurrent.text.clear()
                            etNew.text.clear()
                            etConfirm.text.clear()
                            containerPass.visibility = View.GONE
                            btnToggle.text = "Alterar Palavra-Passe"
                        } else {
                            // Se der erro 401, é porque a senha antiga estava errada
                            Toast.makeText(context, "Erro: A senha atual está incorreta.", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Erro de ligação: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 4. Logout
        val btnLogout = root.findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            session.logout()
            lifecycleScope.launch(Dispatchers.IO) {
                WaterMeDatabase.getDatabase(context).plantDao().deleteAll()
                withContext(Dispatchers.Main) {
                    val intent = Intent(activity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        }

        return root
    }
}

