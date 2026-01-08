package com.paulhenryp.librehabit.ui.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.paulhenryp.librehabit.BuildConfig
import com.paulhenryp.librehabit.UnitSystem
import com.paulhenryp.librehabit.data.UserDataStore
import com.paulhenryp.librehabit.model.AppTheme
import com.paulhenryp.librehabit.model.DarkModePreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class UpdateAvailable(
        val latestVersion: String,
        val downloadUrl: String,
        val releaseNotes: String
    ) : UpdateState()
    object UpToDate : UpdateState()
    data class Error(val message: String) : UpdateState()
}

class SettingsViewModel(private val userDataStore: UserDataStore) : ViewModel() {

    val appTheme: StateFlow<AppTheme> = userDataStore.appTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppTheme.PURPLE
        )

    fun setAppTheme(theme: AppTheme) {
        viewModelScope.launch {
            userDataStore.setAppTheme(theme)
        }
    }

    val darkModePreference: StateFlow<DarkModePreference> = userDataStore.darkModePreference
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DarkModePreference.SYSTEM
        )

    fun setDarkModePreference(preference: DarkModePreference) {
        viewModelScope.launch {
            userDataStore.setDarkModePreference(preference)
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

    val targetWeight: StateFlow<Float> = userDataStore.targetWeight
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0f
        )

    fun setTargetWeight(weight: Float) {
        viewModelScope.launch {
            userDataStore.setTargetWeight(weight)
        }
    }

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    fun checkForUpdates() {
        viewModelScope.launch {
            _updateState.value = UpdateState.Checking
            try {
                val latestRelease = fetchLatestRelease()
                val latestVersionName = latestRelease.getString("tag_name")
                val currentVersionName = BuildConfig.VERSION_NAME

                if (isNewerVersion(current = currentVersionName, latest = latestVersionName)) {
                    val downloadUrl = latestRelease.getString("html_url")
                    val releaseNotes = latestRelease.getString("body")
                    _updateState.value = UpdateState.UpdateAvailable(latestVersionName, downloadUrl, releaseNotes)
                } else {
                    _updateState.value = UpdateState.UpToDate
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = UpdateState.Idle
    }

    private suspend fun fetchLatestRelease(): JSONObject = withContext(Dispatchers.IO) {
        val url = URL("https://api.github.com/repos/Paul-HenryP/LibreHabit/releases/latest")
        val connection = url.openConnection() as HttpURLConnection
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        JSONObject(response)
    }

    private fun isNewerVersion(current: String, latest: String): Boolean {
        val currentParts = current.replace("v", "").split(".").map { it.toInt() }
        val latestParts = latest.replace("v", "").split(".").map { it.toInt() }
        val partCount = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until partCount) {
            val currentPart = currentParts.getOrElse(i) { 0 }
            val latestPart = latestParts.getOrElse(i) { 0 }
            if (latestPart > currentPart) return true
            if (latestPart < currentPart) return false
        }
        return false
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