package pt.ipt.dam.waterme.data.repository

import androidx.lifecycle.LiveData
import pt.ipt.dam.waterme.data.dao.PlantDao
import pt.ipt.dam.waterme.data.dao.PlantLogDao
import pt.ipt.dam.waterme.data.model.Plant
import pt.ipt.dam.waterme.data.model.PlantLog


class PlantRepository(private val plantDao: PlantDao, private val plantLogDao: PlantLogDao) {

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

    suspend fun waterPlant(plantId: Int, frequencyInDays: Int) {
        val now = System.currentTimeMillis()
        // Calcula a próxima data (dias * 24h * 60m * 60s * 1000ms)
        val next = now + (frequencyInDays.toLong() * 86400000L)

        // 1. Atualizar a planta
        plantDao.updateWateringDates(plantId, now, next)

        // 2. Criar o log
        val log = PlantLog(plantId = plantId, date = now)


        plantLogDao.insertLog(log)
    }

    suspend fun update(plant: Plant) {
        plantDao.updatePlant(plant)
    }
}