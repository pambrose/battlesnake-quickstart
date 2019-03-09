package io.battlesnake

import io.battlesnake.SnakeTest.TestSnake.GameContext
import io.battlesnake.core.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SnakeTest {

    object TestSnake : BattleSnake<GameContext>() {

        class GameContext : AbstractGameContext()

        override fun gameContext() = GameContext()

        override fun gameStrategy(): DslStrategy<GameContext> =
            strategy {
                onStart { _: GameContext, _: StartRequest -> StartResponse("#ff00ff") }
                onMove { _: GameContext, _: MoveRequest -> RIGHT }
            }

        override fun moveTo(context: GameContext, request: MoveRequest, position: Position) = RIGHT
    }

    @Test
    internal fun pingTest() {
        //val response = TestSnake.strategy.ping.map { it.invoke() }.lastOrNull() ?: PingResponse
        val response = PingResponse
        assertEquals(PingResponse.javaClass.simpleName, response.toString())
    }

    @Test
    internal fun startTest() {
        val json =
            """{"game":{"id":"1551594939037768058"},"turn":1,"board":{"height":10,"width":10,"food":[{"x":1,"y":1}],"snakes":[{"id":"you","name":"you","health":0,"body":[{"x":2,"y":2}]}]},"you":{"id":"you","name":"you","health":0,"body":[{"x":2,"y":2}]}}"""
        val request = StartRequest.toObject(json)
        val response =
            TestSnake.strategy.start.map { it.invoke(TestSnake.gameContext(), request) }.lastOrNull() ?: StartResponse()
        assertEquals("#ff00ff", response.color)
        assertEquals("", response.headType)
        assertEquals("", response.tailType)
    }

    @Test
    internal fun moveTest() {
        val json =
            """{"game":{"id":"1551628170849008209"},"turn":1,"board":{"height":10,"width":10,"food":[{"x":1,"y":1}],"snakes":[{"id":"you","name":"you","health":0,"body":[{"x":2,"y":2}]}]},"you":{"id":"you","name":"you","health":0,"body":[{"x":2,"y":2}]}}"""
        val request = MoveRequest.toObject(json)
        val response =
            TestSnake.strategy.move.map { it.invoke(TestSnake.gameContext(), request) }.lastOrNull() ?: RIGHT
        assertEquals("right", response.move)
    }

    @Test
    internal fun endTest() {
        val json =
            """{"game":{"id":"1551628170849008209"},"turn":1,"board":{"height":10,"width":10,"food":[{"x":1,"y":1}],"snakes":[{"id":"you","name":"you","health":0,"body":[{"x":2,"y":2}]}]},"you":{"id":"you","name":"you","health":0,"body":[{"x":2,"y":2}]}}"""
        val request = EndRequest.toObject(json)
        val response =
            TestSnake.strategy.end.map { it.invoke(TestSnake.gameContext(), request) }.lastOrNull() ?: EndResponse
        assertEquals(EndResponse.javaClass.simpleName, response.toString())
    }
}