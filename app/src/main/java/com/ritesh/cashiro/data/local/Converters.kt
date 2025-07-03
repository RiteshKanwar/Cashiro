package com.ritesh.cashiro.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritesh.cashiro.data.local.entity.Recurrence
import com.ritesh.cashiro.data.local.entity.RecurrenceFrequency
import com.ritesh.cashiro.data.local.entity.TransactionType
import com.ritesh.cashiro.presentation.ui.features.activity_logs.ActivityActionType

class Converters {

    // Existing converters
    @TypeConverter
    fun fromRecurrence(recurrence: Recurrence?): String? {
        return recurrence?.let {
            "${it.frequency.name},${it.interval},${it.endRecurrenceDate ?: ""}"
        }
    }

    @TypeConverter
    fun toRecurrence(value: String?): Recurrence? {
        if (value.isNullOrEmpty()) return null
        val parts = value.split(",")
        val frequency = RecurrenceFrequency.valueOf(parts[0])
        val interval = parts[1].toInt()
        val endDate = parts.getOrNull(2)?.toLongOrNull()
        return Recurrence(frequency, interval, endDate)
    }

    @TypeConverter
    fun fromTransactionType(transactionType: TransactionType): String {
        return transactionType.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }

    @TypeConverter
    fun fromRecurrenceFrequency(frequency: RecurrenceFrequency): String {
        return frequency.name
    }

    @TypeConverter
    fun toRecurrenceFrequency(value: String): RecurrenceFrequency {
        return RecurrenceFrequency.valueOf(value)
    }

    @TypeConverter
    fun fromMap(value: Map<String, Double>?): String? {
        return value?.let { Gson().toJson(it) }
    }

    @TypeConverter
    fun toMap(value: String?): Map<String, Double>? {
        return value?.let {
            val mapType = object : TypeToken<Map<String, Double>>() {}.type
            Gson().fromJson(it, mapType)
        }
    }

    // NEW: ActivityActionType converters
    @TypeConverter
    fun fromActivityActionType(actionType: ActivityActionType): String {
        return actionType.name
    }

    @TypeConverter
    fun toActivityActionType(value: String): ActivityActionType {
        return ActivityActionType.valueOf(value)
    }

    // NEW: String Map converters for activity log metadata
//    @TypeConverter
//    fun fromStringMap(value: Map<String, String>?): String? {
//        return if (value.isNullOrEmpty()) {
//            null
//        } else {
//            Gson().toJson(value)
//        }
//    }
//
//    @TypeConverter
//    fun toStringMap(value: String?): Map<String, String> {
//        return if (value.isNullOrEmpty()) {
//            emptyMap()
//        } else {
//            try {
//                val mapType = object : TypeToken<Map<String, String>>() {}.type
//                Gson().fromJson(value, mapType) ?: emptyMap()
//            } catch (e: Exception) {
//                emptyMap()
//            }
//        }
//    }
    private val gson = Gson()

    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String {
        return if (value == null || value.isEmpty()) {
            "{}" // Return empty JSON object for null or empty maps
        } else {
            gson.toJson(value)
        }
    }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String> {
        return if (value.isNullOrBlank() || value == "{}") {
            emptyMap() // Return empty map for null, blank, or empty JSON
        } else {
            try {
                val type = object : TypeToken<Map<String, String>>() {}.type
                gson.fromJson(value, type) ?: emptyMap()
            } catch (e: Exception) {
                // If JSON parsing fails, return empty map
                emptyMap()
            }
        }
    }
}

//class Converters {
//
//    // Convert Recurrence to String for database storage
//    @TypeConverter
//    fun fromRecurrence(recurrence: Recurrence?): String? {
//        return recurrence?.let {
//            "${it.frequency.name},${it.interval},${it.endRecurrenceDate ?: ""}"
//        }
//    }
//
//    // Convert String back to Recurrence for database retrieval
//    @TypeConverter
//    fun toRecurrence(value: String?): Recurrence? {
//        if (value.isNullOrEmpty()) return null
//        val parts = value.split(",")
//        val frequency = RecurrenceFrequency.valueOf(parts[0])
//        val interval = parts[1].toInt()
//        val endDate = parts.getOrNull(2)?.toLongOrNull()
//        return Recurrence(frequency, interval, endDate)
//    }
//
//    // Convert TransactionType to String for database storage
//    @TypeConverter
//    fun fromTransactionType(transactionType: TransactionType): String {
//        return transactionType.name
//    }
//
//    // Convert String back to TransactionType for database retrieval
//    @TypeConverter
//    fun toTransactionType(value: String): TransactionType {
//        return TransactionType.valueOf(value)
//    }
//
//    // Convert RecurrenceFrequency to String for database storage
//    @TypeConverter
//    fun fromRecurrenceFrequency(frequency: RecurrenceFrequency): String {
//        return frequency.name
//    }
//
//    // Convert String back to RecurrenceFrequency for database retrieval
//    @TypeConverter
//    fun toRecurrenceFrequency(value: String): RecurrenceFrequency {
//        return RecurrenceFrequency.valueOf(value)
//    }
//
//    // Convert Map<String, Double> to String for database storage
//    @TypeConverter
//    fun fromMap(value: Map<String, Double>?): String? {
//        return value?.let { Gson().toJson(it) }
//    }
//
//    // Convert String back to Map<String, Double> for database retrieval
//    @TypeConverter
//    fun toMap(value: String?): Map<String, Double>? {
//        return value?.let {
//            val mapType = object : TypeToken<Map<String, Double>>() {}.type
//            Gson().fromJson(it, mapType)
//        }
//    }
//}