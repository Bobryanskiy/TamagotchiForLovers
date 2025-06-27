package com.github.bobryanskiy.tamagotchiforlovers.ui.title

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.data.title.TitleRepository
import com.github.bobryanskiy.tamagotchiforlovers.util.Result
import kotlinx.coroutines.launch

class TitleViewModel(private val repository: TitleRepository) : ViewModel() {
    private val _playResult = MutableLiveData<UserPetInfoResult>()
    val playResult: LiveData<UserPetInfoResult> = _playResult

    fun playButton(isOnline: Boolean) {
        viewModelScope.launch {
            val result = repository.playButton(isOnline)
            if (result is Result.Success) {
                _playResult.value = UserPetInfoResult(success = result.data)
            } else _playResult.value = UserPetInfoResult(error = R.string.title_play_error)
        }
    }
}