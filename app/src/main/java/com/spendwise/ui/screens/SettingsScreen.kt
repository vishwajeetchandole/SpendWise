package com.spendwise.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ScreenshotMonitor
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.spendwise.R
import com.spendwise.data.preferences.AppSettings
import com.spendwise.ui.components.ChangePinDialog
import com.spendwise.ui.components.CreatorFooterText
import com.spendwise.ui.components.UserProfileAvatar
import com.spendwise.util.SecurityUtils
import com.spendwise.util.SoundAndNotificationHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onOpenEditProfile: () -> Unit,
    onVerifyPin: (String) -> Boolean,
    onChangePin: (String) -> Unit,
    onUpdateBiometric: (Boolean) -> Unit,
    onUpdateAutoLockTimeout: (Int) -> Unit,
    onUpdateHideAmounts: (Boolean) -> Unit,
    onUpdateBlockScreenshots: (Boolean) -> Unit,
    onUpdateReminder: (Boolean, String) -> Unit,
    onUpdateSaveConfirmationNotification: (Boolean) -> Unit,
    onUpdateSoundOnSave: (Boolean) -> Unit,
    onUpdateCurrency: (String) -> Unit,
    onUpdateIndianFormat: (Boolean) -> Unit,
    onUpdateTheme: (String) -> Unit,
    onOpenCategoryManager: () -> Unit,
    onOpenExportDialog: () -> Unit,
    onRestoreJson: (String) -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val biometricSupported = remember(context) { SecurityUtils.isBiometricAvailable(context) }

    var showChangePinDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showNotificationRationaleDialog by remember { mutableStateOf(false) }
    var showNotificationDeniedDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onUpdateReminder(true, settings.reminderTime)
        } else {
            showNotificationDeniedDialog = true
            onUpdateReminder(false, settings.reminderTime)
        }
    }

    val restoreJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (!content.isNullOrBlank()) {
                    onRestoreJson(content)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun requestOrEnableReminder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPerm) {
                onUpdateReminder(true, settings.reminderTime)
            } else {
                showNotificationRationaleDialog = true
            }
        } else {
            onUpdateReminder(true, settings.reminderTime)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. PROFILE SECTION
        item(key = "section_profile") {
            SettingsSectionCard(
                sectionTitle = "Profile",
                sectionIcon = Icons.Default.Person
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenEditProfile() }
                        .padding(vertical = 6.dp)
                        .testTag("settings_edit_profile_row"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserProfileAvatar(
                        name = settings.userName.ifBlank { "User" },
                        photoPath = settings.profilePhotoPath,
                        size = 56.dp,
                        fontSize = 20.sp,
                        borderColor = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = settings.userName.ifBlank { "SpendWise User" },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val subInfo = listOfNotNull(
                            settings.userEmail.takeIf { it.isNotBlank() },
                            settings.userPhone.takeIf { it.isNotBlank() }
                        ).joinToString(" • ").ifBlank { "Tap to view or edit your profile & photo" }
                        Text(
                            text = subInfo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. SECURITY & PRIVACY SECTION
        item(key = "section_security") {
            SettingsSectionCard(
                sectionTitle = "Security & Privacy",
                sectionIcon = Icons.Default.Security
            ) {
                // Change 4-Digit PIN
                SettingsActionRow(
                    icon = Icons.Default.Lock,
                    title = "Change 4-Digit PIN",
                    subtitle = "Update your app lock PIN code",
                    onClick = { showChangePinDialog = true },
                    testTag = "settings_change_pin_row"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Fingerprint / Biometric Unlock
                SettingsSwitchRow(
                    icon = Icons.Default.Fingerprint,
                    title = "Fingerprint / Biometric Unlock",
                    subtitle = if (biometricSupported) {
                        "Use fingerprint or biometric sensor to unlock"
                    } else {
                        "Biometric hardware not available on this device"
                    },
                    checked = settings.biometricEnabled && biometricSupported,
                    enabled = biometricSupported,
                    onCheckedChange = { onUpdateBiometric(it) },
                    testTag = "settings_biometric_switch"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Auto-Lock Timeout
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Lock Timeout",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Lock the app when returning from the background",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val options = listOf(
                            0 to "Immediately",
                            1 to "1 min",
                            5 to "5 min"
                        )
                        options.forEach { (minutes, label) ->
                            FilterChip(
                                selected = settings.autoLockTimeoutMinutes == minutes,
                                onClick = { onUpdateAutoLockTimeout(minutes) },
                                label = {
                                    Text(
                                        text = label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag("timeout_chip_$minutes")
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Privacy: Hide Amounts
                SettingsSwitchRow(
                    icon = Icons.Default.VisibilityOff,
                    title = "Hide Expense Amounts",
                    subtitle = "Mask currency amounts across screens with ••••",
                    checked = settings.hideAmounts,
                    onCheckedChange = onUpdateHideAmounts,
                    testTag = "settings_hide_amounts_switch"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Privacy: Block Screenshots (FLAG_SECURE)
                SettingsSwitchRow(
                    icon = Icons.Default.ScreenshotMonitor,
                    title = "Block Screenshots",
                    subtitle = "Prevent screenshots and screen recording (FLAG_SECURE)",
                    checked = settings.blockScreenshots,
                    onCheckedChange = onUpdateBlockScreenshots,
                    testTag = "settings_block_screenshots_switch"
                )
            }
        }

        // 3. NOTIFICATIONS & SOUND SECTION
        item(key = "section_notifications") {
            SettingsSectionCard(
                sectionTitle = "Notifications & Sound",
                sectionIcon = Icons.Default.Notifications
            ) {
                // Daily Reminder Notification Toggle
                SettingsSwitchRow(
                    icon = Icons.Default.NotificationsActive,
                    title = "Daily Expense Reminder",
                    subtitle = "\"Did you add today's expenses?\"",
                    checked = settings.reminderEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            requestOrEnableReminder()
                        } else {
                            onUpdateReminder(false, settings.reminderTime)
                        }
                    },
                    testTag = "settings_daily_reminder_switch"
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Reminder Time Selector
                Surface(
                    onClick = { showTimePickerDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_reminder_time_row")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Reminder Time",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Reminder Time",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tap to choose daily reminder time",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = SoundAndNotificationHelper.formatTime12Hour(settings.reminderTime),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Confirmation Notification on Save
                SettingsSwitchRow(
                    icon = Icons.Default.Notifications,
                    title = "Save Confirmation Notification",
                    subtitle = "Show a quick confirmation when an expense is saved",
                    checked = settings.saveConfirmationNotification,
                    onCheckedChange = onUpdateSaveConfirmationNotification,
                    testTag = "settings_save_notification_switch"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Sound on Save
                SettingsSwitchRow(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    title = "Sound When Expense Saved",
                    subtitle = "Play a pleasant chime when an expense is added",
                    checked = settings.soundOnSaveEnabled,
                    onCheckedChange = onUpdateSoundOnSave,
                    testTag = "settings_sound_switch"
                )
            }
        }

        // 4. APPEARANCE & CURRENCY SECTION
        item(key = "section_appearance") {
            SettingsSectionCard(
                sectionTitle = "Appearance",
                sectionIcon = Icons.Default.DarkMode
            ) {
                // Theme selector
                Text(
                    text = "App Theme",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themes = listOf(
                        "system" to "Follow System",
                        "light" to "Light",
                        "dark" to "Dark"
                    )
                    themes.forEach { (key, label) ->
                        FilterChip(
                            selected = settings.themeMode == key,
                            onClick = { onUpdateTheme(key) },
                            label = {
                                Text(
                                    text = label,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier.testTag("theme_chip_$key")
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Currency Symbol Selection
                Text(
                    text = "Currency Symbol",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val currencies = listOf("₹", "$", "€", "£", "¥", "AED")
                    currencies.forEach { symbol ->
                        FilterChip(
                            selected = settings.currencySymbol == symbol,
                            onClick = { onUpdateCurrency(symbol) },
                            label = { Text(symbol, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("currency_chip_$symbol")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                SettingsSwitchRow(
                    icon = Icons.Default.CurrencyRupee,
                    title = "Indian Number Formatting",
                    subtitle = "Format large amounts as 1,00,000 instead of 100,000",
                    checked = settings.indianNumberFormat,
                    onCheckedChange = onUpdateIndianFormat,
                    testTag = "settings_indian_format_switch"
                )
            }
        }

        // 5. DATA SECTION (Categories, Backup, Export, Clear All Data)
        item(key = "section_data") {
            SettingsSectionCard(
                sectionTitle = "Data",
                sectionIcon = Icons.Default.Storage
            ) {
                SettingsActionRow(
                    icon = Icons.Default.Category,
                    title = "Manage Categories",
                    subtitle = "Add, rename, recolor, or delete custom categories",
                    onClick = onOpenCategoryManager,
                    testTag = "settings_manage_categories_row"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                SettingsActionRow(
                    icon = Icons.Default.FileDownload,
                    title = "Export & Backup Data",
                    subtitle = "Printable PDF report, CSV spreadsheet, or JSON backup",
                    onClick = onOpenExportDialog,
                    testTag = "settings_export_row"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                SettingsActionRow(
                    icon = Icons.Default.FileUpload,
                    title = "Restore from JSON Backup",
                    subtitle = "Import a previously exported SpendWise JSON file",
                    onClick = { restoreJsonLauncher.launch("application/json") },
                    testTag = "settings_restore_json_row"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                SettingsActionRow(
                    icon = Icons.Default.DeleteForever,
                    title = "Clear All Expenses",
                    subtitle = "Permanently delete all recorded expenses",
                    isDestructive = true,
                    onClick = { showClearConfirmDialog = true },
                    testTag = "settings_clear_data_row"
                )
            }
        }

        // 6. ABOUT SECTION (App version only)
        item(key = "section_about") {
            SettingsSectionCard(
                sectionTitle = "About",
                sectionIcon = Icons.Default.Info
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Application Version",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.app_version_label),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Creator Text at the very bottom of the Settings screen ONLY
        item(key = "creator_footer") {
            CreatorFooterText()
        }
    }

    if (showChangePinDialog) {
        ChangePinDialog(
            onVerifyCurrentPin = onVerifyPin,
            onDismiss = { showChangePinDialog = false },
            onPinChanged = onChangePin
        )
    }

    if (showTimePickerDialog) {
        val (initHour, initMinute) = SoundAndNotificationHelper.parseTime(settings.reminderTime)
        val timePickerState = rememberTimePickerState(
            initialHour = initHour,
            initialMinute = initMinute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            title = {
                Text(
                    text = "Select Daily Reminder Time",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val formatted = String.format(
                            "%02d:%02d",
                            timePickerState.hour,
                            timePickerState.minute
                        )
                        showTimePickerDialog = false
                        onUpdateReminder(settings.reminderEnabled, formatted)
                    }
                ) {
                    Text("Save Time")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showNotificationRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationRationaleDialog = false },
            title = {
                Text(
                    text = "Allow Expense Reminders?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "SpendWise uses notifications to send your daily \"Did you add today's expenses?\" reminder at ${
                        SoundAndNotificationHelper.formatTime12Hour(settings.reminderTime)
                    } and optional save confirmations.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNotificationRationaleDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onUpdateReminder(true, settings.reminderTime)
                        }
                    }
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showNotificationRationaleDialog = false }) {
                    Text("Not Now")
                }
            }
        )
    }

    if (showNotificationDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDeniedDialog = false },
            title = {
                Text(
                    text = "Notifications Turned Off",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Notification permission was denied. You can still use all SpendWise features normally, or enable notifications anytime in Android system settings.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = { showNotificationDeniedDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = "Clear All Expenses?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "This will permanently delete all recorded expenses from your device. Consider exporting a JSON backup first.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearAllData()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("confirm_clear_all_button")
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    sectionTitle: String,
    sectionIcon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = sectionIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = sectionTitle,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit,
    testTag: String
) {
    val tintColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val titleColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(tintColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
