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
        // 1. ACHAR O NOVO TEXTO
        private val nextWateringView: TextView = itemView.findViewById(R.id.textViewNextWatering)
        private val plantImageView: ImageView = itemView.findViewById(R.id.ivPlantItem)

        fun bind(plant: Plant) {
            plantNameView.text = plant.name

            // 2. LINHA 1: A Frequência
            plantStatusView.text = "Rega a cada ${plant.waterFrequency} dias"

            // 3. LINHA 2: A Data Formatada
            val sdf = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault())
            val dataFormatada = sdf.format(java.util.Date(plant.nextWateringDate))

            nextWateringView.text = "Próxima: $dataFormatada"

            //
            if (plant.photoUri != null) {
                try {
                    plantImageView.setImageURI(Uri.parse(plant.photoUri))
                } catch (e: Exception) {
                    plantImageView.setImageResource(android.R.drawable.ic_menu_report_image)
                }
            } else {
                plantImageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }

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