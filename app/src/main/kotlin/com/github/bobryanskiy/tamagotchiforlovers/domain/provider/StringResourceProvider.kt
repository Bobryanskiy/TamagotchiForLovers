package com.github.bobryanskiy.tamagotchiforlovers.domain.provider

interface StringResourceProvider {
    fun getString(resId: Int, vararg formatArgs: Any): String
}
