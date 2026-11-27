package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.localization.AppLanguage
import com.example.core.localization.NumeralStyle
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.jomaKhorochDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "jomakhoroch_prefs"
)

data class AppPreferences(
    val activeNotebookId: Long = 1L,
    val language: AppLanguage = AppLanguage.BANGLA,
    val numeralStyle: NumeralStyle = NumeralStyle.BANGLA,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val pinHash: String = "",
    val pinSalt: String = "",
    val biometricEnabled: Boolean = false,
    val hideBalance: Boolean = false,
    val welcomeBannerDismissed: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val budgetAlertsEnabled: Boolean = true,
    val loanDueAlertsEnabled: Boolean = true,
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderHour: Int = 21,
    val dailyReminderMinute: Int = 0,
    val updateAlertsEnabled: Boolean = true,
    val updateRepoOverride: String = "",
    val lastNotifiedUpdateVersion: String = "",
    val dismissedUpdateVersion: String = "",
    val profileNameOverride: String = "",
    val profileBusinessName: String = "",
    val profilePhotoPath: String = ""
) {
    val isPinEnabled: Boolean get() = pinHash.isNotEmpty() && pinSalt.isNotEmpty()
}

class AppPreferencesRepository(private val context: Context) {
    private object Keys {
        val ACTIVE_NOTEBOOK_ID = longPreferencesKey("active_notebook_id")
        val LANGUAGE = stringPreferencesKey("app_language")
        val NUMERAL_STYLE = stringPreferencesKey("numeral_style")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val HIDE_BALANCE = booleanPreferencesKey("hide_balance")
        val WELCOME_DISMISSED = booleanPreferencesKey("welcome_dismissed")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val BUDGET_ALERTS_ENABLED = booleanPreferencesKey("budget_alerts_enabled")
        val LOAN_DUE_ALERTS_ENABLED = booleanPreferencesKey("loan_due_alerts_enabled")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val DAILY_REMINDER_HOUR = longPreferencesKey("daily_reminder_hour")
        val DAILY_REMINDER_MINUTE = longPreferencesKey("daily_reminder_minute")
        val UPDATE_ALERTS_ENABLED = booleanPreferencesKey("update_alerts_enabled")
        val UPDATE_REPO_OVERRIDE = stringPreferencesKey("update_repo_override")
        val LAST_NOTIFIED_UPDATE_VERSION = stringPreferencesKey("last_notified_update_version")
        val DISMISSED_UPDATE_VERSION = stringPreferencesKey("dismissed_update_version")
        val PROFILE_NAME_OVERRIDE = stringPreferencesKey("profile_name_override")
        val PROFILE_BUSINESS_NAME = stringPreferencesKey("profile_business_name")
        val PROFILE_PHOTO_PATH = stringPreferencesKey("profile_photo_path")
    }

    val preferencesFlow: Flow<AppPreferences> = context.jomaKhorochDataStore.data.map { prefs ->
        AppPreferences(
            activeNotebookId = prefs[Keys.ACTIVE_NOTEBOOK_ID] ?: 1L,
            language = prefs[Keys.LANGUAGE]?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() }
                ?: AppLanguage.BANGLA,
            numeralStyle = prefs[Keys.NUMERAL_STYLE]?.let { runCatching { NumeralStyle.valueOf(it) }.getOrNull() }
                ?: NumeralStyle.BANGLA,
            themeMode = prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            pinHash = prefs[Keys.PIN_HASH] ?: "",
            pinSalt = prefs[Keys.PIN_SALT] ?: "",
            biometricEnabled = prefs[Keys.BIOMETRIC_ENABLED] ?: false,
            hideBalance = prefs[Keys.HIDE_BALANCE] ?: false,
            welcomeBannerDismissed = prefs[Keys.WELCOME_DISMISSED] ?: false,
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            budgetAlertsEnabled = prefs[Keys.BUDGET_ALERTS_ENABLED] ?: true,
            loanDueAlertsEnabled = prefs[Keys.LOAN_DUE_ALERTS_ENABLED] ?: true,
            dailyReminderEnabled = prefs[Keys.DAILY_REMINDER_ENABLED] ?: false,
            dailyReminderHour = (prefs[Keys.DAILY_REMINDER_HOUR] ?: 21L).toInt().coerceIn(0, 23),
            dailyReminderMinute = (prefs[Keys.DAILY_REMINDER_MINUTE] ?: 0L).toInt().coerceIn(0, 59),
            updateAlertsEnabled = prefs[Keys.UPDATE_ALERTS_ENABLED] ?: true,
            updateRepoOverride = prefs[Keys.UPDATE_REPO_OVERRIDE] ?: "",
            lastNotifiedUpdateVersion = prefs[Keys.LAST_NOTIFIED_UPDATE_VERSION] ?: "",
            dismissedUpdateVersion = prefs[Keys.DISMISSED_UPDATE_VERSION] ?: "",
            profileNameOverride = prefs[Keys.PROFILE_NAME_OVERRIDE] ?: "",
            profileBusinessName = prefs[Keys.PROFILE_BUSINESS_NAME] ?: "",
            profilePhotoPath = prefs[Keys.PROFILE_PHOTO_PATH] ?: ""
        )
    }

    suspend fun setActiveNotebookId(id: Long) {
        context.jomaKhorochDataStore.edit { it[Keys.ACTIVE_NOTEBOOK_ID] = id }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.jomaKhorochDataStore.edit {
            it[Keys.LANGUAGE] = language.name
            it[Keys.NUMERAL_STYLE] =
                if (language == AppLanguage.BANGLA) NumeralStyle.BANGLA.name else NumeralStyle.ENGLISH.name
        }
    }

    suspend fun setNumeralStyle(style: NumeralStyle) {
        context.jomaKhorochDataStore.edit { it[Keys.NUMERAL_STYLE] = style.name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.jomaKhorochDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun savePinCredentials(saltBase64: String, hashBase64: String) {
        context.jomaKhorochDataStore.edit {
            it[Keys.PIN_SALT] = saltBase64
            it[Keys.PIN_HASH] = hashBase64
        }
    }

    suspend fun clearPinCredentials() {
        context.jomaKhorochDataStore.edit {
            it.remove(Keys.PIN_SALT)
            it.remove(Keys.PIN_HASH)
            it[Keys.BIOMETRIC_ENABLED] = false
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.jomaKhorochDataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setHideBalance(hide: Boolean) {
        context.jomaKhorochDataStore.edit { it[Keys.HIDE_BALANCE] = hide }
    }

    suspend fun dismissWelcomeBanner() {
        context.jomaKhorochDataStore.edit { it[Keys.WELCOME_DISMISSED] = true }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.jomaKhorochDataStore.edit {
            it[Keys.ONBOARDING_COMPLETED] = completed
            if (completed) {
                it[Keys.WELCOME_DISMISSED] = true
            }
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.jomaKhorochDataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setBudgetAlertsEnabled(enabled: Boolean) {
        context.jomaKhorochDataStore.edit { it[Keys.BUDGET_ALERTS_ENABLED] = enabled }
    }

    suspend fun setLoanDueAlertsEnabled(enabled: Boolean) {
        context.jomaKhorochDataStore.edit { it[Keys.LOAN_DUE_ALERTS_ENABLED] = enabled }
    }

    suspend fun setDailyReminder(enabled: Boolean, hour: Int? = null, minute: Int? = null) {
        context.jomaKhorochDataStore.edit {
            it[Keys.DAILY_REMINDER_ENABLED] = enabled
            if (hour != null) it[Keys.DAILY_REMINDER_HOUR] = hour.coerceIn(0, 23).toLong()
            if (minute != null) it[Keys.DAILY_REMINDER_MINUTE] = minute.coerceIn(0, 59).toLong()
        }
    }

    suspend fun setUpdateAlertsEnabled(enabled: Boolean) {
        context.jomaKhorochDataStore.edit { it[Keys.UPDATE_ALERTS_ENABLED] = enabled }
    }

    suspend fun setUpdateRepoOverride(repoSlug: String) {
        context.jomaKhorochDataStore.edit { it[Keys.UPDATE_REPO_OVERRIDE] = repoSlug }
    }

    suspend fun setLastNotifiedUpdateVersion(version: String) {
        context.jomaKhorochDataStore.edit { it[Keys.LAST_NOTIFIED_UPDATE_VERSION] = version }
    }

    suspend fun setDismissedUpdateVersion(version: String) {
        context.jomaKhorochDataStore.edit { it[Keys.DISMISSED_UPDATE_VERSION] = version }
    }

    suspend fun setProfileDetails(displayName: String, businessName: String) {
        context.jomaKhorochDataStore.edit {
            it[Keys.PROFILE_NAME_OVERRIDE] = displayName.trim()
            it[Keys.PROFILE_BUSINESS_NAME] = businessName.trim()
        }
    }

    suspend fun setProfilePhotoPath(path: String) {
        context.jomaKhorochDataStore.edit {
            it[Keys.PROFILE_PHOTO_PATH] = path.trim()
        }
    }
}
