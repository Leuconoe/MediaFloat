package sw2.io.mediafloat.overlay

internal class TripleTapDetector(
    private val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
    private val requiredTapCount: Int = REQUIRED_TAP_COUNT,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private var lastTapTimeMillis: Long = 0L
    private var tapCount: Int = 0

    fun recordTap(): Boolean {
        val now = clock()
        tapCount = if (lastTapTimeMillis > 0L && now - lastTapTimeMillis <= timeoutMillis) {
            tapCount + 1
        } else {
            1
        }
        lastTapTimeMillis = now

        if (tapCount >= requiredTapCount) {
            reset()
            return true
        }

        return false
    }

    fun reset() {
        tapCount = 0
        lastTapTimeMillis = 0L
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MILLIS = 500L
        const val REQUIRED_TAP_COUNT = 3
    }
}
