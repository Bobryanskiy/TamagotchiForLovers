package com.github.bobryanskiy.tamagotchiforlovers.util

import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.NameLimits

object ValidationUtils {

    private val FIREBASE_EMAIL_REGEX = Regex(
        """^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*\.[a-zA-Z]{2,}$"""
    )

    private const val MAX_EMAIL_LENGTH = 254

    fun getEmailErrorResId(email: String): Int? {
        val trimmed = email.trim()
        return when {
            trimmed.isBlank() -> R.string.error_empty_email
            trimmed.length > MAX_EMAIL_LENGTH -> R.string.error_invalid_email_format
            !FIREBASE_EMAIL_REGEX.matches(trimmed) -> R.string.error_invalid_email_format
            else -> null
        }
    }

    fun getPasswordErrorResId(password: String): Int? = when {
        password.isBlank() -> R.string.error_empty_password
        password.length < 6 -> R.string.error_short_password
        else -> null
    }

    fun getPetNameErrorResId(name: String): Int? = validateNameResId(
        value = name,
        minLen = NameLimits.PET_NAME_MIN,
        maxLen = NameLimits.PET_NAME_MAX,
        emptyResId = R.string.error_empty_pet_name,
        tooShortResId = R.string.error_pet_name_too_short,
        tooLongResId = R.string.error_pet_name_too_long,
        invalidCharsResId = R.string.error_pet_name_invalid_chars
    )

    fun getPairNameErrorResId(name: String): Int? = validateNameResId(
        value = name,
        minLen = NameLimits.PAIR_NAME_MIN,
        maxLen = NameLimits.PAIR_NAME_MAX,
        emptyResId = R.string.error_empty_pair_name,
        tooShortResId = R.string.error_pair_name_too_short,
        tooLongResId = R.string.error_pair_name_too_long,
        invalidCharsResId = R.string.error_pair_name_invalid_chars
    )

    fun getNicknameErrorResId(name: String): Int? = validateNameResId(
        value = name,
        minLen = NameLimits.NICKNAME_MIN,
        maxLen = NameLimits.NICKNAME_MAX,
        emptyResId = R.string.error_empty_nickname,
        tooShortResId = R.string.error_nickname_too_short,
        tooLongResId = R.string.error_nickname_too_long,
        invalidCharsResId = R.string.error_nickname_invalid_chars
    )

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

    private fun containsForbiddenChars(value: String): Boolean {
        val allowed = Regex("^[\\p{L}\\p{N} '\\-]+$")
        return !allowed.matches(value)
    }
}
