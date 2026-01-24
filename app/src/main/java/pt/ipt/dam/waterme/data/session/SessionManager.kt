package pt.ipt.dam.waterme.data.session

import android.content.Context
import android.content.SharedPreferences

/**
 * Classe responsável pela gestão da sessão do utilizador.
 * Utiliza o SharedPreferences do Android para guardar dados simples (como ID e nome)
 * de forma persistente, permitindo que a aplicação se lembre de quem está logado
 * mesmo depois de ser fechada.
 */
class SessionManager(context: Context) {
    // Inicializa o ficheiro de preferências chamado "user_session" em modo privado
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    /**
     * Objeto Companion para guardar as constantes (chaves) usadas no SharedPreferences.
     * Isto evita erros de escrita (typos) ao chamar as chaves em vários sítios.
     */
    companion object {
        const val KEY_USER_ID = "USER_ID"
        const val KEY_USER_NAME = "USER_NAME"
        const val KEY_IS_LOGGED_IN = "IS_LOGGED_IN"
        // Guardamos o último utilizador para casos de uso específicos (ex: sugerir o último email)
        const val KEY_LAST_USER_ID = "LAST_USER_ID"
    }

    // Guardar dados ao fazer login
    /**
     * Guarda as informações do utilizador nas preferências partilhadas.
     * Esta função é chamada assim que o login é validado com sucesso pela API.
     * @param userId O ID único do utilizador vindo da base de dados.
     * @param name O nome do utilizador para mostrar na UI.
     */
    fun saveUserSession(userId: Int, name: String) {
        val editor = prefs.edit() // Abre o editor para escrever
        editor.putInt(KEY_USER_ID, userId)
        editor.putString(KEY_USER_NAME, name)
        editor.putBoolean(KEY_IS_LOGGED_IN, true) // Marca como logado

        // Também atualizamos o histórico de "último utilizador"
        editor.putInt(KEY_LAST_USER_ID, userId)

        // apply() grava as alterações em background (assíncrono) para não bloquear a interface
        editor.apply()
    }

    /**
     * Recupera o ID do utilizador atualmente logado.
     * @return O ID do utilizador ou -1 se não houver sessão ativa.
     */
    fun fetchUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }


    /**
     * Recupera o ID do último utilizador que fez login, mesmo que já tenha feito logout.
     * Útil se quisermos manter dados em cache mas saber a quem pertencem.
     * @return O ID ou -1.
     */
    fun fetchLastUserId(): Int {
        return prefs.getInt(KEY_LAST_USER_ID, -1)
    }

    /**
     * Obtém o nome do utilizador para mostrar em mensagens de boas-vindas (ex: "Olá, Tomás").
     * @return O nome ou null se não existir.
     */
    fun fetchUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    /**
     * Verifica se existe uma sessão ativa.
     * Usado no SplashScreen ou LoginActivity para decidir se vamos direto para a MainActivity.
     * @return true se o utilizador já estiver logado.
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Termina a sessão do utilizador.
     * Remove os dados da sessão ativa (ID, Nome, Estado de Login), obrigando a um novo login.
     */
    fun logout() {
        val editor = prefs.edit()
        editor.remove(KEY_USER_ID)
        editor.remove(KEY_USER_NAME)
        editor.remove(KEY_IS_LOGGED_IN)
        // Nota: Não removemos o KEY_LAST_USER_ID intencionalmente
        editor.apply()
    }
}