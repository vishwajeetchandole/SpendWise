package com.spendwise.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.spendwise.util.Formatters
import java.util.Calendar

enum class ExportScope {
    CURRENT_MONTH,
    CUSTOM_RANGE,
    ALL_TIME
}

enum class ExportFormat {
    PDF,
    CSV,
    JSON_BACKUP,
    DIRECT_PRINT
}

@Composable
fun ExportDialog(
    currentMonthLabel: String,
    currentMonthStart: Long,
    currentMonthEnd: Long,
    onDismiss: () -> Unit,
    onExport: (format: ExportFormat, scope: ExportScope, startDate: Long, endDate: Long, scopeTitle: String) -> Unit
) {
    val context = LocalContext.current
    var selectedScope by remember { mutableStateOf(ExportScope.CURRENT_MONTH) }
    var selectedFormat by remember { mutableStateOf(ExportFormat.PDF) }

    var customStartDate by remember {
        mutableLongStateOf(
            Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -30)
            }.timeInMillis
        )
    }
    var customEndDate by remember {
        mutableLongStateOf(System.currentTimeMillis())
    }

    val startCal = Calendar.getInstance().apply { timeInMillis = customStartDate }
    val endCal = Calendar.getInstance().apply { timeInMillis = customEndDate }

    val startDatePicker = DatePickerDialog(
        context,
        { _, y, m, d ->
            val c = Calendar.getInstance().apply {
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m)
                set(Calendar.DAY_OF_MONTH, d)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
            }
            customStartDate = c.timeInMillis
        },
        startCal.get(Calendar.YEAR),
        startCal.get(Calendar.MONTH),
        startCal.get(Calendar.DAY_OF_MONTH)
    )

    val endDatePicker = DatePickerDialog(
        context,
        { _, y, m, d ->
            val c = Calendar.getInstance().apply {
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m)
                set(Calendar.DAY_OF_MONTH, d)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
            }
            customEndDate = c.timeInMillis
        },
        endCal.get(Calendar.YEAR),
        endCal.get(Calendar.MONTH),
        endCal.get(Calendar.DAY_OF_MONTH)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Export & Reports",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scope Picker
                Text(
                    text = "Select Time Range",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedScope == ExportScope.CURRENT_MONTH,
                        onClick = { selectedScope = ExportScope.CURRENT_MONTH },
                        label = { Text("This Month") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedScope == ExportScope.CUSTOM_RANGE,
                        onClick = { selectedScope = ExportScope.CUSTOM_RANGE },
                        label = { Text("Custom") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedScope == ExportScope.ALL_TIME,
                        onClick = { selectedScope = ExportScope.ALL_TIME },
                        label = { Text("All Time") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (selectedScope == ExportScope.CUSTOM_RANGE) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { startDatePicker.show() }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "From",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = Formatters.formatDate(customStartDate, "dd MMM yyyy"),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { endDatePicker.show() }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "To",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = Formatters.formatDate(customEndDate, "dd MMM yyyy"),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Format Picker
                Text(
                    text = "Select Format",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                val formats = listOf(
                    Triple(ExportFormat.PDF, "PDF Statement", Icons.Default.PictureAsPdf),
                    Triple(ExportFormat.DIRECT_PRINT, "Direct Print", Icons.Default.Print),
                    Triple(ExportFormat.CSV, "CSV Spreadsheet", Icons.Default.TableChart),
                    Triple(ExportFormat.JSON_BACKUP, "JSON Backup", Icons.Default.DataObject)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    formats.forEach { (format, label, icon) ->
                        val isSelected = selectedFormat == format
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedFormat = format }
                                .testTag("export_format_${format.name}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val (sDate, eDate, title) = when (selectedScope) {
                                ExportScope.CURRENT_MONTH -> Triple(
                                    currentMonthStart,
                                    currentMonthEnd,
                                    currentMonthLabel
                                )
                                ExportScope.CUSTOM_RANGE -> Triple(
                                    customStartDate,
                                    customEndDate,
                                    "${Formatters.formatDate(customStartDate, "dd MMM")} - ${Formatters.formatDate(customEndDate, "dd MMM yyyy")}"
                                )
                                ExportScope.ALL_TIME -> Triple(
                                    0L,
                                    Long.MAX_VALUE,
                                    "All Time Records"
                                )
                            }

                            onExport(selectedFormat, selectedScope, sDate, eDate, title)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("confirm_export_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (selectedFormat == ExportFormat.DIRECT_PRINT) "Print Document" else "Generate & Share",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
