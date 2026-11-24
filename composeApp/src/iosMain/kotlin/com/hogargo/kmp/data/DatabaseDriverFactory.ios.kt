package com.hogargo.kmp.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.hogargo.kmp.db.HogarGoDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(HogarGoDatabase.Schema, "hogargo.db")
}
