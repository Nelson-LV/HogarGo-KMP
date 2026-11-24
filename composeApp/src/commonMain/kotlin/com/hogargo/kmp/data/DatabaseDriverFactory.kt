package com.hogargo.kmp.data

import app.cash.sqldelight.db.SqlDriver

/** Cada plataforma aporta su driver SQLite (Android: AndroidSqliteDriver, iOS: NativeSqliteDriver). */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
