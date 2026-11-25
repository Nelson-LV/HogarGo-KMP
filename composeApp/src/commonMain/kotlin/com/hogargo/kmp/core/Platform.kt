package com.hogargo.kmp.core

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Identificador unico para filas nuevas. */
@OptIn(ExperimentalUuidApi::class)
fun newId(): String = Uuid.random().toString()

/** Marca de tiempo actual en milisegundos. */
@OptIn(ExperimentalTime::class)
fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

/** Fecha de hoy en la zona horaria del dispositivo. */
@OptIn(ExperimentalTime::class)
fun todayLocal(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
