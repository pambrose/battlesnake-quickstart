package io.battlesnake.core

import mu.KLogging
import spark.Request
import spark.Response

fun <T : AbstractGameContext> strategy(verbose: Boolean = false, init: DslStrategy<T>.() -> Unit) =
    DslStrategy<T>()
        .apply {
            onStart { context, request ->
                logger.info { startLoggingMsg(context, request) }
                StartResponse()
            }

            onEnd { context, request ->
                logger.info { endLoggingMsg(context, request) }
                EndResponse
            }

            if (verbose)
                onAfterTurn { request, response, gameResponse, millis ->
                    logger.info { turnLoggingMsg(request, response, gameResponse, millis) }
                }

            init.invoke(this)
        }


open class DslStrategy<T : AbstractGameContext> : KLogging() {

    internal fun startLoggingMsg(context: T, request: StartRequest) =
        "Starting game ${request.gameId} from ${context.request.ip()}"

    internal fun endLoggingMsg(context: T, request: EndRequest) =
        "Game ${request.gameId} ended in ${request.turn} moves from ${context.request.ip()}"

    internal fun turnLoggingMsg(request: Request, response: Response, gameResponse: GameResponse, millis: Long) =
        "Responded to ${request.uri()} in ${millis}ms with: $gameResponse"

    internal val ping: MutableList<() -> PingResponse> = mutableListOf()

    internal val start: MutableList<(context: T, request: StartRequest) -> StartResponse> = mutableListOf()

    internal val move: MutableList<(context: T, request: MoveRequest) -> MoveResponse> = mutableListOf()

    internal val end: MutableList<(context: T, request: EndRequest) -> EndResponse> = mutableListOf()

    internal val afterTurn: MutableList<(
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