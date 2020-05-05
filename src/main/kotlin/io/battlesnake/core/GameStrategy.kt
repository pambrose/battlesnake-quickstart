@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package io.battlesnake.core

import io.battlesnake.core.GameStrategy.Companion.afterTurnMsg
import io.battlesnake.core.GameStrategy.Companion.endMsg
import io.battlesnake.core.GameStrategy.Companion.pingMsg
import io.battlesnake.core.GameStrategy.Companion.startMsg
import io.ktor.application.ApplicationCall
import io.ktor.features.origin
import io.ktor.request.uri
import mu.KLogging
import kotlin.time.Duration
import kotlin.time.milliseconds

fun <T : SnakeContext> strategy(verbose: Boolean = false, init: GameStrategy<T>.() -> Unit) =
  GameStrategy<T>()
      .apply {
        onPing { call ->
          logger.info { pingMsg(call) }
          PingResponse
        }

        onStart { context, request ->
          logger.info { startMsg(context, request) }
          StartResponse()
        }

        onEnd { context, request ->
          logger.info { endMsg(context, request) }
          EndResponse()
        }

        if (verbose) {
          onAfterTurn { context: T?, call, gameResponse, millis ->
            logger.info { afterTurnMsg(context, call, gameResponse, millis) }
          }
        }

        init.invoke(this)
      }

open class GameStrategy<T : SnakeContext> : KLogging() {

  internal val pingActions: MutableList<(call: ApplicationCall) -> PingResponse> = mutableListOf()

  internal val startActions: MutableList<(context: T, request: StartRequest) -> StartResponse> = mutableListOf()

  internal val moveActions: MutableList<(context: T, request: MoveRequest) -> MoveResponse> = mutableListOf()

  internal val endActions: MutableList<(context: T, request: EndRequest) -> EndResponse> = mutableListOf()

  internal val afterTurnActions: MutableList<(context: T?,
                                              call: ApplicationCall,
                                              gameResponse: GameResponse,
                                              duration: Duration) -> Unit> = mutableListOf()

  fun onPing(block: (call: ApplicationCall) -> PingResponse) = let { pingActions += block }

  fun onStart(block: (context: T, request: StartRequest) -> StartResponse) = let { startActions += block }

  fun onMove(block: (context: T, request: MoveRequest) -> MoveResponse) = let { moveActions += block }

  fun onEnd(block: (context: T, request: EndRequest) -> EndResponse) = let { endActions += block }

  fun onAfterTurn(block: (context: T?,
                          call: ApplicationCall,
                          gameResponse: GameResponse,
                          duration: Duration) -> Unit) = let { afterTurnActions += block }

  companion object {
    internal fun pingMsg(call: ApplicationCall) = "Ping from ${call.request.origin.host}"

    internal fun <T : SnakeContext> startMsg(context: T, request: StartRequest) =
      "Starting Game/Snake '${request.gameId}/${context.snakeId}' [${context.call.request.origin.host ?: "Unknown IP"}]"

    internal fun <T : SnakeContext> endMsg(context: T, request: EndRequest): String {
      val avg =
        if (context.moveCount > 0) {
          val rate = (context.computeTime.inMilliseconds / context.moveCount.toDouble()).milliseconds
          "\navg time/move: $rate "
        }
        else
          ""

      return "\nEnding Game/Snake '${request.gameId}/${context.snakeId}'" +
             "\ntotal moves: ${context.moveCount} " +
             "\ntotal game time: ${context.elapsedGameTime} " +
             "\ntotal compute time: ${context.computeTime}" +
             "$avg[${context.call.request.origin.host ?: "Unknown IP"}]"
    }

    internal fun <T : SnakeContext> afterTurnMsg(context: T?,
                                                 call: ApplicationCall,
                                                 gameResponse: GameResponse,
                                                 duration: Duration): String =
      "Responded to ${call.request.uri} in $duration with: " +
      (if (gameResponse is MoveResponse) gameResponse.move.toUpperCase() else "$gameResponse") +
      (context?.let { " [${context.snakeId}]" } ?: "")
  }
}