package com.spendwise.util

import android.content.Context
import androidx.biometric.BiometricManager
import java.security.MessageDigest
import java.security.SecureRandom

object SecurityUtils {

    val securityQuestions = listOf(
        "What was the name of your first school?",
        "What is your favorite childhood book or movie?",
        "What city were you born in?",
        "What was the name of your first pet?",
        "What is your mother's maiden name?",
        "What is your favorite food?"
    )

    fun isBiometricAvailable(context: Context): Boolean {
        return try {
            val manager = BiometricManager.from(context)
            val result = manager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            result == BiometricManager.BIOMETRIC_SUCCESS
        } catch (_: Exception) {
            false
        }
    }


    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hashSecret(secret: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val combined = "$salt:${secret.trim()}"
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(enteredPin: String, salt: String, storedHash: String): Boolean {
        if (enteredPin.length != 4 || salt.isBlank() || storedHash.isBlank()) return false
        return hashSecret(enteredPin, salt) == storedHash
    }

    fun hashSecurityAnswer(answer: String, salt: String): String {
        return hashSecret(answer.trim().lowercase(), salt)
    }

    fun verifySecurityAnswer(enteredAnswer: String, salt: String, storedHash: String): Boolean {
        if (enteredAnswer.isBlank() || salt.isBlank() || storedHash.isBlank()) return false
        return hashSecurityAnswer(enteredAnswer, salt) == storedHash
    }
}
