package com.github.bobryanskiy.tamagotchiforlovers.firebase.firestore.util

import com.github.bobryanskiy.tamagotchiforlovers.firebase.firestore.model.Rating
import java.security.SecureRandom
import java.util.ArrayList
import java.util.Random
import java.util.UUID
import kotlin.math.floor

/**
 * Utilities for Ratings.
 */
object RatingUtil {

    private val REVIEW_CONTENTS = arrayOf(
        // 0 - 1 stars
        "This was awful! Totally inedible.",

        // 1 - 2 stars
        "This was pretty bad, would not go back.",

        // 2 - 3 stars
        "I was fed, so that's something.",

        // 3 - 4 stars
        "This was a nice meal, I'd go back.",

        // 4 - 5 stars
        "This was fantastic!  Best ever!",
    )

    /**
     * Create a random Rating POJO.
     */
    private val random: Rating
        get() {
            val rating = Rating()

            val random = SecureRandom()

            val score = random.nextDouble() * 5.0
            val text = REVIEW_CONTENTS[floor(score).toInt()]

            rating.userId = UUID.randomUUID().toString()
            rating.userName = "Random User"
            rating.rating = score
            rating.text = text

            return rating
        }

    /**
     * Get a list of random Rating POJOs.
     */
    fun getRandomList(length: Int): List<Rating> {
        val result = ArrayList<Rating>()

        for (i in 0 until length) {
            result.add(random)
        }

        return result
    }

    /**
     * Get the average rating of a List.
     */
    fun getAverageRating(ratings: List<Rating>): Double {
        var sum = 0.0

        for (rating in ratings) {
            sum += rating.rating
        }

        return sum / ratings.size
    }
}
