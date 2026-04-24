package com.github.bobryanskiy.tamagotchiforlovers.data.repository

import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.data.exception.RepositoryException
import com.github.bobryanskiy.tamagotchiforlovers.data.model.dto.PetDto
import com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper.toDomain
import com.github.bobryanskiy.tamagotchiforlovers.di.IoDispatcher
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetCriticalState
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetCriticalStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetProfile
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetStats
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultPetRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PetRepository {
    private val petFlowCache = mutableMapOf<String, Flow<Pet?>>()
    private val cacheLock = Any()
    private val scope = CoroutineScope(ioDispatcher + SupervisorJob() + CoroutineName("PetRepo"))

    override fun observePet(petId: String): Flow<Pet?> = synchronized(cacheLock) {
        petFlowCache.getOrPut(petId) {
            callbackFlow {
                val registration = firestore.collection("pets").document(petId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(RepositoryException(mapToPetError(error)))
                            return@addSnapshotListener
                        }
                        val dto = snapshot?.toObject<PetDto>()
                        val result = trySend(dto?.toDomain(petId))
                        if (result.isFailure) Log.w("TAMAGOTCHI", "Failed to emit pet $petId")
                    }
                awaitClose {
                    registration.remove()
                    Log.d("TAMAGOTCHI", "Listener removed for pet: $petId")
                }
            }.stateIn(
                scope= scope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                initialValue = null
            )
        }
    }

    override suspend fun createPet(name: String, ownerUserId: String): DomainResult<String> = try {
        val petId = firestore.collection("pets").document().id
        val now = System.currentTimeMillis()

        val newPet = Pet(
            id = petId,
            profile = PetProfile(
                name = name,
                ownerUserId = ownerUserId,
                createdAt = System.currentTimeMillis(),
                criticalStatus = PetCriticalStatus.NORMAL,
                recoveryEndTime = null,
                abandonedAt = null,
                currentPairId = null
            ),
            stats = PetStats(
                hunger = 80,
                energy = 80,
                cleanliness = 80,
                happiness = 80,
                updatedAt = System.currentTimeMillis()
            )
        )

        val docData = mapOf(
            "profile" to mapOf("name" to name.trim(), "ownerUserId" to ownerUserId, "createdAt" to now),
            "stats" to mapOf("hunger" to 50, "energy" to 50, "cleanliness" to 50, "happiness" to 50, "updatedAt" to now)
        )

        firestore.collection("pets").document(petId).set(docData).await()
        userRepository.updateUserSession(ownerUserId, petId, null)
        DomainResult.Success(petId)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPetError(e))
    }

    override suspend fun updatePairId(petId: String, pairId: String) {
        firestore.collection("pets").document(petId)
            .update("profile.currentPairId", pairId)
            .await()
    }

    override suspend fun abandonPet(petId: String): DomainResult<Unit> = try {
        val now = System.currentTimeMillis()
        firestore.collection("pets").document(petId)
            .update("profile.abandonedAt", now)
            .await()
        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPetError(e))
    }

    override suspend fun applyAction(petId: String, action: PetAction): DomainResult<Unit> = try {
        val (field, delta) = when (action) {
            PetAction.Feed -> "stats.hunger" to 20
            PetAction.Play -> "stats.happiness" to 15
            PetAction.Clean -> "stats.cleanliness" to 25
            PetAction.Rest -> "stats.energy" to 30
        }

        firestore.collection("pets").document(petId)
            .update(
                field, FieldValue.increment(delta.toLong()),
                "stats.updatedAt", FieldValue.serverTimestamp()
            ).await()
        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPetError(e))
    }

    override suspend fun updateCriticalState(petId: String, state: PetCriticalState): DomainResult<Unit> = try {
        firestore.collection("pets").document(petId)
            .update(
                "profile.criticalStatus", state.status.name,
                "profile.recoveryEndTime", state.recoveryEndTime
            ).await()
        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPetError(e))
    }

    private fun mapToPetError(error: Throwable): PetError = when (error) {
        is FirebaseFirestoreException -> when (error.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            FirebaseFirestoreException.Code.NOT_FOUND -> PetError.NotFound
            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> PetError.Network(error)
            else -> PetError.Unknown
        }
        else -> PetError.Unknown
    }
}