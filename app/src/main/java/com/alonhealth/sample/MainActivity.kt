package com.alonhealth.sample

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alonhealth.android.AlonAndroid
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var alonAndroid: AlonAndroid
    
    private lateinit var requestPermissionsButton: Button
    private lateinit var calculateScoreButton: Button
    private lateinit var healthScoreTextView: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize views
        requestPermissionsButton = findViewById(R.id.requestPermissionsButton)
        calculateScoreButton = findViewById(R.id.calculateScoreButton)
        healthScoreTextView = findViewById(R.id.healthScoreTextView)
        
        // Initialize AlonAndroid
        alonAndroid = AlonAndroid(this)
        
        // Set up button click listeners
        requestPermissionsButton.setOnClickListener {
            requestPermissions()
        }
        
        calculateScoreButton.setOnClickListener {
            calculateHealthScore()
        }
        
        // Initially disable the calculate button until permissions are granted
        calculateScoreButton.isEnabled = false
    }
    
    private fun requestPermissions() {
        alonAndroid.requestAuthorization { success, error ->
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
                    calculateScoreButton.isEnabled = true
                } else {
                    Toast.makeText(this, "Permission error: ${error?.message}", Toast.LENGTH_LONG).show()
                    calculateScoreButton.isEnabled = false
                }
            }
        }
    }
    
    private fun calculateHealthScore() {
        lifecycleScope.launch {
            try {
                alonAndroid.calculateHealthScore { score ->
                    runOnUiThread {
                        healthScoreTextView.text = score.toString()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    healthScoreTextView.text = "Error: ${e.message}"
                }
            }
        }
    }
} 