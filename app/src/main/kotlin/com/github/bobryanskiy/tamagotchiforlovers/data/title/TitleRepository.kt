package com.github.bobryanskiy.tamagotchiforlovers.data.title

import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.data.PetConstants.PetConstants.HAPPINESS_RATE
import com.github.bobryanskiy.tamagotchiforlovers.data.PetConstants.PetConstants.HEALTH_RATE
import com.github.bobryanskiy.tamagotchiforlovers.data.PetConstants.PetConstants.HUNGER_RATE
import com.github.bobryanskiy.tamagotchiforlovers.data.PetConstants.PetConstants.TIREDNESS_RATE
import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model.PairData
import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model.UserData
import com.github.bobryanskiy.tamagotchiforlovers.data.pet.model.PetState
import com.github.bobryanskiy.tamagotchiforlovers.data.storage.PairStorage
import com.github.bobryanskiy.tamagotchiforlovers.data.storage.PetStorage
import com.github.bobryanskiy.tamagotchiforlovers.data.title.model.UserPetInfo
import com.github.bobryanskiy.tamagotchiforlovers.util.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlin.time.Duration.Companion.milliseconds

class TitleRepository(private val pairStorage: PairStorage, private val petStorage: PetStorage) {
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = Firebase.firestore

    suspend fun playButton(isOnline: Boolean): Result<UserPetInfo> {
        return try {
            val localPetState = petStorage.getPetState()
            val localPairId = pairStorage.getPairId()
            if (isOnline) {
                val currentUser = auth.currentUser?.uid
                    ?: return Result.Success(UserPetInfo(dest = R.id.action_titleFragment_to_loginFragment))
                val currentUserDocRef = firestore.collection("users").document(currentUser)
                val pairsDocRef = firestore.collection("pairs")
                var pairId: String? = null
                var petState: PetState? = null
                var result: Result<UserPetInfo>? = firestore.runTransaction { transaction ->
                    val userData = transaction[currentUserDocRef].toObject<UserData>()
                    pairId = userData?.pairId ?: throw Exception("Invalid user data")
                    if (pairId == "") {
                        return@runTransaction Result.Success(UserPetInfo(dest = R.id.action_titleFragment_to_pairFragment))
                    }

                    val pairDocRef = pairsDocRef.document(pairId)
                    val pairDoc = transaction[pairDocRef]
                    val pairData = pairDoc.toObject<PairData>()

                    if (pairData == null) {
                        return@runTransaction Result.Success(UserPetInfo(dest = R.id.action_titleFragment_to_pairFragment))
                    }
                    petState = pairData.petState
                    transaction.update(pairDocRef, "petState", updatePetState(if (petState.lastUpdateTime > localPetState.lastUpdateTime) petState else localPetState))
                    null
                }.await()
                if (pairId != null && pairId != "" && pairId == localPairId) {
                    result = if (petState == null) Result.Success(UserPetInfo(dest = R.id.action_titleFragment_to_pairFragment))
                    else Result.Success(
                        UserPetInfo(
                            pairId = pairId,
                            petState = petStorage.getPetState(),
                            action = true,
                            dest = R.id.action_titleFragment_to_petFragment
                        )
                    )
                }
                result ?: Result.Success(UserPetInfo(dest = R.id.action_titleFragment_to_pairFragment))
            } else {
                Result.Success(
                    UserPetInfo(
                        pairId = localPairId,
                        petState = localPetState,
                        action = true,
                        dest = R.id.action_titleFragment_to_petFragment
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("TitleRepository", e.toString())
            Result.Error(e)
        }
    }

    private fun updatePetState(petState: PetState): PetState {
        val difTime = (System.currentTimeMillis() - petState.lastUpdateTime).milliseconds.inWholeMinutes
        val newPetState = petState.copy(
            hunger = (petState.hunger + difTime * HUNGER_RATE).coerceIn(0, 100).toInt(),
            health = (petState.health + difTime * HEALTH_RATE).coerceIn(0, 100).toInt(),
            tiredness = (petState.tiredness + difTime * TIREDNESS_RATE).coerceIn(0, 100).toInt(),
            happiness = (petState.happiness + difTime * HAPPINESS_RATE).coerceIn(0, 100).toInt(),
            lastUpdateTime = System.currentTimeMillis()
        )
        return newPetState
    }
}