package sw2.io.mediafloat.overlay

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TripleTapDetectorTest {

    @Test
    fun recordTap_triggersOnThirdTapInsideTimeout() {
        var now = 1_000L
        val detector = TripleTapDetector(clock = { now })

        assertFalse(detector.recordTap())
        now += 120L
        assertFalse(detector.recordTap())
        now += 120L
        assertTrue(detector.recordTap())
    }

    @Test
    fun recordTap_resetsWhenTapIsOutsideTimeout() {
        var now = 1_000L
        val detector = TripleTapDetector(clock = { now })

        assertFalse(detector.recordTap())
        now += 600L
        assertFalse(detector.recordTap())
        now += 120L
        assertFalse(detector.recordTap())
    }

    @Test
    fun reset_clearsPartialTapSequence() {
        var now = 1_000L
        val detector = TripleTapDetector(clock = { now })

        assertFalse(detector.recordTap())
        now += 120L
        assertFalse(detector.recordTap())

        detector.reset()

        now += 120L
        assertFalse(detector.recordTap())
    }
}
