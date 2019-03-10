package io.battlesnake.example.kotlin

import io.battlesnake.core.*
import io.battlesnake.example.kotlin.ExampleSnake.GameContext

object ExampleSnake : AbstractBattleSnake<GameContext>() {

    // GameContext can contain any data you want
    class GameContext : AbstractGameContext()

    // Called at the beginning of each game on Start
    override fun gameContext(): GameContext = GameContext()

    override fun gameStrategy(): Strategy<GameContext> =
        strategy(true) {

            // StartReponse describes snake color and head/tail type
            onStart { context: GameContext, request: StartRequest ->
                StartResponse("#ff00ff", "beluga", "bolt")
            }

            // MoveReponse can be LEFT, RIGHT, UP or DOWN
            onMove { context: GameContext, request: MoveRequest ->
                RIGHT
            }
        }

    @JvmStatic
    fun main(args: Array<String>) {
        run()
    }
}