package com.github.bobryanskiy.tamagotchiforlovers.domain.model

sealed class PetAction {
    object Feed: PetAction()
    object Rest: PetAction()
    object Clean: PetAction()
    object Play: PetAction()
}