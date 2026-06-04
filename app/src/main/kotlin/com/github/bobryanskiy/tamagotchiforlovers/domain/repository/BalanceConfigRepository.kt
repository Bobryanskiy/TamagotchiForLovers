package com.github.bobryanskiy.tamagotchiforlovers.domain.repository

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.GameBalanceConfig
import kotlinx.coroutines.flow.Flow

interface BalanceConfigRepository {
    fun observe(): Flow<GameBalanceConfig>
    fun get(): GameBalanceConfig
}
