package com.alonhealth.example

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.alonhealth.alonandroid.HealthConnectManager
import kotlinx.coroutines.launch
import java.time.Instant

class MainActivity : AppCompatActivity() {
    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var stepsTextView: TextView
    private lateinit var insertStepsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        healthConnectManager = HealthConnectManager(this)
        stepsTextView = findViewById(R.id.stepsTextView)
        insertStepsButton = findViewById(R.id.btn_insert_steps)

        checkPermissionsAndSetupButton()
    }

    private fun checkPermissionsAndSetupButton() {
        Log.d(TAG, "Checking permissions")
        val requiredPermissions = arrayOf(
            "android.permission.health.READ_STEPS",
            "android.permission.health.WRITE_STEPS"
        )

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            Log.d(TAG, "Requesting missing permissions: $missingPermissions")
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), REQUEST_CODE_PERMISSIONS)
        } else {
            Log.d(TAG, "All permissions granted")
            setupInsertStepsButton()
            readSteps()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            val deniedPermissions = permissions.filterIndexed { index, _ ->
                grantResults[index] != PackageManager.PERMISSION_GRANTED
            }
            if (deniedPermissions.isEmpty()) {
                Log.d(TAG, "All requested permissions granted")
                setupInsertStepsButton()
                readSteps()
            } else {
                Log.d(TAG, "Permissions denied: $deniedPermissions")
                showPermissionsDeniedDialog()
            }
        }
    }

    private fun showPermissionsDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("The app requires these permissions to function properly. Please grant all permissions.")
            .setPositiveButton("Retry") { dialog, which ->
                Log.d(TAG, "Retrying permission request")
                checkPermissionsAndSetupButton()
            }
            .setNegativeButton("Exit") { dialog, which ->
                Toast.makeText(this, "Permissions denied. App cannot function properly without these permissions.", Toast.LENGTH_LONG).show()
                finish() // Optionally exit the app if critical permissions are denied
            }
            .setCancelable(false)
            .show()
    }

    private fun setupInsertStepsButton() {
        insertStepsButton.setOnClickListener {
            Log.d(TAG, "Insert Steps button clicked")
            Toast.makeText(this, "Insert Steps button clicked", Toast.LENGTH_SHORT).show()
            insertSteps(500L, Instant.now().minusSeconds(7200), Instant.now()) // Example count and time
        }
    }

    private fun readSteps() {
        lifecycleScope.launch {
            try {
                val stepsCount = healthConnectManager.readSteps()
                // Update the UI with the total steps count
                stepsTextView.text = getString(R.string.steps_text, stepsCount)
                Toast.makeText(this@MainActivity, "Read steps: $stepsCount", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Error reading steps", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun insertSteps(count: Long, startTime: Instant, endTime: Instant) {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@MainActivity, "Inserting steps: count=$count", Toast.LENGTH_SHORT).show()
                healthConnectManager.insertSteps(count, startTime, endTime)
                Toast.makeText(this@MainActivity, "Steps inserted successfully", Toast.LENGTH_SHORT).show()
                readSteps()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Error inserting steps", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1
        private const val TAG = "MainActivity"
    }
}
