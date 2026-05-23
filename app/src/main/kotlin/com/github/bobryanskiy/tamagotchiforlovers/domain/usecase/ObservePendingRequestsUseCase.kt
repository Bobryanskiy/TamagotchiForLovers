package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PendingRequest
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

class ObservePendingRequestsUseCase @Inject constructor(
    private val pairRepository: PairRepository
) {
    operator fun invoke(pairId: String): Flow<List<PendingRequest>> {
        return pairRepository.observePendingRequests(pairId)
            .catch { e ->
                emit(emptyList())
            }
    }
}