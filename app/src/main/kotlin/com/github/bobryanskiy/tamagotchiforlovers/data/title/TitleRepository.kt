package com.github.bobryanskiy.tamagotchiforlovers.data.title

import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model.PairData
import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model.UserData
import com.github.bobryanskiy.tamagotchiforlovers.data.pet.model.PetState
import com.github.bobryanskiy.tamagotchiforlovers.data.storage.PairStorage
import com.github.bobryanskiy.tamagotchiforlovers.data.storage.PetStorage
import com.github.bobryanskiy.tamagotchiforlovers.data.title.model.UserPetInfo
import com.github.bobryanskiy.tamagotchiforlovers.ui.title.TitleFragmentDirections
import com.github.bobryanskiy.tamagotchiforlovers.util.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class TitleRepository(private val pairStorage: PairStorage, private val petStorage: PetStorage) {
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = Firebase.firestore

    suspend fun playButton(): Result<UserPetInfo> {
        return try {
            val currentUser = auth.currentUser?.uid ?: return Result.Success(UserPetInfo(dest = R.id.action_titleFragment_to_loginFragment))
            val currentUserDoc = firestore.collection("users").document(currentUser)
            val pairsDocRef = firestore.collection("pairs")
            var pairId: String? = null
            var petState: PetState? = null
            val result: Result<UserPetInfo>? = firestore.runTransaction { transaction ->
                val userData = transaction[currentUserDoc].toObject<UserData>()
                pairId = userData?.pairId ?: throw Exception("Invalid user data")
                if (pairId == "") {
                    return@runTransaction Result.Success(UserPetInfo(dest = R.id.action_titleFragment_to_pairFragment))
                }

                val pairDocRef = pairsDocRef.document(pairId)
                val pairDoc = transaction[pairDocRef]
                val pairData = pairDoc.toObject<PairData>()

                if(pairData == null) {
                    return@runTransaction Result.Success(UserPetInfo(dest = R.id.action_titleFragment_to_pairFragment))
                }
                petState = pairData.petState
                null
            }.await()
            if (pairId != null) pairStorage.savePairId(pairId)
            if (petState == null) petStorage.getPetState()
            result ?: Result.Success(UserPetInfo(pairId = pairId, petState = petState!!, action = true, dest = R.id.action_titleFragment_to_petFragment))
        } catch (e: Exception) {
            Log.e("TitleRepository", e.toString())
            Result.Error(e)
        }
    }
}