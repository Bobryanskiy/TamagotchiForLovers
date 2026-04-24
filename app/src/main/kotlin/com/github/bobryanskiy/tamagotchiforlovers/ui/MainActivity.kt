package com.github.bobryanskiy.tamagotchiforlovers.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.ui.navigation.AppNavGraph
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavGraph(navController = rememberNavController())
        }
        FirebaseApp.initializeApp(this)

        FirebaseAuth.getInstance().signInAnonymously()
            .addOnSuccessListener {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            userRepository.createUser(userId)
                            Log.d("AUTH", "User document created in Firestore")
                        } catch (e: Exception) {
                            Log.e("AUTH", "Failed to create user: ${e.message}")
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("AUTH", "Auth failed: ${e.message}")
            }
    }
}