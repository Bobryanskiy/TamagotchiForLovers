package com.github.bobryanskiy.tamagotchiforlovers.presentation.mapper

import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.UserError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.User

fun PetError.toUiErrorStringRes(): Int = when (this) {
    PetError.PetNotFound -> R.string.error_pet_not_found
    PetError.ActionBlocked -> R.string.error_action_blocked
    PetError.NotAuthenticated -> R.string.error_not_authenticated
    PetError.SessionNotFound -> R.string.error_session_not_found
    PetError.Database -> R.string.error_database
    PetError.Network -> R.string.error_network
    PetError.Unknown -> R.string.error_unknown
    PetError.InvalidInput -> R.string.error_pet_invalid_input
    PetError.PairNotActive -> R.string.error_pair_not_active
    PetError.PairNotFound -> R.string.error_pair_not_found
}

fun PairError.toUiErrorStringRes(): Int = when (this) {
    PairError.PairNotFound -> R.string.error_pair_not_found
    PairError.SessionNotActive -> R.string.error_pair_not_active
    PairError.InvalidInput -> R.string.error_pair_invalid_input
    PairError.GuestOnly -> R.string.error_pair_guest_only
    PairError.CreatorOnly -> R.string.error_pair_creator_only
    PairError.AlreadyEnded -> R.string.error_pair_already_ended

    PairError.AlreadyJoined -> R.string.error_pair_already_joined
    PairError.InvalidRequest -> R.string.error_pair_invalid_request
    PairError.Network -> R.string.error_pair_network
    PairError.Unknown -> R.string.error_unknown
}

fun UserError.toUiErrorStringRes(): Int = when (this) {
    UserError.EmailAlreadyExists -> R.string.error_email_exists
    UserError.WeakPassword -> R.string.error_weak_password
    UserError.NotAuthenticated -> R.string.error_not_authenticated
    UserError.LoginError -> R.string.login_error
}