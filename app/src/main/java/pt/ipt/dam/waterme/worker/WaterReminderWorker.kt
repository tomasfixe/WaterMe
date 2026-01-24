package pt.ipt.dam.waterme.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pt.ipt.dam.waterme.MainActivity
import pt.ipt.dam.waterme.R
import pt.ipt.dam.waterme.data.database.WaterMeDatabase

/**
 * Worker responsável por verificar, em segundo plano, se existem plantas a precisar de água.
 *
 * Utiliza o WorkManager do Android para agendar tarefas periódicas, garantindo que
 * o código corre mesmo que a aplicação esteja fechada.
 * Herda de 'CoroutineWorker' para permitir operações assíncronas (como aceder à BD) de forma simples.
 */
class WaterReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    /**
     * Método principal onde o trabalho é executado.
     * O Android chama este método automaticamente quando as condições do agendamento forem cumpridas.
     *
     * @return Result.success() se tudo correr bem, ou Result.failure() se houver erro (para o WorkManager saber se deve tentar de novo).
     */
    override suspend fun doWork(): Result {
        // Mudamos para o contexto Dispatchers.IO porque aceder à base de dados é uma operação de Input/Output
        return withContext(Dispatchers.IO) {
            try {
                // 1. Aceder à Base de Dados Local
                // Precisamos de instanciar a BD manualmente porque os Workers não têm acesso direto aos ViewModels
                val database = WaterMeDatabase.getDatabase(applicationContext)
                val plantDao = database.plantDao()

                val now = System.currentTimeMillis()

                // 2. Procurar plantas para regar
                // O DAO tem uma query específica que compara a data de 'nextWatering' com o tempo atual ('now')
                val thirstyPlants = plantDao.getPlantsNeedingWater(now)

                if (thirstyPlants.isNotEmpty()) {
                    // Prepara o texto (ex: "Cato, Rosa, Tulipa")
                    // A função joinToString cria uma string separada por vírgulas com os nomes de todas as plantas na lista
                    val plantNames = thirstyPlants.joinToString(", ") { it.name }

                    // 3. Enviar notificação para o utilizador
                    sendNotification(
                        "Hora de Regar! 💧",
                        "As seguintes plantas precisam de água: $plantNames"
                    )
                }

                // Indica ao sistema que o trabalho foi concluído com sucesso
                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                // Indica falha (o WorkManager pode tentar executar novamente mais tarde dependendo da configuração)
                Result.failure()
            }
        }
    }

    /**
     * Função auxiliar para construir e disparar a notificação na barra de estado.
     *
     * @param title O título da notificação.
     * @param message O corpo da mensagem.
     */
    private fun sendNotification(title: String, message: String) {
        val context = applicationContext
        val channelId = "water_me_channel_id"

        // Criar o Canal de Notificação
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Lembretes de Rega"
            val descriptionText = "Notifica quando as plantas precisam de água"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            // Regista o canal no sistema
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Configurar o clique na notificação
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Construir a notificação
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp) // Usa um ícone pequeno
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // Define o que acontece ao clicar
            .setAutoCancel(true) // A notificação desaparece quando clicada

        // Enviar a notificação
        with(NotificationManagerCompat.from(context)) {
            // Verificar se temos permissão
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // O ID (1001) serve para identificar a notificação.
                // Se usarmos sempre o mesmo ID, a notificação nova substitui a antiga.
                notify(1001, builder.build())
            }
        }
    }
}