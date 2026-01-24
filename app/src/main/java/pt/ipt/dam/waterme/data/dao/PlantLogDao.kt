package pt.ipt.dam.waterme.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import pt.ipt.dam.waterme.data.model.PlantLog

/**
 * Interface DAO (Data Access Object) para a tabela 'plant_logs'.
 * Esta interface gere o histórico de regas, permitindo guardar quando
 * uma planta foi regada e consultar esse histórico posteriormente.
 */
@Dao
interface PlantLogDao {

    /**
     * Regista uma nova ação de rega na base de dados.
     * @param log O objeto PlantLog com a data e o ID da planta.
     */
    @Insert
    suspend fun insertLog(log: PlantLog)

    /**
     * Obtém o histórico de regas de uma planta.
     * Retorna um LiveData, se adicionarmos um log novo,
     * quem estiver a observar esta função (ex:UI) recebe a lista atualizada automaticamente.
     * @param plantId O ID da planta que queremos consultar.
     * @return LiveData com a lista de logs ordenada da mais recente para a mais antiga.
     */
    // Buscar logs
    @Query("SELECT * FROM plant_logs WHERE plantId = :plantId ORDER BY date DESC")
    fun getLogsForPlant(plantId: Int): LiveData<List<PlantLog>>

    /**
     * Obtém o histórico de regas como uma lista estática.
     * Ao contrário da função anterior, esta não observa mudanças. É útil para
     * carregar dados uma única vez, como ao abrir um Dialog ou para o WorkManager.
     * @param plantId O ID da planta.
     * @return Lista suspensa de logs ordenada por data decrescente.
     */
    // Buscar lista
    @Query("SELECT * FROM plant_logs WHERE plantId = :plantId ORDER BY date DESC")
    suspend fun getLogsList(plantId: Int): List<PlantLog>
}