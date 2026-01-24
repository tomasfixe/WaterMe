package pt.ipt.dam.waterme.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pt.ipt.dam.waterme.data.dao.PlantDao
import pt.ipt.dam.waterme.data.dao.PlantLogDao
import pt.ipt.dam.waterme.data.model.Plant
import pt.ipt.dam.waterme.data.model.PlantLog

/**
 * Classe principal da Base de Dados Room.
 * Define a configuração da base de dados, incluindo as entidades (tabelas) e a versão.
 * Atua como o ponto de acesso principal para os DAOs.
 *
 * @entities Lista de classes que representam as tabelas na BD (Plant e PlantLog).
 * @version Número da versão da base de dados. Deve ser incrementado sempre que mudamos a estrutura (esquema).
 */
@Database(entities = [Plant::class, PlantLog::class], version = 3, exportSchema = false)
abstract class WaterMeDatabase : RoomDatabase() {

    /**
     * Obtém o DAO para a entidade Plant.
     * @return A implementação da interface PlantDao gerada automaticamente pelo Room.
     */
    abstract fun plantDao(): PlantDao

    /**
     * Obtém o DAO para a entidade PlantLog.
     * @return A implementação da interface PlantLogDao gerada automaticamente pelo Room.
     */
    abstract fun plantLogDao(): PlantLogDao

    // O Companion Object permite aceder a métodos estáticos (como o getDatabase) sem instanciar a classe.
    companion object {
        // @Volatile garante que mudanças nesta variável são imediatamente visíveis para todos os threads.
        @Volatile
        private var INSTANCE: WaterMeDatabase? = null

        /**
         * Padrão Singleton para garantir que apenas existe uma instância da base de dados aberta
         * durante todo o ciclo de vida da aplicação.
         *
         * @param context O contexto da aplicação.
         * @return A instância única da base de dados.
         */
        fun getDatabase(context: Context): WaterMeDatabase {
            // Se a instância já existir, devolve-a. Se não, entra no bloco synchronized para criar.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WaterMeDatabase::class.java,
                    "waterme_database" // Nome do ficheiro da BD no telemóvel
                )
                    // Estratégia de migração: Se a versão mudar, apaga tudo e recria as tabelas.
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}