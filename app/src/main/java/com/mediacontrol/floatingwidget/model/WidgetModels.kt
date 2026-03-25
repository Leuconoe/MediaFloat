package com.mediacontrol.floatingwidget.model

enum class WidgetButton {
    Previous,
    PlayPause,
    Next
}

enum class DragHandlePlacement {
    Left,
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

enum class WidgetWidthStyle {
    Regular,
    Wide
}

enum class WidgetThemePreset {
    Light,
    Dark,
    DarkBlue,
    MediumYellow,
    Pink
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
        return dragHandlePlacement in DragHandlePlacement.entries && visibleButtons in supportedButtonSets
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
    val widthStyle: WidgetWidthStyle = WidgetWidthStyle.Regular,
    val themePreset: WidgetThemePreset = WidgetThemePreset.Dark,
    val opacity: Float = 1f,
    val persistentOverlayEnabled: Boolean = true
)

data class WidgetOverlaySizing(
    val containerWidthDp: Int,
    val containerHeightDp: Int,
    val containerStartPaddingDp: Int,
    val containerEndPaddingDp: Int,
    val containerVerticalPaddingDp: Int,
    val containerCornerRadiusDp: Int,
    val itemSpacingDp: Int,
    val buttonWidthDp: Int,
    val buttonHeightDp: Int,
    val buttonIconPaddingDp: Int,
    val handleWidthDp: Int,
    val handleHeightDp: Int,
    val handleCornerRadiusDp: Int,
    val handleTextSizeSp: Float
)

data class WidgetOverlayColors(
    val surfaceColor: Int,
    val surfaceStrokeColor: Int,
    val buttonEnabledColor: Int,
    val buttonDisabledColor: Int,
    val iconEnabledColor: Int,
    val iconDisabledColor: Int,
    val handleColor: Int,
    val handleTextColor: Int
)

data class WidgetOverlayAppearance(
    val sizing: WidgetOverlaySizing,
    val colors: WidgetOverlayColors
)

fun WidgetConfig.overlayAppearance(): WidgetOverlayAppearance {
    return WidgetOverlayAppearance(
        sizing = overlaySizing(),
        colors = themePreset.overlayColors()
    )
}

private fun WidgetConfig.overlaySizing(): WidgetOverlaySizing {
    val buttonHeightDp = when (sizePreset) {
        WidgetSizePreset.Compact -> 36
        WidgetSizePreset.Standard -> 40
        WidgetSizePreset.Large -> 44
    }
    val regularButtonWidthDp = buttonHeightDp
    val wideButtonExtraDp = when (sizePreset) {
        WidgetSizePreset.Compact -> 10
        WidgetSizePreset.Standard -> 12
        WidgetSizePreset.Large -> 14
    }
    val regularHandleWidthDp = when (sizePreset) {
        WidgetSizePreset.Compact -> 24
        WidgetSizePreset.Standard -> 28
        WidgetSizePreset.Large -> 32
    }
    val wideHandleExtraDp = when (sizePreset) {
        WidgetSizePreset.Compact -> 4
        WidgetSizePreset.Standard -> 6
        WidgetSizePreset.Large -> 6
    }
    val containerWidthDeltaDp = when (sizePreset) {
        WidgetSizePreset.Compact -> 32
        WidgetSizePreset.Standard -> 40
        WidgetSizePreset.Large -> 48
    }
    val containerStartPaddingDp = when (sizePreset) {
        WidgetSizePreset.Compact -> 10
        WidgetSizePreset.Standard -> 12
        WidgetSizePreset.Large -> 14
    }
    val containerEndPaddingDp = when (sizePreset) {
        WidgetSizePreset.Compact -> 8
        WidgetSizePreset.Standard -> 8
        WidgetSizePreset.Large -> 10
    }
    val itemSpacingDp = if (widthStyle == WidgetWidthStyle.Wide) 10 else 8
    val buttonWidthDp = regularButtonWidthDp + if (widthStyle == WidgetWidthStyle.Wide) wideButtonExtraDp else 0
    val handleWidthDp = regularHandleWidthDp + if (widthStyle == WidgetWidthStyle.Wide) wideHandleExtraDp else 0

    return WidgetOverlaySizing(
        containerWidthDp = sizePreset.widthDp + if (widthStyle == WidgetWidthStyle.Wide) containerWidthDeltaDp else 0,
        containerHeightDp = sizePreset.heightDp,
        containerStartPaddingDp = containerStartPaddingDp,
        containerEndPaddingDp = containerEndPaddingDp,
        containerVerticalPaddingDp = ((sizePreset.heightDp - buttonHeightDp) / 2).coerceAtLeast(6),
        containerCornerRadiusDp = sizePreset.heightDp / 2,
        itemSpacingDp = itemSpacingDp,
        buttonWidthDp = buttonWidthDp,
        buttonHeightDp = buttonHeightDp,
        buttonIconPaddingDp = when (sizePreset) {
            WidgetSizePreset.Compact -> 7
            WidgetSizePreset.Standard -> 8
            WidgetSizePreset.Large -> 9
        },
        handleWidthDp = handleWidthDp,
        handleHeightDp = buttonHeightDp,
        handleCornerRadiusDp = buttonHeightDp / 2,
        handleTextSizeSp = when (sizePreset) {
            WidgetSizePreset.Compact -> 12f
            WidgetSizePreset.Standard -> 14f
            WidgetSizePreset.Large -> 16f
        }
    )
}

private fun WidgetThemePreset.overlayColors(): WidgetOverlayColors {
    return when (this) {
        WidgetThemePreset.Light -> WidgetOverlayColors(
            surfaceColor = 0xF7F4EEE3.toInt(),
            surfaceStrokeColor = 0x331C1A18,
            buttonEnabledColor = 0xFFFFFFFF.toInt(),
            buttonDisabledColor = 0xFFE5DDD2.toInt(),
            iconEnabledColor = 0xFF201D1A.toInt(),
            iconDisabledColor = 0x7A201D1A.toInt(),
            handleColor = 0xFFE9DDCC.toInt(),
            handleTextColor = 0xFF3A3024.toInt()
        )

        WidgetThemePreset.Dark -> WidgetOverlayColors(
            surfaceColor = 0xE61B1F26.toInt(),
            surfaceStrokeColor = 0x33FFFFFF,
            buttonEnabledColor = 0x1FFFFFFF,
            buttonDisabledColor = 0x12FFFFFF,
            iconEnabledColor = 0xFFFFFFFF.toInt(),
            iconDisabledColor = 0x7AFFFFFF,
            handleColor = 0x24FFFFFF,
            handleTextColor = 0xCCFFFFFF.toInt()
        )

        WidgetThemePreset.DarkBlue -> WidgetOverlayColors(
            surfaceColor = 0xE6122237.toInt(),
            surfaceStrokeColor = 0x4D8FC4FF,
            buttonEnabledColor = 0x265384B8,
            buttonDisabledColor = 0x163D628A,
            iconEnabledColor = 0xFFF3F8FF.toInt(),
            iconDisabledColor = 0x80F3F8FF.toInt(),
            handleColor = 0x3379AEE8,
            handleTextColor = 0xFFF3F8FF.toInt()
        )

        WidgetThemePreset.MediumYellow -> WidgetOverlayColors(
            surfaceColor = 0xF0D7B45A.toInt(),
            surfaceStrokeColor = 0x667A5A16,
            buttonEnabledColor = 0xFFF7EBC2.toInt(),
            buttonDisabledColor = 0xFFD9C88F.toInt(),
            iconEnabledColor = 0xFF4A3510.toInt(),
            iconDisabledColor = 0x804A3510.toInt(),
            handleColor = 0xE0B78826.toInt(),
            handleTextColor = 0xFF3D2808.toInt()
        )

        WidgetThemePreset.Pink -> WidgetOverlayColors(
            surfaceColor = 0xF0BB5C80.toInt(),
            surfaceStrokeColor = 0x66FFF0F5.toInt(),
            buttonEnabledColor = 0xFFFCE4EC.toInt(),
            buttonDisabledColor = 0xFFE6B9CA.toInt(),
            iconEnabledColor = 0xFF5F1837.toInt(),
            iconDisabledColor = 0x805F1837.toInt(),
            handleColor = 0xE0A23D66.toInt(),
            handleTextColor = 0xFFFFF4F8.toInt()
        )
    }
}
