package io.battlesnake.core

import mu.KLogging
import spark.Request
import spark.Response

fun <T : AbstractGameContext> strategy(verbose: Boolean = false, init: Strategy<T>.() -> Unit) =
    Strategy<T>()
        .apply {
            onPing { request, response ->
                logger.info { pingMsg(request, response) }
                PingResponse
            }

            onStart { context, request ->
                logger.info { startMsg(context, request) }
                StartResponse()
            }

            onEnd { context, request ->
                logger.info { endMsg(context, request) }
                EndResponse
            }

            if (verbose)
                onAfterTurn { request, response, gameResponse, millis ->
                    logger.info { turnMsg(request, response, gameResponse, millis) }
                }

            init.invoke(this)
        }

open class Strategy<T : AbstractGameContext> : KLogging() {

    internal fun pingMsg(request: Request, response: Response) =
        "Ping from ${request.ip()}"

    internal fun startMsg(context: T, request: StartRequest) =
        "Starting game: \"${request.gameId}\" [${context.request?.ip() ?: "Unkown IP"}]"

    internal fun endMsg(context: T, request: EndRequest): String {
        val avg =
            if (context.moveCount > 0) "with ${"%.2f".format(context.elapsedMoveTimeMillis / (context.moveCount.toFloat()))} ms/move " else ""
        return "Ending game: \"${request.gameId}\" game time: ${context.elapsedGameTimeMsg} moves: ${context.moveCount} $avg[${context.request?.ip()
            ?: "Unknown IP"}]"
    }

    internal fun turnMsg(request: Request, response: Response, gameResponse: GameResponse, millis: Long) =
        "Responded to ${request.uri()} in $millis ms with: $gameResponse"

    val ping: MutableList<(request: Request, response: Response) -> PingResponse> = mutableListOf()

    val start: MutableList<(context: T, request: StartRequest) -> StartResponse> = mutableListOf()

    val move: MutableList<(context: T, request: MoveRequest) -> MoveResponse> = mutableListOf()

    val end: MutableList<(context: T, request: EndRequest) -> EndResponse> = mutableListOf()

    val afterTurn: MutableList<(
        request: Request,
        response: Response,
        gameResponse: GameResponse,
        millis: Long
    ) -> Unit> = mutableListOf()

    fun onPing(block: (request: Request, response: Response) -> PingResponse) {
        ping += block
    }

    fun onStart(block: (context: T, request: StartRequest) -> StartResponse) {
        start += block
    }

    fun onMove(block: (context: T, request: MoveRequest) -> MoveResponse) {
        move += block
    }

    fun onEnd(block: (context: T, request: EndRequest) -> EndResponse) {
        end += block
    }

    fun onAfterTurn(block: (request: Request, response: Response, gameResponse: GameResponse, millis: Long) -> Unit) {
        afterTurn += block
    }
}