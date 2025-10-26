package com.example.librehabit.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.librehabit.UnitSystem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserDataStore(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val UNIT_SYSTEM = stringPreferencesKey("unit_system")
    }

    val isDarkMode: Flow<Boolean?> = dataStore.data.map { preferences ->
        preferences[IS_DARK_MODE]
    }

    suspend fun setDarkMode(isDarkMode: Boolean?) {
        dataStore.edit { preferences ->
            if (isDarkMode == null) {
                preferences.remove(IS_DARK_MODE)
            } else {
                preferences[IS_DARK_MODE] = isDarkMode
            }
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
}