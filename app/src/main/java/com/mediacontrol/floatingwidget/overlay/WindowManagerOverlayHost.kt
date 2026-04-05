package sw2.io.mediafloat.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import sw2.io.mediafloat.R
import sw2.io.mediafloat.debug.DebugLogWriter
import sw2.io.mediafloat.debug.NoOpDebugLogWriter
import sw2.io.mediafloat.model.MediaCommand
import sw2.io.mediafloat.model.MediaArtwork
import sw2.io.mediafloat.model.MediaSessionState
import sw2.io.mediafloat.model.PlaybackStatus
import sw2.io.mediafloat.model.currentTitle
import sw2.io.mediafloat.model.WidgetAnchor
import sw2.io.mediafloat.model.WidgetButton
import sw2.io.mediafloat.model.WidgetWidthStyle
import sw2.io.mediafloat.model.DragHandlePlacement
import sw2.io.mediafloat.model.WidgetOverlayAppearance
import sw2.io.mediafloat.model.WidgetPosition
import sw2.io.mediafloat.model.overlayAppearance
import sw2.io.mediafloat.model.supports
import sw2.io.mediafloat.overlay.OverlayLayoutCalculator
import sw2.io.mediafloat.overlay.OverlayBitmapLoader
import sw2.io.mediafloat.overlay.OverlayPresentationSpecFactory

class WindowManagerOverlayHost(
    context: Context,
    private val positionStore: OverlayPositionStore,
    private val onMediaCommand: (MediaCommand) -> Unit,
    private val onPositionChanged: (WidgetPosition) -> Unit,
    private val debugLogWriter: DebugLogWriter = NoOpDebugLogWriter
) : OverlayHost {

    private val appContext = context.applicationContext
    private val windowManager = appContext.getSystemService(WindowManager::class.java)
    private val bitmapLoader = OverlayBitmapLoader(appContext.contentResolver)
    private val previousButton by lazy { createCommandButton(MediaCommand.Previous) }
    private val playPauseButton by lazy { createCommandButton(MediaCommand.TogglePlayPause) }
    private val nextButton by lazy { createCommandButton(MediaCommand.Next) }
    private val dragHandle by lazy { createDragHandle() }
    private val thumbnailView by lazy { createThumbnailView() }
    private val titleStrip by lazy { createTitleStrip() }
    private val mediaRow by lazy { createMediaRow() }
    private val controlsRow by lazy { createControlsRow() }
    private val rootView by lazy { createRootView() }
    private val layoutParams by lazy {
        WindowManager.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
    }
    private var attached = false
    private var currentViewState: OverlayViewState? = null
    private var isDragging = false
    private var appliedDragHandlePlacement: DragHandlePlacement = DragHandlePlacement.Right
    private var appliedThumbnailSignature: String? = null
    private var onToggleWidget: (() -> Unit)? = null
    private var lastTapTime: Long = 0
    private var tapCount: Int = 0

    override fun setOnToggleWidget(onToggle: () -> Unit) {
        onToggleWidget = onToggle
    }

    override fun attach(viewState: OverlayViewState) {
        currentViewState = viewState
        updateOverlayAppearance(viewState, OverlayPresentationSpecFactory.create(viewState.config.overlayAppearance(), viewState.mediaState, viewState.config.allowLowQualityThumbnailFallback, viewState.layout.dragHandlePlacement))
        if (!attached) {
            rootView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            val appearance = viewState.config.overlayAppearance()
            val spec = OverlayPresentationSpecFactory.create(
                appearance = appearance,
                mediaState = viewState.mediaState,
                thumbnailEnabled = viewState.config.allowLowQualityThumbnailFallback,
                dragHandlePlacement = viewState.layout.dragHandlePlacement
            )
            layoutParams.width = dp(spec.metrics.totalWidthDp)
            layoutParams.height = dp(spec.metrics.totalHeightDp)
            applyPosition(viewState.position, spec.metrics.totalWidthDp)
            windowManager.addView(rootView, layoutParams)
            attached = true
            Log.d(TAG, "Attached overlay at x=${layoutParams.x}, y=${layoutParams.y}")
            debugLogWriter.info(TAG, "Attached overlay window", "x=${layoutParams.x} y=${layoutParams.y}")
        }
        update(viewState)
    }

    override fun update(viewState: OverlayViewState) {
        currentViewState = viewState
        previousButton.visibility = buttonVisibility(WidgetButton.Previous, viewState)
        playPauseButton.visibility = buttonVisibility(WidgetButton.PlayPause, viewState)
        nextButton.visibility = buttonVisibility(WidgetButton.Next, viewState)

        val appearance = viewState.config.overlayAppearance()
        val spec = OverlayPresentationSpecFactory.create(
            appearance = appearance,
            mediaState = viewState.mediaState,
            thumbnailEnabled = viewState.config.allowLowQualityThumbnailFallback,
            dragHandlePlacement = viewState.layout.dragHandlePlacement
        )
        updateOverlayAppearance(viewState, spec)
        bindButton(previousButton, MediaCommand.Previous, viewState.mediaState, appearance, viewState.config.widthStyle)
        bindButton(playPauseButton, MediaCommand.TogglePlayPause, viewState.mediaState, appearance, viewState.config.widthStyle)
        bindButton(nextButton, MediaCommand.Next, viewState.mediaState, appearance, viewState.config.widthStyle)

        val playPauseIcon = if ((viewState.mediaState as? MediaSessionState.Active)?.playbackStatus == PlaybackStatus.Playing) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        playPauseButton.setImageResource(playPauseIcon)
        bindTitleStrip(spec)
        bindThumbnail(viewState, appearance, spec)
        rootView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        layoutParams.width = dp(spec.metrics.totalWidthDp)
        layoutParams.height = dp(spec.metrics.totalHeightDp)
        applyPosition(viewState.position, spec.metrics.totalWidthDp)
        if (attached) {
            windowManager.updateViewLayout(rootView, layoutParams)
        }
    }

    override fun detach() {
        if (!attached) {
            return
        }

        windowManager.removeView(rootView)
        attached = false
        Log.d(TAG, "Detached overlay")
        debugLogWriter.info(TAG, "Detached overlay window")
    }

    private fun createRootView(): LinearLayout {
        return LinearLayout(appContext).also { container ->
            container.orientation = LinearLayout.HORIZONTAL
            container.gravity = Gravity.CENTER_VERTICAL
            container.addView(thumbnailView)
            container.addView(mediaRow)
        }
    }

    private fun createMediaRow(): LinearLayout {
        return LinearLayout(appContext).also { container ->
            container.orientation = LinearLayout.VERTICAL
            container.gravity = Gravity.CENTER_HORIZONTAL
            container.addView(titleStrip)
            container.addView(controlsRow)
        }
    }

    private fun createControlsRow(): LinearLayout {
        return LinearLayout(appContext).also { container ->
            container.orientation = LinearLayout.HORIZONTAL
            container.gravity = Gravity.CENTER
            syncChildOrder(container, DragHandlePlacement.Right)
        }
    }

    private fun createTitleStrip(): TextView {
        return TextView(appContext).apply {
            gravity = Gravity.CENTER_VERTICAL
            isSingleLine = true
            ellipsize = TextUtils.TruncateAt.MARQUEE
            marqueeRepeatLimit = -1
            isSelected = true
            setHorizontallyScrolling(true)
            isHorizontalFadingEdgeEnabled = true
        }
    }

    private fun createThumbnailView(): ImageView {
        return ImageView(appContext).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            clipToOutline = true
            contentDescription = null
        }
    }

    private fun createCommandButton(command: MediaCommand): ImageButton {
        val iconRes = when (command) {
            MediaCommand.Previous -> android.R.drawable.ic_media_previous
            MediaCommand.TogglePlayPause -> android.R.drawable.ic_media_play
            MediaCommand.Next -> android.R.drawable.ic_media_next
        }
        val labelRes = when (command) {
            MediaCommand.Previous -> R.string.overlay_button_previous
            MediaCommand.TogglePlayPause -> R.string.overlay_button_play_pause
            MediaCommand.Next -> R.string.overlay_button_next
        }

        return ImageButton(appContext).apply {
            setImageResource(iconRes)
            contentDescription = appContext.getString(labelRes)
            scaleType = ImageView.ScaleType.CENTER
            setOnClickListener { onMediaCommand(command) }
        }
    }

    private fun createDragHandle(): TextView {
        return TextView(appContext).apply {
            text = "|||"
            contentDescription = appContext.getString(R.string.overlay_drag_handle)
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            setOnTouchListener(DragTouchListener())
        }
    }

    private fun buttonVisibility(button: WidgetButton, viewState: OverlayViewState): Int {
        return if (button in viewState.layout.visibleButtons) View.VISIBLE else View.GONE
    }
    }

    private fun buttonVisibility(button: WidgetButton, viewState: OverlayViewState): Int {
        return if (button in viewState.layout.visibleButtons) View.VISIBLE else View.GONE
    }

    private fun bindButton(
        button: ImageButton,
        command: MediaCommand,
        mediaState: MediaSessionState,
        appearance: WidgetOverlayAppearance,
        widthStyle: WidgetWidthStyle
    ) {
        val enabled = mediaState.supports(command)
        button.isEnabled = enabled
        button.alpha = if (enabled) 1f else 0.4f
        button.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = if (widthStyle == WidgetWidthStyle.Wide) {
                dp(appearance.sizing.buttonHeightDp).toFloat() * 0.34f
            } else {
                dp(appearance.sizing.buttonHeightDp / 2).toFloat()
            }
            setColor(if (enabled) appearance.colors.buttonEnabledColor else appearance.colors.buttonDisabledColor)
        }
        button.setColorFilter(if (enabled) appearance.colors.iconEnabledColor else appearance.colors.iconDisabledColor)
    }

    private fun updateOverlayAppearance(viewState: OverlayViewState, spec: OverlayPresentationSpec) {
        val appearance = viewState.config.overlayAppearance()
        val sizing = appearance.sizing
        val colors = appearance.colors

        rootView.alpha = viewState.config.opacity
        rootView.gravity = spec.rootGravity
        mediaRow.orientation = spec.mediaRowOrientation
        mediaRow.gravity = spec.mediaRowGravity
        if (!isDragging && (appliedDragHandlePlacement != viewState.layout.dragHandlePlacement || controlsRow.childCount == 0)) {
            syncChildOrder(controlsRow, viewState.layout.dragHandlePlacement)
        }

        controlsRow.setPadding(
            dp(sizing.containerStartPaddingDp),
            dp(sizing.containerVerticalPaddingDp),
            dp(sizing.containerEndPaddingDp),
            dp(sizing.containerVerticalPaddingDp)
        )
        controlsRow.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(sizing.containerCornerRadiusDp).toFloat()
            setColor(colors.surfaceColor)
            setStroke(dp(1), colors.surfaceStrokeColor)
        }
        controlsRow.layoutParams = LinearLayout.LayoutParams(
            dp(spec.controlsRowWidthDp),
            dp(spec.controlsRowHeightDp)
        )

        thumbnailView.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(sizing.thumbnailCornerRadiusDp).toFloat()
            setColor(colors.titleBackgroundColor)
            setStroke(dp(1), colors.surfaceStrokeColor)
        }

        titleStrip.textSize = sizing.titleTextSizeSp
        titleStrip.setTextColor(colors.titleTextColor)
        titleStrip.setPadding(
            dp(sizing.titleStripHorizontalPaddingDp),
            0,
            dp(sizing.titleStripHorizontalPaddingDp),
            0
        )
        titleStrip.minHeight = dp(sizing.titleStripMinHeightDp)
        titleStrip.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(sizing.titleStripCornerRadiusDp).toFloat()
            setColor(colors.titleBackgroundColor)
            setStroke(dp(1), colors.surfaceStrokeColor)
        }
        titleStrip.layoutParams = LinearLayout.LayoutParams(
            dp(spec.titleWidthDp),
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            marginEnd = dp(spec.titleMarginEndDp)
            bottomMargin = dp(spec.titleBottomMarginDp)
        }

        updateButtonLayout(previousButton, sizing)
        updateButtonLayout(playPauseButton, sizing)
        updateButtonLayout(nextButton, sizing)

        dragHandle.textSize = sizing.handleTextSizeSp
        dragHandle.setTextColor(colors.handleTextColor)
        dragHandle.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(sizing.handleCornerRadiusDp).toFloat()
            setColor(colors.handleColor)
        }
        dragHandle.layoutParams = LinearLayout.LayoutParams(
            dp(sizing.handleWidthDp),
            dp(sizing.handleHeightDp)
        ).apply {
            marginEnd = dp(spec.dragHandleMarginEndDp)
        }
    }

    private fun syncChildOrder(container: LinearLayout, placement: DragHandlePlacement) {
        container.removeAllViews()
        if (placement == DragHandlePlacement.Left) {
            container.addView(dragHandle)
        }
        container.addView(previousButton)
        container.addView(playPauseButton)
        container.addView(nextButton)
        if (placement == DragHandlePlacement.Right) {
            container.addView(dragHandle)
        }
        appliedDragHandlePlacement = placement
    }

    private fun bindTitleStrip(spec: OverlayPresentationSpec) {
        titleStrip.visibility = if (spec.titleVisible) View.VISIBLE else View.GONE
        titleStrip.text = spec.titleText
        titleStrip.isSelected = spec.titleText.isNotEmpty()
    }

    private fun bindThumbnail(viewState: OverlayViewState, appearance: WidgetOverlayAppearance, spec: OverlayPresentationSpec) {
        val presentation = resolveOverlayThumbnailPresentation(
            mediaState = viewState.mediaState,
            sizing = appearance.sizing,
            allowLowQualityFallback = viewState.config.allowLowQualityThumbnailFallback
        )

        if (presentation == null) {
            if (viewState.config.allowLowQualityThumbnailFallback) {
                thumbnailView.visibility = View.VISIBLE
                thumbnailView.layoutParams = LinearLayout.LayoutParams(
                    dp(spec.thumbnailWidthDp),
                    dp(spec.thumbnailHeightDp)
                ).apply {
                    marginEnd = dp(spec.thumbnailMarginEndDp)
                }
                thumbnailView.setImageDrawable(null)
                appliedThumbnailSignature = "fallback"
            } else {
                thumbnailView.visibility = View.GONE
                thumbnailView.setImageDrawable(null)
                appliedThumbnailSignature = null
            }
            return
        }

        thumbnailView.visibility = View.VISIBLE
        thumbnailView.layoutParams = LinearLayout.LayoutParams(
            dp(spec.thumbnailWidthDp),
            dp(spec.thumbnailHeightDp)
        ).apply {
            marginEnd = dp(spec.thumbnailMarginEndDp)
        }

        val thumbnailSignature = presentation.signature()
        if (appliedThumbnailSignature == thumbnailSignature) {
            return
        }

        val thumbnailBitmap = bitmapLoader.load(
            artwork = presentation.artwork,
            targetSizePx = dp(presentation.sizeDp)
        )

        if (thumbnailBitmap == null) {
            thumbnailView.visibility = View.VISIBLE
            thumbnailView.setImageDrawable(null)
            appliedThumbnailSignature = "fallback"
            return
        }

        thumbnailView.setImageBitmap(thumbnailBitmap)
        appliedThumbnailSignature = thumbnailSignature
    }

    private fun OverlayThumbnailPresentation.signature(): String {
        val artworkKey = when (val resolvedArtwork = artwork) {
            is MediaArtwork.UriSource -> resolvedArtwork.uri
            is MediaArtwork.BitmapSource -> System.identityHashCode(resolvedArtwork.bitmap).toString()
        }

        return "${artwork.source.name}|$artworkKey|${artwork.widthPx}x${artwork.heightPx}|$sizeDp"
    }

    private fun updateButtonLayout(button: ImageButton, sizing: sw2.io.mediafloat.model.WidgetOverlaySizing) {
        button.layoutParams = LinearLayout.LayoutParams(
            dp(sizing.buttonWidthDp),
            dp(sizing.buttonHeightDp)
        ).apply {
            marginEnd = dp(sizing.itemSpacingDp)
        }
        val buttonPadding = dp(sizing.buttonIconPaddingDp)
        button.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding)
    }

    private fun applyPosition(position: WidgetPosition, overlayWidth: Int) {
        val screenWidth = appContext.resources.displayMetrics.widthPixels
        layoutParams.x = when (position.anchor) {
            WidgetAnchor.Start -> positionStore.dpToPx(position.xOffsetDp)
            WidgetAnchor.End -> (screenWidth - overlayWidth - positionStore.dpToPx(position.xOffsetDp)).coerceAtLeast(0)
        }
        layoutParams.y = positionStore.dpToPx(position.yOffsetDp).coerceAtLeast(0)
    }

    private fun persistCurrentPosition() {
        val overlayWidth = rootView.width.takeIf { it > 0 } ?: rootView.measuredWidth
        val screenWidth = appContext.resources.displayMetrics.widthPixels
        val anchor = if (layoutParams.x + overlayWidth / 2 <= screenWidth / 2) {
            WidgetAnchor.Start
        } else {
            WidgetAnchor.End
        }
        val xOffsetPx = when (anchor) {
            WidgetAnchor.Start -> layoutParams.x.coerceAtLeast(0)
            WidgetAnchor.End -> (screenWidth - overlayWidth - layoutParams.x).coerceAtLeast(0)
        }
        val position = WidgetPosition(
            anchor = anchor,
            xOffsetDp = positionStore.pxToDp(xOffsetPx),
            yOffsetDp = positionStore.pxToDp(layoutParams.y.coerceAtLeast(0))
        )

        positionStore.save(position)
        onPositionChanged(position)
        Log.d(TAG, "Saved overlay position $position")
        debugLogWriter.debug(
            TAG,
            "Persisted overlay position after drag",
            "anchor=${position.anchor.name} xOffsetDp=${position.xOffsetDp} yOffsetDp=${position.yOffsetDp}"
        )
    }

    private fun dp(value: Int): Int = positionStore.dpToPx(value)

    private inner class DragTouchListener : View.OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f
        
        private var tapCount = 0
        private var lastTapTime = 0L
        private val TRIPLE_TAP_TIMEOUT = 500L

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    isDragging = true
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = (initialX + (event.rawX - initialTouchX).toInt()).coerceAtLeast(0)
                    layoutParams.y = (initialY + (event.rawY - initialTouchY).toInt()).coerceAtLeast(0)
                    if (attached) {
                        windowManager.updateViewLayout(rootView, layoutParams)
                    }
                    return true
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    try {
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime < TRIPLE_TAP_TIMEOUT) {
                            tapCount++
                        } else {
                            tapCount = 1
                        }
                        lastTapTime = now

                        if (tapCount >= 3) {
                            onToggleWidget?.invoke()
                            tapCount = 0
                        }
                        
                        persistCurrentPosition()
                    } finally {
                        isDragging = false
                    }
                    return true
                }
            }
            return false
        }
    }

    private companion object {
        const val TAG = "OverlayHost"
    }
}
