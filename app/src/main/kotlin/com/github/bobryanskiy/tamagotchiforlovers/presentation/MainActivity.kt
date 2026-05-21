package com.github.bobryanskiy.tamagotchiforlovers.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.github.bobryanskiy.tamagotchiforlovers.presentation.navigation.AppNavGraph
import com.github.bobryanskiy.tamagotchiforlovers.presentation.theme.TamagotchiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            TamagotchiTheme {
                val navController = rememberNavController()

                AppNavGraph(
                    navController = navController
                )
            }
        }
    }
}