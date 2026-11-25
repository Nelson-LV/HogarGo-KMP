package com.hogargo.kmp

import androidx.compose.ui.window.ComposeUIViewController
import com.hogargo.kmp.data.DatabaseDriverFactory
import com.hogargo.kmp.household.notifications.HouseholdNotifier

private val notifier by lazy { HouseholdNotifier() }

@Suppress("unused", "FunctionName")
fun MainViewController() = ComposeUIViewController { App(DatabaseDriverFactory(), notifier) }
