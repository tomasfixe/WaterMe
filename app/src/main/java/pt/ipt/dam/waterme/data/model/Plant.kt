package pt.ipt.dam.waterme.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int = -1, // Identifica o dono da planta
    val name: String,
    val description: String?,
    val photoUri: String?, // Caminho para a foto no telemóvel
    val waterFrequency: Int, // De quantos em quantos dias
    val lastWateredDate: Long?, // Timestamp
    val nextWateringDate: Long, // Timestamp
    val lightLevel: Float? = null, // Valor do sensor de luz
    val latitude: Double? = null,
    val longitude: Double? = null,
    val synced: Boolean = false // Para saber se já foi enviada para a API
)