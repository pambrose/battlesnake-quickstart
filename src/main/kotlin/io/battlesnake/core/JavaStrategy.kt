package io.battlesnake.core

abstract class JavaStrategy<T> : Strategy<T>() {
    init {
        onPing { onPing() }

        onStart { context: T, request: StartRequest -> onStart(context, request) }

        onMove { context: T, request: MoveRequest -> onMove(context, request) }

        onEnd { context: T, request: EndRequest -> onEnd(context, request) }
    }

    open fun onPing() = PingResponse

    open fun onStart(context: T, request: StartRequest) = StartResponse()

    abstract fun onMove(context: T, request: MoveRequest): MoveResponse

    open fun onEnd(context: T, request: EndRequest) = EndResponse
}