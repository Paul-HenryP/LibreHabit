package com.paulhenryp.librehabit

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

fun Date.toStartOfDay(): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

class HabitViewModel(private val database: AppDatabase) : ViewModel() {

    val allHabits: StateFlow<List<Habit>> = database.habitDao().getAllHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedDate = MutableStateFlow(Date().toStartOfDay())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val habitEntriesForDate: StateFlow<List<HabitEntry>> = _selectedDate
        .flatMapLatest { date ->
            database.habitDao().getHabitEntriesForDate(date)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSelectedDate(date: Date) {
        _selectedDate.value = date.toStartOfDay()
    }

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

    fun saveHabitEntry(habitId: Int, value: Float, date: Date) {
        viewModelScope.launch {
            val entryDate = date.toStartOfDay()
            val existingEntry = habitEntriesForDate.value.find { it.habitId == habitId && it.date == entryDate }

            val entryToSave = if (existingEntry != null) {
                existingEntry.copy(value = value)
            } else {
                HabitEntry(habitId = habitId, date = entryDate, value = value)
            }
            database.habitDao().insertHabitEntry(entryToSave)
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            database.habitDao().deleteAllHabitEntries()
            database.habitDao().deleteAllHabits()
        }
    }
}

class HabitViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
            val database = AppDatabase.getDatabase(application)
            return HabitViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}