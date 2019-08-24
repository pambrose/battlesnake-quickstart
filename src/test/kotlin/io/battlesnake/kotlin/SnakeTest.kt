@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package io.battlesnake.kotlin

import io.battlesnake.core.AbstractBattleSnake
import io.battlesnake.core.AbstractGameContext
import io.battlesnake.core.EndRequest
import io.battlesnake.core.EndResponse
import io.battlesnake.core.MoveRequest
import io.battlesnake.core.PingResponse
import io.battlesnake.core.RIGHT
import io.battlesnake.core.StartRequest
import io.battlesnake.core.StartResponse
import io.battlesnake.core.Strategy
import io.battlesnake.core.strategy
import io.battlesnake.kotlin.SnakeTest.TestSnake.GameContext
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test

class SnakeTest {

    object TestSnake : AbstractBattleSnake<GameContext>() {

        class GameContext : AbstractGameContext()

        override fun gameContext() = GameContext()

        override fun gameStrategy(): Strategy<GameContext> =
            strategy {
                onStart { _: GameContext, _: StartRequest -> StartResponse("#ff00ff") }
                onMove { _: GameContext, _: MoveRequest -> RIGHT }
            }
    }

    @Test
    internal fun pingTest() {
        // val response = TestSnake.strategy.ping.map { it.invoke() }.lastOrNull() ?: PingResponse
        val response = PingResponse
        response.toString() shouldEqual PingResponse.javaClass.simpleName
    }

    @Test
    internal fun startTest() {
        val json =
            """{"game":{"id":"1551594939037768058"},"turn":1,"board":{"height":10,"width":10,"food":[{"x":1,"y":1}],
                |"snakes":[{"id":"you","name":"you","health":0,"body":[{"x":2,"y":2}]}]},"you":{"id":"you","name":"you",
                |"health":0,"body":[{"x":2,"y":2}]}}""".trimMargin()
        val request = StartRequest.toObject(json)
        val response =
            TestSnake.strategy.start.map { it.invoke(TestSnake.gameContext(), request) }.lastOrNull() ?: StartResponse()

        response.apply {
            color shouldEqual "#ff00ff"
            headType shouldEqual ""
            tailType shouldEqual ""
        }
    }

    @Test
    internal fun moveTest() {
        val json =
            """{"game":{"id":"1551628170849008209"},"turn":1,"board":{"height":10,"width":10,"food":[{"x":1,"y":1}],
                |"snakes":[{"id":"you","name":"you","health":0,"body":[{"x":2,"y":2}]}]},"you":{"id":"you","name":"you",
                |"health":0,"body":[{"x":2,"y":2}]}}""".trimMargin()
        val request = MoveRequest.toObject(json)
        val response =
            TestSnake.strategy.move.map { it.invoke(TestSnake.gameContext(), request) }.lastOrNull() ?: RIGHT
        response.move shouldEqual "right"
    }

    @Test
    internal fun endTest() {
        val json =
            """{"game":{"id":"1551628170849008209"},"turn":1,"board":{"height":10,"width":10,"food":[{"x":1,"y":1}],
                |"snakes":[{"id":"you","name":"you","health":0,"body":[{"x":2,"y":2}]}]},"you":{"id":"you","name":"you",
                |"health":0,"body":[{"x":2,"y":2}]}}""".trimMargin()
        val request = EndRequest.toObject(json)
        val response =
            TestSnake.strategy.end.map { it.invoke(TestSnake.gameContext(), request) }.lastOrNull() ?: EndResponse
        response.toString() shouldEqual EndResponse.javaClass.simpleName
    }
}
