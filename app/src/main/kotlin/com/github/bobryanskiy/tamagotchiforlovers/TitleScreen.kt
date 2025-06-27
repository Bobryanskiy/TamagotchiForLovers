package com.github.bobryanskiy.tamagotchiforlovers

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.navigation.fragment.NavHostFragment
import com.github.bobryanskiy.tamagotchiforlovers.data.notifications.Notifications
import com.google.firebase.firestore.FirebaseFirestore


class TitleScreen : AppCompatActivity() {
    private val REQUEST_CODE_POST_NOTIFICATIONS = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true)

        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notificationChannels.forEach { x ->  notificationManager.deleteNotificationChannel(x.id) }
        Notifications.createNotificationChannels(this)

//        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !alarmManager.canScheduleExactAlarms()) {
//            val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
//                data = "package:${packageName}".toUri()
//            }
//            startActivity(settingsIntent)
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_POST_NOTIFICATIONS)
            }
        }

        setContentView(R.layout.activity_title_screen)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val graph = navHostFragment.navController.navInflater.inflate(R.navigation.nav_main)
//        graph.setStartDestination(R.id.difficultyChooseFragment)
        navHostFragment.navController.setGraph(graph, intent.extras)
//        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            currentFocus!!.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onStop() {
        super.onStop()
//        val calendar = Calendar.getInstance().apply {
//            timeInMillis = System.currentTimeMillis()
//            add(Calendar.MINUTE, 1)
//        }
//        Notifications.PetWantEat.schedule(this, calendar.timeInMillis)
//        Log.d("TEST", "STOPPED")
    }
}