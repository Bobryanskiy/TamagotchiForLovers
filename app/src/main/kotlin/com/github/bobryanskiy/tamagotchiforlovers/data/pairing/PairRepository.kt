package com.github.bobryanskiy.tamagotchiforlovers.data.pairing

import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model.PairModel
import com.github.bobryanskiy.tamagotchiforlovers.data.storage.PairStorage
import com.github.bobryanskiy.tamagotchiforlovers.util.Result

class PairRepository(private val pairStorage: PairStorage, private val dataSource: PairViaFirebase) {

    fun createNewPair(callback: (Result<PairModel>) -> Unit) {
        dataSource.createNewPair {
            if (it is Result.Success) {
                pairStorage.savePairId(it.data.code)
            }
            callback(it)
        }
    }

    fun joinPair(pairId: String, callback: (Result<PairModel>) -> Unit) {
        dataSource.joinPair(pairId) {
            if (it is Result.Success) {
                pairStorage.savePairId(it.data.code)
            }
            callback(it)
        }
    }
}