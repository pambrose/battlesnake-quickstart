package io.battlesnake.core

import spark.Request
import spark.Response

abstract class JavaStrategy<T> : Strategy<T>() {
    init {
        onBeforeTurn { request: Request, response: Response -> onBeforeTurn(request, response) }

        onPing { onPing() }

        onStart { context: T, request: StartRequest -> onStart(context, request) }

        onMove { context: T, request: MoveRequest -> onMove(context, request) }

        onEnd { context: T, request: EndRequest -> onEnd(context, request) }

        onAfterTurn { response: GameResponse, millis: Long -> onAfterTurn(response, millis) }
    }

    open fun onBeforeTurn(request: Request, response: Response) = {}

    open fun onPing() = PingResponse

    open fun onStart(context: T, request: StartRequest) = StartResponse()

    abstract fun onMove(context: T, request: MoveRequest): MoveResponse

    open fun onEnd(context: T, request: EndRequest) = EndResponse

    open fun onAfterTurn(response: GameResponse, millis: Long) = {}
}