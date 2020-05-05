/*
 * Copyright © 2020 Paul Ambrose (pambrose@mac.com)
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