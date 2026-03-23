package com.mediacontrol.floatingwidget.debug

import android.content.Context
import com.mediacontrol.floatingwidget.storage.PreferencesStorage
import com.mediacontrol.floatingwidget.storage.SharedPreferencesStorage
import java.nio.charset.StandardCharsets
import java.util.Base64

interface DebugLogRepository {
    val retentionLimit: Int

    fun entries(): List<DebugLogEntry>

    fun addListener(listener: DebugLogListener)

    fun removeListener(listener: DebugLogListener)

    fun clear()
}

interface DebugLogWriter {
    fun log(
        level: DebugLogLevel,
        tag: String,
        message: String,
        details: String? = null
    )

    fun debug(tag: String, message: String, details: String? = null) {
        log(DebugLogLevel.Debug, tag, message, details)
    }

    fun info(tag: String, message: String, details: String? = null) {
        log(DebugLogLevel.Info, tag, message, details)
    }

    fun warn(tag: String, message: String, details: String? = null) {
        log(DebugLogLevel.Warning, tag, message, details)
    }

    fun error(tag: String, message: String, details: String? = null) {
        log(DebugLogLevel.Error, tag, message, details)
    }
}

object NoOpDebugLogWriter : DebugLogWriter {
    override fun log(level: DebugLogLevel, tag: String, message: String, details: String?) = Unit
}

class PreferencesDebugLogRepository(
    private val storage: PreferencesStorage,
    override val retentionLimit: Int,
    private val clock: () -> Long
) : DebugLogRepository, DebugLogWriter {

    constructor(
        context: Context,
        retentionLimit: Int = DEFAULT_RETENTION_LIMIT,
        clock: () -> Long = { System.currentTimeMillis() }
    ) : this(
        storage = SharedPreferencesStorage(
            context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        ),
        retentionLimit = retentionLimit,
        clock = clock
    )

    private val listeners = linkedSetOf<DebugLogListener>()

    @Volatile
    private var cachedEntries: List<DebugLogEntry> = DebugLogStorageFormat.decode(storage.getString(KEY_ENTRIES, null))
        .takeLast(retentionLimit)

    override fun entries(): List<DebugLogEntry> = cachedEntries

    override fun addListener(listener: DebugLogListener) {
        listeners += listener
        listener.onEntriesChanged(cachedEntries)
    }

    override fun removeListener(listener: DebugLogListener) {
        listeners -= listener
    }

    @Synchronized
    override fun clear() {
        cachedEntries = emptyList()
        persist()
        notifyListeners()
    }

    @Synchronized
    override fun log(level: DebugLogLevel, tag: String, message: String, details: String?) {
        cachedEntries = (cachedEntries + DebugLogEntry(
            timestampEpochMillis = clock(),
            level = level,
            tag = tag,
            message = message,
            details = details
        )).takeLast(retentionLimit)
        persist()
        notifyListeners()
    }

    private fun persist() {
        storage.edit {
            putString(KEY_ENTRIES, DebugLogStorageFormat.encode(cachedEntries))
        }
    }

    private fun notifyListeners() {
        val snapshot = cachedEntries
        listeners.forEach { it.onEntriesChanged(snapshot) }
    }

    private companion object {
        const val PREFERENCES_NAME = "debug_logs"
        const val KEY_ENTRIES = "entries"
        const val DEFAULT_RETENTION_LIMIT = 200
    }
}

object DebugLogStorageFormat {
    private const val ENTRY_SEPARATOR = "\n"
    private const val FIELD_SEPARATOR = "|"

    fun encode(entries: List<DebugLogEntry>): String {
        return entries.joinToString(separator = ENTRY_SEPARATOR, transform = ::encodeEntry)
    }

    fun decode(raw: String?): List<DebugLogEntry> {
        if (raw.isNullOrBlank()) {
            return emptyList()
        }

        return raw.split(ENTRY_SEPARATOR)
            .mapNotNull(::decodeEntry)
    }

    private fun encodeEntry(entry: DebugLogEntry): String {
        return listOf(
            entry.timestampEpochMillis.toString(),
            entry.level.name,
            encodeField(entry.tag),
            encodeField(entry.message),
            encodeField(entry.details)
        ).joinToString(separator = FIELD_SEPARATOR)
    }

    private fun decodeEntry(encoded: String): DebugLogEntry? {
        val parts = encoded.split(FIELD_SEPARATOR)
        if (parts.size != 5) {
            return null
        }

        val timestamp = parts[0].toLongOrNull() ?: return null
        val level = DebugLogLevel.entries.firstOrNull { it.name == parts[1] } ?: return null
        val tag = decodeField(parts[2]) ?: return null
        val message = decodeField(parts[3]) ?: return null

        return DebugLogEntry(
            timestampEpochMillis = timestamp,
            level = level,
            tag = tag,
            message = message,
            details = decodeField(parts[4])
        )
    }

    private fun encodeField(value: String?): String {
        if (value == null) {
            return ""
        }

        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(value.toByteArray(StandardCharsets.UTF_8))
    }

    private fun decodeField(value: String): String? {
        if (value.isEmpty()) {
            return null
        }

        return String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8)
    }
}
