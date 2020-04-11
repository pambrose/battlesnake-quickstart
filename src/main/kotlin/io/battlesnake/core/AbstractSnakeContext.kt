@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package io.battlesnake.core

import spark.Request
import spark.Response

abstract class AbstractSnakeContext() {
  private val gameStartTimeMillis: Long = System.currentTimeMillis()

  var elapsedMoveTimeMillis = 0L
  var moveCount = 0L

  lateinit var gameId: String
    internal set
  lateinit var snakeId: String
    internal set

  lateinit var request: Request
    internal set
  lateinit var response: Response
    internal set

  internal fun assignIds(gameId: String, snakeId: String) {
    this.gameId = gameId
    this.snakeId = snakeId
  }

  internal fun assignRequestResponse(request: Request, response: Response) {
    this.request = request
    this.response = response
  }

  val elapsedGameTimeMillis get() = System.currentTimeMillis() - gameStartTimeMillis

  val elapsedGameTimeMsg: String
    get() {
      val time = elapsedGameTimeMillis
      return if (time > 1_000) "${time / 1_000} secs" else "$time ms"
    }
}