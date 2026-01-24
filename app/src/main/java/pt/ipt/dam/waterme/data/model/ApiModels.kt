package pt.ipt.dam.waterme.data.model

import com.google.gson.annotations.SerializedName

/**
 * Ficheiro de Modelos de Dados (DTOs - Data Transfer Objects) para a API.
 * Estas data classes definem a estrutura exata dos JSONs que enviamos (Requests)
 * e recebemos (Responses) do servidor.
 */

/**
 * Modelo para enviar os dados de login.
 * Contém apenas o email e a palavra-passe, conforme esperado pelo endpoint '/login'.
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Modelo de resposta do login.
 * O servidor devolve uma mensagem de sucesso, o ID do utilizador e o seu nome.
 * Estes dados são depois guardados na SessionManager.
 */
data class LoginResponse(
    val message: String,
    val user_id: Int,
    val name: String
)

/**
 * Modelo para o registo de um novo utilizador.
 * Envia nome, email e password para o endpoint '/register'.
 */
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

/**
 * Resposta do servidor após um registo bem-sucedido.
 * Recebemos o ID do novo utilizador criado e uma mensagem de confirmação.
 */
data class RegisterResponse(
    val id: Int,
    val message: String
)

/**
 * Modelo para o pedido de alteração de palavra-passe.
 *
 * Utilizamos a anotação @SerializedName para mapear os nomes das variáveis em Kotlin (camelCase)
 * para os nomes das chaves no JSON do servidor (snake_case).
 * Exemplo: 'userId' no código -> 'user_id' no JSON.
 */
data class ChangePasswordRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("old_password") val oldPass: String,
    @SerializedName("new_password") val newPass: String
)

// Enviar dados
/**
 * Modelo usado para CRIAR ou ATUALIZAR uma planta no servidor.
 * Todos os campos correspondem às colunas esperadas pela API.
 */
data class PlantRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("photo_url") val photoUrl: String,
    // As datas são enviadas como String para facilitar a serialização
    @SerializedName("next_watering") val nextWatering: String,
    @SerializedName("last_watering") val lastWatering: String?,
    @SerializedName("light_level") val lightLevel: Float?,
    @SerializedName("water_frequency") val waterFrequency: Int
)

// Receber dados
/**
 * Modelo usado para RECEBER a lista de plantas do servidor.
 * Nota: Muitos campos são Nullable porque a base de dados remota pode ter campos vazios
 * e para evitar que a aplicação crashe ao fazer o parse do JSON.
 */
data class PlantResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("photo_url") val photoUrl: String?,
    @SerializedName("next_watering") val nextWatering: String?,
    @SerializedName("last_watering") val lastWatering: String?,
    @SerializedName("light_level") val lightLevel: Float?,
    @SerializedName("water_frequency") val waterFrequency: Int?
)