@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package io.battlesnake.kotlin

import io.battlesnake.core.AbstractBattleSnake
import io.battlesnake.core.EndRequest
import io.battlesnake.core.EndResponse
import io.battlesnake.core.GameStrategy
import io.battlesnake.core.MoveRequest
import io.battlesnake.core.PingResponse
import io.battlesnake.core.RIGHT
import io.battlesnake.core.SnakeContext
import io.battlesnake.core.StartRequest
import io.battlesnake.core.StartResponse
import io.battlesnake.core.strategy
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class SnakeTest {

  object TestSnake : AbstractBattleSnake<TestSnake.MySnakeContext>() {

    class MySnakeContext : SnakeContext()

    override fun snakeContext() = MySnakeContext()

    override fun gameStrategy(): GameStrategy<MySnakeContext> =
      strategy {
        onStart { _: MySnakeContext, _: StartRequest -> StartResponse("#ff00ff") }
        onMove { _: MySnakeContext, _: MoveRequest -> RIGHT }
      }
  }

  @Test
  internal fun pingTest() {
    // val response = TestSnake.strategy.ping.map { it.invoke() }.lastOrNull() ?: PingResponse
    val response = PingResponse
    response.toString() shouldBeEqualTo PingResponse.javaClass.simpleName
  }

  @Test
  internal fun startTest() {
    val json =
      """{"game":{"id":"1551594939037768058"},"turn":1,"board":{"height":10,"width":10,"food":[{"x":1,"y":1}],
                |"snakes":[{"id":"you","name":"you","shout":"","health":0,"body":[{"x":2,"y":2}]}]},"you":{"id":"you","name":"you",
                |"shout":"","health":0,"body":[{"x":2,"y":2}]}}""".trimMargin()
    val request = StartRequest.toObject(json)
    val response =
      TestSnake.strategy.startActions.map { it.invoke(TestSnake.snakeContext(), request) }.lastOrNull()
      ?: StartResponse()

    response.apply {
      color shouldBeEqualTo "#ff00ff"
      headType shouldBeEqualTo ""
      tailType shouldBeEqualTo ""
    }
  }

  @Test
  internal fun moveTest() {
    val json =
      """{"game":{"id":"1551628170849008209"},"turn":1,"board":{"height":10,"width":10,"food":[{"x":1,"y":1}],
                |"snakes":[{"id":"you","name":"you","shout":"","health":0,"body":[{"x":2,"y":2}]}]},"you":{"id":"you","name":"you",
                |"shout":"","health":0,"body":[{"x":2,"y":2}]}}""".trimMargin()
    val request = MoveRequest.toObject(json)
    val response =
      TestSnake.strategy.moveActions.map { it.invoke(TestSnake.snakeContext(), request) }.lastOrNull() ?: RIGHT
    response.move shouldBeEqualTo "right"
  }

  @Test
  internal fun endTest() {
    val json =
      """{"game":{"id":"1551628170849008209"},"turn":1,"board":{"height":10,"width":10,"food":[{"x":1,"y":1}],
                |"snakes":[{"id":"you","name":"you","shout":"","health":0,"body":[{"x":2,"y":2}]}]},"you":{"id":"you","name":"you",
                |"shout":"","health":0,"body":[{"x":2,"y":2}]}}""".trimMargin()
    val request = EndRequest.toObject(json)
    val response =
      TestSnake.strategy.endActions.map { it.invoke(TestSnake.snakeContext(), request) }.lastOrNull() ?: EndResponse()
    response.toString() shouldBeEqualTo EndResponse::class.java.simpleName
  }
}