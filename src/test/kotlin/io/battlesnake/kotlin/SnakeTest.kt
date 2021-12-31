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
import io.battlesnake.core.Board
import io.battlesnake.core.DESCRIBE
import io.battlesnake.core.DescribeResponse
import io.battlesnake.core.END
import io.battlesnake.core.EndRequest
import io.battlesnake.core.EndResponse
import io.battlesnake.core.Game
import io.battlesnake.core.GameStrategy
import io.battlesnake.core.MOVE
import io.battlesnake.core.MoveRequest
import io.battlesnake.core.MoveResponse
import io.battlesnake.core.RIGHT
import io.battlesnake.core.Ruleset
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
import io.ktor.server.application.*
import io.ktor.server.testing.*
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
        onDescribe { call: ApplicationCall -> DescribeResponse(color = "#ff00ff") }
        onMove { _: MySnakeContext, _: MoveRequest -> RIGHT }
      }
  }

  @Test
  fun describeTest() {
    //val response = TestSnake.strategy.describe.map { it.invoke() }.lastOrNull() ?: DescribeResponse
    //val response = DescribeResponse
    //response.toString() shouldBeEqualTo DescribeResponse.javaClass.simpleName
  }

  @Test
  fun startTest() {
    val json =
      """
        {
            "game": {
                "id": "2da9736c-b7cc-4505-88a6-90c4afd5313a",
                "ruleset": { "name": "solo", "version": "v1.0.15" },
                "timeout": 500
            },
            "turn": 0,
            "board": {
                "height": 7,
                "width": 7,
                "snakes": [
                    {
                        "id": "gs_8PJvMSqK4MxGQ48C48hqBhgF",
                        "name": "PaulTest",
                        "latency": "",
                        "health": 100,
                        "body": [
                            { "x": 1, "y": 1 },
                            { "x": 1, "y": 1 },
                            { "x": 1, "y": 1 }
                        ],
                        "head": { "x": 1, "y": 1 },
                        "length": 3,
                        "shout": ""
                    }
                ],
                "food": [
                    { "x": 0, "y": 2 },
                    { "x": 3, "y": 3 }
                ],
                "hazards": []
            },
            "you": {
                "id": "gs_8PJvMSqK4MxGQ48C48hqBhgF",
                "name": "PaulTest",
                "latency": "",
                "health": 100,
                "body": [
                    { "x": 1, "y": 1 }, 
                    { "x": 1, "y": 1 }, 
                    { "x": 1, "y": 1 }
                ],
                "head": { "x": 1, "y": 1 },
                "length": 3,
                "shout": ""
            }
        }
      """.trimIndent()
    val request = StartRequest.toObject(json)
    TestSnake.strategy.startActions.map { it.invoke(TestSnake.snakeContext(), request) }
    val response = StartResponse

    /*
    response.apply {
      color shouldBeEqualTo "#ff00ff"
      headType shouldBeEqualTo ""
      tailType shouldBeEqualTo ""
    }
     */
  }

  @Test
  fun moveTest() {
    val json =
      """
        {
            "game": {
                "id": "2da9736c-b7cc-4505-88a6-90c4afd5313a",
                "ruleset": { "name": "solo", "version": "v1.0.15" },
                "timeout": 500
            },
            "turn": 0,
            "board": {
                "height": 7,
                "width": 7,
                "snakes": [
                    {
                        "id": "gs_8PJvMSqK4MxGQ48C48hqBhgF",
                        "name": "PaulTest",
                        "latency": "",
                        "health": 100,
                        "body": [
                            { "x": 1, "y": 1 },
                            { "x": 1, "y": 1 },
                            { "x": 1, "y": 1 }
                        ],
                        "head": { "x": 1, "y": 1 },
                        "length": 3,
                        "shout": ""
                    }
                ],
                "food": [
                    { "x": 0, "y": 2 },
                    { "x": 3, "y": 3 }
                ],
                "hazards": []
            },
            "you": {
                "id": "gs_8PJvMSqK4MxGQ48C48hqBhgF",
                "name": "PaulTest",
                "latency": "",
                "health": 100,
                "body": [
                    { "x": 1, "y": 1 }, 
                    { "x": 1, "y": 1 }, 
                    { "x": 1, "y": 1 }
                ],
                "head": { "x": 1, "y": 1 },
                "length": 3,
                "shout": ""
            }
        }
      """.trimIndent()
    val request = MoveRequest.toObject(json)
    val response =
      TestSnake.strategy.moveActions.map { it.invoke(TestSnake.snakeContext(), request) }.lastOrNull()
        ?: RIGHT
    response.move shouldBeEqualTo "right"
  }

  @Test
  fun endTest() {
    val json =
      """
        {
            "game": {
                "id": "2da9736c-b7cc-4505-88a6-90c4afd5313a",
                "ruleset": { "name": "solo", "version": "v1.0.15" },
                "timeout": 500
            },
            "turn": 0,
            "board": {
                "height": 7,
                "width": 7,
                "snakes": [
                    {
                        "id": "gs_8PJvMSqK4MxGQ48C48hqBhgF",
                        "name": "PaulTest",
                        "latency": "",
                        "health": 100,
                        "body": [
                            { "x": 1, "y": 1 },
                            { "x": 1, "y": 1 },
                            { "x": 1, "y": 1 }
                        ],
                        "head": { "x": 1, "y": 1 },
                        "length": 3,
                        "shout": ""
                    }
                ],
                "food": [
                    { "x": 0, "y": 2 },
                    { "x": 3, "y": 3 }
                ],
                "hazards": []
            },
            "you": {
                "id": "gs_8PJvMSqK4MxGQ48C48hqBhgF",
                "name": "PaulTest",
                "latency": "",
                "health": 100,
                "body": [
                    { "x": 1, "y": 1 }, 
                    { "x": 1, "y": 1 }, 
                    { "x": 1, "y": 1 }
                ],
                "head": { "x": 1, "y": 1 },
                "length": 3,
                "shout": ""
            }
        }
      """.trimIndent()
    val request = EndRequest.toObject(json)
    val response =
      TestSnake.strategy.endActions.map { it.invoke(TestSnake.snakeContext(), request) }.lastOrNull() ?: EndResponse()
    response.toString() shouldBeEqualTo EndResponse::class.java.simpleName
  }

  object ObjProvider {
    val counter = AtomicInteger(0)
    val newId get() = counter.incrementAndGet().toString()
    val board = Board(0, 0, emptyList(), emptyList(), emptyList())
    val game get() = Game(newId, Ruleset("", ""), 500)
    val you get() = You(name = "", id = newId, health = 0, body = emptyList(), latency = "", shout = "")
  }

  @Test
  fun serverTest() {
    withTestApplication({
                          module(TestSnake)
                        }) {

      handleRequest(Get, "/").apply {
        assertEquals(OK, response.status())
      }

      handleRequest(Get, DESCRIBE).apply {
        assertEquals(OK, response.status())
      }

      handleRequest(Post, DESCRIBE).apply {
        assertEquals(OK, response.status())
      }

      runBlocking {

        repeat(100) {
          launch {

            val board = ObjProvider.board
            val game = ObjProvider.game
            val you = ObjProvider.you

            handleRequest(Post, DESCRIBE) {
              addHeader(ContentType, "application/json")
            }.apply {
              assertEquals(OK, response.status())

              val json = String(response.byteContent!!)
              val obj = DescribeResponse.toObject(json)
              assertEquals(DescribeResponse::class, obj::class)
            }

            handleRequest(Post, START) {
              addHeader(ContentType, "application/json")
              setBody(StartRequest(board, game, 0, you).toJson())
            }.apply {
              assertEquals(OK, response.status())
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