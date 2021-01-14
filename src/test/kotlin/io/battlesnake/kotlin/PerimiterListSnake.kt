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

package io.battlesnake.kotlin

import io.battlesnake.core.AbstractBattleSnake
import io.battlesnake.core.DOWN
import io.battlesnake.core.DescribeResponse
import io.battlesnake.core.GameStrategy
import io.battlesnake.core.LEFT
import io.battlesnake.core.MoveRequest
import io.battlesnake.core.MoveResponse
import io.battlesnake.core.RIGHT
import io.battlesnake.core.SnakeContext
import io.battlesnake.core.StartRequest
import io.battlesnake.core.UP
import io.battlesnake.core.strategy
import io.ktor.application.*

object PerimiterListSnake : AbstractBattleSnake<PerimiterListSnake.MySnakeContext>() {

  override fun gameStrategy(): GameStrategy<MySnakeContext> =
    strategy(verbose = true) {

      onDescribe { call: ApplicationCall ->
        DescribeResponse("me", "#ff0000", "beluga", "bolt")
      }

      onStart { context: MySnakeContext, request: StartRequest ->
        fun originPath(x: Int, y: Int): List<MoveResponse> =
          buildList {
            repeat(y) { add(DOWN) }
            repeat(x) { add(LEFT) }
          }

        val you = request.you
        val board = request.board

        context.gotoOriginMoves = originPath(you.headPosition.x, you.headPosition.y).iterator()

        logger.info { "Position: ${you.headPosition.x},${you.headPosition.y} game id: ${request.gameId}" }
        logger.info { "Board: ${board.width}x${board.height} game id: ${request.gameId}" }
      }

      onMove { context: MySnakeContext, request: MoveRequest ->
        fun perimeterPath(width: Int, height: Int): List<MoveResponse> =
          buildList {
            repeat(height - 1) { add(UP) }
            repeat(width - 1) { add(RIGHT) }
            repeat(height - 1) { add(DOWN) }
            repeat(width - 1) { add(LEFT) }
          }

        if (request.isAtOrigin) {
          context.visitedOrigin = true
          context.perimeterMoves = perimeterPath(request.board.width, request.board.height).iterator()
        }

        if (context.visitedOrigin) {
          logger.info { "Using perimeter moves" }
          context.perimeterMoves.next()
        }
        else {
          logger.info { "Using goto moves" }
          context.gotoOriginMoves.next()
        }
      }
    }

  class MySnakeContext : SnakeContext() {
    lateinit var gotoOriginMoves: Iterator<MoveResponse>
    lateinit var perimeterMoves: Iterator<MoveResponse>
    var visitedOrigin = false
  }

  override fun snakeContext(): MySnakeContext = MySnakeContext()

  @JvmStatic
  fun main(args: Array<String>) {
    run()
  }
}