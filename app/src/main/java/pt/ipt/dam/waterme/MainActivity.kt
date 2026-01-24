package pt.ipt.dam.waterme

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import pt.ipt.dam.waterme.databinding.ActivityMainBinding
import pt.ipt.dam.waterme.worker.WaterReminderWorker
import java.util.concurrent.TimeUnit

/**
 * Activity Principal da aplicação (Home).
 * É apresentada logo após o Login e contém a barra de navegação inferior (BottomNavigation),
 * servindo de contentor para os Fragmentos (Lista de Plantas e Perfil).
 *
 * Também é responsável por iniciar os serviços de background, como as notificações de rega.
 */
class MainActivity : AppCompatActivity() {

    // Variável para aceder aos elementos da interface (ViewBinding)
    private lateinit var binding: ActivityMainBinding

    /**
     * Contrato para pedir permissões em tempo de execução.
     * Se a permissão for concedida, o código dentro do bloco é executado (neste caso, vazio).
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permissão concedida
        }
    }

    /**
     * Método de criação da Activity.
     * Configura o layout, a navegação entre fragmentos e inicia o agendamento de tarefas.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate do layout usando Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        // Configurar o NavController
        // O fragmento 'nav_host_fragment_activity_main' no XML é que troca os ecrãs
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Define quais são os ecrãs de topo (para não mostrar o botão de voltar nestes)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_plants, R.id.navigation_profile
            )
        )


        // Liga a barra de baixo (ícones) ao controlador de navegação.
        navView.setupWithNavController(navController)

        // ---  Lógica de Notificações ---
        // Verifica permissões e agenda o worker
        setupNotifications()
        scheduleWaterReminder()
    }

    /**
     * Verifica a versão do Android e solicita a permissão de notificações se necessário.
     */
    private fun setupNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Verifica se JÁ temos a permissão
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                // Se não temos, lança o popup do sistema a pedir
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    /**
     * Agenda a tarefa de fundo (Worker) que verifica periodicamente as plantas.
     */
    private fun scheduleWaterReminder() {
        // Define a frequência (Mínimo aceite pelo Android é 15 minutos)
        val workRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(15, TimeUnit.MINUTES)
            .build()

        // Enfileira o trabalho
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "WaterReminderWork", // Nome único da tarefa (ID)
            ExistingPeriodicWorkPolicy.KEEP, // Se já existir uma tarefa com este nome, MANTÉM a antiga (não duplica)
            workRequest
        )
    }
}