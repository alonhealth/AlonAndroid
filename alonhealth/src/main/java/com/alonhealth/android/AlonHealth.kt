package com.alonhealth.android

import android.content.Context
import androidx.health.connect.client.permission.Permission
import com.alonhealth.android.models.HeartRateData
import com.alonhealth.android.models.HeartRateVariabilityData
import com.alonhealth.android.models.SleepData
import com.alonhealth.android.models.StepCountData
import com.alonhealth.android.models.WeightData
import com.alonhealth.android.models.toHeartRateData
import com.alonhealth.android.models.toHeartRateVariabilityData
import com.alonhealth.android.models.toSleepData
import com.alonhealth.android.models.toStepCountData
import com.alonhealth.android.models.toWeightData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

/**
 * Main entry point for the AlonHealth Android library.
 * This class provides access to health data on Android devices.
 */
class AlonHealth private constructor(private val context: Context) {

    private val healthDataManager = HealthDataManager(context)
    private val permissionHelper = HealthPermissionHelper(context)
    private val healthScoreCalculator = HealthScoreCalculator(context)

    companion object {
        @Volatile
        private var instance: AlonHealth? = null

        /**
         * Gets or creates an instance of AlonHealth
         * @param context the application context
         * @return an instance of AlonHealth
         */
        fun getInstance(context: Context): AlonHealth {
            return instance ?: synchronized(this) {
                instance ?: AlonHealth(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Checks if Health Connect is available on the device
     * @return true if Health Connect is available, false otherwise
     */
    fun isHealthConnectAvailable(): Boolean {
        return healthDataManager.isHealthConnectAvailable()
    }

    /**
     * Gets the required permissions for accessing health data
     * @return a set of permissions needed for health data access
     */
    fun getRequiredPermissions(): Set<Permission> {
        return healthDataManager.getRequiredPermissions()
    }

    /**
     * Checks if all required permissions are granted
     * @return true if all permissions are granted, false otherwise
     */
    suspend fun hasAllPermissions(): Boolean {
        return permissionHelper.hasAllPermissions(getRequiredPermissions())
    }

    /**
     * Creates a permission request contract
     * @return a permission request contract
     */
    fun createPermissionRequestContract() = permissionHelper.createPermissionRequestContract()

    /**
     * Opens the Health Connect settings page
     * @return true if the settings page was opened, false otherwise
     */
    fun openHealthConnectSettings(): Boolean {
        return permissionHelper.openHealthConnectSettings()
    }

    /**
     * Opens the Google Play Store page for Health Connect
     */
    fun openHealthConnectPlayStore() {
        permissionHelper.openHealthConnectPlayStore()
    }

    /**
     * Fetches heart rate data for a given time range
     * @param startTime the start time of the range
     * @param endTime the end time of the range
     * @return a flow of heart rate data
     */
    suspend fun getHeartRateData(
        startTime: Instant = Instant.now().minusSeconds(86400),
        endTime: Instant = Instant.now()
    ): Flow<List<HeartRateData>> {
        return healthDataManager.getHeartRateData(startTime, endTime)
            .map { records -> records.map { it.toHeartRateData() } }
    }

    /**
     * Fetches heart rate variability data for a given time range
     * @param startTime the start time of the range
     * @param endTime the end time of the range
     * @return a flow of heart rate variability data
     */
    suspend fun getHeartRateVariabilityData(
        startTime: Instant = Instant.now().minusSeconds(86400),
        endTime: Instant = Instant.now()
    ): Flow<List<HeartRateVariabilityData>> {
        return healthDataManager.getHeartRateVariabilityData(startTime, endTime)
            .map { records -> records.map { it.toHeartRateVariabilityData() } }
    }

    /**
     * Fetches step count data for a given time range
     * @param startTime the start time of the range
     * @param endTime the end time of the range
     * @return a flow of step count data
     */
    suspend fun getStepCountData(
        startTime: Instant = Instant.now().minusSeconds(86400),
        endTime: Instant = Instant.now()
    ): Flow<List<StepCountData>> {
        return healthDataManager.getStepCountData(startTime, endTime)
            .map { records -> records.map { it.toStepCountData() } }
    }

    /**
     * Fetches sleep data for a given time range
     * @param startTime the start time of the range
     * @param endTime the end time of the range
     * @return a flow of sleep data
     */
    suspend fun getSleepData(
        startTime: Instant = Instant.now().minusSeconds(86400),
        endTime: Instant = Instant.now()
    ): Flow<List<SleepData>> {
        return healthDataManager.getSleepData(startTime, endTime)
            .map { records -> records.map { it.toSleepData() } }
    }

    /**
     * Fetches weight data for a given time range
     * @param startTime the start time of the range
     * @param endTime the end time of the range
     * @return a flow of weight data
     */
    suspend fun getWeightData(
        startTime: Instant = Instant.now().minusSeconds(86400),
        endTime: Instant = Instant.now()
    ): Flow<List<WeightData>> {
        return healthDataManager.getWeightData(startTime, endTime)
            .map { records -> records.map { it.toWeightData() } }
    }

    /**
     * Calculates a health score based on the user's health data
     * @param completion callback with the calculated health score
     */
    suspend fun calculateHealthScore(completion: (Int) -> Unit) {
        healthScoreCalculator.calculateHealthScore(completion)
    }
} 