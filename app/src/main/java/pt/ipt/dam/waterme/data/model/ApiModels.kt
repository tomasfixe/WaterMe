package pt.ipt.dam.waterme.data.model

import com.google.gson.annotations.SerializedName

// Enviar dados
data class PlantRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("photo_url") val photoUrl: String,
    @SerializedName("next_watering") val nextWatering: String,
    @SerializedName("last_watering") val lastWatering: String?
)

// Receber dados
data class PlantResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("photo_url") val photoUrl: String?,
    @SerializedName("next_watering") val nextWatering: String?,
    @SerializedName("last_watering") val lastWatering: String?
)