package com.alonhealth.android

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.ZonedDateTime

/**
 * HealthDataManager is the main entry point for the AlonAndroid library.
 * It provides methods to fetch health data from various sources on Android.
 */
class HealthDataManager(private val context: Context) {

    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    /**
     * Checks if Health Connect is available on the device
     * @return true if Health Connect is available, false otherwise
     */
    fun isHealthConnectAvailable(): Boolean {
        return HealthConnectClient.isAvailable(context)
    }

    /**
     * Gets the required permissions for accessing health data
     * @return a set of permissions needed for health data access
     */
    fun getRequiredPermissions(): Set<HealthPermission> {
        return setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(WeightRecord::class)
        )
    }

    /**
     * Creates a permission request launcher
     * @return a permission controller that can be used to request permissions
     */
    fun createPermissionLauncher(): PermissionController {
        return healthConnectClient.permissionController
    }

    /**
     * Fetches heart rate data for a given time range
     * @param startTime the start time of the range
     * @param endTime the end time of the range
     * @return a flow of heart rate records
     */
    suspend fun getHeartRateData(
        startTime: Instant = Instant.now().minusSeconds(86400),
        endTime: Instant = Instant.now()
    ): Flow<List<HeartRateRecord>> = flow {
        val request = ReadRecordsRequest(
            recordType = HeartRateRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        emit(response.records)
    }

    /**
     * Fetches heart rate variability data for a given time range
     * @param startTime the start time of the range
     * @param endTime the end time of the range
     * @return a flow of heart rate variability records
     */
    suspend fun getHeartRateVariabilityData(
        startTime: Instant = Instant.now().minusSeconds(86400),
        endTime: Instant = Instant.now()
    ): Flow<List<HeartRateVariabilityRmssdRecord>> = flow {
        val request = ReadRecordsRequest(
            recordType = HeartRateVariabilityRmssdRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        emit(response.records)
    }

    /**
     * Fetches step count data for a given time range
     * @param startTime the start time of the range
     * @param endTime the end time of the range
     * @return a flow of step count records
     */
    suspend fun getStepCountData(
        startTime: Instant = Instant.now().minusSeconds(86400),
        endTime: Instant = Instant.now()
    ): Flow<List<StepsRecord>> = flow {
        val request = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        emit(response.records)
    }

    /**
     * Fetches sleep data for a given time range
     * @param startTime the start time of the range
     * @param endTime the end time of the range
     * @return a flow of sleep session records
     */
    suspend fun getSleepData(
        startTime: Instant = Instant.now().minusSeconds(86400),
        endTime: Instant = Instant.now()
    ): Flow<List<SleepSessionRecord>> = flow {
        val request = ReadRecordsRequest(
            recordType = SleepSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        emit(response.records)
    }

    /**
     * Fetches weight data for a given time range
     * @param startTime the start time of the range
     * @param endTime the end time of the range
     * @return a flow of weight records
     */
    suspend fun getWeightData(
        startTime: Instant = Instant.now().minusSeconds(86400),
        endTime: Instant = Instant.now()
    ): Flow<List<WeightRecord>> = flow {
        val request = ReadRecordsRequest(
            recordType = WeightRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        emit(response.records)
    }
} 