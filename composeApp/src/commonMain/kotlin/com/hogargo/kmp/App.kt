package com.hogargo.kmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.hogargo.kmp.data.DatabaseDriverFactory
import com.hogargo.kmp.data.HogarGoRepository
import com.hogargo.kmp.navigation.AppNavHost
import com.hogargo.kmp.ui.theme.HogarGoTheme

@Composable
fun App(driverFactory: DatabaseDriverFactory) {
    val repository = remember { HogarGoRepository(driverFactory) }
    HogarGoTheme {
        AppNavHost(repository)
    }
}
