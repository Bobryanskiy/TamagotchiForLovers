package com.github.bobryanskiy.tamagotchiforlovers.data.exception

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.DomainError

class RepositoryException(val error: DomainError) : RuntimeException()