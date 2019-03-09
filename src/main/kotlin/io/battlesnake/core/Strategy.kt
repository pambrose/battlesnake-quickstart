package io.battlesnake.core

import spark.Request
import spark.Response

abstract class Strategy<T : AbstractGameContext>(val verbose: Boolean = false) : DslStrategy<T>() {

    init {
        onPing { onPing() }

        onStart { context: T, request: StartRequest ->
            logger.info { startLoggingMsg(context, request) }
            onStart(context, request)
        }

        onMove { context: T, request: MoveRequest -> onMove(context, request) }

        onEnd { context: T, request: EndRequest ->
            logger.info { endLoggingMsg(context, request) }
            onEnd(context, request)
        }

        onAfterTurn { request: Request,
                      response: Response,
                      gameResponse: GameResponse,
                      millis: Long ->
            if (verbose)
                logger.info { turnLoggingMsg(request, response, gameResponse, millis) }
            onAfterTurn(gameResponse, millis)
        }
    }

    open fun onPing() = PingResponse

    open fun onStart(context: T, request: StartRequest) = StartResponse()

    abstract fun onMove(context: T, request: MoveRequest): MoveResponse

    open fun onEnd(context: T, request: EndRequest) = EndResponse

    open fun onAfterTurn(response: GameResponse, millis: Long) {}
}