package com.example.librehabit

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class WeightViewModel(private val database: AppDatabase) : ViewModel() {

    val allEntries: StateFlow<List<WeightEntry>> = database.weightDao().getAllEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveWeight(weightValue: Float, date: Date) {
        viewModelScope.launch {
            val newEntry = WeightEntry(weight = weightValue, date = date)
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

    fun deleteAllData() {
        viewModelScope.launch {
            database.weightDao().deleteAll()
        }
    }

    fun calculateBmi(weightInKg: Float, heightInCm: Float): Float {
        if (heightInCm <= 0) return 0f
        val heightInM = heightInCm / 100
        return weightInKg / (heightInM * heightInM)
    }

    fun exportToCsv(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val entries = database.weightDao().getAllEntries().first()
                val csvBuilder = StringBuilder()
                csvBuilder.append("date,weight_kg\n")

                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")

                entries.forEach { entry ->
                    val dateStr = dateFormat.format(entry.date)
                    csvBuilder.append("$dateStr,${entry.weight}\n")
                }

                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvBuilder.toString().toByteArray())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun importFromCsv(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val entriesToAdd = mutableListOf<WeightEntry>()

                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")

                reader.readLine()

                var line: String? = reader.readLine()
                while (line != null) {
                    try {
                        val parts = line.split(",")
                        if (parts.size >= 2) {
                            val dateStr = parts[0]
                            val weightStr = parts[1]

                            val date = dateFormat.parse(dateStr)
                            val weight = weightStr.toFloat()

                            if (date != null) {
                                entriesToAdd.add(WeightEntry(weight = weight, date = date))
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("CSVImport", "Error parsing line: $line", e)
                    }
                    line = reader.readLine()
                }

                if (entriesToAdd.isNotEmpty()) {
                    database.weightDao().insertAll(entriesToAdd)
                }

                reader.close()
                inputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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