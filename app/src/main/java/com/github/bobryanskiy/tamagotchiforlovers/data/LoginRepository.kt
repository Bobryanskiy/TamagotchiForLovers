package com.github.bobryanskiy.tamagotchiforlovers.data

import android.app.Activity
import com.github.bobryanskiy.tamagotchiforlovers.TitleScreen
import com.github.bobryanskiy.tamagotchiforlovers.data.model.LoggedInUser
import com.google.firebase.auth.FirebaseAuth

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource, val auth: FirebaseAuth) {

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun logout() {
        user = null
        dataSource.logout()
    }

    fun login(activity: Activity, auth: FirebaseAuth, username: String, password: String): Result<LoggedInUser> {
        // handle login
        val result = dataSource.login(activity, auth, username, password)

        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }

        return result
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}