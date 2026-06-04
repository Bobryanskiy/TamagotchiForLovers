package com.github.bobryanskiy.tamagotchiforlovers.presentation

import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.navigation.compose.rememberNavController
import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.presentation.navigation.AppNavGraph
import com.github.bobryanskiy.tamagotchiforlovers.presentation.theme.TamagotchiTheme
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val appViewModel: AppViewModel by viewModels()

    @Inject lateinit var logger: AppLogger

    private val notificationPermissionLauncher = registerForActivityResult(
        RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            logger.d(TAG, "✅ POST_NOTIFICATIONS granted")
        } else {
            logger.w(TAG, "❌ POST_NOTIFICATIONS denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                logger.d(TAG, "Requesting POST_NOTIFICATIONS permission")
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                logger.d(TAG, "✅ POST_NOTIFICATIONS already granted")
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                logger.w(TAG, "⚠️ SCHEDULE_EXACT_ALARM not granted, opening settings")
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:$packageName".toUri()
                }
                startActivity(intent)
            } else {
                logger.d(TAG, "✅ SCHEDULE_EXACT_ALARM granted")
            }
        }

        enableEdgeToEdge()
        setContent {
            TamagotchiTheme {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    appViewModel = appViewModel
                )
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
