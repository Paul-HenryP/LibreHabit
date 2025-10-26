package com.example.librehabit

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
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
    fun `saveWeight_shouldAddEntryToDatabase`() = runTest {
        // Given
        val weightValue = 75.5f

        // When
        viewModel.saveWeight(weightValue)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val allEntries = viewModel.allEntries.first()
        assertEquals(1, allEntries.size)
        assertEquals(weightValue, allEntries[0].weight)
    }

    @Test
    fun `deleteEntry_shouldRemoveEntryFromDatabase`() = runTest {
        // Given
        val entry = WeightEntry(id = 1, weight = 75.5f, date = Date())
        database.weightDao().insert(entry)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.deleteEntry(entry)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val allEntries = viewModel.allEntries.first()
        assertEquals(0, allEntries.size)
    }

    @Test
    fun `editEntry_shouldUpdateEntryInDatabase`() = runTest {
        // Given
        val entry = WeightEntry(id = 1, weight = 75.5f, date = Date())
        database.weightDao().insert(entry)
        testDispatcher.scheduler.advanceUntilIdle()
        val updatedEntry = entry.copy(weight = 76.0f)

        // When
        viewModel.editEntry(updatedEntry)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val allEntries = viewModel.allEntries.first()
        assertEquals(1, allEntries.size)
        assertEquals(76.0f, allEntries[0].weight)
    }
}