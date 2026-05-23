package com.github.bobryanskiy.tamagotchiforlovers.core.string

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import com.github.bobryanskiy.tamagotchiforlovers.domain.provider.StringResourceProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provider for string resources. Used to avoid passing Context into domain layer.
 */
@Singleton
class ResourceStringProvider @Inject constructor(
    @param:ApplicationContext private val context: Context
    ) : StringResourceProvider {
        override fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }
}
