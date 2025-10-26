package com.example.librehabit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.librehabit.ui.settings.SettingsScreen
import com.example.librehabit.ui.settings.SettingsViewModel
import com.example.librehabit.ui.settings.SettingsViewModelFactory
import com.example.librehabit.ui.theme.LibreHabitTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val weightViewModel: WeightViewModel by viewModels {
        WeightViewModelFactory(application)
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
            val unitSystem by settingsViewModel.unitSystem.collectAsState()
            val height by settingsViewModel.height.collectAsState()

            LibreHabitTheme(darkTheme = isDarkMode ?: isSystemInDarkTheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "main") {
                        composable("main") {
                            val entries by weightViewModel.allEntries.collectAsState()
                            LibreHabitScreen(
                                entries = entries,
                                onSaveWeight = { weight ->
                                    weightViewModel.saveWeight(weight)
                                },
                                onNavigateToSettings = { navController.navigate("settings") },
                                onDeleteEntry = { entry -> weightViewModel.deleteEntry(entry) },
                                onEditEntry = { entry -> weightViewModel.editEntry(entry) },
                                unitSystem = unitSystem,
                                height = height,
                                calculateBmi = { weight, height -> weightViewModel.calculateBmi(weight, height) }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                isDarkMode = isDarkMode,
                                onDarkModeChange = { settingsViewModel.setDarkMode(it) },
                                unitSystem = unitSystem,
                                onUnitSystemChange = { settingsViewModel.setUnitSystem(it) },
                                height = height,
                                onHeightChange = { settingsViewModel.setHeight(it) },
                                onNavigateUp = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibreHabitScreen(
    entries: List<WeightEntry>,
    onSaveWeight: (Float) -> Unit,
    onNavigateToSettings: () -> Unit,
    onDeleteEntry: (WeightEntry) -> Unit,
    onEditEntry: (WeightEntry) -> Unit,
    unitSystem: UnitSystem,
    height: Float,
    calculateBmi: (Float, Float) -> Float
) {
    var weightInput by remember { mutableStateOf("") }
    var editingEntry by remember { mutableStateOf<WeightEntry?>(null) }

    if (editingEntry != null) {
        EditWeightDialog(
            entry = editingEntry!!,
            onDismiss = { editingEntry = null },
            onSave = {
                onEditEntry(it)
                editingEntry = null
            },
            unitSystem = unitSystem
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LibreHabit") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Enter Your Weight", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = weightInput,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() || it == '.' }) {
                        weightInput = newValue
                    }
                },
                label = { Text("Weight (${if (unitSystem == UnitSystem.METRIC) "kg" else "lbs"})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val weight = weightInput.toFloatOrNull()
                    if (weight != null && weight > 0) {
                        val weightInKg = if (unitSystem == UnitSystem.IMPERIAL) weight / 2.20462f else weight
                        onSaveWeight(weightInKg)
                        weightInput = ""
                    }
                },
                enabled = weightInput.toFloatOrNull()?.let { it > 0 } ?: false
            ) {
                Text("Save Weight")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()

            Text(
                "History",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 16.dp)
            )
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(entries) { entry ->
                    HistoryItem(
                        entry = entry,
                        onDelete = { onDeleteEntry(entry) },
                        onEdit = { editingEntry = entry },
                        unitSystem = unitSystem,
                        height = height,
                        calculateBmi = calculateBmi
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItem(
    entry: WeightEntry,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    unitSystem: UnitSystem,
    height: Float,
    calculateBmi: (Float, Float) -> Float
) {
    val formattedDate = remember(entry.date) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(entry.date)
    }
    val weightInSelectedUnit = if (unitSystem == UnitSystem.IMPERIAL) entry.weight * 2.20462f else entry.weight
    val bmi = if (height > 0) calculateBmi(entry.weight, height) else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = formattedDate)
        Text(text = "${String.format("%.1f", weightInSelectedUnit)} ${if (unitSystem == UnitSystem.METRIC) "kg" else "lbs"}", style = MaterialTheme.typography.bodyLarge)
        if (bmi > 0) {
            Text(text = "BMI: ${String.format("%.1f", bmi)}", style = MaterialTheme.typography.bodyLarge)
        }
        Row {
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
fun EditWeightDialog(
    entry: WeightEntry,
    onDismiss: () -> Unit,
    onSave: (WeightEntry) -> Unit,
    unitSystem: UnitSystem
) {
    var newWeight by remember { mutableStateOf(if (unitSystem == UnitSystem.IMPERIAL) (entry.weight * 2.20462f).toString() else entry.weight.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Weight") },
        text = {
            OutlinedTextField(
                value = newWeight,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() || it == '.' }) {
                        newWeight = newValue
                    }
                },
                label = { Text("Weight (${if (unitSystem == UnitSystem.METRIC) "kg" else "lbs"})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val weight = newWeight.toFloatOrNull()
                    if (weight != null && weight > 0) {
                        val weightInKg = if (unitSystem == UnitSystem.IMPERIAL) weight / 2.20462f else weight
                        onSave(entry.copy(weight = weightInKg))
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LibreHabitTheme {
        val sampleEntries = listOf(
            WeightEntry(id = 1, weight = 75.5f, date = Date()),
            WeightEntry(id = 2, weight = 76.0f, date = Date(System.currentTimeMillis() - 86400000))
        )
        LibreHabitScreen(
            entries = sampleEntries,
            onSaveWeight = {},
            onNavigateToSettings = {},
            onDeleteEntry = {},
            onEditEntry = {},
            unitSystem = UnitSystem.METRIC,
            height = 180f,
            calculateBmi = { _, _ -> 23.3f }
        )
    }
}