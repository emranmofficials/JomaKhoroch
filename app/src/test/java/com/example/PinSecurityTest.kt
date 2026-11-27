package com.example

import com.example.infrastructure.PinSecurityManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinSecurityTest {

    @Test
    fun `salted PBKDF2 PIN hashing verifies correct PIN and rejects wrong PIN`() {
        val first = PinSecurityManager.hashPin("4829")
        val second = PinSecurityManager.hashPin("4829")

        // Random per-hash salt guarantees distinct hashes for identical PINs
        assertNotEquals(first.saltBase64, second.saltBase64)
        assertNotEquals(first.hashBase64, second.hashBase64)

        assertTrue(PinSecurityManager.verifyPin("4829", first.saltBase64, first.hashBase64))
        assertFalse(PinSecurityManager.verifyPin("1234", first.saltBase64, first.hashBase64))
        assertFalse(PinSecurityManager.verifyPin("", first.saltBase64, first.hashBase64))
    }
}
