package com.mediacontrol.floatingwidget.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
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
import com.mediacontrol.floatingwidget.R
import com.mediacontrol.floatingwidget.debug.DebugLogWriter
import com.mediacontrol.floatingwidget.debug.NoOpDebugLogWriter
import com.mediacontrol.floatingwidget.model.MediaCommand
import com.mediacontrol.floatingwidget.model.MediaSessionState
import com.mediacontrol.floatingwidget.model.PlaybackStatus
import com.mediacontrol.floatingwidget.model.WidgetAnchor
import com.mediacontrol.floatingwidget.model.WidgetButton
import com.mediacontrol.floatingwidget.model.WidgetPosition
import com.mediacontrol.floatingwidget.model.supports

class WindowManagerOverlayHost(
    context: Context,
    private val positionStore: OverlayPositionStore,
    private val onMediaCommand: (MediaCommand) -> Unit,
    private val onPositionChanged: (WidgetPosition) -> Unit,
    private val debugLogWriter: DebugLogWriter = NoOpDebugLogWriter
) : OverlayHost {

    private val appContext = context.applicationContext
    private val windowManager = appContext.getSystemService(WindowManager::class.java)
    private val previousButton by lazy { createCommandButton(MediaCommand.Previous) }
    private val playPauseButton by lazy { createCommandButton(MediaCommand.TogglePlayPause) }
    private val nextButton by lazy { createCommandButton(MediaCommand.Next) }
    private val dragHandle by lazy { createDragHandle() }
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

    override fun attach(viewState: OverlayViewState, position: WidgetPosition) {
        currentViewState = viewState
        if (!attached) {
            rootView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            applyPosition(position, rootView.measuredWidth)
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

        bindButton(previousButton, MediaCommand.Previous, viewState.mediaState)
        bindButton(playPauseButton, MediaCommand.TogglePlayPause, viewState.mediaState)
        bindButton(nextButton, MediaCommand.Next, viewState.mediaState)

        val playPauseIcon = if ((viewState.mediaState as? MediaSessionState.Active)?.playbackStatus == PlaybackStatus.Playing) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        playPauseButton.setImageResource(playPauseIcon)

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
        return LinearLayout(appContext).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(8), dp(8), dp(8))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(26).toFloat()
                setColor(0xE61B1F26.toInt())
                setStroke(dp(1), 0x33FFFFFF)
            }
            addView(previousButton)
            addView(playPauseButton)
            addView(nextButton)
            addView(dragHandle)
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
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0x1FFFFFFF)
            }
            scaleType = ImageView.ScaleType.CENTER
            layoutParams = LinearLayout.LayoutParams(dp(40), dp(40)).apply {
                marginEnd = dp(8)
            }
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setColorFilter(0xFFFFFFFF.toInt())
            setOnClickListener { onMediaCommand(command) }
        }
    }

    private fun createDragHandle(): TextView {
        return TextView(appContext).apply {
            text = "|||"
            textSize = 14f
            contentDescription = appContext.getString(R.string.overlay_drag_handle)
            setTextColor(0xCCFFFFFF.toInt())
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dp(28), dp(40))
            setOnTouchListener(DragTouchListener())
        }
    }

    private fun buttonVisibility(button: WidgetButton, viewState: OverlayViewState): Int {
        return if (button in viewState.layout.visibleButtons) View.VISIBLE else View.GONE
    }

    private fun bindButton(button: ImageButton, command: MediaCommand, mediaState: MediaSessionState) {
        val enabled = mediaState.supports(command)
        button.isEnabled = enabled
        button.alpha = if (enabled) 1f else 0.4f
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

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
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
                    persistCurrentPosition()
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
