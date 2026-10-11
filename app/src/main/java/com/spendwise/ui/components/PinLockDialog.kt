package com.spendwise.ui.components

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.spendwise.ui.theme.SpendWiseBrandGradient
import com.spendwise.util.SecurityUtils

/**
 * Mandatory App Lock Setup screen shown during first-time onboarding right after account creation.
 */
@Composable
fun SetAppLockScreen(
    onSetupComplete: (pin: String, enableBiometric: Boolean) -> Unit
) {
    val context = LocalContext.current
    val biometricSupported = remember(context) { SecurityUtils.isBiometricAvailable(context) }

    var step by remember { mutableStateOf(1) } // 1 = Create PIN, 2 = Confirm PIN
    var firstPin by remember { mutableStateOf("") }
    var currentPin by remember { mutableStateOf("") }
    var enableBiometric by remember { mutableStateOf(biometricSupported) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(SpendWiseBrandGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Set App Lock",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (step == 1) "Set Your 4-Digit PIN" else "Confirm Your 4-Digit PIN",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (step == 1) {
                        "Protect your expenses with a secure 4-digit PIN."
                    } else {
                        "Re-enter your 4-digit PIN to confirm."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 4 PIN Dots
                PinDotsRow(
                    enteredLength = currentPin.length,
                    isError = errorMessage != null
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Spacer(modifier = Modifier.height(18.dp))
                }

                if (biometricSupported) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Fingerprint",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Enable Fingerprint Unlock",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Quickly unlock SpendWise with biometrics",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = enableBiometric,
                                onCheckedChange = { enableBiometric = it },
                                modifier = Modifier.testTag("setup_biometric_switch")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Keypad
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NumericPinKeypad(
                    onDigitClick = { digit ->
                        if (currentPin.length < 4) {
                            errorMessage = null
                            currentPin += digit
                            if (currentPin.length == 4) {
                                if (step == 1) {
                                    firstPin = currentPin
                                    currentPin = ""
                                    step = 2
                                } else {
                                    if (currentPin == firstPin) {
                                        onSetupComplete(currentPin, enableBiometric)
                                    } else {
                                        errorMessage = "PINs did not match. Please try again."
                                        firstPin = ""
                                        currentPin = ""
                                        step = 1
                                    }
                                }
                            }
                        }
                    },
                    onBackspaceClick = {
                        if (currentPin.isNotEmpty()) {
                            currentPin = currentPin.dropLast(1)
                            errorMessage = null
                        }
                    },
                    leftButton = if (step == 2) {
                        {
                            TextButton(
                                onClick = {
                                    step = 1
                                    firstPin = ""
                                    currentPin = ""
                                    errorMessage = null
                                }
                            ) {
                                Text("Reset", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else null
                )
            }
        }
    }
}

/**
 * Returning-user and background-resume App Lock Screen.
 * Supports 4-digit PIN entry, BiometricPrompt auto-prompt + fallback, and Forgot PIN recovery.
 */
@Composable
fun PinLockScreen(
    userName: String,
    userPhotoPath: String?,
    biometricEnabled: Boolean,
    securityQuestion: String,
    onVerifyPin: (String) -> Boolean,
    onVerifySecurityAnswer: (String) -> Boolean,
    onResetPin: (String) -> Unit,
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var biometricStatusMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPinDialog by remember { mutableStateOf(false) }

    val canUseBiometric = remember(context, biometricEnabled) {
        biometricEnabled && SecurityUtils.isBiometricAvailable(context)
    }

    fun triggerBiometricPrompt() {
        val activity = context as? FragmentActivity ?: return
        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock SpendWise")
            .setSubtitle(
                if (userName.isNotBlank()) "Verify identity for $userName"
                else "Authenticate to view your expenses"
            )
            .setNegativeButtonText("Use PIN")
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    biometricStatusMessage = null
                    onUnlocked()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_USER_CANCELED
                    ) {
                        biometricStatusMessage = errString.toString()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    biometricStatusMessage = "Fingerprint not recognized. Try again or enter your PIN."
                }
            }
        )
        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            biometricStatusMessage = "Use your 4-digit PIN to unlock."
        }
    }

    // Auto-prompt fingerprint when the lock screen opens
    LaunchedEffect(canUseBiometric) {
        if (canUseBiometric) {
            triggerBiometricPrompt()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                if (userName.isNotBlank() || !userPhotoPath.isNullOrBlank()) {
                    UserProfileAvatar(
                        name = userName,
                        photoPath = userPhotoPath,
                        size = 76.dp,
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        borderWidth = 2.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(SpendWiseBrandGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (userName.isNotBlank()) "Welcome back, $userName" else "SpendWise Locked",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Enter your 4-digit PIN to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                PinDotsRow(
                    enteredLength = enteredPin.length,
                    isError = isError
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isError) {
                    Text(
                        text = "Incorrect PIN. Please try again.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                } else if (biometricStatusMessage != null) {
                    Text(
                        text = biometricStatusMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NumericPinKeypad(
                    onDigitClick = { digit ->
                        if (enteredPin.length < 4) {
                            isError = false
                            biometricStatusMessage = null
                            enteredPin += digit
                            if (enteredPin.length == 4) {
                                if (onVerifyPin(enteredPin)) {
                                    onUnlocked()
                                } else {
                                    isError = true
                                    enteredPin = ""
                                }
                            }
                        }
                    },
                    onBackspaceClick = {
                        if (enteredPin.isNotEmpty()) {
                            enteredPin = enteredPin.dropLast(1)
                            isError = false
                        }
                    },
                    leftButton = if (canUseBiometric) {
                        {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .clickable { triggerBiometricPrompt() }
                                    .testTag("pin_biometric_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Unlock with Fingerprint",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    } else null
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (canUseBiometric) {
                        TextButton(
                            onClick = { triggerBiometricPrompt() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Use Fingerprint", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    TextButton(
                        onClick = { showForgotPinDialog = true },
                        modifier = Modifier.testTag("forgot_pin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockReset,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Forgot PIN?", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (showForgotPinDialog) {
        ForgotPinRecoveryDialog(
            securityQuestion = securityQuestion,
            onVerifyAnswer = onVerifySecurityAnswer,
            onDismiss = { showForgotPinDialog = false },
            onPinResetSuccess = { newPin ->
                onResetPin(newPin)
                showForgotPinDialog = false
                onUnlocked()
            }
        )
    }
}

@Composable
private fun PinDotsRow(
    enteredLength: Int,
    isError: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            val filled = index < enteredLength
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isError -> MaterialTheme.colorScheme.error
                            filled -> MaterialTheme.colorScheme.primary
                            else -> Color.Transparent
                        }
                    )
                    .border(
                        width = 2.dp,
                        color = when {
                            isError -> MaterialTheme.colorScheme.error
                            filled -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outline
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun NumericPinKeypad(
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    leftButton: (@Composable () -> Unit)? = null
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("LEFT", "0", "DEL")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    when (key) {
                        "LEFT" -> {
                            if (leftButton != null) {
                                Box(
                                    modifier = Modifier.size(72.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    leftButton()
                                }
                            } else {
                                Spacer(modifier = Modifier.size(72.dp))
                            }
                        }
                        "DEL" -> {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .clickable { onBackspaceClick() }
                                    .testTag("pin_key_backspace"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .clickable { onDigitClick(key) }
                                    .testTag("pin_key_$key"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ForgotPinRecoveryDialog(
    securityQuestion: String,
    onVerifyAnswer: (String) -> Boolean,
    onDismiss: () -> Unit,
    onPinResetSuccess: (String) -> Unit
) {
    var answerInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var isAnswerVerified by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (!isAnswerVerified) "Recover PIN" else "Create New 4-Digit PIN",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isAnswerVerified) {
                    Text(
                        text = "Answer your security question to reset your PIN:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = securityQuestion,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = answerInput,
                        onValueChange = {
                            answerInput = it
                            errorText = null
                        },
                        label = { Text("Your Answer") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recovery_answer_input")
                    )
                } else {
                    Text(
                        text = "Identity verified! Enter a new 4-digit PIN:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                                newPinInput = it
                                errorText = null
                            }
                        },
                        label = { Text("New 4-Digit PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                                confirmPinInput = it
                                errorText = null
                            }
                        },
                        label = { Text("Confirm 4-Digit PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isAnswerVerified) {
                        if (onVerifyAnswer(answerInput)) {
                            isAnswerVerified = true
                            errorText = null
                        } else {
                            errorText = "Incorrect answer. Please try again."
                        }
                    } else {
                        if (newPinInput.length != 4) {
                            errorText = "PIN must be 4 digits."
                        } else if (newPinInput != confirmPinInput) {
                            errorText = "PINs do not match."
                        } else {
                            onPinResetSuccess(newPinInput)
                        }
                    }
                }
            ) {
                Text(if (!isAnswerVerified) "Verify" else "Save PIN")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ChangePinDialog(
    onVerifyCurrentPin: (String) -> Boolean,
    onDismiss: () -> Unit,
    onPinChanged: (String) -> Unit
) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Change 4-Digit PIN",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = currentPin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                            currentPin = it
                            errorText = null
                        }
                    },
                    label = { Text("Current 4-Digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                            newPin = it
                            errorText = null
                        }
                    },
                    label = { Text("New 4-Digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                            confirmPin = it
                            errorText = null
                        }
                    },
                    label = { Text("Confirm New PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        !onVerifyCurrentPin(currentPin) -> {
                            errorText = "Current PIN is incorrect."
                        }
                        newPin.length != 4 -> {
                            errorText = "New PIN must be 4 digits."
                        }
                        newPin != confirmPin -> {
                            errorText = "New PINs do not match."
                        }
                        else -> {
                            onPinChanged(newPin)
                            onDismiss()
                        }
                    }
                }
            ) {
                Text("Update PIN")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
