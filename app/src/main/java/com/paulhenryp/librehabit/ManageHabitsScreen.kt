package com.paulhenryp.librehabit.ui.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.paulhenryp.librehabit.Habit
import com.paulhenryp.librehabit.HabitType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageHabitsScreen(
    habits: List<Habit>,
    onAddHabit: (String, HabitType, Float?, String?) -> Unit,
    onUpdateHabit: (Habit) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    onNavigateUp: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }

    if (showCreateDialog) {
        HabitDialog(
            habit = null,
            onDismiss = { showCreateDialog = false },
            onSave = { name, type, goal, unit ->
                onAddHabit(name, type, goal, unit)
                showCreateDialog = false
            }
        )
    }

    if (habitToEdit != null) {
        HabitDialog(
            habit = habitToEdit,
            onDismiss = { habitToEdit = null },
            onSave = { name, type, goal, unit ->
                onUpdateHabit(habitToEdit!!.copy(name = name, type = type, goal = goal, unit = unit))
                habitToEdit = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Habits") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { paddingValues ->
        if (habits.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No habits yet.\nTap + to create one!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(habits) { habit ->
                    HabitListItem(
                        habit = habit,
                        onEdit = { habitToEdit = habit },
                        onDelete = { onDeleteHabit(habit) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) } // FAB clearance
            }
        }
    }
}

@Composable
fun HabitListItem(
    habit: Habit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = habit.name, style = MaterialTheme.typography.titleMedium)
                val typeText = if (habit.type == HabitType.CHECKMARK) {
                    "Yes/No"
                } else {
                    "Numeric goal: ${habit.goal ?: 0} ${habit.unit ?: ""}"
                }
                Text(
                    text = typeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun HabitDialog(
    habit: Habit?,
    onDismiss: () -> Unit,
    onSave: (name: String, type: HabitType, goal: Float?, unit: String?) -> Unit
) {
    var name by remember { mutableStateOf(habit?.name ?: "") }
    var type by remember { mutableStateOf(habit?.type ?: HabitType.CHECKMARK) }
    var goalString by remember { mutableStateOf(habit?.goal?.toString() ?: "") }
    var unit by remember { mutableStateOf(habit?.unit ?: "") }

    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (habit == null) "New Habit" else "Edit Habit") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showError = false
                    },
                    label = { Text("Habit Name") },
                    singleLine = true,
                    isError = showError && name.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (showError && name.isBlank()) {
                    Text("Name cannot be empty", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Type:", style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.selectable(selected = type == HabitType.CHECKMARK, onClick = { type = HabitType.CHECKMARK })) {
                    RadioButton(selected = type == HabitType.CHECKMARK, onClick = { type = HabitType.CHECKMARK })
                    Text("Yes/No (Checkmark)")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.selectable(selected = type == HabitType.NUMERIC, onClick = { type = HabitType.NUMERIC })) {
                    RadioButton(selected = type == HabitType.NUMERIC, onClick = { type = HabitType.NUMERIC })
                    Text("Numeric Value")
                }

                if (type == HabitType.NUMERIC) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = goalString,
                            onValueChange = { if (it.isEmpty() || it.toFloatOrNull() != null) goalString = it },
                            label = { Text("Daily Goal") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit (e.g. ml)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val goalFloat = if (type == HabitType.NUMERIC) goalString.toFloatOrNull() else null
                        val finalUnit = if (type == HabitType.NUMERIC) unit.ifBlank { null } else null
                        onSave(name.trim(), type, goalFloat, finalUnit)
                    } else {
                        showError = true
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}