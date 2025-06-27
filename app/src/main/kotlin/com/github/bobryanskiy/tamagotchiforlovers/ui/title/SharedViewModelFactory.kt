package com.github.bobryanskiy.tamagotchiforlovers.ui.title

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.bobryanskiy.tamagotchiforlovers.data.storage.PetStorage
import com.github.bobryanskiy.tamagotchiforlovers.data.title.SharedRepository

class SharedViewModelFactory(private val application: Application): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharedViewModel::class.java)) {
            return SharedViewModel(
                application = application,
                repository = SharedRepository(
                    petStorage = PetStorage(application.applicationContext)
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}