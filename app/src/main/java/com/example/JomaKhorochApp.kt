package com.example

import android.app.Application
import android.content.Context
import com.example.data.cloud.CloudSyncRepository
import com.example.data.cloud.NetworkMonitor
import com.example.data.local.JomaKhorochDatabase
import com.example.data.preferences.AppPreferencesRepository
import com.example.data.repository.JomaKhorochRepository
import com.example.infrastructure.EncryptedBackupManager
import com.example.infrastructure.ReminderScheduler
import java.util.UUID

class AppContainer(application: Application) {
    val appContext: Context = application.applicationContext
    val database: JomaKhorochDatabase = JomaKhorochDatabase.getInstance(application)
    val repository: JomaKhorochRepository = JomaKhorochRepository(database)
    val preferences: AppPreferencesRepository = AppPreferencesRepository(application)
    val backupManager: EncryptedBackupManager = EncryptedBackupManager(database)
    val networkMonitor: NetworkMonitor = NetworkMonitor(application)
    val cloudSyncRepository: CloudSyncRepository = CloudSyncRepository(application, database)
    val deviceId: String = loadOrCreateDeviceId(application)

    private fun loadOrCreateDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences("jomakhoroch_sync_meta", Context.MODE_PRIVATE)
        val existing = prefs.getString("device_id", null)
        if (!existing.isNullOrBlank()) return existing
        val created = "android_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        prefs.edit().putString("device_id", created).apply()
        return created
    }
}

class JomaKhorochApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ReminderScheduler.ensureChannel(this)
    }
}
