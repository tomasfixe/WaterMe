package pt.ipt.dam.waterme.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel para o fragmento de Perfil (ProfileFragment).
 * Esta classe é responsável por guardar e gerir os dados da UI do perfil.
 */
class ProfileViewModel : ViewModel() {

    // Apenas o ViewModel deve ter permissão para modificar os dados diretamente.
    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }

    /**
     * Variável pública e imutável (LiveData).
     * O Fragmento vai observar esta variável para atualizar o ecrã.
     */
    val text: LiveData<String> = _text
}