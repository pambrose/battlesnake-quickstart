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

    fun onBeforeTurn(func: (request: Request, response: Response) -> Unit) {
        beforeTurn = func
    }

    fun onPing(func: () -> PingResponse) {
        ping = func
    }

    fun onStart(func: (context: T, request: StartRequest) -> StartResponse) {
        start = func
    }

    fun onMove(func: (context: T, request: MoveRequest) -> MoveResponse) {
        move = func
    }

    fun onEnd(func: (context: T, request: EndRequest) -> EndResponse) {
        end = func
    }

    fun onAfterTurn(func: (response: GameResponse, millis: Long) -> Unit) {
        afterTurn = func
    }
}

