package com.example.alonhealthsample

import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alonhealth.android.AlonHealth
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Sample activity demonstrating how to use the AlonHealth library
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var alonHealth: AlonHealth
    
    // Create a permission launcher
    private val permissionLauncher by lazy {
        registerForActivityResult(
            alonHealth.createPermissionRequestContract()
        ) { permissions ->
            if (permissions.containsAll(alonHealth.getRequiredPermissions())) {
                // All permissions granted, proceed with data access
                fetchHealthData()
            } else {
                // Some permissions were denied
                Log.d("AlonHealth", "Some permissions were denied")
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize AlonHealth
        alonHealth = AlonHealth.getInstance(this)
        
        // Check if Health Connect is available
        if (alonHealth.isHealthConnectAvailable()) {
            // Health Connect is available, check permissions
            checkAndRequestPermissions()
        } else {
            // Health Connect is not available, prompt user to install it
            Log.d("AlonHealth", "Health Connect is not available")
            alonHealth.openHealthConnectPlayStore()
        }
    }
    
    private fun checkAndRequestPermissions() {
        lifecycleScope.launch {
            if (!alonHealth.hasAllPermissions()) {
                // Request permissions
                permissionLauncher.launch(alonHealth.getRequiredPermissions())
            } else {
                // Permissions already granted, proceed with data access
                fetchHealthData()
            }
        }
    }
    
    private fun fetchHealthData() {
        // Fetch health data for the last 7 days
        val startTime = Instant.now().minus(7, ChronoUnit.DAYS)
        val endTime = Instant.now()
        
        // Fetch heart rate data
        fetchHeartRateData(startTime, endTime)
        
        // Fetch step count data
        fetchStepCountData(startTime, endTime)
        
        // Fetch sleep data
        fetchSleepData(startTime, endTime)
        
        // Fetch weight data
        fetchWeightData(startTime, endTime)
    }
    
    private fun fetchHeartRateData(startTime: Instant, endTime: Instant) {
        lifecycleScope.launch {
            alonHealth.getHeartRateData(startTime, endTime).collect { heartRateDataList ->
                Log.d("AlonHealth", "Heart rate data count: ${heartRateDataList.size}")
                
                for (heartRateData in heartRateDataList) {
                    Log.d("AlonHealth", "Heart rate source: ${heartRateData.source}")
                    Log.d("AlonHealth", "Heart rate samples count: ${heartRateData.samples.size}")
                    
                    for (sample in heartRateData.samples) {
                        Log.d("AlonHealth", "Heart rate: ${sample.beatsPerMinute} BPM at ${sample.time}")
                    }
                }
            }
        }
    }
    
    private fun fetchStepCountData(startTime: Instant, endTime: Instant) {
        lifecycleScope.launch {
            alonHealth.getStepCountData(startTime, endTime).collect { stepCountDataList ->
                Log.d("AlonHealth", "Step count data count: ${stepCountDataList.size}")
                
                for (stepCountData in stepCountDataList) {
                    Log.d("AlonHealth", "Steps: ${stepCountData.count} from ${stepCountData.source}")
                    Log.d("AlonHealth", "Time range: ${stepCountData.startTime} to ${stepCountData.endTime}")
                }
            }
        }
    }
    
    private fun fetchSleepData(startTime: Instant, endTime: Instant) {
        lifecycleScope.launch {
            alonHealth.getSleepData(startTime, endTime).collect { sleepDataList ->
                Log.d("AlonHealth", "Sleep data count: ${sleepDataList.size}")
                
                for (sleepData in sleepDataList) {
                    val duration = Duration.between(sleepData.startTime, sleepData.endTime)
                    Log.d("AlonHealth", "Sleep duration: ${duration.toHours()} hours, ${duration.toMinutesPart()} minutes")
                    Log.d("AlonHealth", "Sleep source: ${sleepData.source}")
                    
                    for (stage in sleepData.stages) {
                        val stageDuration = Duration.between(stage.startTime, stage.endTime)
                        Log.d("AlonHealth", "Sleep stage: ${stage.stage}, duration: ${stageDuration.toMinutes()} minutes")
                    }
                }
            }
        }
    }
    
    private fun fetchWeightData(startTime: Instant, endTime: Instant) {
        lifecycleScope.launch {
            alonHealth.getWeightData(startTime, endTime).collect { weightDataList ->
                Log.d("AlonHealth", "Weight data count: ${weightDataList.size}")
                
                for (weightData in weightDataList) {
                    Log.d("AlonHealth", "Weight: ${weightData.weightKg} kg from ${weightData.source}")
                    Log.d("AlonHealth", "Time: ${weightData.startTime}")
                }
            }
        }
    }
} 