package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.GameBalanceConfig
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeState
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetProfile
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetStats
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.BalanceConfigRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ApplyPetActionUseCaseTest {

    private val petRepository: PetRepository = mock()
    private val evaluateStateUseCase: EvaluatePetCriticalStateUseCase = mock()
    private val calculateLiveStatsUseCase: CalculateLiveStatsUseCase = mock()
    private val balanceConfigRepository: BalanceConfigRepository = mock()
    private val clock: Clock = mock()

    private val useCase = ApplyPetActionUseCase(
        petRepository = petRepository,
        evaluateStateUseCase = evaluateStateUseCase,
        calculateLiveStatsUseCase = calculateLiveStatsUseCase,
        balanceConfigRepository = balanceConfigRepository,
        clock = clock
    )

    private fun buildPet(
        status: PetLifeStatus = PetLifeStatus.NORMAL,
        stats: PetStats = PetStats(hunger = 50, energy = 50, cleanliness = 50, happiness = 50, updatedAt = 0L)
    ): Pet = Pet(
        id = "pet-1",
        profile = PetProfile(
            name = "Murzik",
            ownerUserId = "user-1",
            currentPairId = "pair-1",
            createdAt = 1000L,
            abandonedAt = null
        ),
        stats = stats,
        lifeState = PetLifeState(status = status, deathCause = null)
    )

    private fun setupDefaultConfig() {
        whenever(balanceConfigRepository.get()).thenReturn(GameBalanceConfig.DEFAULT)
    }

    @Test
    fun `should return PetNotFound when repository returns null`() = runTest {
        setupDefaultConfig()
        whenever(petRepository.getPetById("pet-1")).thenReturn(DomainResult.Success(null))

        val result = useCase("pet-1", PetAction.Feed)

        assertTrue(result is DomainResult.Failure)
        assertEquals(PetError.PetNotFound, (result as DomainResult.Failure).error)
    }

    @Test
    fun `should block action when pet is DEAD`() = runTest {
        setupDefaultConfig()
        val pet = buildPet(status = PetLifeStatus.DEAD)
        whenever(petRepository.getPetById("pet-1")).thenReturn(DomainResult.Success(pet))

        val result = useCase("pet-1", PetAction.Feed)

        assertTrue(result is DomainResult.Failure)
        assertEquals(PetError.ActionBlocked, (result as DomainResult.Failure).error)
    }

    @Test
    fun `should save new stats and state on success`() = runTest {
        setupDefaultConfig()
        val pet = buildPet()
        val newState = PetLifeState(PetLifeStatus.NORMAL, null)

        whenever(petRepository.getPetById("pet-1")).thenReturn(DomainResult.Success(pet))
        whenever(clock.currentTimeMillis()).thenReturn(1000L)
        whenever(calculateLiveStatsUseCase(any(), any())).thenReturn(pet)
        whenever(evaluateStateUseCase(any(), any())).thenReturn(newState)
        whenever(petRepository.updateStats(eq("pet-1"), any(), any(), any(), any()))
            .thenReturn(DomainResult.Success(Unit))
        whenever(petRepository.updateCriticalState(eq("pet-1"), any()))
            .thenReturn(DomainResult.Success(Unit))

        val result = useCase("pet-1", PetAction.Feed)

        assertTrue(result is DomainResult.Success)
        verify(petRepository).updateStats(eq("pet-1"), any(), any(), any(), any())
        verify(petRepository).updateCriticalState(eq("pet-1"), any())
    }
}
