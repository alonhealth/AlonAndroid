package com.alonhealth.android

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.permission.Permission

/**
 * Helper class for handling health data permissions
 */
class HealthPermissionHelper(private val context: Context) {

    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    /**
     * Checks if all required permissions are granted
     * @param permissions the set of permissions to check
     * @return true if all permissions are granted, false otherwise
     */
    suspend fun hasAllPermissions(permissions: Set<Permission>): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions().containsAll(permissions)
    }

    /**
     * Creates an activity contract for requesting health permissions
     * @return an activity result contract for requesting permissions
     */
    fun createPermissionRequestContract(): ActivityResultContract<Set<Permission>, Set<Permission>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    /**
     * Opens the Health Connect settings page if available
     * @return true if the settings page was opened, false otherwise
     */
    fun openHealthConnectSettings(): Boolean {
        if (HealthConnectClient.isAvailable(context)) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse("health-connect://settings")
            if (intent.resolveActivity(context.packageManager) != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            }
        }
        return false
    }

    /**
     * Opens the Google Play Store page for Health Connect if it's not installed
     */
    fun openHealthConnectPlayStore() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
        context.startActivity(intent)
    }
} 