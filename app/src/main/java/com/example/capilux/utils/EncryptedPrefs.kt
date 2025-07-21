
package com.example.capilux.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object EncryptedPrefs {
    private const val PREF_NAME = "secure_prefs"
    private const val KEY_PIN = "user_pin"
    private const val KEY_LAST_PINS = "last_pins"
    private const val KEY_BIOMETRICS = "use_biometric"
    private const val KEY_SETUP_DONE = "setup_done"
    private const val KEY_SECURITY_Q = "security_question"
    private const val KEY_SECURITY_A = "security_answer"
    private const val KEY_USERNAME = "username"
    private const val KEY_IMAGE_URI = "imageUri"

    fun getPrefs(context: Context) = EncryptedSharedPreferences.create(
        PREF_NAME,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun savePin(context: Context, pin: String) {
        val prefs = getPrefs(context)
        prefs.edit().putString(KEY_PIN, pin).apply()
    }

    fun getPin(context: Context): String? {
        return getPrefs(context).getString(KEY_PIN, null)
    }

    fun saveLastPins(context: Context, newPin: String) {
        val prefs = getPrefs(context)
        val oldList = getLastPins(context).toMutableList()
        oldList.add(0, newPin)
        val trimmed = oldList.distinct().take(3)
        prefs.edit().putString(KEY_LAST_PINS, trimmed.joinToString("|")).apply()
    }

    fun getLastPins(context: Context): List<String> {
        val raw = getPrefs(context).getString(KEY_LAST_PINS, "") ?: ""
        return raw.split("|").filter { it.isNotBlank() }
    }

    fun setUseBiometrics(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BIOMETRICS, enabled).apply()
    }

    fun canUseBiometrics(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BIOMETRICS, false)
    }

    fun setSetupDone(context: Context, done: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SETUP_DONE, done).apply()
    }

    fun isSetupDone(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SETUP_DONE, false)
    }

    fun setSecurityQuestion(context: Context, question: String, answer: String) {
        val prefs = getPrefs(context)
        prefs.edit().putString(KEY_SECURITY_Q, question).apply()
        prefs.edit().putString(KEY_SECURITY_A, answer.lowercase().trim()).apply()
    }

    fun getSecurityQuestion(context: Context): String {
        return getPrefs(context).getString(KEY_SECURITY_Q, "") ?: ""
    }

    fun isSecurityAnswerCorrect(context: Context, input: String): Boolean {
        val realAnswer = getPrefs(context).getString(KEY_SECURITY_A, "") ?: ""
        return input.lowercase().trim() == realAnswer
    }

    fun setUsername(context: Context, username: String) {
        getPrefs(context).edit().putString(KEY_USERNAME, username).apply()
    }

    fun getUsername(context: Context): String? {
        return getPrefs(context).getString(KEY_USERNAME, null)
    }

    fun setImageUri(context: Context, uri: String?) {
        getPrefs(context).edit().putString(KEY_IMAGE_URI, uri).apply()
    }

    fun getImageUri(context: Context): String? {
        return getPrefs(context).getString(KEY_IMAGE_URI, null)
    }

    fun clearSession(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().apply {
            remove(KEY_USERNAME)
            remove(KEY_IMAGE_URI)
            apply()
        }
    }
}
