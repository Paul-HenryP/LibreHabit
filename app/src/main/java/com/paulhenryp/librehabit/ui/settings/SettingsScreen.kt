package com.paulhenryp.librehabit.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.paulhenryp.librehabit.R
import com.paulhenryp.librehabit.UnitSystem
import com.paulhenryp.librehabit.model.AppTheme
import com.paulhenryp.librehabit.model.DarkModePreference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    appTheme: AppTheme,
    onAppThemeChange: (AppTheme) -> Unit,
    darkModePreference: DarkModePreference,
    onDarkModePreferenceChange: (DarkModePreference) -> Unit,
    unitSystem: UnitSystem,
    onUnitSystemChange: (UnitSystem) -> Unit,
    height: Float,
    onHeightChange: (Float) -> Unit,
    targetWeight: Float,
    onTargetWeightChange: (Float) -> Unit,
    updateState: UpdateState,
    onCheckForUpdates: () -> Unit,
    onResetUpdateState: () -> Unit,
    appVersion: String,
    onExportData: (Uri) -> Unit,
    onImportData: (Uri) -> Unit,
    onDeleteAllData: () -> Unit,
    onNavigateUp: () -> Unit
) {
    var heightInput by remember(height) { mutableStateOf(if (height > 0) height.toString() else "") }
    var targetWeightInput by remember(targetWeight) { mutableStateOf(if (targetWeight > 0) targetWeight.toString() else "") }

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            onExportData(it)
            Toast.makeText(context, context.getString(R.string.toast_exporting), Toast.LENGTH_SHORT).show()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            onImportData(it)
            Toast.makeText(context, context.getString(R.string.toast_importing), Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(updateState) {
        when (updateState) {
            is UpdateState.UpToDate -> {
                Toast.makeText(context, context.getString(R.string.toast_up_to_date), Toast.LENGTH_SHORT).show()
                onResetUpdateState()
            }
            is UpdateState.Error -> {
                Toast.makeText(context, String.format(context.getString(R.string.toast_update_error), updateState.message), Toast.LENGTH_LONG).show()
                onResetUpdateState()
            }
            else -> {  }
        }
    }

    if (updateState is UpdateState.UpdateAvailable) {
        UpdateAvailableDialog(
            latestVersion = updateState.latestVersion,
            downloadUrl = updateState.downloadUrl,
            releaseNotes = updateState.releaseNotes,
            onDismiss = onResetUpdateState
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                onDeleteAllData()
                showDeleteDialog = false
                Toast.makeText(context, context.getString(R.string.toast_all_data_deleted), Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text(stringResource(R.string.settings_section_appearance), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.settings_dark_mode), style = MaterialTheme.typography.titleMedium)
            Column {
                DarkModePreference.values().forEach { preference ->
                    ThemeRadioButton(
                        text = stringResource(preference.displayNameResId),
                        selected = darkModePreference == preference,
                        onClick = { onDarkModePreferenceChange(preference) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.settings_color_theme), style = MaterialTheme.typography.titleMedium)
            Column {
                AppTheme.values().forEach { theme ->
                    ThemeRadioButton(
                        text = stringResource(theme.displayNameResId),
                        selected = appTheme == theme,
                        onClick = { onAppThemeChange(theme) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(R.string.settings_section_units), style = MaterialTheme.typography.titleLarge)
            Column {
                UnitRadioButton(
                    text = stringResource(R.string.settings_unit_metric),
                    selected = unitSystem == UnitSystem.METRIC,
                    onClick = { onUnitSystemChange(UnitSystem.METRIC) }
                )
                UnitRadioButton(
                    text = stringResource(R.string.settings_unit_imperial),
                    selected = unitSystem == UnitSystem.IMPERIAL,
                    onClick = { onUnitSystemChange(UnitSystem.IMPERIAL) }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(R.string.settings_section_personal_info), style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = heightInput,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() || it == '.' }) {
                        heightInput = newValue
                        newValue.toFloatOrNull()?.let { onHeightChange(it) }
                    }
                },
                label = { Text(stringResource(if (unitSystem == UnitSystem.METRIC) R.string.settings_height_label_cm else R.string.settings_height_label_in)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = targetWeightInput,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() || it == '.' }) {
                        targetWeightInput = newValue
                        val newTarget = newValue.toFloatOrNull() ?: 0f
                        onTargetWeightChange(newTarget)
                    }
                },
                label = { Text(stringResource(if (unitSystem == UnitSystem.METRIC) R.string.settings_target_weight_label_kg else R.string.settings_target_weight_label_lbs)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(stringResource(R.string.settings_section_data_management), style = MaterialTheme.typography.titleLarge)
            ClickableInfoRow(
                text = stringResource(R.string.settings_export_data),
                icon = Icons.Default.Download,
                onClick = {
                    val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                    exportLauncher.launch("librehabit_data_$dateStr.csv")
                }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            ClickableInfoRow(
                text = stringResource(R.string.settings_import_data),
                icon = Icons.Default.Upload,
                onClick = {
                    importLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/csv"))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.settings_danger_zone), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
            Button(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.settings_delete_all_data))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(stringResource(R.string.settings_section_about), style = MaterialTheme.typography.titleLarge)
            Text(
                text = stringResource(R.string.settings_about_description),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            ClickableInfoRow(
                text = stringResource(R.string.settings_source_code),
                icon = Icons.AutoMirrored.Filled.OpenInNew,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Paul-HenryP/LibreHabit"))
                    context.startActivity(intent)
                }
            )
            val btcAddress = "35k63G5qH3q2y7ssYyrDPXRSkYm5eB5Fc3"
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            ClickableInfoRow(
                text = stringResource(R.string.settings_support_creator),
                icon = null,
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("BTC Address", btcAddress)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(
                        context,
                        String.format(context.getString(R.string.toast_btc_copied), btcAddress),
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.settings_app_version), style = MaterialTheme.typography.bodyLarge)
                Text(appVersion, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onCheckForUpdates,
                enabled = updateState !is UpdateState.Checking,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.settings_check_for_updates))
                    if (updateState is UpdateState.Checking) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var confirmationInput by remember { mutableStateOf("") }
    val confirmationString = stringResource(R.string.delete_dialog_confirmation_string)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_dialog_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.delete_dialog_message, confirmationString),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = confirmationInput,
                    onValueChange = { confirmationInput = it },
                    label = { Text(stringResource(R.string.delete_dialog_confirmation_label)) },
                    placeholder = { Text(confirmationString) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = confirmationInput == confirmationString,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun ClickableInfoRow(
    text: String,
    icon: ImageVector?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun UpdateAvailableDialog(
    latestVersion: String,
    downloadUrl: String,
    releaseNotes: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dialogScrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.update_dialog_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.update_dialog_message, latestVersion),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .verticalScroll(dialogScrollState)
                        .padding(8.dp)
                ) {
                    Text(
                        text = releaseNotes,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                    context.startActivity(intent)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.update_dialog_go_to_download))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.later))
            }
        }
    )
}

@Composable
private fun ThemeRadioButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun UnitRadioButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}