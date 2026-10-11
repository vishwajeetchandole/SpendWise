package com.spendwise.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.spendwise.MainActivity
import java.util.Calendar
import java.util.concurrent.TimeUnit

object SoundAndNotificationHelper {

    const val CHANNEL_REMINDER_ID = "spendwise_daily_reminder"
    const val CHANNEL_SAVED_ID = "spendwise_expense_saved"
    const val NOTIFICATION_ID_REMINDER = 1001
    const val NOTIFICATION_ID_SAVED = 1002
    private const val WORK_NAME_DAILY_REMINDER = "spendwise_daily_reminder_work"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDER_ID,
                "Daily Expense Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminds you daily to log your expenses"
            }

            val savedChannel = NotificationChannel(
                CHANNEL_SAVED_ID,
                "Expense Saved Confirmation",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Confirms when a new expense is recorded"
            }

            manager.createNotificationChannel(reminderChannel)
            manager.createNotificationChannel(savedChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun playExpenseSavedSound() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75)
            toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 160)
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    toneGen.release()
                } catch (_: Exception) {}
            }, 250)
        } catch (_: Exception) {
            // Ignore audio hardware unavailability
        }
    }

    fun showExpenseSavedNotification(context: Context, title: String, formattedAmount: String) {
        if (!hasNotificationPermission(context)) return
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_SAVED_ID)
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle("Expense Saved: $formattedAmount")
            .setContentText("Logged \"$title\" to SpendWise")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_SAVED, notification)
        } catch (_: SecurityException) {}
    }

    fun showDailyReminderNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDER_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("SpendWise Daily Reminder")
            .setContentText("Did you add today's expenses?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_REMINDER, notification)
        } catch (_: SecurityException) {}
    }

    fun scheduleDailyReminder(
        context: Context,
        hourOfDay: Int,
        minute: Int,
        enabled: Boolean
    ) {
        val timeString = String.format(java.util.Locale.US, "%02d:%02d", hourOfDay, minute)
        scheduleDailyReminder(context, enabled, timeString)
    }

    fun parseTime(timeString: String): Pair<Int, Int> {
        val parts = timeString.split(":")
        val hour = (parts.getOrNull(0)?.toIntOrNull() ?: 21).coerceIn(0, 23)
        val minute = (parts.getOrNull(1)?.toIntOrNull() ?: 0).coerceIn(0, 59)
        return hour to minute
    }

    fun formatTime12Hour(timeString: String): String {
        val (hour, minute) = parseTime(timeString)
        val amPm = if (hour >= 12) "PM" else "AM"
        val hour12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format(java.util.Locale.US, "%d:%02d %s", hour12, minute, amPm)
    }

    fun scheduleDailyReminder(context: Context, enabled: Boolean, timeString: String) {
        createNotificationChannels(context)
        val workManager = WorkManager.getInstance(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmIntent = Intent(context, DailyReminderAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2001,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!enabled) {
            workManager.cancelUniqueWork(WORK_NAME_DAILY_REMINDER)
            alarmManager.cancel(pendingIntent)
            return
        }

        val parts = timeString.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 21
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val initialDelayMillis = (target.timeInMillis - now.timeInMillis).coerceAtLeast(60_000L)

        // 1. Schedule via WorkManager PeriodicWorkRequest (survives reboots and app closure)
        val workRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME_DAILY_REMINDER,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )

        // 2. Also schedule inexact repeating AlarmManager backup
        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                target.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (_: Exception) {}
    }
}

class DailyReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        SoundAndNotificationHelper.showDailyReminderNotification(applicationContext)
        return Result.success()
    }
}
