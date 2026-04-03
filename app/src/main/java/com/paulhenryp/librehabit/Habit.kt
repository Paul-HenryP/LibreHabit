package com.paulhenryp.librehabit

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: HabitType,
    val goal: Float?,
    val unit: String?,
    val creationDate: Date,
    val targetDays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7)
)