package com.github.bobryanskiy.tamagotchiforlovers.domain.model

sealed class PetAction {
    data object Feed : PetAction()
    data object Rest : PetAction()
    data object Clean : PetAction()
    data object Play : PetAction()
}
