package com.alonhealth.alonandroid

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneOffset

class HealthConnectManager(private val context: Context) {
    private val healthConnectClient: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    suspend fun readSteps(): Long {
        return withContext(Dispatchers.IO) {
            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    Instant.now().minusSeconds(86400),
                    Instant.now()
                )
            )
            val response = healthConnectClient.readRecords(request)
            response.records.sumOf { it.count }
        }
    }


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
