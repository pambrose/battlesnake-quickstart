@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package io.battlesnake.kotlin

import io.battlesnake.core.AbstractBattleSnake
import io.battlesnake.core.Board
import io.battlesnake.core.END
import io.battlesnake.core.EndRequest
import io.battlesnake.core.EndResponse
import io.battlesnake.core.Game
import io.battlesnake.core.GameStrategy
import io.battlesnake.core.MOVE
import io.battlesnake.core.MoveRequest
import io.battlesnake.core.MoveResponse
import io.battlesnake.core.PING
import io.battlesnake.core.PingResponse
import io.battlesnake.core.RIGHT
import io.battlesnake.core.START
import io.battlesnake.core.SnakeContext
import io.battlesnake.core.StartRequest
import io.battlesnake.core.StartResponse
import io.battlesnake.core.You
import io.battlesnake.core.module
import io.battlesnake.core.strategy
import io.ktor.http.HttpHeaders.ContentType
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals

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
  fun pingTest() {
    // val response = TestSnake.strategy.ping.map { it.invoke() }.lastOrNull() ?: PingResponse
    val response = PingResponse
    response.toString() shouldBeEqualTo PingResponse.javaClass.simpleName
  }

  @Test
  fun startTest() {
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
  fun moveTest() {
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
  fun endTest() {
    val json =
      """{"game":{"id":"1551628170849008209"},"turn":1,"board":{"height":10,"width":10,"food":[{"x":1,"y":1}],
                |"snakes":[{"id":"you","name":"you","shout":"","health":0,"body":[{"x":2,"y":2}]}]},"you":{"id":"you","name":"you",
                |"shout":"","health":0,"body":[{"x":2,"y":2}]}}""".trimMargin()
    val request = EndRequest.toObject(json)
    val response =
      TestSnake.strategy.endActions.map { it.invoke(TestSnake.snakeContext(), request) }.lastOrNull() ?: EndResponse()
    response.toString() shouldBeEqualTo EndResponse::class.java.simpleName
  }

  object ObjProvider {
    val counter = AtomicInteger(0)
    val newId get() = counter.incrementAndGet().toString()
    val board = Board(0, 0, emptyList(), emptyList())
    val game get() = Game(newId)
    val you get() = You("", newId, emptyList(), 0, "")
  }

  @Test
  fun serverTest() {
    withTestApplication({
                          module(TestSnake)
                        }) {

      handleRequest(Get, "/").apply {
        assertEquals(OK, response.status())
      }

      handleRequest(Get, PING).apply {
        assertEquals(OK, response.status())
      }

      handleRequest(Post, PING).apply {
        assertEquals(OK, response.status())
      }

      runBlocking {

        repeat(100) {
          launch {

            val board = ObjProvider.board
            val game = ObjProvider.game
            val you = ObjProvider.you

            handleRequest(Post, START) {
              addHeader(ContentType, "application/json")
              setBody(StartRequest(board, game, 0, you).toJson())
            }.apply {
              assertEquals(OK, response.status())

              val json = String(response.byteContent!!)
              val obj = StartResponse.toObject(json)
              assertEquals(StartResponse::class, obj::class)
            }

            repeat(100) {
              handleRequest(Post, MOVE) {
                addHeader(ContentType, "application/json")
                setBody(MoveRequest(board, game, 0, you).toJson())
              }.apply {
                assertEquals(OK, response.status())

                val json = String(response.byteContent!!)
                val obj = MoveResponse.toObject(json)
                assertEquals(MoveResponse::class, obj::class)
              }
            }

            handleRequest(Post, END) {
              addHeader(ContentType, "application/json")
              setBody(EndRequest(board, game, 0, you).toJson())
            }.apply {
              assertEquals(OK, response.status())

              val json = String(response.byteContent!!)
              val obj = EndResponse.toObject(json)
              assertEquals(EndResponse::class, obj::class)
            }
          }
        }
      }
    }
  }
}