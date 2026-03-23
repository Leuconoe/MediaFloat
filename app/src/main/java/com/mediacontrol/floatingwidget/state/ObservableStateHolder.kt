package com.mediacontrol.floatingwidget.state

fun interface StateListener<T> {
    fun onStateChanged(state: T)
}

open class ObservableStateHolder<T>(
    initialState: T
) {

    private val listeners = linkedSetOf<StateListener<T>>()

    @Volatile
    private var state: T = initialState

    fun currentState(): T = state

    fun addListener(listener: StateListener<T>) {
        listeners += listener
        listener.onStateChanged(state)
    }

    fun removeListener(listener: StateListener<T>) {
        listeners -= listener
    }

    protected fun updateState(nextState: T) {
        state = nextState
        listeners.forEach { it.onStateChanged(nextState) }
    }
}
