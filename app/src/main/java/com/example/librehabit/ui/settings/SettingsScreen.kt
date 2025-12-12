package com.example.librehabit.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.librehabit.UnitSystem
import com.example.librehabit.model.AppTheme
import com.example.librehabit.model.DarkModePreference

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
    updateState: UpdateState,
    onCheckForUpdates: () -> Unit,
    onResetUpdateState: () -> Unit,
    appVersion: String,
    onNavigateUp: () -> Unit
) {
    var heightInput by remember(height) { mutableStateOf(if (height > 0) height.toString() else "") }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(updateState) {
        when (updateState) {
            is UpdateState.UpToDate -> {
                Toast.makeText(context, "You are on the latest version.", Toast.LENGTH_SHORT).show()
                onResetUpdateState()
            }
            is UpdateState.Error -> {
                Toast.makeText(context, "Error checking for updates: ${updateState.message}", Toast.LENGTH_LONG).show()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
            Text("Appearance", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Dark Mode", style = MaterialTheme.typography.titleMedium)
            Column {
                DarkModePreference.values().forEach { preference ->
                    ThemeRadioButton(
                        text = preference.displayName,
                        selected = darkModePreference == preference,
                        onClick = { onDarkModePreferenceChange(preference) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Color Theme", style = MaterialTheme.typography.titleMedium)
            Column {
                AppTheme.values().forEach { theme ->
                    ThemeRadioButton(
                        text = theme.displayName,
                        selected = appTheme == theme,
                        onClick = { onAppThemeChange(theme) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Units", style = MaterialTheme.typography.titleLarge)
            Column {
                UnitRadioButton(
                    text = "Metric (kg, cm)",
                    selected = unitSystem == UnitSystem.METRIC,
                    onClick = { onUnitSystemChange(UnitSystem.METRIC) }
                )
                UnitRadioButton(
                    text = "Imperial (lbs, in)",
                    selected = unitSystem == UnitSystem.IMPERIAL,
                    onClick = { onUnitSystemChange(UnitSystem.IMPERIAL) }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Personal Info", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = heightInput,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() || it == '.' }) {
                        heightInput = newValue
                        newValue.toFloatOrNull()?.let { onHeightChange(it) }
                    }
                },
                label = { Text("Height (${if (unitSystem == UnitSystem.METRIC) "cm" else "in"})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("About", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "LibreHabit is a simple, open-source, and ad-free habit tracker built with privacy in mind.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            ClickableInfoRow(
                text = "Source Code",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Paul-HenryP/LibreHabit"))
                    context.startActivity(intent)
                }
            )
            val btcAddress = "35k63G5qH3q2y7ssYyrDPXRSkYm5eB5Fc3"
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            ClickableInfoRow(
                text = "Support the Creator",
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("BTC Address", btcAddress)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(
                        context,
                        "BTC address copied to clipboard:\n$btcAddress",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("App Version", style = MaterialTheme.typography.bodyLarge)
                Text(appVersion, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onCheckForUpdates,
                enabled = updateState !is UpdateState.Checking,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Check for Updates")
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
private fun ClickableInfoRow(
    text: String,
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
        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
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
        title = { Text("Update Available") },
        text = {
            Column {
                Text(
                    text = "Version $latestVersion is available. Here's what's new:",
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
                Text("Go to Download")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later")
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