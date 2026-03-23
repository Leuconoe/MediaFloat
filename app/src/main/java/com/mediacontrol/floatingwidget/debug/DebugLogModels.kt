package com.mediacontrol.floatingwidget.debug

enum class DebugLogLevel {
    Debug,
    Info,
    Warning,
    Error
}

data class DebugLogEntry(
    val timestampEpochMillis: Long,
    val level: DebugLogLevel,
    val tag: String,
    val message: String,
    val details: String? = null
)

fun interface DebugLogListener {
    fun onEntriesChanged(entries: List<DebugLogEntry>)
}
