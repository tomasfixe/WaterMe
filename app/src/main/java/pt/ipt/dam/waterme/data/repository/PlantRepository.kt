package pt.ipt.dam.waterme.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import pt.ipt.dam.waterme.data.dao.PlantDao
import pt.ipt.dam.waterme.data.dao.PlantLogDao
import pt.ipt.dam.waterme.data.model.Plant
import pt.ipt.dam.waterme.data.model.PlantRequest
import pt.ipt.dam.waterme.data.network.RetrofitClient
import pt.ipt.dam.waterme.data.session.SessionManager // Importante!
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlantRepository(
    private val plantDao: PlantDao,
    private val plantLogDao: PlantLogDao,
    context: Context
) {

    // 1. Obter o ID do utilizador que fez login
    private val sessionManager = SessionManager(context)
    private val currentUserId = sessionManager.fetchUserId() // Se for -1, algo correu mal no login

    val allPlants: LiveData<List<Plant>> = plantDao.getAllPlants()
    private val api = RetrofitClient.api

    // --- GUARDAR (Insert com Correção de ID) ---
    suspend fun insert(plant: Plant) {
        try {
            // Preparar dados para a API
            val request = PlantRequest(
                userId = currentUserId,
                name = plant.name,
                description = plant.description ?: "",
                photoUrl = "",
                nextWatering = convertLongToDate(plant.nextWateringDate),
                lastWatering = convertLongToDate(plant.lastWateredDate ?: System.currentTimeMillis())
            )

            // Enviar para a API e esperar pelo ID oficial
            val response = api.addPlant(request)

            // Agora que temos o ID Real, guardamos no telemóvel
            // O ID que vem do servidor vai ser forçado no Room
            val finalPlant = plant.copy(id = response.id)
            plantDao.insertPlant(finalPlant)

            Log.d("API_SUCCESS", "Planta criada com ID oficial: ${response.id}")

        } catch (e: Exception) {
            // Se falhar a Internet...
            Log.e("API_ERRO", "Sem net. A gravar localmente como backup.")

            // Guardar localmente com ID automático só para não perder os dados
            // Mas avisa que este ID vai estar dessincronizado do servidor
            plantDao.insertPlant(plant)
        }
    }

    // ATUALIZAR
    suspend fun update(plant: Plant) {
        // Atualiza localmente
        plantDao.updatePlant(plant)

        try {
            val request = PlantRequest(
                userId = currentUserId,
                name = plant.name,
                description = plant.description ?: "",
                photoUrl = "",
                nextWatering = convertLongToDate(plant.nextWateringDate),
                lastWatering = convertLongToDate(plant.lastWateredDate ?: System.currentTimeMillis())
            )

            api.updatePlant(plant.id, request)
            Log.d("API_UPDATE", "Planta ${plant.id} atualizada na nuvem")

        } catch (e: Exception) {
            Log.e("API_ERRO", "Falha ao atualizar: ${e.message}")
        }
    }

    // APAGAR
    suspend fun deleteById(id: Int) {
        plantDao.deleteById(id)
        try {
            api.deletePlant(id)
            Log.d("API_DELETE", "Planta $id apagada da nuvem")
        } catch (e: Exception) {
            Log.e("API_ERRO", "Falha ao apagar: ${e.message}")
        }
    }

    // REGA
    suspend fun waterPlant(plantId: Int, frequencyInDays: Int) {
        val now = System.currentTimeMillis()
        val next = now + (frequencyInDays.toLong() * 86400000L)
        plantDao.updateWateringDates(plantId, now, next)
    }

    private fun convertLongToDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}