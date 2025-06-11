package com.github.bobryanskiy.tamagotchiforlovers.data.pairing

import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model.PairModel
import com.github.bobryanskiy.tamagotchiforlovers.data.storage.PairStorage
import com.github.bobryanskiy.tamagotchiforlovers.util.Result

class PairRepository(private val pairStorage: PairStorage, private val dataSource: PairViaFirebase) {

    suspend fun createNewPair(): Result<PairModel> {
        val result = dataSource.createNewPair()
        if (result is Result.Success) {
            pairStorage.savePairId(result.data.code)
        }
        return result
    }

    suspend fun joinPair(pairId: String): Result<PairModel> {
        val result = dataSource.joinPair(pairId)
        if (result is Result.Success) {
            pairStorage.savePairId(result.data.code)
        }
        return result
    }
}