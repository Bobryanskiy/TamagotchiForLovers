package com.github.bobryanskiy.tamagotchiforlovers

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.github.bobryanskiy.tamagotchiforlovers.data.storage.PairStorage
import com.github.bobryanskiy.tamagotchiforlovers.databinding.FragmentTitleBinding
import com.github.bobryanskiy.tamagotchiforlovers.notifications.Notifications
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

        binding.playButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(android.Manifest.permission.INTERNET)
            }
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val pairId = PairStorage(requireContext().applicationContext).getPairId()
                if (pairId != null) {
                    view.findNavController().navigate(R.id.action_titleFragment_to_petFragment)
                } else {
                    firestore.collection("users").document(currentUser.uid)
                        .get().addOnSuccessListener { document ->
                            if (document["pairId"] != null) {
                                view.findNavController().navigate(R.id.action_titleFragment_to_petFragment)
                                PairStorage(requireContext().applicationContext).savePairId(document["pairId"] as String)
                                firestore.collection("pairs").document(document["pairId"] as String)
                                    .get()
                                    .addOnSuccessListener { document ->

                                    }
                            } else view.findNavController().navigate(R.id.action_titleFragment_to_pairFragment)
                        }.addOnFailureListener {
                            view.findNavController().navigate(R.id.action_titleFragment_to_loginFragment)
                        }
                }
            } else {
                view.findNavController().navigate(R.id.action_titleFragment_to_loginFragment)
            }
        }

        binding.settingButton.setOnClickListener {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.SECOND, 5)
            }
            val intent = Intent(context, TitleScreen::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            context?.let { it1 -> Notifications.PetWantEat.schedule(it1, calendar.timeInMillis, pendingIntent) }
        }
    }
}