package com.hogargo.kmp.household

import com.hogargo.kmp.household.data.HogarError
import com.hogargo.kmp.household.data.HouseholdCode
import com.hogargo.kmp.household.data.RecoveryUser
import com.hogargo.kmp.household.data.Sha256
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HouseholdRulesTest {
    @Test
    fun sha256MatchesKnownVectors() {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", Sha256.hex(""))
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", Sha256.hex("abc"))
        assertEquals(
            "248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1",
            Sha256.hex("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq"),
        )
        // 55 y 56 bytes: los dos casos limite del relleno.
        assertEquals(64, Sha256.hex("a".repeat(55)).length)
        assertEquals("9f4390f8d30c2dd92ec9f095b65e2b9ae9b0a925a5258e241c9f1e910f734318", Sha256.hex("a".repeat(55)))
        assertEquals("b35439a4ac6f0948b6d6f9e3c6af0f5f590ce20f1bde7090ef7970686ec6738a", Sha256.hex("a".repeat(56)))
    }

    @Test
    fun recoveryHashIsCaseAndSpaceInsensitive() {
        assertEquals(RecoveryUser.hash("Familia_Perez"), RecoveryUser.hash("  familia_perez "))
    }

    @Test
    fun recoveryValidation() {
        assertEquals(HogarError.RECOVERY_USER_INVALID, RecoveryUser.validate("abc", "Ana"))
        assertEquals(HogarError.RECOVERY_USER_SAME_AS_NAME, RecoveryUser.validate(" ANA1 ", "ana1"))
        assertNull(RecoveryUser.validate("familia24", "Ana"))
    }

    @Test
    fun householdCodeFormat() {
        assertEquals("HGR789", HouseholdCode.normalize("hgr-789 "))
        assertEquals("HGR-789", HouseholdCode.format("hgr789"))
        repeat(50) {
            val code = HouseholdCode.random()
            assertTrue(HouseholdCode.isValid(code))
            assertTrue(code.none { it in "IO01" })
        }
    }
}
