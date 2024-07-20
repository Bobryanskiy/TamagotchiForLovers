package com.github.bobryanskiy.tamagotchiforlovers

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class GameChooseScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_choose_screen)
    }

    fun goBack(view : View) {
        finish()
    }

    lateinit var PET_NAME : String

    fun continueChoose(view : View) {
        if (R.id.editTextInput.toString() != "") {
            PET_NAME = R.id.editTextInput.toString()
            val intent = Intent(this, DifficultChooserScreen::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
            startActivity(intent, options.toBundle())
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}