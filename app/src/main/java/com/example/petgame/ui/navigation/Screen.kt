package com.example.petgame.ui.navigation

/**
 *sealed class для навигации по экранам приложения.
 * Каждый экран представляет собой отдельный маршрут.
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object PetCreation : Screen("pet_creation")
    object PetMain : Screen("pet_main/{petId}") {
        fun createRoute(petId: Int) = "pet_main/$petId"
    }
    object Login : Screen("login")
    object MiniGames : Screen("mini_games/{statType}") {
        fun createRoute(statType: String) = "mini_games/$statType"
    }
    object Connection : Screen("connection")
}
