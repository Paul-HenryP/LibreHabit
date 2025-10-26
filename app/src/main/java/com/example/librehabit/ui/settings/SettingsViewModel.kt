package com.example.librehabit.ui.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.librehabit.UnitSystem
import com.example.librehabit.data.UserDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val userDataStore: UserDataStore) : ViewModel() {

    val isDarkMode: StateFlow<Boolean?> = userDataStore.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun setDarkMode(isDarkMode: Boolean?) {
        viewModelScope.launch {
            userDataStore.setDarkMode(isDarkMode)
        }
    }

    val unitSystem: StateFlow<UnitSystem> = userDataStore.unitSystem
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UnitSystem.METRIC
        )

    fun setUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch {
            userDataStore.setUnitSystem(unitSystem)
        }
    }

    val height: StateFlow<Float> = userDataStore.height
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0f
        )

    fun setHeight(height: Float) {
        viewModelScope.launch {
            userDataStore.setHeight(height)
        }
    }
}

class SettingsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(UserDataStore(application)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}