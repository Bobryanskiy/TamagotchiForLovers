package com.github.bobryanskiy.tamagotchiforlovers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.bobryanskiy.tamagotchiforlovers.databinding.FragmentPairBinding

class PairFragment : Fragment() {

    private lateinit var viewModel: PairViewModel
    private var pairId: String? = null

    private var _binding: FragmentPairBinding? = null
    private val binding get() = _binding!!

    private fun updateUI(petState: PetState) {
        // Обновите UI здесь
    }

    companion object {
        fun newInstance() = PairFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPairBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, factory = PairViewModelFactory())[PairViewModel::class.java]

        binding.createPairButton.setOnClickListener {
            viewModel.createNewPair { code ->
                code?.let {
                    pairId = code
                }
            }
        }

        binding.findPairButton.setOnClickListener {
            viewModel.joinExistingPair("") {
            }
        }
    }
}