package com.paulhenryp.librehabit

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.paulhenryp.librehabit.ui.components.EmptyState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibreHabitScreen(
    isWeightTrackingEnabled: Boolean,
    weightEntries: List<WeightEntry>,
    onSaveWeight: (Float, Date) -> Unit,
    habits: List<Habit>,
    habitEntries: List<HabitEntry>,
    onSaveHabitEntry: (Habit, Float, Date) -> Unit,
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToGraph: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onDeleteEntry: (WeightEntry) -> Unit,
    onEditEntry: (WeightEntry) -> Unit,
    unitSystem: UnitSystem,
    height: Float,
    calculateBmi: (Float, Float) -> Float
) {
    var weightInput by remember { mutableStateOf("") }
    var editingEntry by remember { mutableStateOf<WeightEntry?>(null) }
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
                onDateSelected(calendar.time)
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    if (editingEntry != null && isWeightTrackingEnabled) {
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
                    IconButton(onClick = onNavigateToHabits) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Manage Habits")
                    }
                    if (isWeightTrackingEnabled) {
                        IconButton(onClick = onNavigateToGraph) {
                            Icon(Icons.Default.BarChart, contentDescription = "Graph")
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Selected Date: $formattedDate",
                    style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { showDatePicker = true }
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text(
                    "Daily Habits",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (habits.isEmpty()) {
                item {
                    EmptyState(
                        message = "No custom habits yet.\nTap the list icon top-right to add some!",
                        icon = Icons.AutoMirrored.Filled.List,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                items(habits) { habit ->
                    val entry = habitEntries.find { it.habitId == habit.id }
                    HabitCard(
                        habit = habit,
                        entry = entry,
                        onValueChange = { newValue ->
                            onSaveHabitEntry(habit, newValue, selectedDate)
                        }
                    )
                }
            }

            if (isWeightTrackingEnabled) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Weight Tracker",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            OutlinedTextField(
                                value = weightInput,
                                onValueChange = { newValue ->
                                    if (newValue.all { it.isDigit() || it == '.' }) {
                                        weightInput = newValue
                                    }
                                },
                                label = { Text("Weight (${if (unitSystem == UnitSystem.METRIC) "kg" else "lbs"})") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val weight = weightInput.toFloatOrNull()
                                    if (weight != null && weight > 0) {
                                        val weightInKg = if (unitSystem == UnitSystem.IMPERIAL) weight / 2.20462f else weight
                                        onSaveWeight(weightInKg, selectedDate)
                                        weightInput = ""
                                    }
                                },
                                enabled = weightInput.toFloatOrNull()?.let { it > 0 } ?: false,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save Weight")
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Text(
                        "Weight History",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    )
                }

                if (weightEntries.isEmpty()) {
                    item {
                        EmptyState(
                            message = "No weight entries yet.\nAdd your first weight above!",
                            icon = Icons.Default.History,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                } else {
                    items(weightEntries) { entry ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
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

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    entry: HabitEntry?,
    onValueChange: (Float) -> Unit
) {
    val isCompleted = habit.type == HabitType.CHECKMARK && entry?.value == 1.0f

    Card(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                enabled = habit.type == HabitType.CHECKMARK // Klikitav ainult siis, kui on checkmark tüüpi
            ) {
                if (habit.type == HabitType.CHECKMARK) {
                    onValueChange(if (isCompleted) 0.0f else 1.0f)
                }
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCompleted) 0.dp else 2.dp // Eemaldame varju, kui tehtud
        ),
        colors = CardDefaults.cardColors(
            // Eemaldasime läbipaistvuse (alpha). Nüüd on puhas ja selge värv.
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (habit.type == HabitType.CHECKMARK) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { checked ->
                            onValueChange(if (checked) 1.0f else 0.0f)
                        }
                    )
                } else {
                    var textValue by remember(entry?.value) {
                        mutableStateOf(if (entry != null && entry.value > 0) {
                            if (entry.value % 1.0f == 0f) entry.value.toInt().toString() else entry.value.toString()
                        } else "")
                    }
                    val focusManager = LocalFocusManager.current

                    OutlinedTextField(
                        value = textValue,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.toFloatOrNull() != null) {
                                textValue = newValue
                            }
                        },
                        modifier = Modifier.width(100.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                val floatVal = textValue.toFloatOrNull() ?: 0f
                                onValueChange(floatVal)
                            }
                        ),
                        singleLine = true,
                        trailingIcon = {
                            if (textValue.isNotEmpty() && textValue.toFloatOrNull() != entry?.value) {
                                IconButton(onClick = {
                                    focusManager.clearFocus()
                                    val floatVal = textValue.toFloatOrNull() ?: 0f
                                    onValueChange(floatVal)
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    )
                    if (!habit.unit.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = habit.unit, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (habit.type == HabitType.NUMERIC && habit.goal != null && habit.goal > 0f) {
                val currentValue = entry?.value ?: 0f
                val progress = (currentValue / habit.goal).coerceIn(0f, 1f)

                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (progress >= 1f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${if (currentValue % 1.0f == 0f) currentValue.toInt() else currentValue} / ${if (habit.goal % 1.0f == 0f) habit.goal.toInt() else habit.goal}",
                        style = MaterialTheme.typography.bodySmall
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