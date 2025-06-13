package com.github.bobryanskiy.tamagotchiforlovers.ui.pairing

import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model.PairModel

data class PairResult (
    val pairModel: PairModel? = null,
    val error: Int? = null
)