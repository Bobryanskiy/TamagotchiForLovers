package com.github.bobryanskiy.tamagotchiforlovers.ui.state

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetCriticalStatus

/**
 * UI-состояние для экрана игры (GameScreen)
 * Содержит только данные, необходимые для отображения UI
 */
data class GameUiState(
    val petName: String = "",
    val hunger: Int = 0,
    val energy: Int = 0,
    val cleanliness: Int = 0,
    val happiness: Int = 0,
    val criticalStatus: PetCriticalStatus = PetCriticalStatus.NORMAL,
    val isLoading: Boolean = true,
    val isActionsBlocked: Boolean = false
) {
    companion object {
        fun loading() = GameUiState(isLoading = true)
        
        fun fromDomain(pet: com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet): GameUiState {
            val isBlocked = pet.profile.criticalStatus in listOf(
                PetCriticalStatus.COLLAPSED,
                PetCriticalStatus.DEAD,
                PetCriticalStatus.ESCAPED
            )
            return GameUiState(
                petName = pet.profile.name,
                hunger = pet.stats.hunger,
                energy = pet.stats.energy,
                cleanliness = pet.stats.cleanliness,
                happiness = pet.stats.happiness,
                criticalStatus = pet.profile.criticalStatus,
                isLoading = false,
                isActionsBlocked = isBlocked
            )
        }
    }
}

/**
 * UI-состояние для экрана лобби (LobbyScreen)
 */
data class LobbyUiState(
    val pairName: String = "",
    val inviteCode: String? = null,
    val status: com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus = com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus.PENDING,
    val hasPendingRequest: Boolean = false,
    val pendingGuestId: String? = null,
    val isHost: Boolean = false,
    val isLoading: Boolean = true
) {
    companion object {
        fun loading() = LobbyUiState(isLoading = true)
        
        fun fromDomain(
            pair: com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pair,
            currentUserId: String?
        ): LobbyUiState {
            return LobbyUiState(
                pairName = pair.name ?: "Без названия",
                inviteCode = pair.inviteKey?.code,
                status = pair.status,
                hasPendingRequest = pair.pendingRequest != null,
                pendingGuestId = pair.pendingRequest?.guestId,
                isHost = pair.userId1 == currentUserId,
                isLoading = false
            )
        }
    }
}

/**
 * UI-состояние для экрана создания питомца (CreatePetScreen)
 */
data class CreatePetUiState(
    val petName: String = "",
    val pairName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * UI-состояние для экрана присоединения к сессии (JoinSessionScreen)
 */
data class JoinSessionUiState(
    val inviteCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * UI-состояние для экрана старта (StartScreen)
 */
data class StartUiState(
    val userId: String? = null,
    val hasActivePet: Boolean = false,
    val hasActivePair: Boolean = false,
    val isLoading: Boolean = true
)
