package com.spendwise

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.spendwise.ui.SpendWiseApp
import com.spendwise.util.SoundAndNotificationHelper

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoundAndNotificationHelper.createNotificationChannels(this)
        enableEdgeToEdge()
        setContent {
            SpendWiseApp()
        }
    }
}
