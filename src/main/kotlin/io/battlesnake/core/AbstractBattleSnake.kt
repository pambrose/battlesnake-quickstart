@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package io.battlesnake.core

import io.battlesnake.core.ktor.installs
import io.battlesnake.core.ktor.routes
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.features.origin
import io.ktor.request.receive
import io.ktor.request.uri
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import mu.KLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.measureTimedValue

abstract class AbstractBattleSnake<T : SnakeContext> : KLogging() {

  abstract fun snakeContext(): T

  abstract fun gameStrategy(): GameStrategy<T>

  internal val strategy by lazy { gameStrategy() }

  private val contextMap = ConcurrentHashMap<String, T>()

  suspend internal fun process(call: ApplicationCall): GameResponse =
    try {
      val uri = call.request.uri
      val (pair, duration) =
        measureTimedValue {
          when (uri) {
            PING -> ping(call)
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

  private fun ping(call: ApplicationCall): Pair<T?, GameResponse> =
    null to (strategy.pingActions.map { it.invoke(call) }.lastOrNull() ?: PingResponse)

  suspend private fun start(call: ApplicationCall): Pair<T?, GameResponse> =
    snakeContext()
        .let { context ->
          val startRequest = call.receive<StartRequest>()
          context.resetStartTime()
          context.assignIds(startRequest.gameId, startRequest.you.id)
          context.assignRequestResponse(call)
          contextMap[context.snakeId] = context
          context to (strategy.startActions.map { it.invoke(context, startRequest) }.lastOrNull() ?: StartResponse())
        }

  suspend private fun move(call: ApplicationCall): Pair<T?, GameResponse> {
    val moveRequest = call.receive<MoveRequest>()
    val context = contextMap[moveRequest.you.id]
                  ?: throw NoSuchElementException("Missing context for user id: ${moveRequest.you.id}")
    assert(context.snakeId == moveRequest.you.id)

    context.assignRequestResponse(call)

    val (response, duration) =
      measureTimedValue {
        strategy.moveActions.map { it.invoke(context, moveRequest) }.lastOrNull() ?: RIGHT
      }

    context.apply {
      computeTime += duration
      moveCount++
    }
    return context to response
  }

  suspend private fun end(call: ApplicationCall): Pair<T?, GameResponse> {
    val endRequest = call.receive<EndRequest>()
    val context = contextMap.remove(endRequest.you.id)
                  ?: throw NoSuchElementException("Missing context for user id: ${endRequest.you.id}")
    assert(context.snakeId == endRequest.you.id)
    context.assignRequestResponse(call)
    return context to (strategy.endActions.map { it.invoke(context, endRequest) }.lastOrNull() ?: EndResponse())
  }

  fun run(port: Int = 8080) {
    val p = Integer.parseInt(System.getProperty("PORT") ?: "$port")
    logger.info { "Listening on port: $p" }
    embeddedServer(CIO, port = p) { module() }.start(wait = true)
  }

  fun Application.module(testing: Boolean = false) {
    installs()
    routes(this@AbstractBattleSnake)

    // Prime the classloader to avoid an expensive first call
    StartRequest.primeClassLoader()
    // Reference strategy to load it
    strategy
  }
}
