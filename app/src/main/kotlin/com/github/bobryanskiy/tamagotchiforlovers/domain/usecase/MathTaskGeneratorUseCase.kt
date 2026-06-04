package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import javax.inject.Inject
import kotlin.random.Random

data class MathTask(
    val question: String,
    val correctAnswer: Int
)

class MathTaskGeneratorUseCase @Inject constructor() {
    companion object {
        private const val OPERATION_ADD = 0
        private const val OPERATION_SUBTRACT = 1
        private const val OPERATION_MULTIPLY = 2
        private const val OPERATIONS_COUNT = 3

        private const val ADDITION_MIN = 1
        private const val ADDITION_MAX = 10

        private const val SUBTRACTION_RESULT_MIN = 5
        private const val SUBTRACTION_RESULT_MAX = 20

        private const val MULTIPLICATION_MIN = 2
        private const val MULTIPLICATION_MAX = 6
    }

    operator fun invoke(): MathTask {
        val type = Random.nextInt(0, OPERATIONS_COUNT)

        return when (type) {
            OPERATION_ADD -> generateAddition()
            OPERATION_SUBTRACT -> generateSubtraction()
            OPERATION_MULTIPLY -> generateMultiplication()
            else -> generateAddition()
        }
    }

    private fun generateAddition(): MathTask {
        val a = Random.nextInt(ADDITION_MIN, ADDITION_MAX)
        val b = Random.nextInt(ADDITION_MIN, ADDITION_MAX)
        return MathTask("$a + $b = ?", a + b)
    }

    private fun generateSubtraction(): MathTask {
        val a = Random.nextInt(SUBTRACTION_RESULT_MIN, SUBTRACTION_RESULT_MAX)
        val b = Random.nextInt(ADDITION_MIN, a)
        return MathTask("$a - $b = ?", a - b)
    }

    private fun generateMultiplication(): MathTask {
        val a = Random.nextInt(MULTIPLICATION_MIN, MULTIPLICATION_MAX)
        val b = Random.nextInt(MULTIPLICATION_MIN, MULTIPLICATION_MAX)
        return MathTask("$a × $b = ?", a * b)
    }
}
