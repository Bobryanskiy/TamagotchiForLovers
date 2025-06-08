package com.github.bobryanskiy.tamagotchiforlovers.data.storage

import android.content.Context
import androidx.core.content.edit

class PairStorage(context: Context) {
    private val sharedPref = context.getSharedPreferences("pair_prefs", Context.MODE_PRIVATE)

    fun savePairId(pairId: String) {
        sharedPref.edit { putString("pair_id", pairId) }
    }

    fun getPairId(): String? {
        return sharedPref.getString("pair_id", null)
    }

    fun clearPairId() {
        sharedPref.edit { remove("pair_id") }
    }
}