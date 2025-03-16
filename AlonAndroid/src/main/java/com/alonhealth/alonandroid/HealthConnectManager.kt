package com.alonhealth.alonandroid

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class HealthConnectManager(private val context: Context) {
    private val healthConnectClient: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    // Permissions needed to match iOS functionality
    private val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class)
    )

    /**
     * Request authorization for health data access
     * @param callback Function to call with result (success, error)
     */
    suspend fun requestAuthorization(callback: (Boolean, Exception?) -> Unit) {
        try {
            val permissionController = PermissionController.getOrCreate(context)
            val grantedPermissions = permissionController.getGrantedPermissions()
            
            if (permissions.all { it in grantedPermissions }) {
                callback(true, null)
                return
            }
            
            // We can't directly request permissions here as it requires an Activity context
            // and a result contract. Instead, we'll inform the caller that permissions are needed.
            callback(false, Exception("Health Connect permissions not granted"))
        } catch (e: Exception) {
            callback(false, e)
        }
    }

    /**
     * Read steps data for the last 7 days
     * @return Total step count or null if unavailable
     */
    suspend fun fetchStepsData(): Double? {
        return withContext(Dispatchers.IO) {
            try {
                val endTime = Instant.now()
                val startTime = endTime.minus(7, ChronoUnit.DAYS)
                
                val request = ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val response = healthConnectClient.readRecords(request)
                val totalSteps = response.records.sumOf { it.count }
                
                totalSteps.toDouble()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Read HRV data for the last 7 days
     * @return Average HRV value or null if unavailable
     */
    suspend fun fetchHRVData(): Double? {
        return withContext(Dispatchers.IO) {
            try {
                val endTime = Instant.now()
                val startTime = endTime.minus(7, ChronoUnit.DAYS)
                
                val request = ReadRecordsRequest(
                    recordType = HeartRateVariabilityRmssdRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val response = healthConnectClient.readRecords(request)
                
                if (response.records.isEmpty()) {
                    return@withContext null
                }
                
                // Calculate average HRV
                val totalHrv = response.records.sumOf { it.rmssd }
                totalHrv / response.records.size
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Read sleep data for the last 7 days
     * @return Average sleep score or null if unavailable
     */
    suspend fun fetchSleepScore(): Double? {
        return withContext(Dispatchers.IO) {
            try {
                val endTime = Instant.now()
                val startTime = endTime.minus(7, ChronoUnit.DAYS)
                
                val request = ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val response = healthConnectClient.readRecords(request)
                
                if (response.records.isEmpty()) {
                    return@withContext null
                }
                
                // Calculate a simple sleep score based on duration
                // This is a simplified approach as Android doesn't have a direct sleep score
                val sleepScores = response.records.map { session ->
                    val durationMinutes = ChronoUnit.MINUTES.between(session.startTime, session.endTime)
                    // Convert to a score out of 100
                    // Assuming 8 hours (480 minutes) is ideal sleep
                    val score = when {
                        durationMinutes >= 480 -> 100.0 // 8+ hours
                        durationMinutes >= 420 -> 90.0 // 7+ hours
                        durationMinutes >= 360 -> 80.0 // 6+ hours
                        durationMinutes >= 300 -> 70.0 // 5+ hours
                        durationMinutes >= 240 -> 60.0 // 4+ hours
                        else -> 50.0 // Less than 4 hours
                    }
                    score
                }
                
                sleepScores.average()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Calculate overall health score based on steps, HRV, and sleep data
     * @param callback Function to call with the calculated health score
     */
    suspend fun calculateHealthScore(callback: (Double?) -> Unit) {
        try {
            var totalScore = 0.0
            
            // Fetch all required data
            val sleepScore = fetchSleepScore()
            val hrv = fetchHRVData()
            val steps = fetchStepsData()
            
            // Calculate score based on sleep data
            if (sleepScore != null) {
                totalScore += when {
                    sleepScore >= 90 -> 40.0
                    sleepScore >= 80 -> 35.0
                    sleepScore >= 70 -> 30.0
                    sleepScore >= 60 -> 20.0
                    sleepScore >= 50 -> 15.0
                    sleepScore >= 20 -> 10.0
                    else -> 0.0
                }
            }
            
            // Calculate score based on HRV data
            if (hrv != null) {
                totalScore += when {
                    hrv >= 70 -> 30.0
                    hrv >= 50 -> 20.0
                    hrv >= 40 -> 10.0
                    hrv > 20 -> 5.0
                    else -> 0.0
                }
            }
            
            // Calculate score based on steps data
            if (steps != null) {
                totalScore += when {
                    steps >= 15000 -> 30.0
                    steps >= 10000 -> 25.0
                    steps >= 8000 -> 20.0
                    steps >= 5000 -> 15.0
                    steps >= 3000 -> 5.0
                    else -> 0.0
                }
            }
            
            callback(totalScore)
        } catch (e: Exception) {
            e.printStackTrace()
            callback(null)
        }
    }

    /**
     * Insert steps data
     */
    suspend fun insertSteps(count: Long, startTime: Instant, endTime: Instant) {
        try {
            val stepsRecord = StepsRecord(
                count = count,
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = ZoneOffset.UTC,
                endZoneOffset = ZoneOffset.UTC
            )
            healthConnectClient.insertRecords(listOf(stepsRecord))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
