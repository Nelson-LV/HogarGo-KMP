package com.hogargo.kmp.household

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.hogargo.kmp.db.HogarGoDatabase
import com.hogargo.kmp.household.data.HogarError
import com.hogargo.kmp.household.data.HogarException
import com.hogargo.kmp.household.data.HouseholdRepository
import com.hogargo.kmp.household.data.HouseholdRepository.LeaveOutcome
import com.hogargo.kmp.household.data.NoticeType
import com.hogargo.kmp.household.data.SessionStore
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HouseholdRepositoryTest {
    private lateinit var db: HogarGoDatabase
    private lateinit var repo: HouseholdRepository
    private var clock = 1_000L

    @BeforeTest
    fun setUp() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        HogarGoDatabase.Schema.create(driver)
        db = HogarGoDatabase(driver)
        repo = HouseholdRepository(db) { clock++ }
    }

    private fun expect(error: HogarError, block: () -> Unit) {
        assertEquals(error, assertFailsWith<HogarException> { block() }.error)
    }

    private fun pendingTypes(memberId: String) =
        db.householdQueries.pendingNotices(memberId).executeAsList().map { it.type }

    /** Crea "Casa" con Ana (admin) y deja a Beto aprobado. */
    private fun homeWithTwo(): Pair<String, String> {
        val ana = repo.createHousehold("Ana", "Casa", "ana_rec", "HGR789")
        repo.joinHousehold("Beto", "beto_rec", "hgr-789")
        val beto = repo.members(ana.householdId).first { it.name == "Beto" }
        repo.acceptRequest(ana.id, beto.id)
        return ana.id to beto.id
    }

    @Test
    fun createMakesAdminAndValidates() {
        val ana = repo.createHousehold(" Ana ", "Casa", "ana_rec", "hgr-789")
        assertTrue(ana.isAdmin && ana.isApproved)
        assertEquals("Ana", ana.name)
        assertEquals("HGR789", repo.household(ana.householdId)!!.code)
        expect(HogarError.CODE_TAKEN) { repo.createHousehold("Luz", "Otra", "luz_rec", "HGR789") }
        expect(HogarError.RECOVERY_USER_SAME_AS_NAME) { repo.createHousehold("Lucia", "Otra", " LUCIA ", "ABC234") }
        expect(HogarError.INVALID_CODE) { repo.createHousehold("Luz", "Otra", "luz_rec", "AB") }
    }

    @Test
    fun joinIsPendingAndNotifiesAdmin() {
        val ana = repo.createHousehold("Ana", "Casa", "ana_rec", "HGR789")
        repo.joinHousehold("Beto", "beto_rec", "HGR789")
        expect(HogarError.PENDING_APPROVAL) { repo.login("beto", "HGR789") }
        assertEquals(listOf(NoticeType.JOIN_REQUEST.name), pendingTypes(ana.id))
        expect(HogarError.NAME_TAKEN) { repo.joinHousehold("BETO", "otro_rec", "HGR789") }
        expect(HogarError.RECOVERY_USER_TAKEN) { repo.joinHousehold("Caro", "beto_rec", "HGR789") }
        // El nombre de alguien no puede ser el usuario de recuperacion de otra persona.
        expect(HogarError.NAME_TAKEN) { repo.joinHousehold("ana_rec", "caro_rec", "HGR789") }
        expect(HogarError.CODE_NOT_FOUND) { repo.joinHousehold("Caro", "caro_rec", "ZZZ999") }
    }

    @Test
    fun loginByNameOrUniqueRecoveryUser() {
        val (anaId, betoId) = homeWithTwo()
        assertEquals(anaId, repo.login("ANA", "HGR789").id)
        assertEquals(betoId, repo.login(" Beto_Rec ", "HGR789").id)
        expect(HogarError.MEMBER_NOT_FOUND) { repo.login("nadie", "HGR789") }
    }

    @Test
    fun onlyAdminCanManage() {
        val (anaId, betoId) = homeWithTwo()
        expect(HogarError.NOT_ALLOWED) { repo.renameHousehold(betoId, repo.member(betoId)!!.householdId, "Mia") }
        expect(HogarError.NOT_ALLOWED) { repo.removeMember(betoId, anaId) }
        expect(HogarError.NOT_ALLOWED) { repo.removeMember(anaId, anaId) }
    }

    @Test
    fun renameNotifiesMembers() {
        val (anaId, betoId) = homeWithTwo()
        val householdId = repo.member(anaId)!!.householdId
        repo.renameHousehold(anaId, householdId, "Casa Nueva")
        assertEquals("Casa Nueva", repo.household(householdId)!!.name)
        assertTrue(NoticeType.RENAMED.name in pendingTypes(betoId))
        assertFalse(NoticeType.RENAMED.name in pendingTypes(anaId))
    }

    @Test
    fun removedMemberSeesWhyAndCanRejoin() {
        val (anaId, betoId) = homeWithTwo()
        repo.removeMember(anaId, betoId)
        assertNull(repo.member(betoId))
        expect(HogarError.REMOVED_FROM_HOUSEHOLD) { repo.login("Beto", "HGR789") }
        repo.joinHousehold("Beto", "beto_rec", "HGR789")
        expect(HogarError.PENDING_APPROVAL) { repo.login("Beto", "HGR789") }
    }

    @Test
    fun adminLeavingPassesRoleToOldest() {
        val (anaId, betoId) = homeWithTwo()
        assertEquals(LeaveOutcome.LEFT, repo.leaveHousehold(anaId))
        val beto = repo.member(betoId)!!
        assertTrue(beto.isAdmin)
        assertTrue(NoticeType.ADMIN.name in pendingTypes(betoId))
    }

    @Test
    fun lastPersonLeavingDeletesHome() {
        val ana = repo.createHousehold("Ana", "Casa", "ana_rec", "HGR789")
        assertEquals(LeaveOutcome.HOUSEHOLD_DELETED, repo.leaveHousehold(ana.id))
        assertNull(repo.household(ana.householdId))
        assertTrue(repo.members(ana.householdId).isEmpty())
    }

    @Test
    fun recoverListsOnlyApprovedHomes() {
        repo.createHousehold("Ana", "Casa", "familia24", "HGR789")
        repo.createHousehold("Ana", "Finca", "familia24", "FNC234")
        val homes = repo.recoverHomes("FAMILIA24")
        assertEquals(listOf("Casa", "Finca"), homes.map { it.householdName })
        assertTrue(repo.recoverHomes("otro").isEmpty())
    }

    @Test
    fun changeNameAndRecoveryRules() {
        val (anaId, _) = homeWithTwo()
        expect(HogarError.NAME_TAKEN) { repo.changeMyName(anaId, "beto") }
        expect(HogarError.NAME_SAME_AS_RECOVERY_USER) { repo.changeMyName(anaId, "ANA_REC") }
        repo.changeMyName(anaId, "Ana María")
        assertEquals("ana maría", repo.member(anaId)!!.nameKey)

        expect(HogarError.RECOVERY_CURRENT_WRONG) { repo.changeRecoveryUser(anaId, "mal", "nuevo_rec", "nuevo_rec") }
        expect(HogarError.RECOVERY_MISMATCH) { repo.changeRecoveryUser(anaId, "ana_rec", "nuevo_rec", "otro_rec") }
        expect(HogarError.RECOVERY_SAME_AS_CURRENT) { repo.changeRecoveryUser(anaId, "ana_rec", "ANA_REC", "ana_rec") }
        expect(HogarError.RECOVERY_USER_TAKEN) { repo.changeRecoveryUser(anaId, "ana_rec", "beto_rec", "beto_rec") }
        repo.changeRecoveryUser(anaId, "ana_rec", "nuevo_rec", "nuevo_rec")
        assertEquals(anaId, repo.login("nuevo_rec", "HGR789").id)
    }

    @Test
    fun claimDeliversNoticeOnlyOnce() {
        val ana = repo.createHousehold("Ana", "Casa", "ana_rec", "HGR789")
        repo.joinHousehold("Beto", "beto_rec", "HGR789")
        val notice = db.householdQueries.pendingNotices(ana.id).executeAsList().single()
        assertTrue(repo.claim(notice.id))
        assertFalse(repo.claim(notice.id))
        assertTrue(pendingTypes(ana.id).isEmpty())
    }

    @Test
    fun sessionStoreRoundTrip() {
        val store = SessionStore(db)
        assertNull(store.load())
        store.save(SessionStore.Session("m1", "h1"))
        assertEquals(SessionStore.Session("m1", "h1"), store.load())
        store.clear()
        assertNull(store.load())
    }
}
