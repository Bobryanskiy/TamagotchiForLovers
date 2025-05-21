package com.github.bobryanskiy.tamagotchiforlovers.data.model

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class FirebaseViewModel : ViewModel() {
    val auth = Firebase.auth
    val firestore = Firebase.firestore
}