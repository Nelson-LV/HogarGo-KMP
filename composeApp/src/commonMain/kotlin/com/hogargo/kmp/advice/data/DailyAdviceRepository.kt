package com.hogargo.kmp.advice.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.random.Random

class DailyAdviceRepository(
    private val client: HttpClient = defaultAdviceHttpClient(),
) {
    companion object {
        const val ADVICE_URL = "https://api.adviceslip.com/advice"

        val FALLBACK_ADVICES = listOf(
            DailyAdvice(1, "Trabajar en equipo divide el trabajo y multiplica los resultados.", isOffline = true),
            DailyAdvice(2, "El orden en el hogar trae paz mental y armonía familiar.", isOffline = true),
            DailyAdvice(3, "Un pequeño hábito diario transforma tu hogar a largo plazo.", isOffline = true),
            DailyAdvice(4, "La comunicación clara evita malentendidos en la convivencia.", isOffline = true),
            DailyAdvice(5, "Celebrar los pequeños logros diarios fortalece la unión del hogar.", isOffline = true),
            DailyAdvice(6, "Agradecer a quienes comparten tu espacio mejora el ambiente de convivencia.", isOffline = true),
            DailyAdvice(7, "Repartir las responsabilidades a tiempo evita la sobrecarga al final de la semana.", isOffline = true),
        )

        fun defaultAdviceHttpClient(): HttpClient = createAdviceHttpClient().config {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    suspend fun fetchRandomAdvice(): Result<DailyAdvice> = runCatching {
        val response: AdviceSlipResponse = client.get(ADVICE_URL).body()
        DailyAdvice(
            id = response.slip.id,
            advice = response.slip.advice,
            isOffline = false,
        )
    }

    fun getRandomFallbackAdvice(): DailyAdvice {
        val index = Random.nextInt(FALLBACK_ADVICES.size)
        return FALLBACK_ADVICES[index]
    }
}
