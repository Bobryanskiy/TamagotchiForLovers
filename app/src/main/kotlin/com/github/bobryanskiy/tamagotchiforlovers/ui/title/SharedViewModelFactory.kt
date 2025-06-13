package com.github.bobryanskiy.tamagotchiforlovers.ui.title

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.bobryanskiy.tamagotchiforlovers.data.title.SharedRepository
import com.github.bobryanskiy.tamagotchiforlovers.data.storage.PetStorage

class SharedViewModelFactory(private val context: Context): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharedViewModel::class.java)) {
            return SharedViewModel(
                repository = SharedRepository(
                    petStorage = PetStorage(context.applicationContext)
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}