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

/**
 * ViewModel responsável pela gestão dos dados da lista de plantas.
 *
 * Seguindo a arquitetura MVVM (Model-View-ViewModel), esta classe serve de ponte
 * entre a UI (PlantsFragment) e os dados (PlantRepository).
 *
 * Herdamos de 'AndroidViewModel' em vez de 'ViewModel' simples porque precisamos
 * do 'Context' da aplicação para instanciar a Base de Dados Room.
 */
class PlantsViewModel(application: Application) : AndroidViewModel(application) {

    // Referência ao repositório que contém a lógica de acesso aos dados
    private val repository: PlantRepository

    // LiveData que contém a lista de plantas.
    // A UI vai observar esta variável. Sempre que o repositório atualizar a base de dados,
    // esta lista é atualizada automaticamente e a UI redesenha-se.
    val allPlants: LiveData<List<Plant>>

    // O bloco 'init' é executado assim que o ViewModel é instanciado
    init {
        // 1. Obter a instância da Base de Dados
        val database = WaterMeDatabase.getDatabase(application)

        // 2. Inicializar o Repositório com os DAOs necessários e o contexto
        repository = PlantRepository(database.plantDao(), database.plantLogDao(), application)

        // 3. Ligar o LiveData do ViewModel ao LiveData do Repositório
        allPlants = repository.allPlants
    }

    /**
     * Adiciona uma nova planta à base de dados.
     * @param plant O objeto a inserir.
     */
    fun insert(plant: Plant) = viewModelScope.launch(Dispatchers.IO) {
        // Dispatchers.IO é usado para operações de Input/Output (Base de Dados/Rede)
        repository.insert(plant)
    }

    /**
     * Remove uma planta da base de dados.
     * @param plant O objeto a eliminar.
     */
    fun delete(plant: Plant) = viewModelScope.launch(Dispatchers.IO) {
        // Chama o método deleteById do repositório
        repository.deleteById(plant.id)
    }

    /**
     * Força a sincronização dos dados com a API (Nuvem).
     * Esta função é chamada quando o Fragmento é criado para garantir que
     * temos os dados mais recentes do servidor.
     */
    fun refreshPlants() {
        viewModelScope.launch(Dispatchers.IO) {
            // Pede ao repositório para ir buscar dados novos à net e atualizar a cache local
            repository.refreshPlantsFromApi()
        }
    }
}