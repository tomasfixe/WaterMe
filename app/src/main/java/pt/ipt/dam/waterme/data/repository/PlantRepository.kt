package pt.ipt.dam.waterme.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import pt.ipt.dam.waterme.data.dao.PlantDao
import pt.ipt.dam.waterme.data.dao.PlantLogDao
import pt.ipt.dam.waterme.data.model.Plant
import pt.ipt.dam.waterme.data.model.PlantLog
import pt.ipt.dam.waterme.data.model.PlantRequest
import pt.ipt.dam.waterme.data.network.RetrofitClient
import pt.ipt.dam.waterme.data.session.SessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlantRepository(
    private val plantDao: PlantDao,
    private val plantLogDao: PlantLogDao,
    context: Context
) {

    // Obter o ID do utilizador que fez login
    private val sessionManager = SessionManager(context)
    private val currentUserId = sessionManager.fetchUserId() // Se for -1, algo correu mal no login

    val allPlants: LiveData<List<Plant>> = plantDao.getAllPlants(currentUserId)
    private val api = RetrofitClient.api

    // SICRONIZAR

    suspend fun refreshPlantsFromApi() {
        if (currentUserId == -1) return

        try {
            // 1. Buscar lista à API
            val remotePlants = api.getPlants(currentUserId)

            // BACKUP DAS FOTOS (como não enviamos fotos para a API, estavamos a apgar as fotos)
            // criar mapa temporário para guardar as fotos locais antes de apagar tudo
            val photoBackup = mutableMapOf<Int, String?>()

            for (remote in remotePlants) {
                // Verificamos se já temos esta planta localmente
                val local = plantDao.getPlantById(remote.id)

                // Se existir localmente e tiver foto, guardamos no backup
                if (local != null && !local.photoUri.isNullOrEmpty()) {
                    photoBackup[remote.id] = local.photoUri
                }
            }

            // 2. Se a API trouxer dados, limpamos o local para garantir que é igual
            plantDao.deleteUserPlants(currentUserId)


            // 3. Guardar tudo o que veio da net no telemóvel
            for (apiPlant in remotePlants) {
                // Converter String da API para Long
                val lastWatering = convertDateToLong(apiPlant.lastWatering)
                val nextWatering = convertDateToLong(apiPlant.nextWatering)

                val savedPhoto = photoBackup[apiPlant.id]
                val finalPhoto = if (!savedPhoto.isNullOrEmpty()) savedPhoto else apiPlant.photoUrl

                val localPlant = Plant(
                    id = apiPlant.id,
                    userId = currentUserId,
                    name = apiPlant.name,
                    description = apiPlant.description,
                    photoUri = finalPhoto,
                    waterFrequency = apiPlant.waterFrequency ?: 3, // Valor padrão
                    lastWateredDate = lastWatering,
                    nextWateringDate = nextWatering,
                    lightLevel = apiPlant.lightLevel
                )
                plantDao.insertPlant(localPlant)
            }
            Log.d("SYNC", "Sincronização completa. ${remotePlants.size} plantas carregadas.")

        } catch (e: Exception) {
            Log.e("SYNC", "Erro ao sincronizar: ${e.message}")
        }
    }

    // --- GUARDAR
    suspend fun insert(plant: Plant) {
        try {
            // Preparar dados para a API
            val request = PlantRequest(
                userId = currentUserId,
                name = plant.name,
                description = plant.description ?: "",
                photoUrl = "",
                nextWatering = convertLongToDate(plant.nextWateringDate),
                lastWatering = convertLongToDate(plant.lastWateredDate ?: System.currentTimeMillis()),
                lightLevel = plant.lightLevel,
                waterFrequency = plant.waterFrequency

            )

            // Enviar para a API e esperar pelo ID oficial
            val response = api.addPlant(request)

            // Agora que temos o ID Real, guardamos no telemóvel
            // O ID que vem do servidor vai ser forçado no Room
            val finalPlant = plant.copy(id = response.id, userId = currentUserId)
            plantDao.insertPlant(finalPlant)

            Log.d("API_SUCCESS", "Planta criada com ID oficial: ${response.id}")

        } catch (e: Exception) {
            // Se falhar a Internet
            Log.e("API_ERRO", "Sem net. A gravar localmente como backup.")

            val offlinePlant = plant.copy(userId = currentUserId)

            // Guardar localmente com ID automático só para não perder os dados
            // Mas avisa que este ID vai estar dessincronizado do servidor
            plantDao.insertPlant(plant)
        }
    }

    // ATUALIZAR
    suspend fun update(plant: Plant) {
        // Atualiza localmente
        val safePlant = plant.copy(userId = currentUserId)
        plantDao.updatePlant(safePlant)

        try {
            val request = PlantRequest(
                userId = currentUserId,
                name = plant.name,
                description = plant.description ?: "",
                photoUrl = "",
                nextWatering = convertLongToDate(plant.nextWateringDate),
                lastWatering = convertLongToDate(plant.lastWateredDate ?: System.currentTimeMillis()),
                lightLevel = plant.lightLevel,
                waterFrequency = plant.waterFrequency
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
    //  (Local + API)
    suspend fun waterPlant(plantId: Int) {
        val plant = plantDao.getPlantById(plantId)

        if (plant != null) {
            val now = System.currentTimeMillis()
            val next = now + (plant.waterFrequency.toLong() * 86400000L)

            // Atualiza a planta
            val updatedPlant = plant.copy(
                lastWateredDate = now,
                nextWateringDate = next,
                userId = currentUserId
            )
            update(updatedPlant)

            // Cria o Log na BD
            val logEntry = PlantLog(plantId = plantId, date = now)
            plantLogDao.insertLog(logEntry)

            Log.d("WATER_ACTION", "Planta regada e log criado!")
        }
    }



    private fun convertLongToDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    private fun convertDateToLong(dateString: String?): Long {
        if (dateString.isNullOrEmpty()) return System.currentTimeMillis()
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            sdf.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    // Função para buscar o histórico de uma planta
    suspend fun getPlantLogs(plantId: Int): List<PlantLog> {
        return plantLogDao.getLogsList(plantId)
    }

}