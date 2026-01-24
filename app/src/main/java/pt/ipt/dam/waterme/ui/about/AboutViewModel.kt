package pt.ipt.dam.waterme.ui.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel para o ecrã "Sobre Nós".
 * Esta classe é responsável por guardar e gerir os dados da UI (Interface de Utilizador)
 * do fragmento AboutFragment. A vantagem de usar um ViewModel é que os dados sobrevivem
 * a mudanças de configuração (como rodar o ecrã), evitando que se percam.
 */
class AboutViewModel : ViewModel() {

    // Variável privada e mutável que guarda o estado do texto.
    // É 'MutableLiveData' porque nós (dentro da classe) precisamos de alterar o valor.
    private val _text = MutableLiveData<String>().apply {
        value = "This is notifications Fragment"
    }

    /**
     * Variável pública e imutável (apenas LiveData) que é exposta para a UI.
     * O fragmento vai "observar" esta variável. Assim, garantimos o encapsulamento:
     * a UI consegue ler os dados, mas não os consegue alterar diretamente.
     */
    val text: LiveData<String> = _text
}