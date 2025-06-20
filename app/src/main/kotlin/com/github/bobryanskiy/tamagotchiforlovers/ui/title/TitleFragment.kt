package com.github.bobryanskiy.tamagotchiforlovers.ui.title

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.github.bobryanskiy.tamagotchiforlovers.TitleScreen
import com.github.bobryanskiy.tamagotchiforlovers.data.notifications.Notifications
import com.github.bobryanskiy.tamagotchiforlovers.data.title.model.UserPetInfo
import com.github.bobryanskiy.tamagotchiforlovers.databinding.FragmentTitleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class TitleFragment : Fragment() {
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = Firebase.firestore
    private lateinit var binding: FragmentTitleBinding
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var titleViewModel: TitleViewModel

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
//            startWebRTC()
        } else {
//            showPermissionDeniedDialog(permission)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTitleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity(), factory = SharedViewModelFactory(requireContext()))[SharedViewModel::class.java]
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

        binding.playButton.setOnClickListener {
            titleViewModel.playButton()
        }

        binding.settingButton.setOnClickListener {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.SECOND, 5)
            }
            context?.let { it1 -> Notifications.Companion.PetWantEat.schedule(it1, calendar.timeInMillis) }
        }
    }

    private fun showSuccess(info: UserPetInfo) {
        Log.d("DSFDSF", info.action.toString())
        if (info.action) {
            view?.findNavController()?.navigate(TitleFragmentDirections.actionTitleFragmentToPetFragment(info.pairId ?: "", info.petState!!))
        } else view?.findNavController()?.navigate(info.dest)
    }

    private fun showFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }
}