package com.alonhealth.android

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * AlonAndroid is a wrapper for health data fetching on Android.
 * It provides a simple API to access health data from Health Connect.
 */
public class AlonAndroid(private val context: Context) {

    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }
    
    public init {}

    /**
     * Requests authorization to access health data
     * @param completion callback with the result of the authorization request
     */
    public fun requestAuthorization(completion: (Boolean, Exception?) -> Unit) {
        if (!isHealthConnectAvailable()) {
            completion(false, Exception("HealthKit is not available on this device"))
            return
        }

        val readTypes: Set<HealthPermission> = setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class)
        )
        
        healthConnectClient.permissionController.requestPermissions(
            permissions = readTypes,
            onPermissionResult = { granted, _ ->
                if (granted.containsAll(readTypes)) {
                    completion(true, null)
                } else {
                    completion(false, Exception("Some permissions were denied"))
                }
            }
        )
    }

    /**
     * Checks if Health Connect is available on the device
     * @return true if Health Connect is available, false otherwise
     */
    private fun isHealthConnectAvailable(): Boolean {
        return HealthConnectClient.isAvailable(context)
    }

    /**
     * Fetches step count data for the last 7 days
     * @param completion callback with the total step count
     */
    private suspend fun fetchStepsData(completion: (Double?) -> Unit) {
        val startTime = Instant.now().minus(7, ChronoUnit.DAYS)
        val endTime = Instant.now()
        
        val request = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        
        try {
            val response = healthConnectClient.readRecords(request)
            val records = response.records
            
            if (records.isEmpty()) {
                completion(null)
                return
            }
            
            val totalSteps = records.sumOf { it.count }.toDouble()
            completion(totalSteps)
        } catch (e: Exception) {
            completion(null)
        }
    }

    /**
     * Fetches heart rate variability data for the last 7 days
     * @param completion callback with the average HRV
     */
    private suspend fun fetchHRVData(completion: (Double?) -> Unit) {
        val startTime = Instant.now().minus(7, ChronoUnit.DAYS)
        val endTime = Instant.now()
        
        val request = ReadRecordsRequest(
            recordType = HeartRateVariabilityRmssdRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        
        try {
            val response = healthConnectClient.readRecords(request)
            val records = response.records
            
            if (records.isEmpty()) {
                completion(null)
                return
            }
            
            val totalHRV = records.sumOf { it.heartRateVariabilityMillis.toDouble() }
            val avgHRV = totalHRV / records.size
            completion(avgHRV)
        } catch (e: Exception) {
            completion(null)
        }
    }

    /**
     * Fetches sleep score data for the last 7 days
     * @param completion callback with the average sleep score
     */
    private suspend fun fetchSleepScore(completion: (Double?) -> Unit) {
        val startTime = Instant.now().minus(7, ChronoUnit.DAYS)
        val endTime = Instant.now()
        
        val request = ReadRecordsRequest(
            recordType = SleepSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        
        try {
            val response = healthConnectClient.readRecords(request)
            val records = response.records
            
            if (records.isEmpty()) {
                completion(null)
                return
            }
            
            // Calculate a sleep score based on sleep samples
            val sleepSamples = records.size
            val totalScore = records.sumOf { 
                // Simple scoring based on sleep stage values
                it.stages.sumOf { stage -> stage.stage.toDouble() }
            }
            
            val avgScore = if (sleepSamples > 0) totalScore / sleepSamples else null
            completion(avgScore)
        } catch (e: Exception) {
            completion(null)
        }
    }

    /**
     * Calculates a health score based on the user's health data
     * @param completion callback with the calculated health score
     */
    public suspend fun calculateHealthScore(completion: (Int) -> Unit) {
        fetchStepsData { steps ->
            fetchHRVData { hrv ->
                fetchSleepScore { sleepScore ->
                    var totalScore = 0
                    
                    if (sleepScore != null) {
                        totalScore += when {
                            sleepScore >= 90 -> 40
                            sleepScore >= 80 -> 35
                            sleepScore >= 70 -> 30
                            sleepScore >= 60 -> 20
                            sleepScore >= 50 -> 15
                            sleepScore >= 20 -> 10
                            else -> 0
                        }
                    }
                    
                    if (hrv != null) {
                        totalScore += when {
                            hrv >= 70 -> 30
                            hrv >= 50 -> 20
                            hrv >= 40 -> 10
                            hrv > 20 -> 5
                            else -> 0
                        }
                    }
                    
                    if (steps != null) {
                        totalScore += when {
                            steps >= 15000 -> 30
                            steps >= 10000 -> 25
                            steps >= 8000 -> 20
                            steps >= 5000 -> 15
                            steps >= 3000 -> 5
                            else -> 0
                        }
                    }
                    
                    completion(totalScore)
                }
            }
        }
    }
} 