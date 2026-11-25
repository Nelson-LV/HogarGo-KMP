package com.hogargo.kmp.advice.data

import kotlinx.serialization.Serializable

@Serializable
data class AdviceSlipResponse(
    val slip: SlipContentDto,
)

@Serializable
data class SlipContentDto(
    val id: Long,
    val advice: String,
)

data class DailyAdvice(
    val id: Long,
    val advice: String,
    val isOffline: Boolean = false,
)
