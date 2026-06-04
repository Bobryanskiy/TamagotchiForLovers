package com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper

import com.github.bobryanskiy.tamagotchiforlovers.data.local.entity.PetEntity
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.LifeStateDto
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.PetDto
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.ProfileDto
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.StatsDto
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.DeathCause
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeState
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetProfile
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetStats

/**
 * Firestore → Room
 */
fun PetDto.toEntity(petId: String): PetEntity {
    val p = profile ?: ProfileDto()
    val s = stats ?: StatsDto()
    val l = lifeState ?: LifeStateDto()
    return PetEntity(
        id = petId,
        name = p.name,
        ownerUserId = p.ownerUserId,
        currentPairId = p.currentPairId,
        createdAt = p.createdAt,
        lifeStatus = l.lifeStatus,
        deathCause = l.deathCause,
        abandonedAt = p.abandonedAt,
        hunger = s.hunger,
        energy = s.energy,
        cleanliness = s.cleanliness,
        happiness = s.happiness,
        updatedAt = s.updatedAt,
        syncStatus = "SYNCED"
    )
}

/**
 * Room → Domain
 */
fun PetEntity.toDomain(): Pet {
    return Pet(
        id = id,
        profile = PetProfile(
            name = name,
            ownerUserId = ownerUserId,
            currentPairId = currentPairId,
            createdAt = createdAt,
            abandonedAt = abandonedAt
        ),
        stats = PetStats(
            hunger = hunger,
            energy = energy,
            cleanliness = cleanliness,
            happiness = happiness,
            updatedAt = updatedAt
        ),
        lifeState = PetLifeState(
            status = PetLifeStatus.valueOf(lifeStatus),
            deathCause = deathCause?.let { DeathCause.valueOf(it) }
        )
    )
}

/**
 * Room → Firestore
 */
fun PetEntity.toDto(): PetDto = PetDto(
    profile = ProfileDto(
        name = name, ownerUserId = ownerUserId, currentPairId = currentPairId,
        createdAt = createdAt, abandonedAt = abandonedAt
    ),
    stats = StatsDto(
        hunger = hunger, energy = energy, cleanliness = cleanliness,
        happiness = happiness, updatedAt = updatedAt
    ),
    lifeState = LifeStateDto(
        lifeStatus = lifeStatus,
        deathCause = deathCause
    )
)

/**
 * Domain → Room
 */
fun Pet.toEntity(): PetEntity = PetEntity(
    id = this.id,
    name = this.profile.name,
    ownerUserId = this.profile.ownerUserId,
    currentPairId = this.profile.currentPairId,
    createdAt = this.profile.createdAt,
    lifeStatus = this.lifeState.status.name,
    deathCause = this.lifeState.deathCause?.name,
    abandonedAt = this.profile.abandonedAt,
    hunger = this.stats.hunger,
    energy = this.stats.energy,
    cleanliness = this.stats.cleanliness,
    happiness = this.stats.happiness,
    updatedAt = this.stats.updatedAt,
    syncStatus = "SYNCED"
)
