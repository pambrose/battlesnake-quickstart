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

package io.battlesnake.core

import io.battlesnake.core.ktor.installs
import io.battlesnake.core.ktor.routes
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import mu.KLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.measureTimedValue

abstract class AbstractBattleSnake<T : SnakeContext> : KLogging() {

  abstract fun snakeContext(): T

  abstract fun gameStrategy(): GameStrategy<T>

  internal val strategy by lazy { gameStrategy() }

  private val contextMap = ConcurrentHashMap<String, T>()

  internal suspend fun process(call: ApplicationCall): GameResponse =
    try {
      val uri = call.request.uri
      val (pair, duration) =
        measureTimedValue {
          when (uri) {
            DESCRIBE -> describe(call)
            START -> start(call)
            MOVE -> move(call)
            END -> end(call)
            else -> throw IllegalAccessError("Invalid call made to the snake: $uri [${call.request.origin.remoteHost}]")
          }
        }

      val context = pair.first
      val gameResponse = pair.second

      strategy.afterTurnActions.forEach { it.invoke(context, call, gameResponse, duration) }
      gameResponse
    } catch (e: Exception) {
      logger.warn(e) { "Something went wrong with ${call.request.uri}" }
      throw e
    }

  private fun describe(call: ApplicationCall): Pair<T?, GameResponse> =
    null to (strategy.describeActions.map { it.invoke(call) }.lastOrNull() ?: DescribeResponse())

  private suspend fun start(call: ApplicationCall): Pair<T?, GameResponse> =
    snakeContext()
        .let { context ->
          val startRequest = call.receive<StartRequest>()
          //logger.info { "Creating new snake context for ${startRequest.gameId}" }
          context.resetStartTime()
          context.assignIds(startRequest.gameId, startRequest.you.id)
          context.assignRequestResponse(call)
          contextMap[context.snakeId] = context
          strategy.startActions.map { it.invoke(context, startRequest) }
          context to StartResponse
        }

  private suspend fun move(call: ApplicationCall): Pair<T?, GameResponse> {
    val moveRequest = call.receive<MoveRequest>()
    val context = contextMap[moveRequest.you.id]
                  ?: throw NoSuchElementException("Missing context for user id: ${moveRequest.you.id}")
    assert(context.snakeId == moveRequest.you.id)

    context.assignRequestResponse(call)

    val (response, duration) =
      measureTimedValue {
        strategy.moveActions
            .map { it.invoke(context, moveRequest) }
            .lastOrNull() ?: throw IllegalStateException("Missing move action")
      }

    context.apply {
      computeTime += duration
      moveCount++
    }

    return context to response
  }

  private suspend fun end(call: ApplicationCall): Pair<T?, GameResponse> {
    val endRequest = call.receive<EndRequest>()
    val context = contextMap.remove(endRequest.you.id)
                  ?: throw NoSuchElementException("Missing context for user id: ${endRequest.you.id}")
    assert(context.snakeId == endRequest.you.id)
    context.assignRequestResponse(call)
    return context to (strategy.endActions.map { it.invoke(context, endRequest) }.lastOrNull() ?: EndResponse())
  }

  fun run(port: Int = 8080) {
    // Prime the classloader to avoid an expensive first call
    StartRequest.primeClassLoader()
    // Reference strategy to load it
    strategy

    logger.info { "Running snake: ${this.javaClass.name}" }

    val p = Integer.parseInt(System.getProperty("PORT") ?: "$port")
    logger.info { "Listening on port: $p" }
    embeddedServer(CIO, port = p) { module(this@AbstractBattleSnake) }.start(wait = true)
  }
}

internal fun Application.module(snake: AbstractBattleSnake<*>) {
  installs()
  routes(snake)
}