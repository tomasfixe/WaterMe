package pt.ipt.dam.waterme.ui.plants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import pt.ipt.dam.waterme.databinding.FragmentPlantsBinding
import pt.ipt.dam.waterme.ui.plants.adapter.PlantAdapter
import kotlin.jvm.java

class PlantsFragment : Fragment() {

    private var _binding: FragmentPlantsBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlantsBinding.inflate(inflater, container, false)

        // Forma de iniciar ViewModel se o 'by viewModels' falhar:
        // plantsViewModel = ViewModelProvider(this).get(PlantsViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar a lista
        val adapter = PlantAdapter { plant ->
            // Código que corre quando clica numa planta
            val intent = android.content.Intent(requireContext(), pt.ipt.dam.waterme.PlantDetailsActivity::class.java)

            // Passar os dados para a outra página
            intent.putExtra("PLANT_ID", plant.id)
            intent.putExtra("PLANT_NAME", plant.name)
            intent.putExtra("PLANT_DESC", plant.description)
            intent.putExtra("PLANT_FREQ", plant.waterFrequency)
            intent.putExtra("PLANT_PHOTO", plant.photoUri)
            // Precisamos de verificar se o valor é nulo antes de passar, ou passar valor default
            if (plant.lightLevel != null) {
                intent.putExtra("PLANT_LIGHT", plant.lightLevel)
            }

            startActivity(intent)
        }
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(context)

        // Ligar ao ViewModel
        val plantsViewModel = ViewModelProvider(this)[PlantsViewModel::class.java]
        plantsViewModel.allPlants.observe(viewLifecycleOwner) { plants ->
            adapter.submitList(plants)
        }

        //
        binding.fabAddPlant.setOnClickListener {
            val intent = android.content.Intent(requireContext(), pt.ipt.dam.waterme.AddPlantActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}