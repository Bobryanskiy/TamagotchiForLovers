package com.github.bobryanskiy.tamagotchiforlovers.domain.result

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.UserError

typealias PetResult<T> = DomainResult<T, PetError>
typealias PairResult<T> = DomainResult<T, PairError>
typealias UserResult<T> = DomainResult<T, UserError>