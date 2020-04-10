@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package io.battlesnake.core

import spark.Request
import spark.Response

abstract class AbstractSnakeContext(val gameId: String, val snakeId: String) {
  private val gameStartTimeMillis: Long = System.currentTimeMillis()

  var elapsedMoveTimeMillis = 0L
  var moveCount = 0L

  var request: Request? = null
    internal set
  var response: Response? = null
    internal set

  internal fun assignRequestResponse(req: Request, res: Response) {
    request = req
    response = res
  }

  val elapsedGameTimeMillis get() = System.currentTimeMillis() - gameStartTimeMillis

  val elapsedGameTimeMsg: String
    get() {
      val time = elapsedGameTimeMillis
      return if (time > 1_000) "${time / 1_000} secs" else "$time ms"
    }
}