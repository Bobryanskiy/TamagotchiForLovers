package com.github.bobryanskiy.tamagotchiforlovers.ui.pet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.databinding.FragmentPetBinding
import com.github.bobryanskiy.tamagotchiforlovers.ui.title.SharedViewModel
import com.github.bobryanskiy.tamagotchiforlovers.ui.title.SharedViewModelFactory

class PetFragment : Fragment() {

    private lateinit var viewModel: PetViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private var _binding: FragmentPetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPetBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pairId = PetFragmentArgs.fromBundle(requireArguments()).pairId
        binding.pairCode.text = pairId
        viewModel = ViewModelProvider(this, factory = PetViewModelFactory(requireContext()))[PetViewModel::class.java]
        sharedViewModel = ViewModelProvider(requireActivity(), SharedViewModelFactory(requireContext()))[SharedViewModel::class.java]

        viewModel.deletePetResult.observe(viewLifecycleOwner, Observer { result ->
            result ?: return@Observer
            result.error?.let {
                showDeletePetFailed(it)
            }
            result.success?.let {
                showDeletePetSuccess(it)
            }
        })
        viewModel.switchVisibilityResult.observe(viewLifecycleOwner, Observer { result ->
            result ?: return@Observer
            result.error?.let {
                showDeletePetFailed(it)
            }
            result.success?.let {
                showDeletePetFailed(it)
            }
        })

        sharedViewModel.petState.observe(viewLifecycleOwner, Observer { state ->
            binding.hungerText.text = state.hunger.toString()
            binding.cleanText.text = state.cleanliness.toString()
            binding.sleepText.text = state.tiredness.toString()
            binding.playGamesText.text = state.happiness.toString()
        })

        binding.logout.setOnClickListener {
            viewModel.logout()
            view.findNavController().navigate(R.id.action_petFragment_to_loginFragment)
        }
        binding.deletePet.setOnClickListener {
            viewModel.deletePet()
        }
        binding.switchPairVisibility.setOnClickListener {
            viewModel.switchVisibility()
        }

        binding.eatButton.setOnClickListener {
            sharedViewModel.feedPet(pairId)
        }
        binding.sleepButton.setOnClickListener {

        }
        binding.cleanButton.setOnClickListener {

        }
        binding.playGamesButton.setOnClickListener {

        }
    }

    private fun showDeletePetSuccess(@StringRes successString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, successString, Toast.LENGTH_LONG).show()
        view?.findNavController()?.navigate(R.id.action_petFragment_to_pairFragment)
    }

    private fun showDeletePetFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }
}