package com.example.librehabit

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
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
import java.util.Locale
import java.util.*

val BarChart = materialIcon(name = "CustomBarChart") {
    materialPath {
        moveTo(4f, 10f)
        lineTo(6f, 10f)
        lineTo(6f, 20f)
        lineTo(4f, 20f)
        close()
        moveTo(10f, 6f)
        lineTo(12f, 6f)
        lineTo(12f, 20f)
        lineTo(10f, 20f)
        close()
        moveTo(16f, 2f)
        lineTo(18f, 2f)
        lineTo(18f, 20f)
        lineTo(16f, 20f)
        close()
    }
}

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
                                onSaveWeight = { weight, date ->
                                    weightViewModel.saveWeight(weight, date)
                                },
                                onNavigateToSettings = { navController.navigate("settings") },
                                onNavigateToGraph = { navController.navigate("graph") },
                                onDeleteEntry = { entry -> weightViewModel.deleteEntry(entry) },
                                onEditEntry = { entry -> weightViewModel.editEntry(entry) },
                                unitSystem = unitSystem,
                                height = height,
                                calculateBmi = { weight, height -> weightViewModel.calculateBmi(weight, height) }
                            )
                        }
                        composable("graph") {
                            val entries by weightViewModel.allEntries.collectAsState()
                            GraphScreen(
                                entries = entries,
                                unitSystem = unitSystem,
                                onNavigateUp = { navController.popBackStack() }
                            )
                        }
                        composable("settings") {
                            val updateState by settingsViewModel.updateState.collectAsState()
                            SettingsScreen(
                                isDarkMode = isDarkMode,
                                onDarkModeChange = { settingsViewModel.setDarkMode(it) },
                                unitSystem = unitSystem,
                                onUnitSystemChange = { settingsViewModel.setUnitSystem(it) },
                                height = height,
                                onHeightChange = { settingsViewModel.setHeight(it) },
                                updateState = updateState,
                                onCheckForUpdates = { settingsViewModel.checkForUpdates() },
                                onResetUpdateState = { settingsViewModel.resetUpdateState() },
                                onNavigateUp = { navController.popBackStack() },
                                appVersion = BuildConfig.VERSION_NAME
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
    onSaveWeight: (Float, Date) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToGraph: () -> Unit,
    onDeleteEntry: (WeightEntry) -> Unit,
    onEditEntry: (WeightEntry) -> Unit,
    unitSystem: UnitSystem,
    height: Float,
    calculateBmi: (Float, Float) -> Float
) {
    var weightInput by remember { mutableStateOf("") }
    var editingEntry by remember { mutableStateOf<WeightEntry?>(null) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val formattedDate = remember(selectedDate) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selectedDate)
    }

    if (showDatePicker) {
        val calendar = Calendar.getInstance().apply { time = selectedDate }
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.time
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

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
                    IconButton(onClick = onNavigateToGraph) {
                        Icon(BarChart, contentDescription = "Graph")

                    }
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
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Date: $formattedDate",
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary),
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { showDatePicker = true }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val weight = weightInput.toFloatOrNull()
                    if (weight != null && weight > 0) {
                        val weightInKg = if (unitSystem == UnitSystem.IMPERIAL) weight / 2.20462f else weight
                        onSaveWeight(weightInKg, selectedDate)
                        weightInput = ""
                        selectedDate = Date()
                    }
                },
                enabled = weightInput.toFloatOrNull()?.let { it > 0 } ?: false
            ) {
                Text("Save Weight")
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()

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
        Text(text = "${String.format(Locale.US, "%.1f", weightInSelectedUnit)} ${if (unitSystem == UnitSystem.METRIC) "kg" else "lbs"}", style = MaterialTheme.typography.bodyLarge)
        if (bmi > 0) {
            Text(text = "BMI: ${String.format(Locale.US, "%.1f", bmi)}", style = MaterialTheme.typography.bodyLarge)
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
    var newDate by remember { mutableStateOf(entry.date) }
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val formattedDate = remember(newDate) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(newDate)
    }

    if (showDatePicker) {
        val calendar = Calendar.getInstance().apply { time = newDate }
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                calendar.set(year, month, dayOfMonth)
                newDate = calendar.time
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Entry") },
        text = {
            Column {
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
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Date: $formattedDate",
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { showDatePicker = true }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val weight = newWeight.toFloatOrNull()
                    if (weight != null && weight > 0) {
                        val weightInKg = if (unitSystem == UnitSystem.IMPERIAL) weight / 2.20462f else weight
                        onSave(entry.copy(weight = weightInKg, date = newDate))
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
            onSaveWeight = { _, _ -> },
            onNavigateToSettings = {},
            onNavigateToGraph = {},
            onDeleteEntry = {},
            onEditEntry = {},
            unitSystem = UnitSystem.METRIC,
            height = 180f,
            calculateBmi = { _, _ -> 23.3f }
        )
    }
}