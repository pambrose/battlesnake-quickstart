@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package io.battlesnake.core

import mu.KLogging
import spark.Request
import spark.Response
import spark.Spark
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.measureTimedValue

abstract class AbstractBattleSnake<T : SnakeContext> : KLogging() {

  abstract fun snakeContext(): T

  abstract fun gameStrategy(): GameStrategy<T>

  val strategy by lazy { gameStrategy() }

  private val contextMap = ConcurrentHashMap<String, T>()

  private fun process(req: Request, res: Response): GameResponse =
    try {
      val uri = req.uri()
      val (pair, duration) =
        measureTimedValue {
          when (uri) {
            PING -> ping(req, res)
            START -> start(req, res)
            MOVE -> move(req, res)
            END -> end(req, res)
            else -> throw IllegalAccessError("Invalid call made to the snake: $uri [${req.ip()}]")
          }
        }
      val context = pair.first
      val gameResponse = pair.second

      strategy.afterTurnActions.forEach { it.invoke(context, req, res, gameResponse, duration) }
      gameResponse
    } catch (e: Exception) {
      logger.warn(e) { "Something went wrong with ${req.uri()}" }
      throw e
    }

  private fun ping(req: Request, res: Response): Pair<T?, GameResponse> =
    null to (strategy.pingActions.map { it.invoke(req, res) }.lastOrNull() ?: PingResponse)

  private fun start(request: Request, response: Response): Pair<T?, GameResponse> =
    snakeContext()
        .let { context ->
          val startRequest = StartRequest.toObject(request.body())
          context.assignIds(startRequest.gameId, startRequest.you.id)
          context.assignRequestResponse(request, response)
          contextMap[context.snakeId] = context
          context to (strategy.startActions.map { it.invoke(context, startRequest) }.lastOrNull() ?: StartResponse())
        }

  private fun move(req: Request, res: Response): Pair<T?, GameResponse> {
    val moveRequest = MoveRequest.toObject(req.body())
    val context = contextMap[moveRequest.you.id]
                  ?: throw NoSuchElementException("Missing context for user id: ${moveRequest.you.id}")
    assert(context.snakeId == moveRequest.you.id)
    context.assignRequestResponse(req, res)
    val (response, duration) =
      measureTimedValue {
        strategy.moveActions.map { it.invoke(context, moveRequest) }.lastOrNull() ?: RIGHT
      }
    context.apply {
      totalMoveTime += duration
      moveCount += 1
    }
    return context to response
  }

  private fun end(req: Request, res: Response): Pair<T?, GameResponse> {
    val endRequest = EndRequest.toObject(req.body())
    val context = contextMap.remove(endRequest.you.id)
                  ?: throw NoSuchElementException("Missing context for user id: ${endRequest.you.id}")
    assert(context.snakeId == endRequest.you.id)
    context.assignRequestResponse(req, res)
    return context to (strategy.endActions.map { it.invoke(context, endRequest) }.lastOrNull() ?: EndResponse)
  }

  fun run(port: Int = 8080) {
    val p = Integer.parseInt(System.getProperty("PORT") ?: "$port")
    logger.info { "Listening on port: $p" }

    Spark.port(p)

    Spark.get("/") { _, _ ->
      """
      <br>
      <h2>You have reached a <a href=\"https://docs.battlesnake.io\">Battlesnake</a> server.</h2>
      <br>
      <h2>Use this URL as your snake URL:</h2>
      <p id="url"></p>
      <script>
        document.getElementById("url").innerHTML = window.location.href;
      </script>
      """
    }

    Spark.get(PING,
              { request, response -> process(request, response) },
              { "pong" })

    Spark.post(PING,
               { request, response -> process(request, response) },
               { "{}}" })

    Spark.post(START,
               { request, response -> process(request, response) },
               { (it as StartResponse).toJson() })

    Spark.post(MOVE,
               { request, response -> process(request, response) },
               { (it as MoveResponse).toJson() })

    Spark.post(END,
               { request, response -> process(request, response) },
               { "{}" })

    // Prime the classloader to avoid an expensive first call
    StartRequest.primeClassLoader()
  }
}
