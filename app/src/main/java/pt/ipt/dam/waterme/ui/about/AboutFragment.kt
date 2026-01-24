package pt.ipt.dam.waterme.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import pt.ipt.dam.waterme.databinding.FragmentAboutBinding

/**
 * Fragmento responsável pelo ecrã "Sobre Nós".
 * Este fragmento é carregado quando o utilizador clica na respetiva opção de navegação
 * ou botão. Utiliza ViewBinding para aceder aos elementos do layout XML de forma segura.
 */
class AboutFragment : Fragment() {

    // Variável para guardar a referência do binding (ligação ao XML).
    private var _binding: FragmentAboutBinding? = null


    /**
     * Propriedade auxiliar para aceder ao binding de forma segura (sem nulls).
     * Só deve ser chamada entre o onCreateView e o onDestroyView.
     * O '!!' afirma que temos a certeza que não é nulo neste intervalo.
     */
    private val binding get() = _binding!!

    /**
     * Método chamado pelo Android para "desenhar" a interface do fragmento.
     *
     * @param inflater O objeto usado para converter XML em View.
     * @param container O pai onde a view do fragmento será inserida.
     * @param savedInstanceState Estado guardado anteriormente (se houver).
     * @return A View raiz do fragmento.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inicializa o ViewModel associado a este fragmento
        val aboutViewModel =
            ViewModelProvider(this).get(AboutViewModel::class.java)

        // Inflate do layout usando o Binding gerado
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        val root: View = binding.root



        // Retorna a view principal para ser mostrada no ecrã
        return root
    }

    /**
     * Método chamado quando a view associada ao fragmento é removida.
     * É fundamental limpar a referência do binding aqui para evitar fugas de memória ,
     * pois o Fragmento pode continuar vivo na memória mesmo sem View.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}