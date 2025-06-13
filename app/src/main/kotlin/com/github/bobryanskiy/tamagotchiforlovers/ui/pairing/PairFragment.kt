package com.github.bobryanskiy.tamagotchiforlovers.ui.pairing

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.github.bobryanskiy.tamagotchiforlovers.data.pet.model.PetState
import com.github.bobryanskiy.tamagotchiforlovers.databinding.FragmentPairBinding

class PairFragment : Fragment() {

    private lateinit var viewModel: PairViewModel

    private var _binding: FragmentPairBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPairBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, factory = PairViewModelFactory(requireContext()))[PairViewModel::class.java]

        viewModel.joinForm.observe(
            viewLifecycleOwner,
            Observer { formState ->
                if (formState == null) {
                    return@Observer
                }
                binding.joinPairButton.isEnabled = formState.isNotEmpty()
            })

        viewModel.pairResult.observe(
            viewLifecycleOwner,
            Observer { pairResult ->
                pairResult ?: return@Observer
                Log.d("PAIR", pairResult.toString())
                binding.loading.visibility = View.GONE
                pairResult.error?.let {
                    showPairingFailed(it)
                }
                pairResult.pairModel?.let {
                    updateUi(it.code, it.petState)
                }
            }
        )

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                viewModel.pairDataChanged(
                    binding.code.text.toString()
                )
            }
        }

        binding.code.addTextChangedListener(afterTextChangedListener)

        binding.createPairButton.setOnClickListener {
            binding.loading.visibility = View.VISIBLE
            viewModel.createNewPair()
        }

        binding.joinPairButton.setOnClickListener {
            binding.loading.visibility = View.VISIBLE
            viewModel.joinExistingPair(binding.code.text.toString())
        }
    }

    private fun updateUi(pairId: String, petState: PetState) {
        val welcome = getString(R.string.welcome)
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
        val action = PairFragmentDirections.actionPairFragmentToPetFragment(pairId, petState)
        view?.findNavController()?.navigate(action)
    }

    private fun showPairingFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }
}