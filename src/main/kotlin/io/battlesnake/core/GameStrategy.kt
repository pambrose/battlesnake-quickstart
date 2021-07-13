/*
 * Copyright © 2021 Paul Ambrose (pambrose@mac.com)
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
import io.battlesnake.core.GameStrategy.Companion.describeMsg
import io.battlesnake.core.GameStrategy.Companion.endMsg
import io.battlesnake.core.GameStrategy.Companion.startMsg
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.request.*
import mu.KLogging
import kotlin.time.Duration
import kotlin.time.DurationUnit

fun <T : SnakeContext> strategy(verbose: Boolean = false, init: GameStrategy<T>.() -> Unit) =
  GameStrategy<T>()
    .apply {
      onDescribe { call ->
        logger.info { describeMsg(call) }
        DescribeResponse()
      }

      onStart { context, request ->
        logger.info { startMsg(context, request) }
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

  internal val describeActions = mutableListOf<(call: ApplicationCall) -> DescribeResponse>()
  internal val startActions = mutableListOf<(context: T, request: StartRequest) -> Unit>()
  internal val moveActions = mutableListOf<(context: T, request: MoveRequest) -> MoveResponse>()
  internal val endActions = mutableListOf<(context: T, request: EndRequest) -> EndResponse>()
  internal val afterTurnActions = mutableListOf<(
    context: T?,
    call: ApplicationCall,
    gameResponse: GameResponse,
    duration: Duration
  ) -> Unit>()

  fun onDescribe(block: (call: ApplicationCall) -> DescribeResponse) = let { describeActions += block }

  fun onStart(block: (context: T, request: StartRequest) -> Unit) = let { startActions += block }

  fun onMove(block: (context: T, request: MoveRequest) -> MoveResponse) = let { moveActions += block }

  fun onEnd(block: (context: T, request: EndRequest) -> EndResponse) = let { endActions += block }

  fun onAfterTurn(
    block: (context: T?, call: ApplicationCall, gameResponse: GameResponse, duration: Duration) -> Unit
  ) =
    let { afterTurnActions += block }

  companion object {
    internal fun describeMsg(call: ApplicationCall) = "Describe from ${call.request.origin.host}"

    internal fun <T : SnakeContext> startMsg(context: T, request: StartRequest) =
      "Starting Game/Snake '${request.gameId}/${context.snakeId}' [${context.call.request.origin.host}]"

    internal fun <T : SnakeContext> endMsg(context: T, request: EndRequest): String =
      context.let {
        val avg =
          if (it.moveCount > 0)
            "\nAvg time/move: ${Duration.milliseconds((it.computeTime.toDouble(DurationUnit.MILLISECONDS) / it.moveCount.toDouble()))} "
          else
            ""

        "\nEnding Game/Snake '${request.gameId}/${it.snakeId}'" +
            "\nTotal moves: ${it.moveCount} " +
            "\nTotal game time: ${it.elapsedGameTime} " +
            "\nTotal compute time: ${it.computeTime}" +
            "$avg[${it.call.request.origin.host}]"
      }

    internal fun <T : SnakeContext> afterTurnMsg(
      context: T?,
      call: ApplicationCall,
      gameResponse: GameResponse,
      duration: Duration
    ): String =
      "Responded to ${call.request.uri} in $duration with: $gameResponse" +
          (context?.let { " [${context.snakeId}]" } ?: "")
  }
}