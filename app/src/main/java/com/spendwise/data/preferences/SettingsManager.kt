package com.spendwise.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.spendwise.util.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

private val Context.spendWiseDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "spendwise_datastore"
)

data class AppSettings(
    val isLoaded: Boolean = false,
    val accountCreated: Boolean = false,
    val appLockConfigured: Boolean = false,
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val profilePhotoPath: String = "",
    val securityQuestion: String = SecurityUtils.securityQuestions.first(),
    val securityAnswerHash: String = "",
    val securitySalt: String = "",
    val pinHash: String = "",
    val pinSalt: String = "",
    val biometricEnabled: Boolean = false,
    val autoLockTimeoutMinutes: Int = 0, // 0 = Immediately, 1 = 1 min, 5 = 5 min
    val currencySymbol: String = "₹",
    val indianNumberFormat: Boolean = true,
    val themeMode: String = "system", // "system", "light", "dark"
    val hideAmounts: Boolean = false,
    val blockScreenshots: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "21:00", // Default 9:00 PM
    val saveConfirmationNotification: Boolean = true,
    val soundOnSaveEnabled: Boolean = true,
    val sampleDataLoaded: Boolean = false
) {
    val isFirstTimeUser: Boolean
        get() = !accountCreated || !appLockConfigured || pinHash.isBlank()
}

class SettingsManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val legacyPrefs = context.getSharedPreferences("spendwise_prefs", Context.MODE_PRIVATE)

    val settingsFlow: Flow<AppSettings> = context.spendWiseDataStore.data.map { prefs ->
        mapPreferences(prefs)
    }

    private val _settings = MutableStateFlow(
        AppSettings(
            isLoaded = false,
            themeMode = legacyPrefs.getString("key_theme_mode", "system") ?: "system"
        )
    )
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        scope.launch {
            migrateLegacyPrefsIfNeeded()
            context.spendWiseDataStore.data.collect { prefs ->
                _settings.value = mapPreferences(prefs).copy(isLoaded = true)
            }
        }
    }

    private suspend fun migrateLegacyPrefsIfNeeded() {
        val current = context.spendWiseDataStore.data.first()
        if (current[KEY_MIGRATED_FROM_PREFS] == true) return

        val legacyName = legacyPrefs.getString("key_user_name", "") ?: ""
        val legacyPhoto = legacyPrefs.getString("key_profile_photo", "") ?: ""
        val legacyCurrency = legacyPrefs.getString("key_currency", "₹") ?: "₹"
        val legacyTheme = legacyPrefs.getString("key_theme_mode", "system") ?: "system"
        val legacyIndian = legacyPrefs.getBoolean("key_indian_format", true)
        val legacyPin = legacyPrefs.getString("key_pin_code", "") ?: ""
        val legacySampleLoaded = legacyPrefs.getBoolean("key_sample_data_loaded", false)

        context.spendWiseDataStore.edit { prefs ->
            prefs[KEY_MIGRATED_FROM_PREFS] = true
            if (legacyName.isNotBlank() && legacyName != "Alex" && prefs[KEY_USER_NAME].isNullOrBlank()) {
                prefs[KEY_USER_NAME] = legacyName
            }
            if (legacyPhoto.isNotBlank() && prefs[KEY_PROFILE_PHOTO].isNullOrBlank()) {
                prefs[KEY_PROFILE_PHOTO] = legacyPhoto
            }
            if (prefs[KEY_CURRENCY] == null) {
                prefs[KEY_CURRENCY] = legacyCurrency
            }
            if (prefs[KEY_THEME_MODE] == null) {
                prefs[KEY_THEME_MODE] = legacyTheme
            }
            if (prefs[KEY_INDIAN_FORMAT] == null) {
                prefs[KEY_INDIAN_FORMAT] = legacyIndian
            }
            if (prefs[KEY_SAMPLE_DATA_LOADED] == null) {
                prefs[KEY_SAMPLE_DATA_LOADED] = legacySampleLoaded
            }
            if (legacyPin.length == 4 && prefs[KEY_PIN_HASH].isNullOrBlank()) {
                val salt = SecurityUtils.generateSalt()
                prefs[KEY_PIN_SALT] = salt
                prefs[KEY_PIN_HASH] = SecurityUtils.hashSecret(legacyPin, salt)
                prefs[KEY_APP_LOCK_CONFIGURED] = true
            }
        }
    }

    private fun mapPreferences(prefs: Preferences): AppSettings {
        val name = prefs[KEY_USER_NAME] ?: ""
        val email = prefs[KEY_USER_EMAIL] ?: ""
        val phone = prefs[KEY_USER_PHONE] ?: ""
        val photo = prefs[KEY_PROFILE_PHOTO] ?: ""
        val accountCreated = prefs[KEY_ACCOUNT_CREATED] ?: false
        val appLockConfigured = prefs[KEY_APP_LOCK_CONFIGURED] ?: false
        val secQuestion = prefs[KEY_SECURITY_QUESTION] ?: SecurityUtils.securityQuestions.first()
        val secAnswerHash = prefs[KEY_SECURITY_ANSWER_HASH] ?: ""
        val secSalt = prefs[KEY_SECURITY_SALT] ?: ""
        val pinHash = prefs[KEY_PIN_HASH] ?: ""
        val pinSalt = prefs[KEY_PIN_SALT] ?: ""
        val biometric = prefs[KEY_BIOMETRIC_ENABLED] ?: false
        val timeout = prefs[KEY_AUTO_LOCK_TIMEOUT] ?: 0
        val currency = prefs[KEY_CURRENCY] ?: "₹"
        val indianFormat = prefs[KEY_INDIAN_FORMAT] ?: (currency == "₹")
        val theme = prefs[KEY_THEME_MODE] ?: "system"
        val hideAmounts = prefs[KEY_HIDE_AMOUNTS] ?: false
        val blockScreenshots = prefs[KEY_BLOCK_SCREENSHOTS] ?: false
        val reminderEnabled = prefs[KEY_REMINDER_ENABLED] ?: false
        val reminderTime = prefs[KEY_REMINDER_TIME] ?: "21:00"
        val saveNotification = prefs[KEY_SAVE_NOTIFICATION] ?: true
        val soundEnabled = prefs[KEY_SOUND_ENABLED] ?: true
        val sampleLoaded = prefs[KEY_SAMPLE_DATA_LOADED] ?: false

        return AppSettings(
            isLoaded = true,
            accountCreated = accountCreated,
            appLockConfigured = appLockConfigured,
            userName = name,
            userEmail = email,
            userPhone = phone,
            profilePhotoPath = photo,
            securityQuestion = secQuestion,
            securityAnswerHash = secAnswerHash,
            securitySalt = secSalt,
            pinHash = pinHash,
            pinSalt = pinSalt,
            biometricEnabled = biometric,
            autoLockTimeoutMinutes = timeout,
            currencySymbol = currency,
            indianNumberFormat = indianFormat,
            themeMode = theme,
            hideAmounts = hideAmounts,
            blockScreenshots = blockScreenshots,
            reminderEnabled = reminderEnabled,
            reminderTime = reminderTime,
            saveConfirmationNotification = saveNotification,
            soundOnSaveEnabled = soundEnabled,
            sampleDataLoaded = sampleLoaded
        )
    }

    fun createAccount(
        fullName: String,
        email: String,
        phone: String,
        photoPath: String?,
        securityQuestion: String,
        securityAnswer: String
    ) {
        val cleanName = fullName.trim()
        val salt = SecurityUtils.generateSalt()
        val answerHash = SecurityUtils.hashSecurityAnswer(securityAnswer, salt)

        // Optimistic state update for responsive navigation
        _settings.value = _settings.value.copy(
            accountCreated = true,
            userName = cleanName,
            userEmail = email.trim(),
            userPhone = phone.trim(),
            profilePhotoPath = photoPath ?: _settings.value.profilePhotoPath,
            securityQuestion = securityQuestion,
            securityAnswerHash = answerHash,
            securitySalt = salt
        )

        scope.launch {
            context.spendWiseDataStore.edit { prefs ->
                prefs[KEY_ACCOUNT_CREATED] = true
                prefs[KEY_USER_NAME] = cleanName
                prefs[KEY_USER_EMAIL] = email.trim()
                prefs[KEY_USER_PHONE] = phone.trim()
                if (photoPath != null) {
                    prefs[KEY_PROFILE_PHOTO] = photoPath
                }
                prefs[KEY_SECURITY_QUESTION] = securityQuestion
                prefs[KEY_SECURITY_SALT] = salt
                prefs[KEY_SECURITY_ANSWER_HASH] = answerHash
            }
        }
    }

    fun setupAppLock(pin: String, enableBiometric: Boolean) {
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashSecret(pin, salt)

        _settings.value = _settings.value.copy(
            appLockConfigured = true,
            pinSalt = salt,
            pinHash = hash,
            biometricEnabled = enableBiometric
        )

        scope.launch {
            context.spendWiseDataStore.edit { prefs ->
                prefs[KEY_APP_LOCK_CONFIGURED] = true
                prefs[KEY_PIN_SALT] = salt
                prefs[KEY_PIN_HASH] = hash
                prefs[KEY_BIOMETRIC_ENABLED] = enableBiometric
            }
        }
    }

    fun updatePin(newPin: String) {
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashSecret(newPin, salt)

        _settings.value = _settings.value.copy(
            appLockConfigured = true,
            pinSalt = salt,
            pinHash = hash
        )

        scope.launch {
            context.spendWiseDataStore.edit { prefs ->
                prefs[KEY_APP_LOCK_CONFIGURED] = true
                prefs[KEY_PIN_SALT] = salt
                prefs[KEY_PIN_HASH] = hash
            }
        }
    }

    fun verifyPin(enteredPin: String): Boolean {
        val current = _settings.value
        return SecurityUtils.verifyPin(enteredPin, current.pinSalt, current.pinHash)
    }

    fun verifySecurityAnswer(enteredAnswer: String): Boolean {
        val current = _settings.value
        return SecurityUtils.verifySecurityAnswer(
            enteredAnswer,
            current.securitySalt,
            current.securityAnswerHash
        )
    }

    fun updateProfile(
        fullName: String,
        email: String,
        phone: String,
        photoPath: String?,
        securityQuestion: String? = null,
        securityAnswer: String? = null
    ) {
        val cleanName = fullName.trim()
        val current = _settings.value
        var newSalt = current.securitySalt
        var newHash = current.securityAnswerHash
        val newQuestion = securityQuestion ?: current.securityQuestion

        if (!securityAnswer.isNullOrBlank()) {
            newSalt = SecurityUtils.generateSalt()
            newHash = SecurityUtils.hashSecurityAnswer(securityAnswer, newSalt)
        }

        _settings.value = current.copy(
            userName = cleanName,
            userEmail = email.trim(),
            userPhone = phone.trim(),
            profilePhotoPath = photoPath ?: current.profilePhotoPath,
            securityQuestion = newQuestion,
            securitySalt = newSalt,
            securityAnswerHash = newHash
        )

        scope.launch {
            context.spendWiseDataStore.edit { prefs ->
                prefs[KEY_USER_NAME] = cleanName
                prefs[KEY_USER_EMAIL] = email.trim()
                prefs[KEY_USER_PHONE] = phone.trim()
                if (photoPath != null) {
                    prefs[KEY_PROFILE_PHOTO] = photoPath
                }
                prefs[KEY_SECURITY_QUESTION] = newQuestion
                prefs[KEY_SECURITY_SALT] = newSalt
                prefs[KEY_SECURITY_ANSWER_HASH] = newHash
            }
        }
    }

    fun removeProfilePhoto() {
        val currentPath = _settings.value.profilePhotoPath
        if (currentPath.isNotBlank()) {
            try {
                val file = File(currentPath)
                if (file.exists()) file.delete()
            } catch (_: Exception) {
            }
        }
        _settings.value = _settings.value.copy(profilePhotoPath = "")
        scope.launch {
            context.spendWiseDataStore.edit { prefs ->
                prefs.remove(KEY_PROFILE_PHOTO)
            }
        }
    }

    fun updateBiometricEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(biometricEnabled = enabled)
        scope.launch {
            context.spendWiseDataStore.edit { prefs ->
                prefs[KEY_BIOMETRIC_ENABLED] = enabled
            }
        }
    }

    fun updateAutoLockTimeout(minutes: Int) {
        _settings.value = _settings.value.copy(autoLockTimeoutMinutes = minutes)
        scope.launch {
            context.spendWiseDataStore.edit { prefs ->
                prefs[KEY_AUTO_LOCK_TIMEOUT] = minutes
            }
        }
    }

    fun updateCurrency(currency: String) {
        val indianFormat = currency == "₹"
        _settings.value = _settings.value.copy(
            currencySymbol = currency,
            indianNumberFormat = indianFormat
        )
        scope.launch {
            context.spendWiseDataStore.edit { prefs ->
                prefs[KEY_CURRENCY] = currency
                prefs[KEY_INDIAN_FORMAT] = indianFormat
            }
        }
    }

    fun updateIndianNumberFormat(enabled: Boolean) {
        _settings.value = _settings.value.copy(indianNumberFormat = enabled)
        scope.launch {
            context.spendWiseDataStore.edit { prefs ->
                prefs[KEY_INDIAN_FORMAT] = enabled
            }
        }
    }

    fun updateThemeMode(themeMode: String) {
        _settings.value = _settings.value.copy(themeMode = themeMode)
        legacyPrefs.edit().putString("key_theme_mode", themeMode).apply()
        scope.launch {
            context.spendWiseDataStore.edit { prefs ->
                prefs[KEY_THEME_MODE] = themeMode
            }
        }
    }

    fun updateHideAmounts(hide: Boolean) {
        _settings.value = _settings.value.copy(hideAmounts = hide)
        scope.launch {
            context.spendWiseDataStore.edit { prefs ->
                prefs[KEY_HIDE_AMOUNTS] = hide
            }
        }
    }

    fun updateBlockScreenshots(block: Boolean) {
        _settings.value = _settings.value.copy(blockScreenshots = block)
        scope.launch {
            context.spendWiseDataStore.edit { prefs ->
                prefs[KEY_BLOCK_SCREENSHOTS] = block
            }
        }
    }

    fun updateReminder(enabled: Boolean, time: String) {
        _settings.value = _settings.value.copy(reminderEnabled = enabled, reminderTime = time)
        scope.launch {
            context.spendWiseDataStore.edit { prefs ->
                prefs[KEY_REMINDER_ENABLED] = enabled
                prefs[KEY_REMINDER_TIME] = time
            }
        }
    }

    fun updateSaveConfirmationNotification(enabled: Boolean) {
        _settings.value = _settings.value.copy(saveConfirmationNotification = enabled)
        scope.launch {
            context.spendWiseDataStore.edit { prefs ->
                prefs[KEY_SAVE_NOTIFICATION] = enabled
            }
        }
    }

    fun updateSoundOnSave(enabled: Boolean) {
        _settings.value = _settings.value.copy(soundOnSaveEnabled = enabled)
        scope.launch {
            context.spendWiseDataStore.edit { prefs ->
                prefs[KEY_SOUND_ENABLED] = enabled
            }
        }
    }

    fun setSampleDataLoaded(loaded: Boolean) {
        _settings.value = _settings.value.copy(sampleDataLoaded = loaded)
        scope.launch {
            context.spendWiseDataStore.edit { prefs ->
                prefs[KEY_SAMPLE_DATA_LOADED] = loaded
            }
        }
    }

    companion object {
        private val KEY_MIGRATED_FROM_PREFS = booleanPreferencesKey("migrated_from_prefs")
        private val KEY_ACCOUNT_CREATED = booleanPreferencesKey("account_created")
        private val KEY_APP_LOCK_CONFIGURED = booleanPreferencesKey("app_lock_configured")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_PHONE = stringPreferencesKey("user_phone")
        private val KEY_PROFILE_PHOTO = stringPreferencesKey("profile_photo")
        private val KEY_SECURITY_QUESTION = stringPreferencesKey("security_question")
        private val KEY_SECURITY_ANSWER_HASH = stringPreferencesKey("security_answer_hash")
        private val KEY_SECURITY_SALT = stringPreferencesKey("security_salt")
        private val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
        private val KEY_PIN_SALT = stringPreferencesKey("pin_salt")
        private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        private val KEY_AUTO_LOCK_TIMEOUT = intPreferencesKey("auto_lock_timeout")
        private val KEY_CURRENCY = stringPreferencesKey("currency")
        private val KEY_INDIAN_FORMAT = booleanPreferencesKey("indian_format")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_HIDE_AMOUNTS = booleanPreferencesKey("hide_amounts")
        private val KEY_BLOCK_SCREENSHOTS = booleanPreferencesKey("block_screenshots")
        private val KEY_REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        private val KEY_REMINDER_TIME = stringPreferencesKey("reminder_time")
        private val KEY_SAVE_NOTIFICATION = booleanPreferencesKey("save_notification")
        private val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        private val KEY_SAMPLE_DATA_LOADED = booleanPreferencesKey("sample_data_loaded")
    }
}
