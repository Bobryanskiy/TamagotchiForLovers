package com.github.bobryanskiy.tamagotchiforlovers.domain.model

data class PetTask(
    val id: String,
    val type: TaskType,
    val description: String,
    val example: TaskExample,
    val correctAnswer: String
)

enum class TaskType {
    MATH,
    LOGIC,
    MEMORY,
    QUIZ
}

data class TaskExample(
    val question: String,
    val hint: String,
    val solution: String
)
