package com.github.bobryanskiy.tamagotchiforlovers.data.storage

import android.content.Context
import androidx.core.content.edit
import com.github.bobryanskiy.tamagotchiforlovers.data.pet.model.PetState

class PetStorage(context: Context) {
    private val sharedPref = context.getSharedPreferences("pet_data", Context.MODE_PRIVATE)

    fun savePetState(petState: PetState) {
        sharedPref.edit {
            putInt("hunger", petState.hunger)
            putInt("happiness", petState.happiness)
            putInt("health", petState.health)
            putInt("tiredness", petState.tiredness)
            putInt("cleanliness", petState.cleanliness)
            putInt("age", petState.age)
            putBoolean("sleeping", petState.isSleeping)
            putLong("lastUpdateTime", petState.lastUpdateTime)
            putLong("startSleepTime", petState.startSleepTime)
        }
    }

    fun getPetState(): PetState {
        return PetState(
            sharedPref.getInt("hunger", PetState().hunger),
            sharedPref.getInt("happiness", PetState().happiness),
            sharedPref.getInt("health", PetState().health),
            sharedPref.getInt("tiredness", PetState().tiredness),
            sharedPref.getInt("cleanliness", PetState().cleanliness),
            sharedPref.getInt("age", PetState().age),
            sharedPref.getBoolean("sleeping", PetState().isSleeping),
            sharedPref.getLong("lastUpdateTime", PetState().lastUpdateTime),
            sharedPref.getLong("startSleepTime", PetState().startSleepTime)
        )
    }

    fun clearPetState() {
        sharedPref.edit {
            remove("hunger")
            remove("happiness")
            remove("health")
            remove("tiredness")
            remove("tiredness")
            remove("age")
            remove("sleeping")
            remove("lastUpdateTime")
            remove("startSleepTime")
        }
    }

}