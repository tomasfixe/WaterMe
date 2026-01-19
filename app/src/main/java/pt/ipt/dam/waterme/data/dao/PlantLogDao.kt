package pt.ipt.dam.waterme.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import pt.ipt.dam.waterme.data.model.PlantLog

@Dao
interface PlantLogDao {
    @Insert
    suspend fun insertLog(log: PlantLog)

    // Buscar logs de uma planta específica
    @Query("SELECT * FROM plant_logs WHERE plantId = :plantId ORDER BY date DESC")
    fun getLogsForPlant(plantId: Int): LiveData<List<PlantLog>>
}