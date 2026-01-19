package pt.ipt.dam.waterme.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plant_logs")
data class PlantLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val plantId: Int,      // Liga log a uma planta específica
    val date: Long         // Data e hora da rega
)