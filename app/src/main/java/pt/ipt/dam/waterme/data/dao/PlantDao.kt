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

    // Atualizar planta (ex: quando regas)
    @Update
    suspend fun updatePlant(plant: Plant)

    // Apagar planta
    @Delete
    suspend fun deletePlant(plant: Plant)

    @Query("DELETE FROM plants WHERE id = :plantId")
    suspend fun deleteById(plantId: Int)

    // Ler todas as plantas (LiveData atualiza a lista automaticamente)
    @Query("SELECT * FROM plants ORDER BY nextWateringDate ASC")
    fun getAllPlants(): LiveData<List<Plant>>

    // Ler uma planta específica pelo ID
    @Query("SELECT * FROM plants WHERE id = :plantId")
    suspend fun getPlantById(plantId: Int): Plant?
}