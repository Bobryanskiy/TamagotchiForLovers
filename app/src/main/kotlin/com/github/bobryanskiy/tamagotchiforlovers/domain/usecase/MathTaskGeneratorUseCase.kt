package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import javax.inject.Inject
import kotlin.random.Random

data class MathTask(
    val question: String,
    val correctAnswer: Int
)

class MathTaskGeneratorUseCase @Inject constructor() {

    operator fun invoke(): MathTask {
        val type = Random.nextInt(0, 3)

        return when (type) {
            0 -> generateAddition()
            1 -> generateSubtraction()
            else -> generateMultiplication()
        }
    }

    private fun generateAddition(): MathTask {
        val a = Random.nextInt(1, 10)
        val b = Random.nextInt(1, 10)
        return MathTask("$a + $b = ?", a + b)
    }

    private fun generateSubtraction(): MathTask {
        val a = Random.nextInt(5, 20)
        val b = Random.nextInt(1, a)
        return MathTask("$a - $b = ?", a - b)
    }

    private fun generateMultiplication(): MathTask {
        val a = Random.nextInt(2, 6)
        val b = Random.nextInt(2, 6)
        return MathTask("$a × $b = ?", a * b)
    }
}