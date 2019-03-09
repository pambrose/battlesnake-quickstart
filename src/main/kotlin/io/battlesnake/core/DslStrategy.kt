package io.battlesnake.core

import mu.KLogging
import spark.Request
import spark.Response

fun <T> strategy(verbose: Boolean = false, init: DslStrategy<T>.() -> Unit) =
    DslStrategy<T>()
        .apply {
            if (verbose) {
                onStart { context, request ->
                    logger.info { "Starting game ${request.gameId}" }
                    StartResponse()
                }
                onEnd { context, request ->
                    logger.info { "Game ${request.gameId} ended in ${request.turn} moves" }
                    EndResponse
                }
                onAfterTurn { request, response, gameResponse, millis ->
                    logger.info { "Responded to ${request.uri()} in ${millis}ms with: $gameResponse" }
                }
            }
            init.invoke(this)
        }


open class DslStrategy<T> : KLogging() {

    internal var ping: MutableList<() -> PingResponse> = mutableListOf()

    internal var start: MutableList<(context: T, request: StartRequest) -> StartResponse> = mutableListOf()

    internal var move: MutableList<(context: T, request: MoveRequest) -> MoveResponse> = mutableListOf()

    internal var end: MutableList<(context: T, request: EndRequest) -> EndResponse> = mutableListOf()

    internal var afterTurn: MutableList<(
        request: Request,
        response: Response,
        gameResponse: GameResponse,
        millis: Long
    ) -> Unit> = mutableListOf()

    fun onPing(block: () -> PingResponse) {
        ping.add(block)
    }

    fun onStart(block: (context: T, request: StartRequest) -> StartResponse) {
        start.add(block)
    }

    fun onMove(block: (context: T, request: MoveRequest) -> MoveResponse) {
        move.add(block)
    }

    fun onEnd(block: (context: T, request: EndRequest) -> EndResponse) {
        end.add(block)
    }

    fun onAfterTurn(
        block: (
            request: Request,
            response: Response,
            gameResponse: GameResponse,
            millis: Long
        ) -> Unit
    ) {
        afterTurn.add(block)
    }
}