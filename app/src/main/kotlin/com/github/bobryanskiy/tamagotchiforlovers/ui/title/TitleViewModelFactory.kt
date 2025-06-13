package com.github.bobryanskiy.tamagotchiforlovers.ui.title

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.bobryanskiy.tamagotchiforlovers.data.storage.PairStorage
import com.github.bobryanskiy.tamagotchiforlovers.data.storage.PetStorage
import com.github.bobryanskiy.tamagotchiforlovers.data.title.TitleRepository

class TitleViewModelFactory(private val context: Context): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TitleViewModel::class.java)) {
            return TitleViewModel(
                repository = TitleRepository(
                    pairStorage = PairStorage(context.applicationContext),
                    petStorage = PetStorage(context.applicationContext)
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}