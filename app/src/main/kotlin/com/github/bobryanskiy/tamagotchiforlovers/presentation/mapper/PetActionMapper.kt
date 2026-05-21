package com.github.bobryanskiy.tamagotchiforlovers.presentation.mapper

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction

enum class PetActionType { Feed, Rest, Clean, Play }

fun PetActionType.toDomainAction(): PetAction = when (this) {
    PetActionType.Feed -> PetAction.Feed
    PetActionType.Rest -> PetAction.Rest
    PetActionType.Clean -> PetAction.Clean
    PetActionType.Play -> PetAction.Play
}