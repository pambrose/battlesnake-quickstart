@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package io.battlesnake.core

import spark.Request
import spark.Response
import kotlin.time.TimeSource
import kotlin.time.seconds

open class SnakeContext {
  private val gameStartTimeMillis = TimeSource.Monotonic

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

  val elapsedGameTime get() = gameStartTimeMillis.markNow().elapsedNow()

  val elapsedGameTimeMsg: String
    get() {
      val time = elapsedGameTime
      return if (time > 1_000.seconds) "${time.inSeconds} secs" else "${time.inMilliseconds} ms"
    }
}