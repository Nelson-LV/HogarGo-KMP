package com.hogargo.kmp.advice

import com.hogargo.kmp.advice.data.DailyAdviceRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DailyAdviceRepositoryTest {

    @Test
    fun fetchRandomAdvice_success_returnsParsedAdvice() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"slip": {"id": 42, "advice": "Always listen to your family."}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val repository = DailyAdviceRepository(client)
        val result = repository.fetchRandomAdvice()

        assertTrue(result.isSuccess)
        val advice = result.getOrThrow()
        assertEquals(42L, advice.id)
        assertEquals("Always listen to your family.", advice.advice)
        assertFalse(advice.isOffline)
    }

    @Test
    fun fetchRandomAdvice_serverError_returnsFailure() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError,
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val repository = DailyAdviceRepository(client)
        val result = repository.fetchRandomAdvice()

        assertTrue(result.isFailure)
        val fallback = repository.getRandomFallbackAdvice()
        assertTrue(fallback.isOffline)
        assertTrue(fallback.advice.isNotEmpty())
    }

    @Test
    fun fallbackAdvices_areNotEmpty() {
        val repository = DailyAdviceRepository()
        val fallback = repository.getRandomFallbackAdvice()
        assertTrue(fallback.advice.isNotBlank())
        assertTrue(fallback.isOffline)
    }
}
