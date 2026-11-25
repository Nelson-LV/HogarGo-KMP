package com.hogargo.kmp.household.data

import com.hogargo.kmp.db.HogarGoDatabase

/** Sesion del dispositivo: solo guarda memberId y householdId de quien la tiene abierta. */
class SessionStore(db: HogarGoDatabase) {
    data class Session(val memberId: String, val householdId: String)

    private val settings = db.hogarGoQueries
    private val household = db.householdQueries

    fun load(): Session? {
        val memberId = settings.selectSetting(MEMBER_KEY).executeAsOneOrNull() ?: return null
        val householdId = settings.selectSetting(HOUSEHOLD_KEY).executeAsOneOrNull() ?: return null
        return Session(memberId, householdId)
    }

    fun save(session: Session) {
        settings.transaction {
            settings.upsertSetting(MEMBER_KEY, session.memberId)
            settings.upsertSetting(HOUSEHOLD_KEY, session.householdId)
        }
    }

    fun clear() {
        household.transaction {
            household.deleteSetting(MEMBER_KEY)
            household.deleteSetting(HOUSEHOLD_KEY)
        }
    }

    private companion object {
        const val MEMBER_KEY = "session.memberId"
        const val HOUSEHOLD_KEY = "session.householdId"
    }
}
