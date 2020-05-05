@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package io.battlesnake.core

import io.ktor.application.ApplicationCall
import kotlin.time.Duration

abstract class AbstractGameStrategy<T : SnakeContext>(private val verbose: Boolean = false) : GameStrategy<T>() {

  init {
    onPing { call: ApplicationCall ->
      logger.info { pingMsg(call) }
      onPing(call)
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

    onAfterTurn { context: T?, call: ApplicationCall, gameResponse: GameResponse, duration: Duration ->
      if (verbose)
        logger.info { afterTurnMsg(context, call, gameResponse, duration) }
      onAfterTurn(gameResponse, duration)
    }
  }

  open fun onPing(call: ApplicationCall) = PingResponse

  open fun onStart(context: T, request: StartRequest) = StartResponse()

  abstract fun onMove(context: T, request: MoveRequest): MoveResponse

  open fun onEnd(context: T, request: EndRequest) = EndResponse()

  open fun onAfterTurn(response: GameResponse, duration: Duration) {}
}