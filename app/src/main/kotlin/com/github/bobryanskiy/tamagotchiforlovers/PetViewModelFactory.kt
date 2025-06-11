package com.github.bobryanskiy.tamagotchiforlovers

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.bobryanskiy.tamagotchiforlovers.data.login.LoginDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.login.LoginRepository
import com.github.bobryanskiy.tamagotchiforlovers.data.storage.PairStorage
import com.github.bobryanskiy.tamagotchiforlovers.data.storage.PetStorage

class PetViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PetViewModel::class.java)) {
            return PetViewModel(repository = PetRepository(petStorage = PetStorage(context.applicationContext), pairStorage = PairStorage(context.applicationContext)), logout = LoginRepository(dataSource = LoginDataSource())) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}