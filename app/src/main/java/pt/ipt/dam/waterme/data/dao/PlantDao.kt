package pt.ipt.dam.waterme.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import pt.ipt.dam.waterme.data.model.Plant

/**
 * Interface DAO (Data Access Object) para a entidade Plant.
 * Esta interface define todos os métodos para interagir com a tabela 'plants' na base de dados Room.
 * O Room gera automaticamente a implementação destas funções em tempo de compilação.
 */
@Dao
interface PlantDao {

    /**
     * Insere uma nova planta na base de dados.
     * @param plant O objeto Plant a ser inserido.
     * @return O ID da nova linha inserida (Long).
     */

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Se já existir uma planta com o mesmo ID, substitui-a
    suspend fun insertPlant(plant: Plant): Long

    /**
     * Atualiza os dados de uma planta existente na base de dados.
     * O Room procura a planta pelo ID e atualiza os restantes campos.
     * @param plant O objeto Plant com os dados atualizados.
     */
    @Update
    suspend fun updatePlant(plant: Plant)

    /**
     * Elimina uma planta específica da base de dados.
     * @param plant O objeto Plant a ser eliminado.
     */
    @Delete
    suspend fun deletePlant(plant: Plant)

    /**
     * Elimina uma planta usando apenas o seu ID.
     * Útil quando não temos o objeto planta completo, apenas o identificador.
     * @param plantId O ID da planta a eliminar.
     */
    @Query("DELETE FROM plants WHERE id = :plantId")
    suspend fun deleteById(plantId: Int)

    /**
     * Obtém todas as plantas associadas a um utilizador específico.
     * A lista é devolvida como LiveData para permitir que a Interface
     * seja atualizada automaticamente sempre que houver mudanças na base de dados.
     * @param userId O ID do utilizador cujas plantas queremos.
     * @return LiveData contendo a lista de plantas, ordenada pela data da próxima rega (ascendente).
     */
    @Query("SELECT * FROM plants WHERE userId = :userId ORDER BY nextWateringDate ASC")
    fun getAllPlants(userId: Int): LiveData<List<Plant>>

    /**
     * Elimina todas as plantas de um utilizador específico.
     * Usado quando um utilizador faz logout limpando dados (ou futuramente quando queremos apagar todas as plantas -> por implementar).
     * @param userId O ID do utilizador.
     */
    @Query("DELETE FROM plants WHERE userId = :userId")
    suspend fun deleteUserPlants(userId: Int)

    /**
     * Elimina TODAS as entradas na tabela de plantas.
     * Usado para limpar completamente a cache local da aplicação.
     */
    @Query("DELETE FROM plants")
    suspend fun deleteAll()

    /**
     * Procura e retorna uma planta específica pelo seu ID.
     * @param plantId O ID da planta.
     * @return O objeto Plant se encontrado, ou null caso contrário.
     */
    @Query("SELECT * FROM plants WHERE id = :plantId")
    suspend fun getPlantById(plantId: Int): Plant?

    /**
     * Atualiza apenas as datas de rega de uma planta.
     * @param id O ID da planta.
     * @param last A nova data da última rega (timestamp).
     * @param next A nova data da próxima rega (timestamp).
     */
    @Query("UPDATE plants SET lastWateredDate = :last, nextWateringDate = :next WHERE id = :id")
    suspend fun updateWateringDates(id: Int, last: Long, next: Long)

    /**
     * Obtém uma lista de plantas que precisam de ser regadas (ou cuja data já passou).
     * Esta função é usada pelo WorkManager para verificar se deve enviar notificações.
     * @param now O timestamp atual.
     * @return Lista de plantas onde a próxima rega é menor ou igual a 'agora'.
     */
    // Compara a data de próxima rega com o tempo atual passado como parâmetro
    @Query("SELECT * FROM plants WHERE nextWateringDate <= :now")
    suspend fun getPlantsNeedingWater(now: Long): List<Plant>
}