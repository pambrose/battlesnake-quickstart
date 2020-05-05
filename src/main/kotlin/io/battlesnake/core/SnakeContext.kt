@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package io.battlesnake.core

import io.ktor.application.ApplicationCall
import kotlin.time.TimeSource
import kotlin.time.seconds

open class SnakeContext {
  private val clock = TimeSource.Monotonic
  private var gameStartTime = clock.markNow()

  var computeTime = 0.seconds
    internal set

  var moveCount = 0L
    internal set

  lateinit var gameId: String
    internal set
  lateinit var snakeId: String
    internal set
  lateinit var call: ApplicationCall
    internal set

  internal fun resetStartTime() {
    gameStartTime = clock.markNow()
  }

  internal fun assignIds(gameId: String, snakeId: String) {
    this.gameId = gameId
    this.snakeId = snakeId
  }

  internal fun assignRequestResponse(call: ApplicationCall) {
    this.call = call
  }

  val elapsedGameTime get() = gameStartTime.elapsedNow()
}