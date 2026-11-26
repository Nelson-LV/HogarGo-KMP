package com.hogargo.kmp.advice.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun createAdviceHttpClient(): HttpClient = HttpClient(Darwin)
