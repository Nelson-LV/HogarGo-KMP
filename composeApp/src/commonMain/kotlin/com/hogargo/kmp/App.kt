package com.hogargo.kmp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hogargo.kmp.data.DatabaseDriverFactory
import com.hogargo.kmp.db.HogarGoDatabase
import com.hogargo.kmp.di.AppModules
import com.hogargo.kmp.household.HouseholdViewModel
import com.hogargo.kmp.household.SessionPhase
import com.hogargo.kmp.household.data.HouseholdRepository
import com.hogargo.kmp.household.data.SessionStore
import com.hogargo.kmp.household.notifications.HouseholdNotifier
import com.hogargo.kmp.household.ui.HogarGoColors
import com.hogargo.kmp.navigation.AppNavHost
import com.hogargo.kmp.navigation.LoadingScreen
import com.hogargo.kmp.navigation.WelcomeNavHost
import com.hogargo.kmp.tasks.ui.TaskViewModel
import com.hogargo.kmp.ui.theme.HogarGoTheme

@Composable
fun App(driverFactory: DatabaseDriverFactory, notifier: HouseholdNotifier) {
    val database = remember { HogarGoDatabase(driverFactory.createDriver()) }
    val modules = remember { AppModules(database) }

    val householdViewModel = viewModel {
        HouseholdViewModel(HouseholdRepository(database), SessionStore(database), notifier)
    }
    val taskViewModel = viewModel {
        TaskViewModel(modules.task, householdViewModel, notifier)
    }

    val phase by householdViewModel.phase.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(householdViewModel) {
        householdViewModel.toasts.collect { message ->
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(message)
        }
    }

    HogarGoTheme {
        Box(Modifier.fillMaxSize().background(HogarGoColors.background)) {
            when (val current = phase) {
                SessionPhase.Loading -> LoadingScreen()
                SessionPhase.Welcome -> WelcomeNavHost(householdViewModel)
                // La clave por hogar evita mezclar datos entre hogares.
                is SessionPhase.SignedIn -> key(current.householdId) {
                    AppNavHost(modules, householdViewModel, current.householdId, current.memberId, taskViewModel)
                }
            }
            SnackbarHost(
                hostState = snackbar,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(bottom = 88.dp),
            ) { data ->
                Snackbar(data, containerColor = HogarGoColors.primaryText.copy(alpha = 0.92f), contentColor = Color.White)
            }
        }
    }
}
