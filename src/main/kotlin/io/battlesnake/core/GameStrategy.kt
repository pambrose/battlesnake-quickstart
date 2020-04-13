@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package io.battlesnake.core

import io.battlesnake.core.GameStrategy.Companion.afterTurnMsg
import io.battlesnake.core.GameStrategy.Companion.endMsg
import io.battlesnake.core.GameStrategy.Companion.pingMsg
import io.battlesnake.core.GameStrategy.Companion.startMsg
import mu.KLogging
import spark.Request
import spark.Response

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
          onAfterTurn { request, response, gameResponse, millis ->
            logger.info { afterTurnMsg(request, response, gameResponse, millis) }
          }
        }

        init.invoke(this)
      }

open class GameStrategy<T : SnakeContext> : KLogging() {

  internal val pingActions: MutableList<(request: Request, response: Response) -> PingResponse> = mutableListOf()

  internal val startActions: MutableList<(context: T, request: StartRequest) -> StartResponse> = mutableListOf()

  internal val moveActions: MutableList<(context: T, request: MoveRequest) -> MoveResponse> = mutableListOf()

  internal val endActions: MutableList<(context: T, request: EndRequest) -> EndResponse> = mutableListOf()

  internal val afterTurn: MutableList<(request: Request,
                                       response: Response,
                                       gameResponse: GameResponse,
                                       millis: Long) -> Unit> = mutableListOf()

  internal fun onPing(block: (request: Request, response: Response) -> PingResponse) {
    pingActions += block
  }

  internal fun onStart(block: (context: T, request: StartRequest) -> StartResponse) {
    startActions += block
  }

  internal fun onMove(block: (context: T, request: MoveRequest) -> MoveResponse) {
    moveActions += block
  }

  internal fun onEnd(block: (context: T, request: EndRequest) -> EndResponse) {
    endActions += block
  }

  internal fun onAfterTurn(block: (request: Request, response: Response, gameResponse: GameResponse, millis: Long) -> Unit) {
    afterTurn += block
  }

  companion object {
    internal fun pingMsg(request: Request, response: Response) =
      "Ping from ${request.ip()}"

    internal fun <T : SnakeContext> startMsg(context: T, request: StartRequest) =
      "Starting game/snake '${request.gameId}/${request.you.id}' [${context.request.ip() ?: "Unknown IP"}]"

    internal fun <T : SnakeContext> endMsg(context: T, request: EndRequest): String {
      val avg =
        if (context.moveCount > 0)
          "with ${"%.2f".format(context.elapsedMoveTimeMillis / (context.moveCount.toFloat()))} ms/move "
        else
          ""

      return "Ending game/snake '${request.gameId}/${request.you.id}' game time: ${context.elapsedGameTimeMsg} " +
             "moves: ${context.moveCount} $avg[${context.request.ip() ?: "Unknown IP"}]"
    }

    internal fun afterTurnMsg(request: Request, response: Response, gameResponse: GameResponse, millis: Long) =
      "Responded to ${request.uri()} in $millis ms with: $gameResponse"
  }
}