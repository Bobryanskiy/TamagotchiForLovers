package com.github.bobryanskiy.tamagotchiforlovers.util

import android.util.Patterns
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.NameLimits

object ValidationUtils {

    // ═══════════════════════════════════════════════════════════
    // AUTH — существующие методы
    // ═══════════════════════════════════════════════════════════

    fun isValidEmail(email: String): Boolean =
        email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun isValidPassword(password: String): Boolean =
        password.length >= 6

    fun getEmailErrorResId(email: String): Int? = when {
        email.isBlank() -> R.string.error_empty_email
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> R.string.error_invalid_email_format
        else -> null
    }

    fun getPasswordErrorResId(password: String): Int? = when {
        password.isBlank() -> R.string.error_empty_password
        password.length < 6 -> R.string.error_short_password
        else -> null
    }

    // ═══════════════════════════════════════════════════════════
    // NAMES — новые методы (возвращают ID строки ресурса)
    // ═══════════════════════════════════════════════════════════

    /** Возвращает R.string.* ошибки для имени питомца или null */
    fun getPetNameErrorResId(name: String): Int? = validateNameResId(
        value = name,
        minLen = NameLimits.PET_NAME_MIN,
        maxLen = NameLimits.PET_NAME_MAX,
        emptyResId = R.string.error_empty_pet_name,
        tooShortResId = R.string.error_pet_name_too_short,
        tooLongResId = R.string.error_pet_name_too_long,
        invalidCharsResId = R.string.error_pet_name_invalid_chars
    )

    /** Возвращает R.string.* ошибки для названия пары или null */
    fun getPairNameErrorResId(name: String): Int? = validateNameResId(
        value = name,
        minLen = NameLimits.PAIR_NAME_MIN,
        maxLen = NameLimits.PAIR_NAME_MAX,
        emptyResId = R.string.error_empty_pair_name,
        tooShortResId = R.string.error_pair_name_too_short,
        tooLongResId = R.string.error_pair_name_too_long,
        invalidCharsResId = R.string.error_pair_name_invalid_chars
    )

    /** Возвращает R.string.* ошибки для никнейма или null */
    fun getNicknameErrorResId(name: String): Int? = validateNameResId(
        value = name,
        minLen = NameLimits.NICKNAME_MIN,
        maxLen = NameLimits.NICKNAME_MAX,
        emptyResId = R.string.error_empty_nickname,
        tooShortResId = R.string.error_nickname_too_short,
        tooLongResId = R.string.error_nickname_too_long,
        invalidCharsResId = R.string.error_nickname_invalid_chars
    )

    // Удобные boolean-хелперы
    fun isValidPetName(name: String): Boolean = getPetNameErrorResId(name) == null
    fun isValidPairName(name: String): Boolean = getPairNameErrorResId(name) == null
    fun isValidNickname(name: String): Boolean = getNicknameErrorResId(name) == null

    // ═══════════════════════════════════════════════════════════
    // PRIVATE — общая логика валидации
    // ═══════════════════════════════════════════════════════════

    private fun validateNameResId(
        value: String,
        minLen: Int,
        maxLen: Int,
        emptyResId: Int,
        tooShortResId: Int,
        tooLongResId: Int,
        invalidCharsResId: Int
    ): Int? {
        val trimmed = value.trim()
        return when {
            trimmed.isEmpty() -> emptyResId
            trimmed.length < minLen -> tooShortResId
            trimmed.length > maxLen -> tooLongResId
            containsForbiddenChars(trimmed) -> invalidCharsResId
            else -> null
        }
    }

    /**
     * Разрешаем: буквы (включая кириллицу), цифры, пробелы, дефис, апостроф.
     * Запрещаем: спецсимволы, emoji, управляющие символы.
     */
    private fun containsForbiddenChars(value: String): Boolean {
        val allowed = Regex("^[\\p{L}\\p{N} '\\-]+$")
        return !allowed.matches(value)
    }
}