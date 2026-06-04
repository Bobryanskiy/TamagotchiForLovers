package com.github.bobryanskiy.tamagotchiforlovers.domain.error

sealed class SyncError : DomainError {
    data object NotAuthenticated : SyncError()
    data object NetworkError : SyncError()
    data class PartialSuccess(val successCount: Int, val failureCount: Int) : SyncError()
}
