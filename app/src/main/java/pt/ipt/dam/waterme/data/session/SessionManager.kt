package pt.ipt.dam.waterme.data.session

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    // Guardar dados ao fazer login
    fun saveUserSession(userId: Int, name: String) {
        val editor = prefs.edit()
        editor.putInt("USER_ID", userId)
        editor.putString("USER_NAME", name)
        editor.putBoolean("IS_LOGGED_IN", true)
        editor.apply()
    }

    // Buscar o ID de quem está logado (Default é -1 se não houver ninguém)
    fun fetchUserId(): Int {
        return prefs.getInt("USER_ID", -1)
    }

    // Verificar se já está logado
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("IS_LOGGED_IN", false)
    }


    fun fetchUserName(): String? {
        return prefs.getString("USER_NAME", null)
    }

    // Logout (Limpar dados)
    fun logout() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}