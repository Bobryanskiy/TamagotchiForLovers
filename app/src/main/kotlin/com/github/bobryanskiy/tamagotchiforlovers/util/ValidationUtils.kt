package com.github.bobryanskiy.tamagotchiforlovers.util

import android.util.Patterns

/**
 * Утилиты для валидации пользовательского ввода
 */
object ValidationUtils {

    /**
     * Проверяет корректность email адреса
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Проверяет надежность пароля (минимум 6 символов)
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * Возвращает сообщение об ошибке для email
     */
    fun getEmailError(email: String): String? {
        return when {
            email.isBlank() -> "Email не может быть пустым"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Некорректный формат email"
            else -> null
        }
    }

    /**
     * Возвращает сообщение об ошибке для пароля
     */
    fun getPasswordError(password: String): String? {
        return when {
            password.isBlank() -> "Пароль не может быть пустым"
            password.length < 6 -> "Пароль должен содержать минимум 6 символов"
            else -> null
        }
    }
}