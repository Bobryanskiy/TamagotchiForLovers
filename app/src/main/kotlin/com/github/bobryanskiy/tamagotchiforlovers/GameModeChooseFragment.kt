package com.github.bobryanskiy.tamagotchiforlovers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText

class GameModeChooseFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_mode_choose, container, false)
    }

    lateinit var PET_NAME : String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.continueButton).setOnClickListener {
            if (view.findViewById<TextInputEditText>(R.id.editTextInput).text.toString() != "") {
                PET_NAME = R.id.editTextInput.toString()
                findNavController().navigate(R.id.action_gameModeChooseFragment_to_difficultyChooseFragment)
            }
        }
        view.findViewById<Button>(R.id.backButton).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    fun continueChoose(view: View) {

    }
}