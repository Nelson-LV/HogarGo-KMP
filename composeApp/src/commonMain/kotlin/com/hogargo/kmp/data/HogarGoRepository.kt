package com.hogargo.kmp.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import com.hogargo.kmp.db.HogarGoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Punto unico de acceso a datos (SQLDelight). Los modulos de cada integrante
 * (finanzas, mascotas, calendario) agregan aqui sus operaciones.
 */
class HogarGoRepository(db: HogarGoDatabase) {
    private val q = db.hogarGoQueries

    val expenseCount: Flow<Long> = q.countExpenses().asFlow().mapToOne(Dispatchers.Default)
    val petCount: Flow<Long> = q.countPets().asFlow().mapToOne(Dispatchers.Default)
    val eventCount: Flow<Long> = q.countEvents().asFlow().mapToOne(Dispatchers.Default)

    @OptIn(ExperimentalTime::class)
    fun addSampleExpense() =
        q.insertExpense("Gasto de prueba", 10.0, "General", Clock.System.now().toEpochMilliseconds())

    fun addSamplePet() = q.insertPet("Mascota de prueba", "Perro", null)

    fun addSampleEvent() = q.insertEvent("Evento de prueba", "2026-01-01", null)
}
