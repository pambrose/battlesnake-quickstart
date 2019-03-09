package io.battlesnake.core

import mu.KLogging
import spark.Request
import spark.Response

fun <T : AbstractGameContext> strategy(verbose: Boolean = false, init: DslStrategy<T>.() -> Unit) =
    DslStrategy<T>()
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

open class DslStrategy<T : AbstractGameContext> : KLogging() {

    internal fun pingMsg(request: Request, response: Response) =
        "Ping from ${request.ip()}"

    internal fun startMsg(context: T, request: StartRequest) =
        "Starting game \"${request.gameId}\" [${context.request.ip()}]"

    internal fun endMsg(context: T, request: EndRequest): String {
        val mpm =
            if (context.moveCount > 0) "with ${"%.2f".format(context.elapsedMoveTimeMillis / (context.moveCount * 1.0))}ms/move " else ""
        return "Ending game \"${request.gameId}\" Game time: ${context.elapsedGameTimeMsg} Moves: ${context.moveCount} $mpm[${context.request.ip()}]"
    }

    internal fun turnMsg(request: Request, response: Response, gameResponse: GameResponse, millis: Long) =
        "Responded to ${request.uri()} in ${millis}ms with: $gameResponse"

    internal val ping: MutableList<(request: Request, response: Response) -> PingResponse> = mutableListOf()

    internal val start: MutableList<(context: T, request: StartRequest) -> StartResponse> = mutableListOf()

    internal val move: MutableList<(context: T, request: MoveRequest) -> MoveResponse> = mutableListOf()

    internal val end: MutableList<(context: T, request: EndRequest) -> EndResponse> = mutableListOf()

    internal val afterTurn: MutableList<(
        request: Request,
        response: Response,
        gameResponse: GameResponse,
        millis: Long
    ) -> Unit> = mutableListOf()

    fun onPing(block: (request: Request, response: Response) -> PingResponse) {
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