package com.paulhenryp.librehabit

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class HabitViewModel(private val database: AppDatabase) : ViewModel() {

    val allHabits: StateFlow<List<Habit>> = database.habitDao().getAllHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addHabit(name: String, type: HabitType, goal: Float?, unit: String?) {
        viewModelScope.launch {
            val newHabit = Habit(
                name = name,
                type = type,
                goal = goal,
                unit = unit,
                creationDate = Date()
            )
            database.habitDao().insertHabit(newHabit)
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            database.habitDao().updateHabit(habit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            database.habitDao().deleteHabit(habit)
        }
    }
}

class HabitViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val database = AppDatabase.getDatabase(application)
            return HabitViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}