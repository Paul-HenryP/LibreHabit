package com.paulhenryp.librehabit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paulhenryp.librehabit.model.DarkModePreference
import com.paulhenryp.librehabit.ui.habits.ManageHabitsScreen
import com.paulhenryp.librehabit.ui.settings.SettingsScreen
import com.paulhenryp.librehabit.ui.settings.SettingsViewModel
import com.paulhenryp.librehabit.ui.settings.SettingsViewModelFactory
import com.paulhenryp.librehabit.ui.theme.LibreHabitTheme

class MainActivity : ComponentActivity() {

    private val weightViewModel: WeightViewModel by viewModels {
        WeightViewModelFactory(application)
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(application)
    }

    private val habitViewModel: HabitViewModel by viewModels {
        HabitViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val selectedTheme by settingsViewModel.appTheme.collectAsState()
            val darkModePreference by settingsViewModel.darkModePreference.collectAsState()
            val unitSystem by settingsViewModel.unitSystem.collectAsState()
            val height by settingsViewModel.height.collectAsState()
            val targetWeight by settingsViewModel.targetWeight.collectAsState()
            val isWeightTrackingEnabled by settingsViewModel.isWeightTrackingEnabled.collectAsState()

            val useDarkTheme = when (darkModePreference) {
                DarkModePreference.LIGHT -> false
                DarkModePreference.DARK -> true
                else -> isSystemInDarkTheme()
            }

            LibreHabitTheme(
                useDarkTheme = useDarkTheme,
                selectedTheme = selectedTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val context = LocalContext.current

                    NavHost(navController = navController, startDestination = "main") {
                        composable("main") {
                            val weightEntries by weightViewModel.allEntries.collectAsState()

                            val habits by habitViewModel.activeHabitsForDate.collectAsState()
                            val habitEntries by habitViewModel.habitEntriesForDate.collectAsState()
                            val selectedDate by habitViewModel.selectedDate.collectAsState()

                            LibreHabitScreen(
                                isWeightTrackingEnabled = isWeightTrackingEnabled,
                                weightEntries = weightEntries,
                                onSaveWeight = { weight, date ->
                                    weightViewModel.saveWeight(weight, date)
                                },
                                habits = habits,
                                habitEntries = habitEntries,
                                onSaveHabitEntry = { habit, value, date ->
                                    habitViewModel.saveHabitEntry(habit, value, date)
                                },
                                selectedDate = selectedDate,
                                onDateSelected = { date ->
                                    habitViewModel.setSelectedDate(date)
                                },
                                onNavigateToSettings = { navController.navigate("settings") },
                                onNavigateToGraph = { navController.navigate("graph") },
                                onNavigateToHabits = { navController.navigate("manage_habits") },
                                onDeleteEntry = { entry -> weightViewModel.deleteEntry(entry) },
                                onEditEntry = { entry -> weightViewModel.editEntry(entry) },
                                unitSystem = unitSystem,
                                height = height,
                                calculateBmi = { w, h -> weightViewModel.calculateBmi(w, h) }
                            )
                        }
                        composable("graph") {
                            val entries by weightViewModel.allEntries.collectAsState()
                            GraphScreen(
                                entries = entries,
                                unitSystem = unitSystem,
                                targetWeight = targetWeight,
                                onNavigateUp = { navController.popBackStack() }
                            )
                        }
                        composable("manage_habits") {
                            val habits by habitViewModel.allHabits.collectAsState()
                            ManageHabitsScreen(
                                habits = habits,
                                onAddHabit = { name, type, goal, unit, targetDays ->
                                    habitViewModel.addHabit(name, type, goal, unit, targetDays)
                                },
                                onUpdateHabit = { habit -> habitViewModel.updateHabit(habit) },
                                onDeleteHabit = { habit -> habitViewModel.deleteHabit(habit) },
                                onNavigateUp = { navController.popBackStack() }
                            )
                        }
                        composable("settings") {
                            val updateState by settingsViewModel.updateState.collectAsState()
                            SettingsScreen(
                                appTheme = selectedTheme,
                                onAppThemeChange = { settingsViewModel.setAppTheme(it) },
                                darkModePreference = darkModePreference,
                                onDarkModePreferenceChange = { settingsViewModel.setDarkModePreference(it) },
                                unitSystem = unitSystem,
                                onUnitSystemChange = { settingsViewModel.setUnitSystem(it) },
                                height = height,
                                onHeightChange = { settingsViewModel.setHeight(it) },
                                targetWeight = targetWeight,
                                onTargetWeightChange = { settingsViewModel.setTargetWeight(it) },
                                isWeightTrackingEnabled = isWeightTrackingEnabled,
                                onWeightTrackingEnabledChange = { settingsViewModel.setWeightTrackingEnabled(it) },
                                updateState = updateState,
                                onCheckForUpdates = { settingsViewModel.checkForUpdates() },
                                onResetUpdateState = { settingsViewModel.resetUpdateState() },
                                appVersion = BuildConfig.VERSION_NAME,
                                onExportData = { uri ->
                                    weightViewModel.exportToCsv(uri, context.contentResolver)
                                },
                                onImportData = { uri ->
                                    weightViewModel.importFromCsv(uri, context.contentResolver)
                                },
                                onDeleteAllData = {
                                    weightViewModel.deleteAllData()
                                    habitViewModel.deleteAllData()
                                },
                                onNavigateUp = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}