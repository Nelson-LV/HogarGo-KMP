package com.hogargo.kmp.di

import com.hogargo.kmp.db.HogarGoDatabase
import com.hogargo.kmp.finance.FinanceRepository

/** Repositorios compartidos de la app, creados una sola vez sobre la misma base de datos. */
class AppModules(database: HogarGoDatabase) {
    val finance = FinanceRepository(database)
}
