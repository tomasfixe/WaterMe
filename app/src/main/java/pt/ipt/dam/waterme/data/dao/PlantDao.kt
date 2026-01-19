package pt.ipt.dam.waterme.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import pt.ipt.dam.waterme.data.model.Plant

@Dao
interface PlantDao {

    // Adicionar planta
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: Plant)

    // Atualizar planta
    @Update
    suspend fun updatePlant(plant: Plant)

    // Apagar planta
    @Delete
    suspend fun deletePlant(plant: Plant)

    @Query("DELETE FROM plants WHERE id = :plantId")
    suspend fun deleteById(plantId: Int)

    // Ler todas as plantas
    @Query("SELECT * FROM plants ORDER BY nextWateringDate ASC")
    fun getAllPlants(): LiveData<List<Plant>>

    // Ler uma planta específica pelo ID
    @Query("SELECT * FROM plants WHERE id = :plantId")
    suspend fun getPlantById(plantId: Int): Plant?

    // Atualizar a data de última regagem
    @Query("UPDATE plants SET lastWateredDate = :last, nextWateringDate = :next WHERE id = :id")
    suspend fun updateWateringDates(id: Int, last: Long, next: Long)
}