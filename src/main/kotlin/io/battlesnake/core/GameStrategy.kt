@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package io.battlesnake.core

import io.battlesnake.core.GameStrategy.Companion.afterTurnMsg
import io.battlesnake.core.GameStrategy.Companion.endMsg
import io.battlesnake.core.GameStrategy.Companion.pingMsg
import io.battlesnake.core.GameStrategy.Companion.startMsg
import mu.KLogging
import spark.Request
import spark.Response
import kotlin.time.Duration
import kotlin.time.milliseconds

fun <T : SnakeContext> strategy(verbose: Boolean = false, init: GameStrategy<T>.() -> Unit) =
  GameStrategy<T>()
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

        if (verbose) {
          onAfterTurn { context: T?, request, response, gameResponse, millis ->
            logger.info { afterTurnMsg(context, request, response, gameResponse, millis) }
          }
        }

        init.invoke(this)
      }

open class GameStrategy<T : SnakeContext> : KLogging() {

  internal val pingActions: MutableList<(request: Request, response: Response) -> PingResponse> = mutableListOf()

  internal val startActions: MutableList<(context: T, request: StartRequest) -> StartResponse> = mutableListOf()

  internal val moveActions: MutableList<(context: T, request: MoveRequest) -> MoveResponse> = mutableListOf()

  internal val endActions: MutableList<(context: T, request: EndRequest) -> EndResponse> = mutableListOf()

  internal val afterTurnActions: MutableList<(context: T?,
                                              request: Request,
                                              response: Response,
                                              gameResponse: GameResponse,
                                              duration: Duration) -> Unit> = mutableListOf()

  fun onPing(block: (request: Request, response: Response) -> PingResponse) {
    pingActions += block
  }

  fun onStart(block: (context: T, request: StartRequest) -> StartResponse) {
    startActions += block
  }

  fun onMove(block: (context: T, request: MoveRequest) -> MoveResponse) {
    moveActions += block
  }

  fun onEnd(block: (context: T, request: EndRequest) -> EndResponse) {
    endActions += block
  }

  fun onAfterTurn(block: (context: T?, request: Request, response: Response, gameResponse: GameResponse, duration: Duration) -> Unit) {
    afterTurnActions += block
  }

  companion object {
    internal fun pingMsg(request: Request, response: Response) =
      "Ping from ${request.ip()}"

    internal fun <T : SnakeContext> startMsg(context: T, request: StartRequest) =
      "Starting Game/Snake '${request.gameId}/${context.snakeId}' [${context.request.ip() ?: "Unknown IP"}]"

    internal fun <T : SnakeContext> endMsg(context: T, request: EndRequest): String {
      val avg =
        if (context.moveCount > 0) {
          val rate = (context.computeTime.inMilliseconds / context.moveCount.toDouble()).milliseconds
          "\navg time/move: $rate "
        }
        else
          ""

      return "Ending Game/Snake '${request.gameId}/${context.snakeId}'" +
             "\ntotal moves: ${context.moveCount} " +
             "\ntotal game time: ${context.elapsedGameTime} " +
             "\ntotal compute time: ${context.computeTime}" +
             "$avg[${context.request.ip() ?: "Unknown IP"}]"
    }

    internal fun <T : SnakeContext> afterTurnMsg(context: T?,
                                                 request: Request,
                                                 response: Response,
                                                 gameResponse: GameResponse,
                                                 duration: Duration): String {
      return "Responded to ${request.uri()} in $duration with: " +
             (if (gameResponse is MoveResponse) gameResponse.move.toUpperCase() else "$gameResponse") +
             (context?.let { " [${context.snakeId}]" } ?: "")
    }
  }
}