package pt.ipt.dam.waterme.ui.plants

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.ipt.dam.waterme.data.database.WaterMeDatabase
import pt.ipt.dam.waterme.data.model.Plant
import pt.ipt.dam.waterme.data.repository.PlantRepository

class PlantsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PlantRepository
    val allPlants: LiveData<List<Plant>>

    init {
        val database = WaterMeDatabase.getDatabase(application)

        repository = PlantRepository(database.plantDao(), database.plantLogDao())

        allPlants = repository.allPlants
    }

    fun insert(plant: Plant) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(plant)
    }

    fun delete(plant: Plant) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteById(plant.id)
    }
}