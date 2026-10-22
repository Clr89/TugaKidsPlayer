package com.clr89.tugakidsplayer.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppPreferencesTest {
    @Test
    fun missingOrUnknownThemePreferenceKeepsSystemDefaultBehavior() {
        val missingValue: String? = null
        assertNull(missingValue.toDarkThemePreference())
        assertNull("unexpected".toDarkThemePreference())
    }

    @Test
    fun savedThemePreferenceRoundTripsLightAndDarkSelections() {
        assertEquals(true, true.toThemePreferenceValue().toDarkThemePreference())
        assertEquals(false, false.toThemePreferenceValue().toDarkThemePreference())
    }
}
