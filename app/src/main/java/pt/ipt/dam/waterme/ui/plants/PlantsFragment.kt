package pt.ipt.dam.waterme.ui.plants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import pt.ipt.dam.waterme.databinding.FragmentPlantsBinding

class PlantsFragment : Fragment() {

    private var _binding: FragmentPlantsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val plantsViewModel =
            ViewModelProvider(this).get(PlantsViewModel::class.java)

        _binding = FragmentPlantsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        /*val textView: TextView = binding.textHome
        plantsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

         */
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}