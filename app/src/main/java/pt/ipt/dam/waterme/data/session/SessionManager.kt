package pt.ipt.dam.waterme.data.session

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        const val KEY_USER_ID = "USER_ID"
        const val KEY_USER_NAME = "USER_NAME"
        const val KEY_IS_LOGGED_IN = "IS_LOGGED_IN"
        const val KEY_LAST_USER_ID = "LAST_USER_ID"
    }

    // Guardar dados ao fazer login
    fun saveUserSession(userId: Int, name: String) {
        val editor = prefs.edit()
        editor.putInt(KEY_USER_ID, userId)
        editor.putString(KEY_USER_NAME, name)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)

        editor.putInt(KEY_LAST_USER_ID, userId)
        editor.apply()
    }

    fun fetchUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }


    fun fetchLastUserId(): Int {
        return prefs.getInt(KEY_LAST_USER_ID, -1)
    }

    fun fetchUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // O Logout agora: limpa a sessão mas guarda o ID antigo
    fun logout() {
        val editor = prefs.edit()
        editor.remove(KEY_USER_ID)
        editor.remove(KEY_USER_NAME)
        editor.remove(KEY_IS_LOGGED_IN)
        editor.apply()
    }
}