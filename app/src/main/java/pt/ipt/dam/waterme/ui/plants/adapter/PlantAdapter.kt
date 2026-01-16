package pt.ipt.dam.waterme.ui.plants.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam.waterme.R
import pt.ipt.dam.waterme.data.model.Plant

class PlantAdapter : ListAdapter<Plant, PlantAdapter.PlantViewHolder>(PlantComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plant, parent, false)
        return PlantViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class PlantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val plantNameItemView: TextView = itemView.findViewById(R.id.textViewPlantName)
        private val plantStatusItemView: TextView = itemView.findViewById(R.id.textViewWaterStatus)

        fun bind(plant: Plant) {
            plantNameItemView.text = plant.name
            // Aqui podes formatar a data mais tarde
            plantStatusItemView.text = "Rega a cada ${plant.waterFrequency} dias"
        }
    }

    class PlantComparator : DiffUtil.ItemCallback<Plant>() {
        override fun areItemsTheSame(oldItem: Plant, newItem: Plant): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Plant, newItem: Plant): Boolean {
            return oldItem == newItem
        }
    }
}