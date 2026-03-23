package com.mediacontrol.floatingwidget.debug

import com.mediacontrol.floatingwidget.storage.TestPreferencesStorage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DebugLogRepositoryTest {

    @Test
    fun storageFormat_roundTripsEntriesWithDetails() {
        val entry = DebugLogEntry(
            timestampEpochMillis = 1234L,
            level = DebugLogLevel.Warning,
            tag = "OverlayService",
            message = "Runtime blocked",
            details = "reason=MissingOverlayAccess"
        )

        val decoded = DebugLogStorageFormat.decode(DebugLogStorageFormat.encode(listOf(entry)))

        assertEquals(listOf(entry), decoded)
    }

    @Test
    fun repository_appliesRetentionLimitToRecentEntries() {
        val repository = PreferencesDebugLogRepository(
            storage = TestPreferencesStorage(),
            retentionLimit = 2,
            clock = { nextTimestamp++ }
        )

        repository.info("A", "First")
        repository.warn("B", "Second")
        repository.error("C", "Third")

        assertEquals(listOf("Second", "Third"), repository.entries().map { it.message })
    }

    @Test
    fun storageFormat_handlesNullDetails() {
        val entry = DebugLogEntry(
            timestampEpochMillis = 999L,
            level = DebugLogLevel.Debug,
            tag = "MediaDispatcher",
            message = "Ignored command"
        )

        val decoded = DebugLogStorageFormat.decode(DebugLogStorageFormat.encode(listOf(entry))).single()

        assertNull(decoded.details)
    }

    companion object {
        private var nextTimestamp = 1L
    }
}
