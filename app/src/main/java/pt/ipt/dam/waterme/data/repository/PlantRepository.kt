package pt.ipt.dam.waterme.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import pt.ipt.dam.waterme.data.dao.PlantDao
import pt.ipt.dam.waterme.data.dao.PlantLogDao
import pt.ipt.dam.waterme.data.model.Plant
import pt.ipt.dam.waterme.data.model.PlantRequest
import pt.ipt.dam.waterme.data.network.RetrofitClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlantRepository(
    private val plantDao: PlantDao,
    private val plantLogDao: PlantLogDao,
    private val context: Context
) {

    val allPlants: LiveData<List<Plant>> = plantDao.getAllPlants()
    private val api = RetrofitClient.api

    // GUARDAR
    suspend fun insert(plant: Plant) {
        // 1. Guarda no telemóvel
        plantDao.insertPlant(plant)

        // 2. Tenta enviar para a API
        try {


            val request = PlantRequest(
                userId = 1,
                name = plant.name,
                description = plant.description ?: "",
                photoUrl = "",
                nextWatering = convertLongToDate(plant.nextWateringDate),
                lastWatering = convertLongToDate(plant.lastWateredDate ?: System.currentTimeMillis())
            )
            api.addPlant(request)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ATUALIZAR
    suspend fun update(plant: Plant) {
        plantDao.updatePlant(plant) // Atualiza localmente com a foto
        try {
            val request = PlantRequest(
                userId = 1,
                name = plant.name,
                description = plant.description ?: "",
                photoUrl = "",
                nextWatering = convertLongToDate(plant.nextWateringDate),
                lastWatering = convertLongToDate(plant.lastWateredDate ?: System.currentTimeMillis())
            )
            api.updatePlant(plant.id, request)
        } catch (e: Exception) { e.printStackTrace() }
    }

    // APAGAR
    suspend fun deleteById(id: Int) {
        plantDao.deleteById(id)
        try {
            api.deletePlant(id)
        } catch (e: Exception) { e.printStackTrace() }
    }

    // REGA
    suspend fun waterPlant(plantId: Int, frequencyInDays: Int) {
        val now = System.currentTimeMillis()
        val next = now + (frequencyInDays.toLong() * 86400000L)
        plantDao.updateWateringDates(plantId, now, next)
    }

    // Função auxiliar de data
    private fun convertLongToDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }


}