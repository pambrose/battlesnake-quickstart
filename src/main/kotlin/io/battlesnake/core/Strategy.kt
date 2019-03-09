package io.battlesnake.core

import spark.Request
import spark.Response

abstract class Strategy<T : AbstractGameContext>(val verbose: Boolean = false) : DslStrategy<T>() {

    init {
        onPing { request: Request, response: Response ->
            logger.info { pingMsg(request, response) }
            onPing(request, response)
        }

        onStart { context: T, request: StartRequest ->
            logger.info { startMsg(context, request) }
            onStart(context, request)
        }

        onMove { context: T, request: MoveRequest -> onMove(context, request) }

        onEnd { context: T, request: EndRequest ->
            logger.info { endMsg(context, request) }
            onEnd(context, request)
        }

        onAfterTurn { request: Request,
                      response: Response,
                      gameResponse: GameResponse,
                      millis: Long ->
            if (verbose)
                logger.info { turnMsg(request, response, gameResponse, millis) }
            onAfterTurn(gameResponse, millis)
        }
    }

    open fun onPing(request: Request, response: Response) = PingResponse

    open fun onStart(context: T, request: StartRequest) = StartResponse()

    abstract fun onMove(context: T, request: MoveRequest): MoveResponse

    open fun onEnd(context: T, request: EndRequest) = EndResponse

    open fun onAfterTurn(response: GameResponse, millis: Long) {}
}