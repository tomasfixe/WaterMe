package pt.ipt.dam.waterme.ui.plants.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam.waterme.R
import pt.ipt.dam.waterme.data.model.Plant

/**
 * Adaptador para a RecyclerView que mostra a lista de plantas.
 * @param onPlantClick Função que será executada quando o utilizador clicar numa planta.
 */
class PlantAdapter(private val onPlantClick: (Plant) -> Unit) :
    ListAdapter<Plant, PlantAdapter.PlantViewHolder>(PlantComparator()) {

    /**
     * Cria uma nova ViewHolder para mostrar um item da lista.
     * Este método só é chamado quando o RecyclerView precisa de criar novas views
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        // Inflate (transformar XML em View) o layout de cada linha (item_plant.xml)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plant, parent, false)
        return PlantViewHolder(view, onPlantClick)
    }

    /**
     * Associa os dados de uma planta específica a uma view existente.
     * Este método é chamado constantemente enquanto o utilizador faz scroll.
     * @param holder A "caixa" que vai receber os dados.
     * @param position A posição do item na lista.
     */
    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    /**
     * Classe interna que guarda as referências para os elementos visuais de cada linha.
     */
    class PlantViewHolder(itemView: View, val onClick: (Plant) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val plantNameView: TextView = itemView.findViewById(R.id.textViewPlantName)
        private val plantStatusView: TextView = itemView.findViewById(R.id.textViewWaterStatus)
        private val nextWateringView: TextView = itemView.findViewById(R.id.textViewNextWatering)
        private val plantImageView: ImageView = itemView.findViewById(R.id.ivPlantItem)

        /**
         * Função que coloca os dados reais da planta nos campos de texto e imagem.
         */
        fun bind(plant: Plant) {
            plantNameView.text = plant.name

            plantStatusView.text = "Rega a cada ${plant.waterFrequency} dias"

            // Convertemos o Timestamp (Long) para uma data legível (Dia/Mês Hora:Minuto)
            val sdf = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault())
            val dataFormatada = sdf.format(java.util.Date(plant.nextWateringDate))

            nextWateringView.text = "Próxima: $dataFormatada"

            // Carregar a imagem
            if (plant.photoUri != null) {
                try {
                    // Tenta carregar a imagem do caminho guardado no telemóvel
                    plantImageView.setImageURI(Uri.parse(plant.photoUri))
                } catch (e: Exception) {
                    // Se a imagem tiver sido apagada da galeria, mostra um ícone de erro
                    plantImageView.setImageResource(android.R.drawable.ic_menu_report_image)
                }
            } else {
                // Se a planta não tiver foto definida, mostra um ícone genérico
                plantImageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // Configurar o clique na linha inteira para abrir os detalhes
            itemView.setOnClickListener {
                onClick(plant)
            }
        }
    }

    /**
     * Classe utilitária usada pelo ListAdapter para calcular diferenças entre listas.
     * Permite animações suaves quando adicionamos ou removemos plantas.
     */
    class PlantComparator : DiffUtil.ItemCallback<Plant>() {
        // Verifica se é o mesmo objeto (pelo ID único)
        override fun areItemsTheSame(oldItem: Plant, newItem: Plant): Boolean = oldItem.id == newItem.id

        // Verifica se o conteúdo mudou (se o nome, descrição, etc. são iguais)
        // Como 'Plant' é uma data class, o '==' compara todos os campos automaticamente.
        override fun areContentsTheSame(oldItem: Plant, newItem: Plant): Boolean = oldItem == newItem
    }
}