package pt.ipt.dam.waterme.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade que representa a tabela 'plant_logs' na base de dados.
 * Esta classe serve para manter um histórico de todas as vezes que uma planta foi regada.
 * Relação de "um para muitos": uma Planta pode ter muitos Logs.
 */
@Entity(tableName = "plant_logs")
data class PlantLog(
    /**
     * Chave primária (Primary Key) da tabela de logs.
     * O 'autoGenerate = true' garante que cada novo registo recebe um ID único automaticamente.
     */
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    /**
     * ID da planta associada a este registo (Foreign Key lógica).
     * Permite saber a que planta pertence este histórico de rega.
     */
    val plantId: Int,      // Liga log a uma planta específica

    /**
     * A data e hora exata em que a rega aconteceu.
     */
    val date: Long         // Data e hora da rega
)