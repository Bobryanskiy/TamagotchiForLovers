package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Loadable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * App-level ViewModel.
 *
 * Живёт пока живёт MainActivity (переживает перевороты экрана,
 * конфигурационные изменения, но умирает при finish()).
 *
 * Используется для:
 * - Глобального наблюдения за сессией (активный пет, пара)
 * - Реактивной навигации при потере доступа
 * - Хранения общих UI-стейтов (тема, локаль) — в будущем
 *
 * Создаётся в MainActivity через `by viewModels<AppViewModel>()`.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    /** Текущий активный пет — глобально для всего приложения */
    val activePetId: StateFlow<Loadable<String?>> = sessionRepository.observeActivePetId()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = Loadable.Loading
        )

    /** Текущая активная пара */
    val activePairId: StateFlow<Loadable<String?>> = sessionRepository.observeActivePairId()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = Loadable.Loading
        )

    /** Авторизован ли пользователь (синхронно, из FirebaseAuth) */
    val isLoggedIn: Boolean
        get() = authRepository.isLoggedIn()

    /** Текущий userId или null */
    val currentUserId: String?
        get() = authRepository.getCurrentUserId()

    /** Глобальный logout — очищает сессию и auth */
    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
            sessionRepository.clearAllSessionData()
        }
    }
}
