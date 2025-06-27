package com.github.bobryanskiy.tamagotchiforlovers.ui.title

import android.app.Application
import android.os.Bundle
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
import com.github.bobryanskiy.tamagotchiforlovers.data.notifications.Notifications
import com.github.bobryanskiy.tamagotchiforlovers.data.title.model.UserPetInfo
import com.github.bobryanskiy.tamagotchiforlovers.databinding.FragmentTitleBinding
import com.github.bobryanskiy.tamagotchiforlovers.util.NetworkConnectionLiveData
import java.util.*
import kotlin.system.exitProcess

class TitleFragment : Fragment() {
    private lateinit var binding: FragmentTitleBinding
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var titleViewModel: TitleViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTitleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity(), factory = SharedViewModelFactory(requireContext().applicationContext as Application))[SharedViewModel::class.java]
        titleViewModel = ViewModelProvider(this, factory = TitleViewModelFactory(requireContext()))[TitleViewModel::class.java]

        titleViewModel.playResult.observe(viewLifecycleOwner, Observer { result ->
            result ?: return@Observer
            result.error?.let {
                showFailed(it)
            }
            result.success?.let {
                if (it.pairId != null && it.pairId != "") sharedViewModel.subscribeToFirestore(it.pairId)
                it.petState?.let { p ->
                    sharedViewModel.petStateSet(p)
                }
                showSuccess(it)
            }
        })

        var isOnline = false
        val connectionLiveData = NetworkConnectionLiveData(requireContext())
        connectionLiveData.observe(viewLifecycleOwner, Observer { isNetworkAvailable ->
            isNetworkAvailable?.let {
                isOnline = it
            }
        })

        binding.playButton.setOnClickListener {
            titleViewModel.playButton(isOnline)
        }

        binding.settingButton.setOnClickListener {
            context?.let { Notifications.Companion.PetWantEat.schedule(it, 1) }
        }

        binding.exitButton.setOnClickListener {
            requireActivity().finishAndRemoveTask()
        }
    }

    private fun showSuccess(info: UserPetInfo) {
        if (info.action) {
            view?.findNavController()?.navigate(TitleFragmentDirections.actionTitleFragmentToPetFragment(info.pairId ?: "", info.petState!!))
        } else view?.findNavController()?.navigate(info.dest)
    }

    private fun showFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }
}