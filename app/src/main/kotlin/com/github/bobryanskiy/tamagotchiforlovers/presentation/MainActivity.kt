package com.github.bobryanskiy.tamagotchiforlovers.presentation

import android.R.attr.data
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.rememberNavController
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.AppViewModel
import com.github.bobryanskiy.tamagotchiforlovers.presentation.navigation.AppNavGraph
import com.github.bobryanskiy.tamagotchiforlovers.presentation.theme.TamagotchiTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val appViewModel: AppViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "✅ POST_NOTIFICATIONS granted")
        } else {
            Log.w("MainActivity", "❌ POST_NOTIFICATIONS denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("MainActivity", "⚠️ SCHEDULE_EXACT_ALARM not granted")
                // Открываем настройки для запроса
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:$packageName".toUri()
                }
                startActivity(intent)
            } else {
                Log.d("MainActivity", "✅ SCHEDULE_EXACT_ALARM granted")
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
}