package com.hogargo.kmp.advice.data

import io.ktor.client.HttpClient

expect fun createAdviceHttpClient(): HttpClient
