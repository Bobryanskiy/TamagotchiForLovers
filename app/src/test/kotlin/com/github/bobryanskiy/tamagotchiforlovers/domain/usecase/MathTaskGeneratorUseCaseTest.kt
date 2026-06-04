package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MathTaskGeneratorUseCaseTest {

    private val useCase = MathTaskGeneratorUseCase()

    @Test
    fun `should generate task with valid format`() {
        repeat(50) {
            val task = useCase()

            assertTrue("Question should contain '='", task.question.contains("="))
            assertTrue("Question should contain '?'", task.question.contains("?"))
            assertTrue("Answer should be non-negative", task.correctAnswer >= 0)
        }
    }

    @Test
    fun `addition answer should match sum`() {
        repeat(20) {
            val task = useCase()
            if (task.question.contains("+")) {
                val parts = task.question.substringBefore("=").trim().split("+")
                val a = parts[0].trim().toInt()
                val b = parts[1].trim().toInt()
                assertEquals(a + b, task.correctAnswer)
            }
        }
    }
}
