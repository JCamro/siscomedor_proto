package com.example.siscomedor

import com.example.siscomedor.domain.checkCredentials
import com.example.siscomedor.domain.sanitizeCode
import com.example.siscomedor.domain.validateCode
import com.example.siscomedor.domain.validatePassword
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginValidationTest {
    @Test fun emptyFieldsAreRequired() {
        assertNotNull(validateCode(""))
        assertNotNull(validatePassword(""))
    }

    @Test fun shortValuesAreRejected() {
        assertNotNull(validateCode("2024"))
        assertNotNull(validatePassword("abc12"))
    }

    @Test fun nonNumericCodeIsRejectedAndFilteredOnInput() {
        assertNotNull(validateCode("2024a001"))
        assertEquals("20240001", sanitizeCode("2024-a0001xyz9"))
    }

    @Test fun validValuesHaveNoError() {
        assertNull(validateCode("20240001"))
        assertNull(validatePassword("comedor123"))
    }

    @Test fun wrongCredentialsAreRejected() {
        assertFalse(checkCredentials("20240002", "comedor123"))
        assertFalse(checkCredentials("20240001", "otraclave"))
    }

    @Test fun correctCredentialsAreAccepted() {
        assertTrue(checkCredentials("20240001", "comedor123"))
    }
}
