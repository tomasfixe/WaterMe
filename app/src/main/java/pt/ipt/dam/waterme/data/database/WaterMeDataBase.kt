package pt.ipt.dam.waterme.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pt.ipt.dam.waterme.data.dao.PlantDao
import pt.ipt.dam.waterme.data.model.Plant

@Database(entities = [Plant::class], version = 1, exportSchema = false)
abstract class WaterMeDatabase : RoomDatabase() {

    abstract fun plantDao(): PlantDao

    companion object {
        @Volatile
        private var INSTANCE: WaterMeDatabase? = null

        fun getDatabase(context: Context): WaterMeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WaterMeDatabase::class.java,
                    "waterme_database"
                ).fallbackToDestructiveMigration() // Apaga a BD se mudares a estrutura (bom para dev)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}