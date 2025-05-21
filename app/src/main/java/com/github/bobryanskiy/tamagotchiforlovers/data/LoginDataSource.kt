package com.github.bobryanskiy.tamagotchiforlovers.data

import android.app.Activity
import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.data.model.LoggedInUser
import com.google.firebase.auth.FirebaseAuth
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun login(activity: Activity, auth: FirebaseAuth, username: String, password: String): Result<LoggedInUser> {
        try {
            var result: Result<LoggedInUser> = Result.Error(IOException("Login failed"))
            auth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("EmailPassword", "signInWithEmail:success")
                        auth.currentUser?.let { result = Result.Success(LoggedInUser(it.uid, it.displayName)) }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("EmailPassword", "signInWithEmail:failure", task.exception)
                        result = Result.Error(task.exception!!)
                    }
                }
            return result
//            val user = LoggedInUser()
//            // TODO: handle loggedInUser authentication
//            val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), "Jane Doe")
//            return Result.Success(fakeUser)
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}