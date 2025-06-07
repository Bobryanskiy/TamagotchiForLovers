package com.github.bobryanskiy.tamagotchiforlovers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.bobryanskiy.tamagotchiforlovers.data.PairRepository

class PairViewModelFactory() : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PairViewModel::class.java)) {
            return PairViewModel(repository = PairRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}