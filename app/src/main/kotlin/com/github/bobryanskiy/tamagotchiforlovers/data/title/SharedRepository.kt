package com.github.bobryanskiy.tamagotchiforlovers.data.title

import com.github.bobryanskiy.tamagotchiforlovers.data.pet.model.PetState
import com.github.bobryanskiy.tamagotchiforlovers.data.storage.PetStorage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SharedRepository(private val petStorage: PetStorage) {
    private val firestore: FirebaseFirestore = Firebase.firestore

    fun saveToSharedPreferences(petState: PetState) {
        petStorage.savePetState(petState)
    }


    fun feedPet(petState: PetState) {
        petState.health += 5
        petState.tiredness += 10
        petState.hunger -= 30
        petState.happiness += 10
        petState.lastUpdateTime = System.currentTimeMillis()
        petState.health = petState.health.coerceIn(0..100)
        petState.tiredness = petState.tiredness.coerceIn(0..100)
        petState.hunger = petState.hunger.coerceIn(0..100)
        petState.happiness = petState.happiness.coerceIn(0..100)
        saveToSharedPreferences(petState)
    }

    fun cleanPet(petState: PetState) {
        petState.health + 15
        petState.tiredness + 0
        petState.hunger - 0
        petState.happiness + 10
        saveToSharedPreferences(petState)
    }

    fun sleepPet(petState: PetState) {
        if (!petState.isSleeping) {
            petState.startSleepTime = System.currentTimeMillis()
        }
        petState.isSleeping = !petState.isSleeping
        saveToSharedPreferences(petState)
    }

    fun playPet(petState: PetState) {
        petState.health += 5
        petState.tiredness += 15
        petState.hunger += 15
        petState.happiness += 30
        saveToSharedPreferences(petState)
    }

    fun feedPetSimple(petState: PetState?, pairId: String): PetState? {
        return petState?.apply {
            hunger -= 30
            hunger = hunger.coerceIn(0..100)
            firestore.collection("pairs").document(pairId).update("petState.hunger", hunger)
//            saveToSharedPreferences(this)
        }
    }
}