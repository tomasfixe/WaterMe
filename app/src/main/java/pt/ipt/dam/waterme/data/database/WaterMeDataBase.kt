package pt.ipt.dam.waterme.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pt.ipt.dam.waterme.data.dao.PlantDao
import pt.ipt.dam.waterme.data.dao.PlantLogDao
import pt.ipt.dam.waterme.data.model.Plant
import pt.ipt.dam.waterme.data.model.PlantLog

@Database(entities = [Plant::class, PlantLog::class], version = 2, exportSchema = false)
abstract class WaterMeDatabase : RoomDatabase() {

    abstract fun plantDao(): PlantDao
    abstract fun plantLogDao(): PlantLogDao

    companion object {
        @Volatile
        private var INSTANCE: WaterMeDatabase? = null

        fun getDatabase(context: Context): WaterMeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WaterMeDatabase::class.java,
                    "waterme_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}