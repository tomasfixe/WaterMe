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

// Adicionámos 'onPlantClick' ao construtor
class PlantAdapter(private val onPlantClick: (Plant) -> Unit) :
    ListAdapter<Plant, PlantAdapter.PlantViewHolder>(PlantComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plant, parent, false)
        return PlantViewHolder(view, onPlantClick)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class PlantViewHolder(itemView: View, val onClick: (Plant) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val plantNameView: TextView = itemView.findViewById(R.id.textViewPlantName)
        private val plantStatusView: TextView = itemView.findViewById(R.id.textViewWaterStatus)
        private val plantImageView: ImageView = itemView.findViewById(R.id.ivPlantItem)

        fun bind(plant: Plant) {
            plantNameView.text = plant.name
            plantStatusView.text = "Rega a cada ${plant.waterFrequency} dias"

            // Carregar a foto se existir
            if (plant.photoUri != null) {
                try {
                    plantImageView.setImageURI(Uri.parse(plant.photoUri))
                } catch (e: Exception) {
                    plantImageView.setImageResource(android.R.drawable.ic_menu_report_image)
                }
            } else {
                plantImageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // Configurar o clique
            itemView.setOnClickListener {
                onClick(plant)
            }
        }
    }

    class PlantComparator : DiffUtil.ItemCallback<Plant>() {
        override fun areItemsTheSame(oldItem: Plant, newItem: Plant): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Plant, newItem: Plant): Boolean = oldItem == newItem
    }
}