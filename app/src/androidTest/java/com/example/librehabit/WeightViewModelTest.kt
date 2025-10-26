package com.example.librehabit

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.librehabit.data.UserDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
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
    private lateinit var userDataStore: UserDataStore
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userDataStore = UserDataStore(context)
        viewModel = WeightViewModel(database)
    }

    @After
    fun tearDown() {
        database.close()
        runBlocking {
            userDataStore.setDarkMode(null)
            userDataStore.setUnitSystem(UnitSystem.METRIC)
            userDataStore.setHeight(0f)
        }
        Dispatchers.resetMain()
    }

    @Test
    fun saveWeight_shouldAddEntryToDatabase() = runBlocking {
        // Given
        val weightValue = 75.5f

        // When
        val job = launch {
            viewModel.saveWeight(weightValue)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        job.join()

        // Then
        val allEntries = viewModel.allEntries.first()
        assertEquals(1, allEntries.size)
        assertEquals(weightValue, allEntries[0].weight, 0.01f)
    }

    @Test
    fun deleteEntry_shouldRemoveEntryFromDatabase() = runBlocking {
        // Given
        val entry = WeightEntry(id = 1, weight = 75.5f, date = Date())
        val job1 = launch {
            viewModel.saveWeight(entry.weight)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        job1.join()
        val entryToDelete = viewModel.allEntries.first()[0]

        // When
        val job2 = launch {
            viewModel.deleteEntry(entryToDelete)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        job2.join()

        // Then
        val allEntries = viewModel.allEntries.first()
        assertEquals(0, allEntries.size)
    }

    @Test
    fun editEntry_shouldUpdateEntryInDatabase() = runBlocking {
        // Given
        val entry = WeightEntry(id = 1, weight = 75.5f, date = Date())
        val job1 = launch {
            viewModel.saveWeight(entry.weight)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        job1.join()
        val updatedEntry = viewModel.allEntries.first()[0].copy(weight = 76.0f)

        // When
        val job2 = launch {
            viewModel.editEntry(updatedEntry)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        job2.join()

        // Then
        val allEntries = viewModel.allEntries.first()
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