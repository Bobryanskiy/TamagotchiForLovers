package com.github.bobryanskiy.tamagotchiforlovers.ui.mapper

import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.DomainError
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError

object UiErrorMapper {
    fun DomainError.toUiErrorStringRes(): Int {
        return when (this) {
            // Pet Errors
            is PetError.InvalidInput -> R.string.error_pet_invalid_input
            is PetError.ActionNotAllowed -> R.string.error_pet_action_not_allowed
            is PetError.NotFound -> R.string.error_pet_not_found
            is PetError.Network -> R.string.error_pet_network
            is PetError.Unknown -> R.string.error_pet_unknown

            // Pair Errors
            is PairError.GuestOnly -> R.string.error_pair_guest_only
            is PairError.CreatorOnly -> R.string.error_pair_creator_only
            is PairError.SessionNotActive -> R.string.error_pair_session_not_active
            is PairError.AlreadyEnded -> R.string.error_pair_already_ended
            is PairError.AlreadyJoined -> R.string.error_pair_already_joined
            is PairError.InvalidInput -> R.string.error_pair_invalid_input
            is PairError.InvalidRequest -> R.string.error_pair_invalid_request
            is PairError.NotFound -> R.string.error_pair_not_found
            is PairError.Network -> R.string.error_pair_network
            is PairError.Unknown -> R.string.error_pair_unknown
        }
    }
}