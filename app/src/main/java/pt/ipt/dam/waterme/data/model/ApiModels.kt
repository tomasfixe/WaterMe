package pt.ipt.dam.waterme.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val user_id: Int,
    val name: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val id: Int,
    val message: String
)

// Enviar dados
data class PlantRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("photo_url") val photoUrl: String,
    @SerializedName("next_watering") val nextWatering: String,
    @SerializedName("last_watering") val lastWatering: String?,
    @SerializedName("light_level") val lightLevel: Float?
)

// Receber dados
data class PlantResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("photo_url") val photoUrl: String?,
    @SerializedName("next_watering") val nextWatering: String?,
    @SerializedName("last_watering") val lastWatering: String?,
    @SerializedName("light_level") val lightLevel: Float?
)