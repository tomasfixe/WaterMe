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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: Plant): Long

    @Update
    suspend fun updatePlant(plant: Plant)

    @Delete
    suspend fun deletePlant(plant: Plant)

    @Query("DELETE FROM plants WHERE id = :plantId")
    suspend fun deleteById(plantId: Int)

    @Query("SELECT * FROM plants WHERE userId = :userId ORDER BY nextWateringDate ASC")
    fun getAllPlants(userId: Int): LiveData<List<Plant>>

    @Query("DELETE FROM plants WHERE userId = :userId")
    suspend fun deleteUserPlants(userId: Int)

    @Query("DELETE FROM plants")
    suspend fun deleteAll()

    @Query("SELECT * FROM plants WHERE id = :plantId")
    suspend fun getPlantById(plantId: Int): Plant?

    @Query("UPDATE plants SET lastWateredDate = :last, nextWateringDate = :next WHERE id = :id")
    suspend fun updateWateringDates(id: Int, last: Long, next: Long)

    @Query("SELECT * FROM plants WHERE nextWateringDate <= :now")
    suspend fun getPlantsNeedingWater(now: Long): List<Plant>
}