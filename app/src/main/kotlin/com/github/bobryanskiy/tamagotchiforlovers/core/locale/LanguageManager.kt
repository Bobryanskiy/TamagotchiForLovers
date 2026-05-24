package com.github.bobryanskiy.tamagotchiforlovers.core.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageManager @Inject constructor() {

    companion object {
        const val LANG_EN = "en"
        const val LANG_RU = "ru"
        const val LANG_SYSTEM = ""
    }

    /**
     * Текущий выбранный язык (или пустая строка если системный).
     */
    fun getCurrentLanguage(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (locales.isEmpty) LANG_SYSTEM else locales[0]?.language ?: LANG_SYSTEM
    }

    /**
     * Переключает язык приложения.
     * Система автоматически:
     * - Сохранит выбор (при autoStoreLocales=true)
     * - Пересоздаст все активные Activity
     * - Восстановит язык после перезапуска
     */
    fun setLanguage(languageCode: String) {
        val localeList = if (languageCode.isBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageCode)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    /**
     * Список поддерживаемых языков для UI.
     */
    fun getSupportedLanguages(): List<LanguageOption> = listOf(
        LanguageOption(LANG_SYSTEM, "Системный / System"),
        LanguageOption(LANG_RU, "Русский"),
        LanguageOption(LANG_EN, "English")
    )
}

data class LanguageOption(
    val code: String,
    val displayName: String
)