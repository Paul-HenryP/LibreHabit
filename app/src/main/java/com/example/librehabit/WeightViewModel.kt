package com.example.librehabit

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

class WeightViewModel(private val database: AppDatabase) : ViewModel() {

    val allEntries: StateFlow<List<WeightEntry>> = database.weightDao().getAllEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveWeight(weightValue: Float) {
        viewModelScope.launch {
            val newEntry = WeightEntry(weight = weightValue, date = Date())
            database.weightDao().insert(newEntry)
        }
    }

    fun deleteEntry(entry: WeightEntry) {
        viewModelScope.launch {
            database.weightDao().delete(entry)
        }
    }

    fun editEntry(entry: WeightEntry) {
        viewModelScope.launch {
            database.weightDao().update(entry)
        }
    }

    fun calculateBmi(weightInKg: Float, heightInCm: Float): Float {
        if (heightInCm <= 0) return 0f
        val heightInM = heightInCm / 100
        return weightInKg / (heightInM * heightInM)
    }
}

class WeightViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeightViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val database = AppDatabase.getDatabase(application)
            return WeightViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}