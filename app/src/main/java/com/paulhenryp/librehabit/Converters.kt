package com.paulhenryp.librehabit

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromHabitType(value: HabitType): String {
        return value.name
    }

    @TypeConverter
    fun toHabitType(value: String): HabitType {
        return HabitType.valueOf(value)
    }

    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        if (value.isEmpty()) return emptyList()
        return value.split(",").mapNotNull { it.toIntOrNull() }
    }
}