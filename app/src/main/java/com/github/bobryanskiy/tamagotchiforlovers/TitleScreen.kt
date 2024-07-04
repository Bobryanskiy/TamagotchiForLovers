package com.github.bobryanskiy.tamagotchiforlovers

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class TitleScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_title_screen)
    }

    fun playActivity(view : View) {
        val intent = Intent(this, GameChooseScreen::class.java)
        val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
        startActivity(intent, options.toBundle())
    }

    fun exitGame(view : View) {
        finishAffinity()
    }
}