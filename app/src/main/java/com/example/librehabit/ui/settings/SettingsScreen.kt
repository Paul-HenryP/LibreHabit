package com.example.librehabit.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.librehabit.UnitSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkMode: Boolean?,
    onDarkModeChange: (Boolean?) -> Unit,
    unitSystem: UnitSystem,
    onUnitSystemChange: (UnitSystem) -> Unit,
    onNavigateUp: () -> Unit
) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Theme", style = MaterialTheme.typography.titleLarge)
            Column {
                ThemeRadioButton(
                    text = "Light",
                    selected = isDarkMode == false,
                    onClick = { onDarkModeChange(false) }
                )
                ThemeRadioButton(
                    text = "Dark",
                    selected = isDarkMode == true,
                    onClick = { onDarkModeChange(true) }
                )
                ThemeRadioButton(
                    text = "System",
                    selected = isDarkMode == null,
                    onClick = { onDarkModeChange(null) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Units", style = MaterialTheme.typography.titleLarge)
            Column {
                UnitRadioButton(
                    text = "Metric (kg)",
                    selected = unitSystem == UnitSystem.METRIC,
                    onClick = { onUnitSystemChange(UnitSystem.METRIC) }
                )
                UnitRadioButton(
                    text = "Imperial (lbs)",
                    selected = unitSystem == UnitSystem.IMPERIAL,
                    onClick = { onUnitSystemChange(UnitSystem.IMPERIAL) }
                )
            }
        }
    }
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