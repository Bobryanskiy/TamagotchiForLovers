package com.github.bobryanskiy.tamagotchiforlovers

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    private val _petState = MutableLiveData<PetState>()
    val petState: LiveData<PetState> = _petState

    fun observePetState(pairId: String) {
        firestore.collection("pairs").document(pairId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    val pairData = snapshot.toObject<PairData>()
                    _petState.postValue(pairData?.petState ?: PetState())
                }
            }
    }

    fun updatePetState(pairId: String, newPetState: PetState) {
        firestore.collection("pairs").document(pairId)
            .update("petState", newPetState)
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