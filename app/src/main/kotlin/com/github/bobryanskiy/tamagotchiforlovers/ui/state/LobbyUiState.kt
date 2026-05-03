package com.github.bobryanskiy.tamagotchiforlovers.ui.state

/**
 * UI состояние для экрана лобби (LobbyScreen)
 */
data class LobbyUiState(
    val pairId: String? = null,
    val pairName: String? = null,
    val status: String? = null,
    val inviteKey: String? = null,
    val isHost: Boolean = false,
    val pendingGuestId: String? = null,
    val guestName: String? = null,
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {
        fun loading() = LobbyUiState(isLoading = true)
        
        fun error(message: String) = LobbyUiState(
            isError = true,
            errorMessage = message,
            isLoading = false
        )
        
        fun fromDomain(
            domainPair: com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pair,
            currentUserId: String?
        ) = LobbyUiState(
            pairId = domainPair.id,
            pairName = domainPair.name,
            status = domainPair.status.name,
            inviteKey = domainPair.inviteKey,
            isHost = domainPair.userId1 == currentUserId,
            pendingGuestId = domainPair.userId2?.takeIf { domainPair.status.name == "PENDING" },
            isLoading = false
        )
    }
}
