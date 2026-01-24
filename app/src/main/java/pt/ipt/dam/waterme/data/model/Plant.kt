package pt.ipt.dam.waterme.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Classe de Dados (Entity) que representa a tabela 'plants' na base de dados local (Room).
 * Cada instância desta classe corresponde a uma linha na tabela de plantas.
 *
 * Esta classe guarda toda a informação essencial sobre cada planta que o utilizador adiciona.
 */
@Entity(tableName = "plants")
data class Plant(
    /**
     * Chave primária da tabela.
     * 'autoGenerate = true' indica que a base de dados gera este ID automaticamente
     * (1, 2, 3...) cada vez que inserimos uma planta nova.
     */
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // ID do utilizador a quem a planta pertence.
    // Usado para garantir que cada utilizador vê apenas as suas próprias plantas.
    val userId: Int = -1, // Identifica o dono da planta

    // O nome ou alcunha dado à planta.
    val name: String,

    // Descrição opcional (pode ser null) com notas sobre a planta.
    val description: String?,

    // Guardamos o caminho (URI) da foto no armazenamento local do telemóvel
    // em vez de guardar a imagem inteira na base de dados.
    val photoUri: String?, // Caminho para a foto no telemóvel

    // Frequência de rega definida em dias.
    // Usado para calcular a próxima data de rega (nextWateringDate).
    val waterFrequency: Int, // De quantos em quantos dias

    // Data da última vez que o botão "Regar" foi clicado.
    // Guardamos como Long (Timestamp em milissegundos) para facilitar cálculos matemáticos de tempo.
    val lastWateredDate: Long?, // Timestamp

    // Data calculada para a próxima rega (lastWateredDate + waterFrequency).
    // O WorkManager consulta este campo para saber quando enviar notificações.
    val nextWateringDate: Long, // Timestamp

    // Valor lido pelo sensor de luminosidade do telemóvel.
    val lightLevel: Float? = null, // Valor do sensor de luz

    // Flag de controlo para a sincronização com a API.
    // false = Ainda não foi enviada para o servidor (Python).
    // true = Já está segura na cloud.
    val synced: Boolean = false // Para saber se já foi enviada para a API
)