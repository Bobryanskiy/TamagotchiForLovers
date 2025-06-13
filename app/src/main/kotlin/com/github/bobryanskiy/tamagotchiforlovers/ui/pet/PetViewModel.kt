package com.github.bobryanskiy.tamagotchiforlovers.ui.pet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.data.login.LoginRepository
import com.github.bobryanskiy.tamagotchiforlovers.data.pet.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.util.Result
import kotlinx.coroutines.launch

class PetViewModel(private val repository: PetRepository, private val logout: LoginRepository) : ViewModel() {
    private val _deletePetResult = MutableLiveData<ButtonsResult>()
    val deletePetResult: LiveData<ButtonsResult> = _deletePetResult

    private val _switchVisibilityResult = MutableLiveData<ButtonsResult>()
    val switchVisibilityResult: LiveData<ButtonsResult> = _switchVisibilityResult

    fun logout() {
        logout.logout()
    }

    fun deletePet() {
        viewModelScope.launch {
            val result = repository.deletePet()
            if (result is Result.Success) {
                _deletePetResult.value = ButtonsResult(success = R.string.welcome)
            } else {
                _deletePetResult.value = ButtonsResult(error = R.string.invalid_password)
            }
        }
    }

    fun switchVisibility() {
        viewModelScope.launch {
            val result = repository.switchVisibility()
            if (result is Result.Success) {
                _switchVisibilityResult.value = ButtonsResult(success = R.string.welcome)
            } else {
                _switchVisibilityResult.value = ButtonsResult(error = R.string.invalid_password)
            }
        }
    }
}