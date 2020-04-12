@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package io.battlesnake.core

import mu.KLogging
import spark.Request
import spark.Response
import spark.Spark
import kotlin.system.measureTimeMillis

abstract class AbstractBattleSnake<T : AbstractSnakeContext> : KLogging() {

  abstract fun snakeContext(): T

  abstract fun gameStrategy(): Strategy<T>

  val strategy by lazy { gameStrategy() }

  private val contextMap = mutableMapOf<String, T>()

  private fun process(req: Request, res: Response): GameResponse =
    try {
      val uri = req.uri()
      lateinit var gameResponse: GameResponse
      val ms =
        measureTimeMillis {
          gameResponse =
            when (uri) {
              PING -> ping(req, res)
              START -> start(req, res)
              MOVE -> move(req, res)
              END -> end(req, res)
              else -> throw IllegalAccessError("Invalid call made to the snake: $uri [${req.ip()}]")
            }
        }

      strategy.afterTurn.forEach { it.invoke(req, res, gameResponse, ms) }
      gameResponse
    } catch (e: Exception) {
      logger.warn(e) { "Something went wrong with ${req.uri()}" }
      throw e
    }

  private fun ping(req: Request, res: Response): GameResponse =
    strategy.ping.map { it.invoke(req, res) }.lastOrNull() ?: PingResponse

  private fun start(request: Request, response: Response): GameResponse =
    snakeContext()
        .let { context ->
          val startRequest = StartRequest.toObject(request.body())
          context.assignIds(startRequest.gameId, startRequest.you.id)
          context.assignRequestResponse(request, response)
          contextMap[context.snakeId] = context
          strategy.start.map { it.invoke(context, startRequest) }.lastOrNull() ?: StartResponse()
        }

  private fun move(req: Request, res: Response): GameResponse {
    val moveRequest = MoveRequest.toObject(req.body())
    val context = contextMap[moveRequest.you.id]
                  ?: throw NoSuchElementException("Missing context for user id: ${moveRequest.you.id}")
    context.assignRequestResponse(req, res)

    lateinit var response: GameResponse
    val moveTime =
      measureTimeMillis {
        response = strategy.move.map { it.invoke(context, moveRequest) }.lastOrNull() ?: RIGHT
      }
    context.apply {
      elapsedMoveTimeMillis += moveTime
      moveCount += 1
    }
    return response
  }

  private fun end(req: Request, res: Response): GameResponse {
    val endRequest = EndRequest.toObject(req.body())
    val context = contextMap.remove(endRequest.you.id)
                  ?: throw NoSuchElementException("Missing context for user id: ${endRequest.you.id}")
    context.assignRequestResponse(req, res)
    return strategy.end.map { it.invoke(context, endRequest) }.lastOrNull() ?: EndResponse
  }

  fun run(port: Int = 8080) {
    val p = Integer.parseInt(System.getProperty("PORT") ?: "$port")
    logger.info { "Listening on port: $p" }

    Spark.port(p)

    Spark.get("/") { _, _ ->
      """
      <br>
      <h2>You have reached a <a href=\"https://docs.battlesnake.io\">Battlesnake</a> server.</h2>

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
