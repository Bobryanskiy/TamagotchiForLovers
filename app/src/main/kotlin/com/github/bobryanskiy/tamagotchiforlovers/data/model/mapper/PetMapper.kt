package com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper

import com.github.bobryanskiy.tamagotchiforlovers.data.model.dto.PetDto
import com.github.bobryanskiy.tamagotchiforlovers.data.model.dto.ProfileDto
import com.github.bobryanskiy.tamagotchiforlovers.data.model.dto.StatsDto
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetProfile
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetStats

fun PetDto.toDomain(petId: String): Pet {
    val p = profile ?: ProfileDto()
    val s = stats ?: StatsDto()

    return Pet(
        id = petId,
        profile = PetProfile(
            name = p.name,
            ownerUserId = p.ownerUserId,
            currentPairId = p.currentPairId,
            createdAt = p.createdAt,
            criticalStatus = p.criticalStatus,
            recoveryEndTime = p.recoveryEndTime,
            abandonedAt = p.abandonedAt?.toDate()?.time
        ),
        stats = PetStats(s.hunger, s.energy, s.cleanliness, s.happiness, s.updatedAt)
    )
}