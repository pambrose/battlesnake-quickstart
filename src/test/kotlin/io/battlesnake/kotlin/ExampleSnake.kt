@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")
package io.battlesnake.kotlin

import io.battlesnake.core.*
import io.battlesnake.kotlin.ExampleSnake.GameContext

object ExampleSnake : AbstractBattleSnake<GameContext>() {

    // Add any necessary snake-specific data to GameContext class
    class GameContext : AbstractGameContext()

    // Called at the beginning of each game on Start
    override fun gameContext(): GameContext = GameContext()

    override fun gameStrategy(): Strategy<GameContext> =
        strategy(true) {

            // StartResponse describes snake color and head/tail type
            onStart { context: GameContext, request: StartRequest ->
                StartResponse("#ff00ff", "beluga", "bolt")
            }

            // MoveResponse can be LEFT, RIGHT, UP or DOWN
            onMove { context: GameContext, request: MoveRequest ->
                RIGHT
            }
        }

    @JvmStatic
    fun main(args: Array<String>) {
        run()
    }
}