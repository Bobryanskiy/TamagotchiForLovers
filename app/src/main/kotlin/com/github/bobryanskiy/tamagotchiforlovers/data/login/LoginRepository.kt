package com.github.bobryanskiy.tamagotchiforlovers.data.login

import com.github.bobryanskiy.tamagotchiforlovers.data.login.model.LoggedInUser
import com.github.bobryanskiy.tamagotchiforlovers.util.Result
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource) {

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

    suspend fun login(username: String, password: String, loginType: LoginType): Result<LoggedInUser> {
        // handle login
        val result = when (loginType) {
            LoginType.Login -> dataSource.loginViaEmail(username, password)
            LoginType.Register -> dataSource.registerViaEmail(username, password)
        }

        if (result is Result.Success) {
            setLoggedInUser(result.data)
        } else if ((result as Result.Error).exception is FirebaseAuthInvalidCredentialsException) {
            true
        }

        return result
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}