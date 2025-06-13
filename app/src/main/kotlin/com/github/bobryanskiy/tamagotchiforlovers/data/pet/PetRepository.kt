package com.github.bobryanskiy.tamagotchiforlovers.data.pet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.bobryanskiy.tamagotchiforlovers.data.pet.model.PetState
import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model.PairData
import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model.UserData
import com.github.bobryanskiy.tamagotchiforlovers.data.storage.PairStorage
import com.github.bobryanskiy.tamagotchiforlovers.data.storage.PetStorage
import com.github.bobryanskiy.tamagotchiforlovers.util.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

private const val TAG = "Tamagotchi.Pet"

class PetRepository (private val petStorage: PetStorage, private val pairStorage: PairStorage) {
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = Firebase.firestore

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
        petStorage.savePetState(petState)
    }

    fun cleanPet(petState: PetState) {
        petState.health + 15
        petState.tiredness + 0
        petState.hunger - 0
        petState.happiness + 10
        petStorage.savePetState(petState)
    }

    fun sleepPet(petState: PetState) {
        if (!petState.isSleeping) {
            petState.startSleepTime = System.currentTimeMillis()
        }
        petState.isSleeping = !petState.isSleeping
        petStorage.savePetState(petState)
    }

    fun playPet(petState: PetState) {
        petState.health += 5
        petState.tiredness += 15
        petState.hunger += 15
        petState.happiness += 30
        petStorage.savePetState(petState)
    }

    suspend fun deletePet(): Result<Unit> {
        return try {
            val currentUser = auth.currentUser!!.uid
            val usersDocRef = firestore.collection("users")
            val currentUserDoc = usersDocRef.document(currentUser)

            val pairsDocRef = firestore.collection("pairs")
            firestore.runTransaction { transaction ->
                val userData = transaction[currentUserDoc].toObject<UserData>()
                val pairId = userData?.pairId ?: throw Exception("Invalid user data")

                val pairDocRef = pairsDocRef.document(pairId)
                val pairDoc = transaction[pairDocRef]
                val pairData = pairDoc.toObject<PairData>()

                if (pairData?.userId2 != "") {
                    val secondUserDoc = usersDocRef.document(pairData!!.userId2)
                    transaction.update(secondUserDoc, "pairId", "")
                }
                transaction.update(currentUserDoc, "pairId", "")
                transaction.delete(pairDocRef)
            }.await()
            pairStorage.clearPairId()
            Log.d(TAG, "Successfully deleted pet")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.d(TAG, "Error deleting pet: $e")
            Result.Error(e)
        }
    }

    suspend fun switchVisibility(): Result<Unit> {
        return try {
            val pairId = pairStorage.getPairId()!!
            val pair = firestore.collection("pairs").document(pairId).get().await().toObject<PairData>()
            if (pair == null) {
                throw Exception("Pair was not found")
            }
            firestore.collection("pairs").document(pairId).update("isOpen", !pair.isOpen).await()
            Log.d(TAG, "Successfully deleted pet")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.d(TAG, "Error deleting pet: $e")
            Result.Error(e)
        }
    }
}