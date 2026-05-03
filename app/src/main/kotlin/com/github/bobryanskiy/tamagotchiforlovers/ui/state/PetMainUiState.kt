package com.github.bobryanskiy.tamagotchiforlovers.ui.state

/**
 * UI состояние для экрана питомца (PetMainScreen)
 * Отображение питомца, его характеристик и кнопок действий
 */
data class PetMainUiState(
    val petId: String? = null,
    val petName: String? = null,
    val petType: String? = null,
    
    // Характеристики
    val hungerLevel: Int = 50,      // 0-100
    val happinessLevel: Int = 50,   // 0-100
    val energyLevel: Int = 50,      // 0-100
    val healthLevel: Int = 50,      // 0-100
    
    // Состояния
    val isSleeping: Boolean = false,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    
    // Для мини-игры кормления
    val showFeedingGame: Boolean = false,
    val feedingQuestion: String? = null,
    val feedingAnswer: Int? = null
) {
    companion object {
        fun loading() = PetMainUiState(isLoading = true)
        
        fun error(message: String) = PetMainUiState(
            isError = true,
            errorMessage = message,
            isLoading = false
        )
        
        fun fromDomain(
            domainPet: com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
        ) = PetMainUiState(
            petId = domainPet.id,
            petName = domainPet.name,
            petType = domainPet.type?.name,
            hungerLevel = domainPet.hungerLevel,
            happinessLevel = domainPet.happinessLevel,
            energyLevel = domainPet.energyLevel,
            healthLevel = domainPet.healthLevel,
            isSleeping = domainPet.isSleeping,
            isLoading = false
        )
    }
}
