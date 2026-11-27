package com.example.infrastructure

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.core.localization.AppLanguage
import com.example.data.local.JomaKhorochDatabase
import com.example.data.preferences.AppPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

object ReminderScheduler {
    const val CHANNEL_ID = "jomakhoroch_reminders"
    const val CHANNEL_BUDGET_ALERTS = "jomakhoroch_budget_alerts"
    const val CHANNEL_DAILY_DIGEST = "jomakhoroch_daily_digest"
    const val CHANNEL_APP_UPDATES = "jomakhoroch_app_updates"

    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_DESC = "extra_desc"
    const val EXTRA_CHANNEL_ID = "extra_channel_id"
    const val EXTRA_IS_DAILY = "extra_is_daily"
    const val EXTRA_DAILY_HOUR = "extra_daily_hour"
    const val EXTRA_DAILY_MINUTE = "extra_daily_minute"
    const val EXTRA_IS_BANGLA = "extra_is_bangla"
    const val EXTRA_OPEN_APP_UPDATE = "extra_open_app_update"

    private const val DAILY_REMINDER_REQUEST_CODE = 900_001
    private const val LOAN_REQUEST_CODE_OFFSET = 500_000
    private const val BUDGET_NOTIFICATION_OFFSET = 700_000
    private const val APP_UPDATE_NOTIFICATION_ID = 998_001
    private const val TEST_NOTIFICATION_ID = 999_001

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            val remindersChannel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
            }
            val budgetChannel = NotificationChannel(
                CHANNEL_BUDGET_ALERTS,
                context.getString(R.string.notification_channel_budget_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_budget_desc)
            }
            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY_DIGEST,
                context.getString(R.string.notification_channel_daily_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_daily_desc)
            }
            val updatesChannel = NotificationChannel(
                CHANNEL_APP_UPDATES,
                context.getString(R.string.notification_channel_updates_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_updates_desc)
            }
            manager.createNotificationChannels(listOf(remindersChannel, budgetChannel, dailyChannel, updatesChannel))
        }
    }

    fun scheduleReminder(
        context: Context,
        taskId: Long,
        title: String,
        description: String,
        triggerAtMillis: Long
    ) {
        ensureChannel(context)
        if (triggerAtMillis <= System.currentTimeMillis()) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val requestCode = taskId.toInt()
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_ID, requestCode)
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_DESC, description)
            putExtra(EXTRA_CHANNEL_ID, CHANNEL_ID)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
    }

    fun cancelReminder(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pending)
    }

    fun scheduleLoanDueReminder(
        context: Context,
        loanId: Long,
        personName: String,
        isLending: Boolean,
        dueDateMillis: Long,
        isBangla: Boolean
    ) {
        ensureChannel(context)
        val cal = Calendar.getInstance().apply {
            timeInMillis = dueDateMillis
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val triggerAt = cal.timeInMillis
        if (triggerAt <= System.currentTimeMillis()) return

        val title = if (isBangla) {
            if (isLending) "পাওনা আদায়ের তারিখ: $personName" else "ধার পরিশোধের তারিখ: $personName"
        } else {
            if (isLending) "Loan Receivable Due: $personName" else "Loan Repayment Due: $personName"
        }
        val desc = if (isBangla) {
            if (isLending) "$personName-এর কাছ থেকে টাকা ফেরত পাওয়ার নির্ধারিত তারিখ আজ।"
            else "$personName-কে ধার পরিশোধ করার নির্ধারিত তারিখ আজ।"
        } else {
            if (isLending) "Scheduled date to receive repayment from $personName."
            else "Scheduled date to repay loan to $personName."
        }

        val requestCode = LOAN_REQUEST_CODE_OFFSET + loanId.toInt()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_ID, requestCode)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_DESC, desc)
            putExtra(EXTRA_CHANNEL_ID, CHANNEL_ID)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
    }

    fun cancelLoanDueReminder(context: Context, loanId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            LOAN_REQUEST_CODE_OFFSET + loanId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pending)
    }

    fun scheduleDailyReminder(
        context: Context,
        hour: Int,
        minute: Int,
        isBangla: Boolean
    ) {
        ensureChannel(context)
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val title = if (isBangla) "আজকের হিসাব লিখেছেন কি?" else "Daily Ledger Check-in"
        val desc = if (isBangla) {
            "আজকের আয়, ব্যয় এবং দোকানের বাকি জমাখরচে সংরক্ষণ করুন।"
        } else {
            "Take a moment to log today's income, expenses, and dues in JomaKhoroch."
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_ID, DAILY_REMINDER_REQUEST_CODE)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_DESC, desc)
            putExtra(EXTRA_CHANNEL_ID, CHANNEL_DAILY_DIGEST)
            putExtra(EXTRA_IS_DAILY, true)
            putExtra(EXTRA_DAILY_HOUR, hour.coerceIn(0, 23))
            putExtra(EXTRA_DAILY_MINUTE, minute.coerceIn(0, 59))
            putExtra(EXTRA_IS_BANGLA, isBangla)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            DAILY_REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeInMillis, pending)
    }

    fun cancelDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            DAILY_REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pending)
    }

    fun notifyBudgetAlert(
        context: Context,
        budgetId: Long,
        title: String,
        body: String
    ): Boolean {
        return postNotificationNow(
            context = context,
            notificationId = BUDGET_NOTIFICATION_OFFSET + budgetId.toInt(),
            channelId = CHANNEL_BUDGET_ALERTS,
            title = title,
            body = body
        )
    }

    fun sendTestNotification(
        context: Context,
        isBangla: Boolean
    ): Boolean {
        val title = if (isBangla) "জমাখরচ নোটিফিকেশন সক্রিয়" else "JomaKhoroch Notifications Active"
        val body = if (isBangla) {
            "বাজেট সতর্কতা, ধার ফেরত এবং দৈনিক হিসাবের রিমাইন্ডার সঠিকভাবে কাজ করছে।"
        } else {
            "Budget alerts, loan due reminders, and daily ledger check-ins are ready."
        }
        return postNotificationNow(
            context = context,
            notificationId = TEST_NOTIFICATION_ID,
            channelId = CHANNEL_ID,
            title = title,
            body = body
        )
    }

    fun notifyAppUpdateAvailable(
        context: Context,
        versionName: String,
        releaseNotes: String,
        isBangla: Boolean
    ): Boolean {
        val title = if (isBangla) {
            "জমাখরচ নতুন আপডেট (v$versionName)"
        } else {
            "JomaKhoroch Update Available (v$versionName)"
        }
        val cleanNotes = releaseNotes
            .lineSequence()
            .map { it.trim().removePrefix("#").removePrefix("-").removePrefix("*").trim() }
            .firstOrNull { it.isNotBlank() }
            .orEmpty()
        val body = if (isBangla) {
            if (cleanNotes.isNotBlank()) "নতুন ভার্সন রিলিজ হয়েছে — $cleanNotes। আপডেট করতে ট্যাপ করুন।"
            else "নতুন ভার্সন v$versionName রিলিজ হয়েছে। অ্যাপ থেকে ডাউনলোড ও আপডেট করতে ট্যাপ করুন।"
        } else {
            if (cleanNotes.isNotBlank()) "Version v$versionName is out — $cleanNotes. Tap to update."
            else "Version v$versionName is now available. Tap to view details and update."
        }
        return postNotificationNow(
            context = context,
            notificationId = APP_UPDATE_NOTIFICATION_ID,
            channelId = CHANNEL_APP_UPDATES,
            title = title,
            body = body,
            openAppUpdate = true
        )
    }

    fun postNotificationNow(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        body: String,
        openAppUpdate: Boolean = false
    ): Boolean {
        ensureChannel(context)
        if (!hasNotificationPermission(context)) return false

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (openAppUpdate) {
                putExtra(EXTRA_OPEN_APP_UPDATE, true)
            }
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priority = if (channelId == CHANNEL_DAILY_DIGEST) {
            NotificationCompat.PRIORITY_DEFAULT
        } else {
            NotificationCompat.PRIORITY_HIGH
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_wallet)
            .setContentTitle(title)
            .setContentText(body.ifBlank { context.getString(R.string.app_tagline_bn) })
            .setStyle(NotificationCompat.BigTextStyle().bigText(body.ifBlank { context.getString(R.string.app_tagline_bn) }))
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        return runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            true
        }.getOrDefault(false)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(
            ReminderScheduler.EXTRA_NOTIFICATION_ID,
            intent.getLongExtra(ReminderScheduler.EXTRA_TASK_ID, 1L).toInt()
        )
        val channelId = intent.getStringExtra(ReminderScheduler.EXTRA_CHANNEL_ID)
            ?: ReminderScheduler.CHANNEL_ID
        val title = intent.getStringExtra(ReminderScheduler.EXTRA_TITLE) ?: "জমাখরচ রিমাইন্ডার"
        val desc = intent.getStringExtra(ReminderScheduler.EXTRA_DESC).orEmpty()

        ReminderScheduler.postNotificationNow(
            context = context,
            notificationId = notificationId,
            channelId = channelId,
            title = title,
            body = desc
        )

        // Automatically re-arm the next day's accounting reminder
        if (intent.getBooleanExtra(ReminderScheduler.EXTRA_IS_DAILY, false)) {
            val hour = intent.getIntExtra(ReminderScheduler.EXTRA_DAILY_HOUR, 21)
            val minute = intent.getIntExtra(ReminderScheduler.EXTRA_DAILY_MINUTE, 0)
            val isBangla = intent.getBooleanExtra(ReminderScheduler.EXTRA_IS_BANGLA, true)
            ReminderScheduler.scheduleDailyReminder(context, hour, minute, isBangla)
        }
    }
}

class BootRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val appContext = context.applicationContext
                val prefs = AppPreferencesRepository(appContext).preferencesFlow.first()
                if (!prefs.notificationsEnabled) return@launch
                val isBangla = prefs.language == AppLanguage.BANGLA

                if (prefs.dailyReminderEnabled) {
                    ReminderScheduler.scheduleDailyReminder(
                        context = appContext,
                        hour = prefs.dailyReminderHour,
                        minute = prefs.dailyReminderMinute,
                        isBangla = isBangla
                    )
                }

                val db = JomaKhorochDatabase.getInstance(appContext)
                val now = System.currentTimeMillis()
                db.diaryDao().getAllTasksOnce()
                    .filter { !it.isCompleted && (it.reminderAt ?: 0L) > now }
                    .forEach { task ->
                        task.reminderAt?.let { remAt ->
                            ReminderScheduler.scheduleReminder(
                                context = appContext,
                                taskId = task.id,
                                title = task.title,
                                description = task.description,
                                triggerAtMillis = remAt
                            )
                        }
                    }

                if (prefs.loanDueAlertsEnabled) {
                    db.loanDao().getAllLoansOnce()
                        .filter { !it.isSettled && (it.dueDate ?: 0L) > now }
                        .forEach { loan ->
                            loan.dueDate?.let { due ->
                                ReminderScheduler.scheduleLoanDueReminder(
                                    context = appContext,
                                    loanId = loan.id,
                                    personName = loan.personName,
                                    isLending = loan.direction == "GIVEN",
                                    dueDateMillis = due,
                                    isBangla = isBangla
                                )
                            }
                        }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
