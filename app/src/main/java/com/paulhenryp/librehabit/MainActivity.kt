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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val selectedTheme by settingsViewModel.appTheme.collectAsState()
            val darkModePreference by settingsViewModel.darkModePreference.collectAsState()
            val unitSystem by settingsViewModel.unitSystem.collectAsState()
            val height by settingsViewModel.height.collectAsState()

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
                                appTheme = selectedTheme,
                                onAppThemeChange = { settingsViewModel.setAppTheme(it) },
                                darkModePreference = darkModePreference,
                                onDarkModePreferenceChange = { settingsViewModel.setDarkModePreference(it) },
                                unitSystem = unitSystem,
                                onUnitSystemChange = { settingsViewModel.setUnitSystem(it) },
                                height = height,
                                onHeightChange = { settingsViewModel.setHeight(it) },
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