package com.paulhenryp.librehabit

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

@ExperimentalCoroutinesApi
class WeightViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var viewModel: WeightViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Application>()

        // Fix: Force Room to use the test dispatcher for transactions and queries.
        // This ensures database operations don't run on a hidden background thread that escapes our test control.
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .setTransactionExecutor(testDispatcher.asExecutor())
            .setQueryExecutor(testDispatcher.asExecutor())
            .allowMainThreadQueries()
            .build()

        viewModel = WeightViewModel(database)
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun saveWeight_shouldAddEntryToDatabase() = runTest(testDispatcher) {
        // Given
        val weightValue = 75.5f
        val date = Date()

        // When
        viewModel.saveWeight(weightValue, date)

        val allEntries = viewModel.allEntries.filter { it.isNotEmpty() }.first()

        // Then
        assertEquals(1, allEntries.size)
        assertEquals(weightValue, allEntries[0].weight, 0.01f)
    }

    @Test
    fun deleteEntry_shouldRemoveEntryFromDatabase() = runTest(testDispatcher) {
        // Given
        val entry = WeightEntry(weight = 75.5f, date = Date())
        database.weightDao().insert(entry)

        // Wait for the insertion to be reflected in the ViewModel
        val savedEntries = viewModel.allEntries.filter { it.isNotEmpty() }.first()
        val entryToDelete = savedEntries[0]

        // When
        viewModel.deleteEntry(entryToDelete)

        // Wait for the list to become empty again
        val allEntries = viewModel.allEntries.filter { it.isEmpty() }.first()

        // Then
        assertEquals(0, allEntries.size)
    }

    @Test
    fun editEntry_shouldUpdateEntryInDatabase() = runTest(testDispatcher) {
        // Given
        val entry = WeightEntry(weight = 75.5f, date = Date())
        database.weightDao().insert(entry)

        // Wait for insertion
        val savedEntries = viewModel.allEntries.filter { it.isNotEmpty() }.first()
        val entryToEdit = savedEntries[0]

        val updatedEntry = entryToEdit.copy(weight = 76.0f)

        // When
        viewModel.editEntry(updatedEntry)

        // Wait for the update to reflect (check for the new weight)
        val allEntries = viewModel.allEntries.filter {
            it.isNotEmpty() && it[0].weight == 76.0f
        }.first()

        // Then
        assertEquals(1, allEntries.size)
        assertEquals(76.0f, allEntries[0].weight, 0.01f)
    }

    @Test
    fun calculateBmi_shouldReturnCorrectBmi() {
        // Given
        val weightInKg = 75.5f
        val heightInCm = 180f
        val expectedBmi = 23.3f

        // When
        val actualBmi = viewModel.calculateBmi(weightInKg, heightInCm)

        // Then
        assertEquals(expectedBmi, actualBmi, 0.1f)
    }
}