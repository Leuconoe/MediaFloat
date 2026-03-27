package sw2.io.mediafloat.state

import sw2.io.mediafloat.debug.DebugLogEntry
import sw2.io.mediafloat.debug.DebugLogListener
import sw2.io.mediafloat.debug.DebugLogRepository

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
