package com.mediacontrol.floatingwidget.state

import com.mediacontrol.floatingwidget.debug.DebugLogEntry
import com.mediacontrol.floatingwidget.debug.DebugLogListener
import com.mediacontrol.floatingwidget.debug.DebugLogRepository

data class DebugLogScreenState(
    val retentionLimit: Int,
    val entries: List<DebugLogEntry>
)

class DebugLogStateHolder(
    private val repository: DebugLogRepository
) : ObservableStateHolder<DebugLogScreenState>(
    DebugLogScreenState(
        retentionLimit = repository.retentionLimit,
        entries = repository.entries()
    )
) {

    private val listener = DebugLogListener { entries ->
        updateState(
            DebugLogScreenState(
                retentionLimit = repository.retentionLimit,
                entries = entries
            )
        )
    }

    init {
        repository.addListener(listener)
    }

    fun clear() {
        repository.clear()
    }

    fun close() {
        repository.removeListener(listener)
    }
}
