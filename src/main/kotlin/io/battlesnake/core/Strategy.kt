package io.battlesnake.core

import spark.Request
import spark.Response

fun <T> strategy(init: Strategy<T>.() -> Unit) =
    Strategy<T>()
        .apply {
            init.invoke(this)
        }

open class Strategy<T>() {

    internal var beforeTurn: (request: Request, response: Response) -> Unit = { _: Request, _: Response -> }

    internal var ping: () -> PingResponse = { PingResponse }

    internal var start: (context: T, request: StartRequest) -> StartResponse =
        { _: T, _: StartRequest -> StartResponse() }

    internal var move: (context: T, request: MoveRequest) -> MoveResponse = { _: T, _: MoveRequest -> RIGHT }

    internal var end: (context: T, request: EndRequest) -> EndResponse = { _: T, _: EndRequest -> EndResponse }

    internal var afterTurn: (response: GameResponse, millis: Long) -> Unit = { _: GameResponse, _: Long -> }

    fun onBeforeTurn(block: (request: Request, response: Response) -> Unit) {
        beforeTurn = block
    }

    fun onPing(block: () -> PingResponse) {
        ping = block
    }

    fun onStart(block: (context: T, request: StartRequest) -> StartResponse) {
        start = block
    }

    fun onMove(block: (context: T, request: MoveRequest) -> MoveResponse) {
        move = block
    }

    fun onEnd(block: (context: T, request: EndRequest) -> EndResponse) {
        end = block
    }

    fun onAfterTurn(block: (response: GameResponse, millis: Long) -> Unit) {
        afterTurn = block
    }
}