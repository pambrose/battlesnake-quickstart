@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package io.battlesnake.core

import spark.Request
import spark.Response
import kotlin.time.TimeSource
import kotlin.time.seconds

open class SnakeContext {
  private val clock = TimeSource.Monotonic
  private var gameStartTime = clock.markNow()

  var totalMoveTime = 0.seconds
    internal set

  var moveCount = 0L
    internal set

  lateinit var gameId: String
    internal set
  lateinit var snakeId: String
    internal set
  lateinit var request: Request
    internal set
  lateinit var response: Response
    internal set

  internal fun resetStartTime() {
    gameStartTime = clock.markNow()
  }

  internal fun assignIds(gameId: String, snakeId: String) {
    this.gameId = gameId
    this.snakeId = snakeId
  }

  internal fun assignRequestResponse(request: Request, response: Response) {
    this.request = request
    this.response = response
  }

  val elapsedGameTime get() = gameStartTime.elapsedNow()
}