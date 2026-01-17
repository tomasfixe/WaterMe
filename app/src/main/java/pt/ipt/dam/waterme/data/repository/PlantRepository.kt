package pt.ipt.dam.waterme.data.repository

import androidx.lifecycle.LiveData
import pt.ipt.dam.waterme.data.dao.PlantDao
import pt.ipt.dam.waterme.data.model.Plant

class PlantRepository(private val plantDao: PlantDao) {

    // A lista de todas as plantas (atualiza automaticamente)
    val allPlants: LiveData<List<Plant>> = plantDao.getAllPlants()

    // Função para inserir (chamada pelo ViewModel)
    suspend fun insert(plant: Plant) {
        plantDao.insertPlant(plant)
    }

    // Função para apagar
    suspend fun delete(plant: Plant) {
        plantDao.deletePlant(plant)
    }

    suspend fun deleteById(id: Int) {
        plantDao.deleteById(id)
    }
}