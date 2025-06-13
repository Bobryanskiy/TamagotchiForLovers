package com.github.bobryanskiy.tamagotchiforlovers.ui.pairing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.util.Result
import kotlinx.coroutines.launch

class PairViewModel(private val repository: PairRepository) : ViewModel() {
    private val _joinForm = MutableLiveData<String>()
    val joinForm: LiveData<String> = _joinForm

    private val _pairResult = MutableLiveData<PairResult>()
    val pairResult: LiveData<PairResult> = _pairResult

    fun createNewPair() {
        viewModelScope.launch {
            val result = repository.createNewPair()
            if (result is Result.Success) {
                _pairResult.value = PairResult(result.data)
            } else {
                _pairResult.value = PairResult(error = R.string.login_failed)
            }
        }
    }

    fun joinExistingPair(pairId: String) {
        viewModelScope.launch {
            val result = repository.joinPair(pairId)
            if (result is Result.Success) {
                _pairResult.value = PairResult(result.data)
            } else {
                _pairResult.value = PairResult(error = R.string.login_failed)
            }
        }
    }

    fun pairDataChanged(code: String) {
        if (isCodeValid(code)) {
            _joinForm.value = code
        } else _joinForm.value = ""
    }

    private fun isCodeValid(password: String): Boolean {
        return password.length == 8
    }
}