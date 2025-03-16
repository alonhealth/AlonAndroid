package com.alonhealth.android

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Class responsible for calculating health scores based on health data
 */
class HealthScoreCalculator(private val context: Context) {

    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    /**
     * Fetches step count data for a given time range
     * @param startTime the start time of the range
     * @param endTime the end time of the range
     * @return the total step count for the period
     */
    private suspend fun fetchStepsData(
        startTime: Instant = Instant.now().minus(7, ChronoUnit.DAYS),
        endTime: Instant = Instant.now()
    ): Double? {
        val request = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        
        val response = healthConnectClient.readRecords(request)
        val records = response.records
        
        if (records.isEmpty()) {
            return null
        }
        
        return records.sumOf { it.count }.toDouble()
    }

    /**
     * Fetches heart rate variability data for a given time range
     * @param startTime the start time of the range
     * @param endTime the end time of the range
     * @return the average HRV for the period
     */
    private suspend fun fetchHRVData(
        startTime: Instant = Instant.now().minus(7, ChronoUnit.DAYS),
        endTime: Instant = Instant.now()
    ): Double? {
        val request = ReadRecordsRequest(
            recordType = HeartRateVariabilityRmssdRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        
        val response = healthConnectClient.readRecords(request)
        val records = response.records
        
        if (records.isEmpty()) {
            return null
        }
        
        val totalHRV = records.sumOf { it.heartRateVariabilityMillis.toDouble() }
        return totalHRV / records.size
    }

    /**
     * Fetches sleep score data for a given time range
     * @param startTime the start time of the range
     * @param endTime the end time of the range
     * @return the average sleep score for the period
     */
    private suspend fun fetchSleepScore(
        startTime: Instant = Instant.now().minus(7, ChronoUnit.DAYS),
        endTime: Instant = Instant.now()
    ): Double? {
        val request = ReadRecordsRequest(
            recordType = SleepSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        
        val response = healthConnectClient.readRecords(request)
        val records = response.records
        
        if (records.isEmpty()) {
            return null
        }
        
        // Calculate a sleep score based on duration and stages
        var totalScore = 0.0
        
        for (record in records) {
            val durationMinutes = ChronoUnit.MINUTES.between(record.startTime, record.endTime)
            
            // Base score on duration (8 hours = 100, less = proportionally less)
            val durationScore = (durationMinutes.toDouble() / 480.0) * 100.0
            
            // Cap at 100
            val cappedScore = minOf(durationScore, 100.0)
            
            totalScore += cappedScore
        }
        
        return totalScore / records.size
    }

    /**
     * Calculates an overall health score based on steps, HRV, and sleep data
     * @param completion callback with the calculated health score
     */
    suspend fun calculateHealthScore(completion: (Int) -> Unit) {
        val steps = fetchStepsData()
        val hrv = fetchHRVData()
        val sleepScore = fetchSleepScore()
        
        var totalScore = 0
        
        // Calculate score based on sleep
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
        
        // Calculate score based on HRV
        if (hrv != null) {
            totalScore += when {
                hrv >= 70 -> 30
                hrv >= 50 -> 20
                hrv >= 40 -> 10
                hrv > 20 -> 5
                else -> 0
            }
        }
        
        // Calculate score based on steps
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