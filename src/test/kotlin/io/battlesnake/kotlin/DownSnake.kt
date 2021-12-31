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
import io.battlesnake.core.DescribeResponse
import io.battlesnake.core.GameStrategy
import io.battlesnake.core.MoveRequest
import io.battlesnake.core.SnakeContext
import io.battlesnake.core.StartRequest
import io.battlesnake.core.down
import io.battlesnake.core.left
import io.battlesnake.core.strategy
import io.ktor.server.application.*

object DownSnake : AbstractBattleSnake<DownSnake.MySnakeContext>() {

  override fun gameStrategy(): GameStrategy<MySnakeContext> =
    strategy(verbose = true) {

      onDescribe { call: ApplicationCall ->
        DescribeResponse("me", "#ff0000", "beluga", "bolt")
      }

      onStart { context: MySnakeContext, request: StartRequest ->
        val you = request.you
        val board = request.board

        logger.info { "Position: ${you.headPosition.x},${you.headPosition.y} game id: ${request.gameId}" }
        logger.info { "Board: ${board.width}x${board.height} game id: ${request.gameId}" }
      }

      onMove { context: MySnakeContext, request: MoveRequest ->
        if (request.you.headPosition.y > 0)
          down("Going down")
        else
          left("Going left")
      }
    }

  class MySnakeContext : SnakeContext()

  override fun snakeContext(): MySnakeContext = MySnakeContext()

  @JvmStatic
  fun main(args: Array<String>) {
    run()
  }
}