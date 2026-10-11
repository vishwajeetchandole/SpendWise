package com.spendwise.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DailyReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        SoundAndNotificationHelper.showDailyReminderNotification(context)
    }
}
