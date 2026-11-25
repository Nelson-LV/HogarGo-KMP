package com.hogargo.kmp.di

import com.hogargo.kmp.calendar.CalendarRepository
import com.hogargo.kmp.db.HogarGoDatabase
import com.hogargo.kmp.finance.FinanceRepository
import com.hogargo.kmp.pet.PetRepository

/** Repositorios compartidos de la app, creados una sola vez sobre la misma base de datos. */
class AppModules(database: HogarGoDatabase) {
    val finance = FinanceRepository(database)
    val pet = PetRepository(database)
    val calendar = CalendarRepository(database)
}
