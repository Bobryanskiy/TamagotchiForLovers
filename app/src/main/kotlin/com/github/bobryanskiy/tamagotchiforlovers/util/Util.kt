package com.github.bobryanskiy.tamagotchiforlovers.util

object Util {
    fun generatePairCode(length: Int = 6): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
}