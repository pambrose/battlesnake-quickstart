@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package io.battlesnake.kotlin

import io.battlesnake.core.AbstractBattleSnake
import io.battlesnake.core.AbstractSnakeContext
import io.battlesnake.core.MoveRequest
import io.battlesnake.core.RIGHT
import io.battlesnake.core.StartRequest
import io.battlesnake.core.StartResponse
import io.battlesnake.core.Strategy
import io.battlesnake.core.strategy
import io.battlesnake.kotlin.ExampleSnake.SnakeContext

object ExampleSnake : AbstractBattleSnake<SnakeContext>() {

  // Add any necessary snake-specific data to SnakeContext class
  class SnakeContext : AbstractSnakeContext()

  // Called at the beginning of each game on Start
  override fun snakeContext(): SnakeContext = SnakeContext()

  override fun gameStrategy(): Strategy<SnakeContext> =
    strategy(true) {

      // StartResponse describes snake color and head/tail type
      onStart { context: SnakeContext, request: StartRequest ->
        StartResponse("#ff00ff", "beluga", "bolt")
      }

      // MoveResponse can be LEFT, RIGHT, UP or DOWN
      onMove { context: SnakeContext, request: MoveRequest ->
        RIGHT
      }
    }

  @JvmStatic
  fun main(args: Array<String>) {
    run()
  }
}
