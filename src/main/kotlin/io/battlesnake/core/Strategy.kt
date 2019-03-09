package io.battlesnake.core

import spark.Request
import spark.Response

abstract class Strategy<T>(val verbose: Boolean = false) : DslStrategy<T>() {

    init {
        onPing { onPing() }

        onStart { context: T, request: StartRequest ->
            if (verbose)
                logger.info { "Starting game ${request.gameId}" }
            onStart(context, request)
        }

        onMove { context: T, request: MoveRequest -> onMove(context, request) }

        onEnd { context: T, request: EndRequest ->
            if (verbose)
                logger.info { "Game ${request.gameId} ended in ${request.turn} moves" }
            onEnd(context, request)
        }

        onAfterTurn { request: Request,
                      response: Response,
                      gameResponse: GameResponse,
                      millis: Long ->
            if (verbose)
                logger.info { "Responded to ${request.uri()} in ${millis}ms with: $gameResponse" }
            onAfterTurn(gameResponse, millis)
        }
    }

    open fun onPing() = PingResponse

    open fun onStart(context: T, request: StartRequest) = StartResponse()

    abstract fun onMove(context: T, request: MoveRequest): MoveResponse

    open fun onEnd(context: T, request: EndRequest) = EndResponse

    open fun onAfterTurn(response: GameResponse, millis: Long) {}
}