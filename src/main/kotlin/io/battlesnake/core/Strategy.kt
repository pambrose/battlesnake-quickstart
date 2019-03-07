package io.battlesnake.core

fun <T> strategy(init: Strategy<T>.() -> Unit) =
    Strategy<T>()
        .apply {
            init.invoke(this)
        }

open class Strategy<T>() {

    internal var ping: () -> PingResponse = { PingResponse }
    internal var start: (context: T, request: StartRequest) -> StartResponse =
        { _: T, _: StartRequest -> StartResponse() }
    internal var move: (context: T, request: MoveRequest) -> MoveResponse =
        { _: T, _: MoveRequest -> RIGHT }
    internal var end: (context: T, request: EndRequest) -> EndResponse =
        { _: T, _: EndRequest -> EndResponse }

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
}

