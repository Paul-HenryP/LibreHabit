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
    val creationDate: Date
)