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
import io.battlesnake.core.RIGHT
import io.battlesnake.core.SnakeContext
import io.battlesnake.core.strategy
import io.battlesnake.kotlin.ExampleSnake.MySnakeContext
import io.ktor.application.*

object ExampleSnake : AbstractBattleSnake<MySnakeContext>() {

  override fun gameStrategy(): GameStrategy<MySnakeContext> =
    strategy(true) {

      // DescribeResponse describes snake color and head/tail type
      onDescribe { call: ApplicationCall ->
        DescribeResponse(color = "#ff00ff", head = "beluga", tail = "bolt")
      }

      // MoveResponse can be LEFT, RIGHT, UP or DOWN
      onMove { context: MySnakeContext, request: MoveRequest ->
        RIGHT
      }
    }

  // Called at the beginning of each game on Start
  override fun snakeContext(): MySnakeContext = MySnakeContext()

  // Add any necessary snake-specific data to SnakeContext class
  class MySnakeContext : SnakeContext() {
    // Snake-specific context data goes here
  }

  @JvmStatic
  fun main(args: Array<String>) {
    run()
  }
}
