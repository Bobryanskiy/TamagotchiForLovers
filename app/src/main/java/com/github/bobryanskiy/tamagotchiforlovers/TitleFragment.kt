package com.github.bobryanskiy.tamagotchiforlovers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.findNavController
import com.github.bobryanskiy.tamagotchiforlovers.data.model.FirebaseViewModel
import com.github.bobryanskiy.tamagotchiforlovers.databinding.FragmentTitleBinding
import com.github.bobryanskiy.tamagotchiforlovers.firebase.firestore.viewmodel.MainActivityViewModel
import com.google.firebase.auth.auth

class TitleFragment : Fragment() {
    private lateinit var binding: FragmentTitleBinding

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var firebaseViewModel: FirebaseViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTitleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // View model
        viewModel = ViewModelProvider(this).get<MainActivityViewModel>()
        firebaseViewModel = ViewModelProvider(this)[FirebaseViewModel::class.java]

        binding.playButton.setOnClickListener {
            val currentUser = firebaseViewModel.auth.currentUser
            if (currentUser != null) {
                view.findNavController().navigate(R.id.action_titleFragment_to_gameModeChooseFragment)
            } else {
                view.findNavController().navigate(R.id.action_titleFragment_to_loginFragment)
            }
        }

        binding.settingButton.setOnClickListener {
            
        }
    }
}