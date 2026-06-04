package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import app.cash.turbine.test
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PendingRequest
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ObservePendingRequestsUseCaseTest {

    private val pairRepository: PairRepository = mock()
    private val useCase = ObservePendingRequestsUseCase(pairRepository)

    @Test
    fun `should emit requests from repository`() = runTest {
        val requests = listOf(mock<PendingRequest>(), mock())
        whenever(pairRepository.observePendingRequests("pair-1")).thenReturn(flowOf(requests))

        useCase("pair-1").test {
            val emitted = awaitItem()
            assertEquals(2, emitted.size)
            awaitComplete()
        }
    }

    @Test
    fun `should emit empty list on error`() = runTest {
        val failingFlow = flow<List<PendingRequest>> {
            throw RuntimeException("network error")
        }
        whenever(pairRepository.observePendingRequests("pair-1")).thenReturn(failingFlow)

        useCase("pair-1").test {
            val emitted = awaitItem()
            assertTrue(emitted.isEmpty())
            awaitComplete()
        }
    }
}
