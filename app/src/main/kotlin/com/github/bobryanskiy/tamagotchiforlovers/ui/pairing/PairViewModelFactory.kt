package com.github.bobryanskiy.tamagotchiforlovers.ui.pairing

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.PairViaFirebase
import com.github.bobryanskiy.tamagotchiforlovers.data.storage.PairStorage

class PairViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PairViewModel::class.java)) {
            return PairViewModel(repository = PairRepository(pairStorage = PairStorage(context = context.applicationContext), dataSource = PairViaFirebase())) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}