package com.example.librehabit

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Insert
    suspend fun insert(entry: WeightEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<WeightEntry>)

    @Query("SELECT * FROM weight_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<WeightEntry>>

    @Delete
    suspend fun delete(entry: WeightEntry)

    @Update
    suspend fun update(entry: WeightEntry)

    @Query("DELETE FROM weight_entries")
    suspend fun deleteAll()
}