@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package io.battlesnake.kotlin

import io.battlesnake.core.AbstractBattleSnake
import io.battlesnake.core.GameStrategy
import io.battlesnake.core.MoveRequest
import io.battlesnake.core.RIGHT
import io.battlesnake.core.SnakeContext
import io.battlesnake.core.StartRequest
import io.battlesnake.core.StartResponse
import io.battlesnake.core.strategy
import io.battlesnake.kotlin.ExampleSnake.MySnakeContext

object ExampleSnake : AbstractBattleSnake<MySnakeContext>() {

  // Add any necessary snake-specific data to SnakeContext class
  class MySnakeContext : SnakeContext()

  // Called at the beginning of each game on Start
  override fun snakeContext(): MySnakeContext = MySnakeContext()

  override fun gameStrategy(): GameStrategy<MySnakeContext> =
    strategy(true) {

      // StartResponse describes snake color and head/tail type
      onStart { context: MySnakeContext, request: StartRequest ->
        StartResponse("#ff00ff", "beluga", "bolt")
      }

      // MoveResponse can be LEFT, RIGHT, UP or DOWN
      onMove { context: MySnakeContext, request: MoveRequest ->
        RIGHT
      }
    }

  @JvmStatic
  fun main(args: Array<String>) {
    run()
  }
}
