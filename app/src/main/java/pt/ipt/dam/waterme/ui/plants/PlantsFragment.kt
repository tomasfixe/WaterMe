package pt.ipt.dam.waterme.ui.plants

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import pt.ipt.dam.waterme.databinding.FragmentPlantsBinding
import pt.ipt.dam.waterme.ui.plants.adapter.PlantAdapter

/**
 * Fragmento principal que exibe a lista de plantas do utilizador.
 * Utiliza um RecyclerView para listagem eficiente e um ViewModel para observar
 * os dados da base de dados, garantindo que a UI está sempre atualizada.
 */
class PlantsFragment : Fragment() {

    // Variável para o ViewBinding (acesso seguro aos elementos do layout XML)
    private var _binding: FragmentPlantsBinding? = null
    // Propriedade auxiliar que garante acesso não-nulo ao binding
    private val binding get() = _binding!!

    /**
     * Cria e devolve a hierarquia de views associada ao fragmento.
     * @return A view raiz do layout 'fragment_plants.xml'.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlantsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Chamado imediatamente após a view ter sido criada.
     * É aqui que configuramos os adaptadores, observadores e listeners de botões.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar a lista (RecyclerView) e o comportamento do clique em cada item
        val adapter = PlantAdapter { plant ->
            // --- Código que corre quando o utilizador clica numa planta específica ---

            // Prepara a navegação para a atividade de Detalhes
            val intent = Intent(requireContext(), pt.ipt.dam.waterme.PlantDetailsActivity::class.java)

            // Passar os dados da planta selecionada para a outra página através de "Extras"
            // Isto evita ter de fazer uma nova query à base de dados na próxima activity
            intent.putExtra("PLANT_ID", plant.id)
            intent.putExtra("PLANT_NAME", plant.name)
            intent.putExtra("PLANT_DESC", plant.description)
            intent.putExtra("PLANT_FREQ", plant.waterFrequency)
            intent.putExtra("PLANT_PHOTO", plant.photoUri)

            // Passamos a data da próxima rega para mostrar logo no ecrã de detalhes
            intent.putExtra("PLANT_NEXT", plant.nextWateringDate)

            // Só passamos o nível de luz se ele tiver sido medido (não for nulo)
            if (plant.lightLevel != null) {
                intent.putExtra("PLANT_LIGHT", plant.lightLevel)
            }

            // Inicia a activity de detalhes
            startActivity(intent)
        }

        // Liga o adapter ao RecyclerView e define o layout como Linear (lista vertical)
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(context)

        // Ligar ao ViewModel
        // O ViewModel sobrevive a mudanças de configuração e gere os dados da UI
        val plantsViewModel = ViewModelProvider(this)[PlantsViewModel::class.java]

        // Chama a função para sincronizar dados:
        // Isto vai garantir que as plantas do User na API (nuvem) substituem as locais se houver diferenças
        plantsViewModel.refreshPlants()

        // Observar o LiveData 'allPlants'.
        // Sempre que a base de dados mudar (ex: sync da API ou nova planta adicionada),
        // este bloco corre automaticamente e atualiza a lista no ecrã.
        plantsViewModel.allPlants.observe(viewLifecycleOwner) { plants ->
            adapter.submitList(plants) // O ListAdapter calcula as diferenças e anima a atualização
        }

        // Configurar o botão flutuante (FAB) para adicionar nova planta
        binding.fabAddPlant.setOnClickListener {
            val intent = Intent(requireContext(), pt.ipt.dam.waterme.AddPlantActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Limpeza de recursos quando a view do fragmento é destruída.
     * Essencial para evitar memory leaks com o ViewBinding.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}