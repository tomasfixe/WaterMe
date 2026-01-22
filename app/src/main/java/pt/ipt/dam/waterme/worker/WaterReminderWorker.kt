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

class WaterReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Aceder à Base de Dados Local
                val database = WaterMeDatabase.getDatabase(applicationContext)
                val plantDao = database.plantDao()

                val now = System.currentTimeMillis()

                // 2. Procurar plantas com sede
                val thirstyPlants = plantDao.getPlantsNeedingWater(now)

                if (thirstyPlants.isNotEmpty()) {
                    // Prepara o texto (ex: "Cato, Rosa, Tulipa")
                    val plantNames = thirstyPlants.joinToString(", ") { it.name }

                    // 3. Enviar notificação
                    sendNotification(
                        "Hora de Regar! 💧",
                        "As seguintes plantas precisam de água: $plantNames"
                    )
                }

                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }

    private fun sendNotification(title: String, message: String) {
        val context = applicationContext
        val channelId = "water_me_channel_id"

        // Criar o Canal de Notificação (Obrigatório Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Lembretes de Rega"
            val descriptionText = "Notifica quando as plantas precisam de água"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Configurar o clique na notificação (abre a MainActivity)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Construir a notificação
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp) // Usa um ícone que tenhas (ex: o do menu)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Enviar
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notify(1001, builder.build())
            }
        }
    }
}