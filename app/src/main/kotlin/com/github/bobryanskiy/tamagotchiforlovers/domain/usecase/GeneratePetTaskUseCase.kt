package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetTask
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.TaskExample
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.TaskType
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import java.util.UUID
import javax.inject.Inject

class GeneratePetTaskUseCase @Inject constructor() {
    
    private val mathTasks = listOf(
        TaskExample("2 + 2 = ?", "Просто сложи числа", "4"),
        TaskExample("5 * 3 = ?", "Умножь 5 на 3", "15"),
        TaskExample("10 - 4 = ?", "Вычти 4 из 10", "6"),
        TaskExample("8 / 2 = ?", "Раздели 8 на 2", "4"),
        TaskExample("7 + 6 = ?", "Сложи 7 и 6", "13")
    )
    
    private val logicTasks = listOf(
        TaskExample("Что больше: 5 или 3?", "Сравни числа", "5"),
        TaskExample("Какое число четное: 7 или 8?", "Четное делится на 2 без остатка", "8"),
        TaskExample("Продолжи ряд: 2, 4, 6, ...", "Прибавляй по 2", "8")
    )
    
    private val quizTasks = listOf(
        TaskExample("Какой цвет получается при смешивании синего и желтого?", "Вспомни цвета радуги", "Зеленый"),
        TaskExample("Сколько дней в неделе?", "Посчитай дни от понедельника до воскресенья", "7"),
        TaskExample("Какое животное говорит 'мяу'?", "Домашний питомец", "Кошка")
    )
    
    operator fun invoke(): PetTask {
        val types = listOf(TaskType.MATH, TaskType.LOGIC, TaskType.QUIZ)
        val randomType = types.random()
        
        val examples = when (randomType) {
            TaskType.MATH -> mathTasks
            TaskType.LOGIC -> logicTasks
            TaskType.QUIZ -> quizTasks
            TaskType.MEMORY -> mathTasks // Fallback
        }
        
        val example = examples.random()
        
        return PetTask(
            id = UUID.randomUUID().toString(),
            type = randomType,
            description = getTaskDescription(randomType),
            example = example,
            correctAnswer = example.solution
        )
    }
    
    private fun getTaskDescription(type: TaskType): String {
        return when (type) {
            TaskType.MATH -> "Реши математический пример"
            TaskType.LOGIC -> "Реши логическую задачу"
            TaskType.MEMORY -> "Запомни и воспроизведи"
            TaskType.QUIZ -> "Ответь на вопрос викторины"
        }
    }
}
