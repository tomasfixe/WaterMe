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

/**
 * Repositório Principal (Single Source of Truth).
 * Esta classe é responsável por decidir de onde vêm os dados (Base de Dados Local ou API Remota)
 * e para onde devem ir. A UI (ViewModel) nunca deve aceder aos DAOs ou à API diretamente,
 * apenas comunica com este Repositório.
 */
class PlantRepository(
    private val plantDao: PlantDao,
    private val plantLogDao: PlantLogDao,
    context: Context
) {

    // Obter o ID do utilizador que fez login através da nossa gestão de sessão
    private val sessionManager = SessionManager(context)
    private val currentUserId = sessionManager.fetchUserId() // Se for -1, significa que não há login válido

    // Fonte de dados observável: O ViewModel vai "observar" esta variável.
    // Sempre que o Room for atualizado, esta lista emite os novos valores automaticamente.
    val allPlants: LiveData<List<Plant>> = plantDao.getAllPlants(currentUserId)

    // Instância da API para fazer chamadas de rede
    private val api = RetrofitClient.api

    // --- SICRONIZAÇÃO (Puxar dados da Nuvem) ---

    /**
     * Função para sincronizar a base de dados local com o servidor.
     * Estratégia:
     * 1. Busca dados ao servidor.
     * 2. Faz backup das fotos locais (porque o servidor não guarda os URIs locais das imagens).
     * 3. Apaga os dados locais antigos.
     * 4. Insere os dados novos fundidos com as fotos recuperadas.
     */
    suspend fun refreshPlantsFromApi() {
        if (currentUserId == -1) return // Segurança: não tentar sincronizar sem user

        try {
            // 1. Buscar lista atualizada à API (Backend Python)
            val remotePlants = api.getPlants(currentUserId)

            // BACKUP DAS FOTOS:
            // O servidor não guarda o caminho (URI) das fotos que estão no telemóvel.
            // Se apagássemos tudo, as plantas ficariam sem imagem.
            // Criamos um mapa temporário: ID da Planta -> Caminho da Foto.
            val photoBackup = mutableMapOf<Int, String?>()

            for (remote in remotePlants) {
                // Verificamos se já temos esta planta guardada localmente
                val local = plantDao.getPlantById(remote.id)

                // Se existir e tiver foto definida, guardamos no backup
                if (local != null && !local.photoUri.isNullOrEmpty()) {
                    photoBackup[remote.id] = local.photoUri
                }
            }

            // 2. Limpamos a tabela local para este utilizador
            plantDao.deleteUserPlants(currentUserId)


            // 3. Reconstruir a base de dados local com os dados da API + Fotos locais
            for (apiPlant in remotePlants) {
                // Converter as datas que vêm em String (JSON) para Long (Timestamp)
                val lastWatering = convertDateToLong(apiPlant.lastWatering)
                val nextWatering = convertDateToLong(apiPlant.nextWatering)

                // Tentar recuperar a foto do backup. Se não houver, usa o URL que veio da API (se existir).
                val savedPhoto = photoBackup[apiPlant.id]
                val finalPhoto = if (!savedPhoto.isNullOrEmpty()) savedPhoto else apiPlant.photoUrl

                val localPlant = Plant(
                    id = apiPlant.id,
                    userId = currentUserId,
                    name = apiPlant.name,
                    description = apiPlant.description,
                    photoUri = finalPhoto,
                    waterFrequency = apiPlant.waterFrequency ?: 3,
                    lastWateredDate = lastWatering,
                    nextWateringDate = nextWatering,
                    lightLevel = apiPlant.lightLevel
                )
                // Inserir na BD local
                plantDao.insertPlant(localPlant)
            }
            Log.d("SYNC", "Sincronização completa. ${remotePlants.size} plantas carregadas.")

        } catch (e: Exception) {
            // Se falhar (ex: sem internet), apenas registamos o erro.
            // O utilizador continua a ver os dados que já tinha em cache.
            Log.e("SYNC", "Erro ao sincronizar: ${e.message}")
        }
    }

    // --- GUARDAR (Insert) ---
    /**
     * Insere uma planta nova.
     * Tenta enviar primeiro para a API para obter o ID oficial gerado pelo servidor.
     * Se falhar (offline), guarda localmente.
     */
    suspend fun insert(plant: Plant) {
        try {
            // Preparar o objeto DTO (Data Transfer Object) para enviar para a API
            val request = PlantRequest(
                userId = currentUserId,
                name = plant.name,
                description = plant.description ?: "",
                photoUrl = "", // Não enviamos fotos para o backend
                nextWatering = convertLongToDate(plant.nextWateringDate),
                lastWatering = convertLongToDate(plant.lastWateredDate ?: System.currentTimeMillis()),
                lightLevel = plant.lightLevel,
                waterFrequency = plant.waterFrequency

            )

            // Enviar para a API e esperar pela resposta (que contém o ID novo)
            val response = api.addPlant(request)


            val finalPlant = plant.copy(id = response.id, userId = currentUserId)
            plantDao.insertPlant(finalPlant)

            Log.d("API_SUCCESS", "Planta criada com ID oficial: ${response.id}")

        } catch (e: Exception) {
            // Se falhar a Internet ou o Servidor
            Log.e("API_ERRO", "Sem net. A gravar localmente como backup.")

            // Garante que o user ID está correto
            val offlinePlant = plant.copy(userId = currentUserId)

            // Guardar localmente com ID automático do Room.
            // Nota: Isto pode causar problemas de sincronização futura (IDs duplicados),
            // mas garante que o utilizador não perde os dados no momento.
            plantDao.insertPlant(plant)
        }
    }

    // --- ATUALIZAR ---
    /**
     * Atualiza os dados de uma planta.
     * Atualiza primeiro localmente e depois tenta sincronizar com a nuvem.
     */
    suspend fun update(plant: Plant) {
        // 1. Atualiza logo no Room (Feedback instantâneo para o user)
        val safePlant = plant.copy(userId = currentUserId)
        plantDao.updatePlant(safePlant)

        try {
            // 2. Tenta enviar a atualização para o servidor em background
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
            // Se falhar, não faz mal. Já está guardado localmente e será sincronizado
            // na próxima vez que chamarmos refreshPlantsFromApi().
            Log.e("API_ERRO", "Falha ao atualizar: ${e.message}")
        }
    }

    // --- APAGAR ---
    /**
     * Remove uma planta pelo ID.
     * Primeiro apaga localmente, depois manda o comando de delete para a API.
     */
    suspend fun deleteById(id: Int) {
        plantDao.deleteById(id) // Apaga do telemóvel
        try {
            api.deletePlant(id) // Apaga do servidor
            Log.d("API_DELETE", "Planta $id apagada da nuvem")
        } catch (e: Exception) {
            Log.e("API_ERRO", "Falha ao apagar na nuvem (provavelmente offline): ${e.message}")
        }
    }

    // --- REGA (Lógica de Negócio) ---
    /**
     * Função complexa que gere o ato de "Regar".
     * 1. Calcula a nova data de rega.
     * 2. Atualiza a planta.
     * 3. Cria um registo no histórico (Logs).
     */
    suspend fun waterPlant(plantId: Int) {
        val plant = plantDao.getPlantById(plantId)

        if (plant != null) {
            val now = System.currentTimeMillis()
            // Cálculo matemático: Próxima rega = Agora + (Frequência em dias * milissegundos num dia)
            // 86400000 = 24h * 60m * 60s * 1000ms
            val next = now + (plant.waterFrequency.toLong() * 86400000L)

            // Atualiza o objeto planta com as novas datas
            val updatedPlant = plant.copy(
                lastWateredDate = now,
                nextWateringDate = next,
                userId = currentUserId
            )
            // Chama a função update (que trata de guardar local e na API)
            update(updatedPlant)

            // Cria o registo de histórico na tabela separada (plant_logs)
            val logEntry = PlantLog(plantId = plantId, date = now)
            plantLogDao.insertLog(logEntry)

            Log.d("WATER_ACTION", "Planta regada e log criado!")
        }
    }


    // --- FUNÇÕES AUXILIARES (Converters) ---

    /**
     * Converte um Timestamp (Long) para String no formato ISO "yyyy-MM-dd'T'HH:mm:ss".
     * Necessário porque a base de dados SQL guarda números, mas a API espera texto.
     */
    private fun convertLongToDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Converte uma String da API para Timestamp (Long).
     * Se a conversão falhar, devolve a data atual como fallback para não crashar a app.
     */
    private fun convertDateToLong(dateString: String?): Long {
        if (dateString.isNullOrEmpty()) return System.currentTimeMillis()
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            sdf.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    /**
     * Busca o histórico de regas de uma planta específica.
     * Usado para preencher o Dialog de detalhes da planta.
     * @return Lista de logs.
     */
    suspend fun getPlantLogs(plantId: Int): List<PlantLog> {
        return plantLogDao.getLogsList(plantId)
    }

}