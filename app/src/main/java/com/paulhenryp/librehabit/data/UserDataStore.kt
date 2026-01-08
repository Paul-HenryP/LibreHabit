package com.paulhenryp.librehabit.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.paulhenryp.librehabit.UnitSystem
import com.paulhenryp.librehabit.model.AppTheme
import com.paulhenryp.librehabit.model.DarkModePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserDataStore(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val APP_THEME = stringPreferencesKey("app_theme")
        val DARK_MODE_PREFERENCE = stringPreferencesKey("dark_mode_preference")
        val UNIT_SYSTEM = stringPreferencesKey("unit_system")
        val HEIGHT = floatPreferencesKey("height")
        val TARGET_WEIGHT = floatPreferencesKey("target_weight")
    }

    val appTheme: Flow<AppTheme> = dataStore.data.map { preferences ->
        AppTheme.valueOf(preferences[APP_THEME] ?: AppTheme.PURPLE.name)
    }

    suspend fun setAppTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[APP_THEME] = theme.name
        }
    }

    val darkModePreference: Flow<DarkModePreference> = dataStore.data.map { preferences ->
        DarkModePreference.valueOf(preferences[DARK_MODE_PREFERENCE] ?: DarkModePreference.SYSTEM.name)
    }

    suspend fun setDarkModePreference(preference: DarkModePreference) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_PREFERENCE] = preference.name
        }
    }

    val unitSystem: Flow<UnitSystem> = dataStore.data.map { preferences ->
        UnitSystem.valueOf(preferences[UNIT_SYSTEM] ?: UnitSystem.METRIC.name)
    }

    suspend fun setUnitSystem(unitSystem: UnitSystem) {
        dataStore.edit { preferences ->
            preferences[UNIT_SYSTEM] = unitSystem.name
        }
    }

    val height: Flow<Float> = dataStore.data.map { preferences ->
        preferences[HEIGHT] ?: 0f
    }

    suspend fun setHeight(height: Float) {
        dataStore.edit { preferences ->
            preferences[HEIGHT] = height
        }
    }

    val targetWeight: Flow<Float> = dataStore.data.map { preferences ->
        preferences[TARGET_WEIGHT] ?: 0f
    }

    suspend fun setTargetWeight(weight: Float) {
        dataStore.edit { preferences ->
            preferences[TARGET_WEIGHT] = weight
        }
    }
}