/*
 * Copyright © 2020 Paul Ambrose (pambrose@mac.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
      "Starting Game/Snake '${request.gameId}/${context.snakeId}' [${context.call.request.origin.host}]"

    internal fun <T : SnakeContext> endMsg(context: T, request: EndRequest): String =
      context.let {
        val avg =
          if (it.moveCount > 0)
            "\nAvg time/move: ${(it.computeTime.inMilliseconds / it.moveCount.toDouble()).milliseconds} "
          else
            ""

        "\nEnding Game/Snake '${request.gameId}/${it.snakeId}'" +
        "\nTotal moves: ${it.moveCount} " +
        "\nTotal game time: ${it.elapsedGameTime} " +
        "\nTotal compute time: ${it.computeTime}" +
        "$avg[${it.call.request.origin.host}]"
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