package com.github.bobryanskiy.tamagotchiforlovers.ui.title

import com.github.bobryanskiy.tamagotchiforlovers.data.title.model.UserPetInfo

data class UserPetInfoResult(
    val success: UserPetInfo? = null,
    val error: Int? = null,
)