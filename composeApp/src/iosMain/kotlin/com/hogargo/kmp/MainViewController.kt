package com.hogargo.kmp

import androidx.compose.ui.window.ComposeUIViewController
import com.hogargo.kmp.data.DatabaseDriverFactory

@Suppress("unused", "FunctionName")
fun MainViewController() = ComposeUIViewController { App(DatabaseDriverFactory()) }
