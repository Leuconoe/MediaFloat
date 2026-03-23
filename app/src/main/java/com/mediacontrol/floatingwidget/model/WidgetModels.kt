package com.mediacontrol.floatingwidget.model

enum class WidgetButton {
    Previous,
    PlayPause,
    Next
}

enum class DragHandlePlacement {
    Right
}

enum class WidgetSizePreset(
    val widthDp: Int,
    val heightDp: Int
) {
    Compact(widthDp = 188, heightDp = 56),
    Standard(widthDp = 228, heightDp = 64),
    Large(widthDp = 268, heightDp = 72)
}

enum class WidgetAnchor {
    Start,
    End
}

data class WidgetPosition(
    val anchor: WidgetAnchor = WidgetAnchor.End,
    val xOffsetDp: Int = 24,
    val yOffsetDp: Int = 160
)

data class WidgetLayout(
    val visibleButtons: Set<WidgetButton>,
    val dragHandlePlacement: DragHandlePlacement = DragHandlePlacement.Right
) {
    init {
        require(isSupported()) {
            "Only planned horizontal layouts with a right-side drag handle are supported."
        }
    }

    val orderedButtons: List<WidgetButton>
        get() = WidgetButton.entries.filter { it in visibleButtons }

    fun isSupported(): Boolean {
        return dragHandlePlacement == DragHandlePlacement.Right && visibleButtons in supportedButtonSets
    }

    companion object {
        val supportedButtonSets: Set<Set<WidgetButton>> = setOf(
            setOf(WidgetButton.PlayPause),
            setOf(WidgetButton.Previous, WidgetButton.PlayPause),
            setOf(WidgetButton.PlayPause, WidgetButton.Next),
            setOf(WidgetButton.Previous, WidgetButton.PlayPause, WidgetButton.Next)
        )

        val Default = WidgetLayout(
            visibleButtons = setOf(WidgetButton.Previous, WidgetButton.PlayPause, WidgetButton.Next)
        )
    }
}

data class WidgetConfig(
    val layout: WidgetLayout = WidgetLayout.Default,
    val sizePreset: WidgetSizePreset = WidgetSizePreset.Standard,
    val persistentOverlayEnabled: Boolean = true
)
