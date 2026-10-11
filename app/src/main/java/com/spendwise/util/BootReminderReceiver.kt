package com.spendwise.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.spendwise.data.preferences.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val settingsManager = SettingsManager(context.applicationContext)
                    val current = settingsManager.settingsFlow.first()
                    if (current.reminderEnabled) {
                        SoundAndNotificationHelper.scheduleDailyReminder(
                            context = context.applicationContext,
                            enabled = true,
                            timeString = current.reminderTime
                        )
                    }
                } catch (_: Exception) {
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
