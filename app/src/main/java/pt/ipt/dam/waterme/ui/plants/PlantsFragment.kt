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

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlantsBinding.inflate(inflater, container, false)

        // Forma clássica de iniciar ViewModel se o 'by viewModels' falhar:
        // plantsViewModel = ViewModelProvider(this).get(PlantsViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Configurar a lista (RecyclerView)
        val adapter = PlantAdapter()
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(context)

        // 2. Ligar ao ViewModel
        val plantsViewModel = ViewModelProvider(this)[PlantsViewModel::class.java]
        plantsViewModel.allPlants.observe(viewLifecycleOwner) { plants ->
            adapter.submitList(plants)
        }

        // 3. O SEU BOTÃO + (FAB)
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