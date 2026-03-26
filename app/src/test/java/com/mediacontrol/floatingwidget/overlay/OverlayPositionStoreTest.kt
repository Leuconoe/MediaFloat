package sw2.io.mediafloat.overlay

import sw2.io.mediafloat.model.WidgetAnchor
import sw2.io.mediafloat.model.WidgetPosition
import sw2.io.mediafloat.storage.TestPreferencesStorage
import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayPositionStoreTest {

    @Test
    fun load_returnsSavedPosition() {
        val store = OverlayPositionStore(TestPreferencesStorage(), density = 2f)
        val expected = WidgetPosition(
            anchor = WidgetAnchor.Start,
            xOffsetDp = 12,
            yOffsetDp = 48
        )

        store.save(expected)

        assertEquals(expected, store.load())
    }

    @Test
    fun densityConversions_roundTripDpAndPx() {
        val store = OverlayPositionStore(TestPreferencesStorage(), density = 2f)

        assertEquals(24, store.dpToPx(12))
        assertEquals(12, store.pxToDp(24))
    }
}
