package com.github.bobryanskiy.tamagotchiforlovers.data.repository

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.GameBalanceConfig
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.BalanceConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BalanceConfigRepositoryImpl @Inject constructor() : BalanceConfigRepository {
    private val _config = MutableStateFlow(GameBalanceConfig.DEFAULT)

    override fun observe(): Flow<GameBalanceConfig> = _config.asStateFlow()
    override fun get(): GameBalanceConfig = _config.value
}
