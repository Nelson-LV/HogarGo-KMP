package com.hogargo.kmp

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.hogargo.kmp.data.DatabaseDriverFactory
import com.hogargo.kmp.household.notifications.HouseholdNotifier

class MainActivity : ComponentActivity() {
    // Sin permiso los avisos igual quedan como entregados; solo no se muestran.
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val notifier = HouseholdNotifier(applicationContext) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        setContent { App(DatabaseDriverFactory(applicationContext), notifier) }
    }
}
