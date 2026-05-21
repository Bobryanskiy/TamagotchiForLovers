package com.github.bobryanskiy.tamagotchiforlovers.data.util

import com.github.bobryanskiy.tamagotchiforlovers.domain.util.IdGenerator
import java.util.UUID
import javax.inject.Inject

class UuidIdGenerator @Inject constructor() : IdGenerator {
    override fun generate(): String = UUID.randomUUID().toString()
}