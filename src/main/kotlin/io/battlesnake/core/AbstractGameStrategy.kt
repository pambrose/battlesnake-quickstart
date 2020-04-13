@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package io.battlesnake.core

import spark.Request
import spark.Response
import kotlin.time.Duration

abstract class AbstractGameStrategy<T : SnakeContext>(private val verbose: Boolean = false) : GameStrategy<T>() {

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

    onAfterTurn { context: T?, request: Request, response: Response, gameResponse: GameResponse, duration: Duration ->
      if (verbose)
        logger.info { afterTurnMsg(context, request, response, gameResponse, duration) }
      onAfterTurn(gameResponse, duration)
    }

    onEnd { context: T, request: EndRequest ->
      logger.info { endMsg(context, request) }
      onEnd(context, request)
    }
  }

  open fun onPing(request: Request, response: Response) = PingResponse

  open fun onStart(context: T, request: StartRequest) = StartResponse()

  abstract fun onMove(context: T, request: MoveRequest): MoveResponse

  open fun onEnd(context: T, request: EndRequest) = EndResponse

  open fun onAfterTurn(response: GameResponse, duration: Duration) {}
}