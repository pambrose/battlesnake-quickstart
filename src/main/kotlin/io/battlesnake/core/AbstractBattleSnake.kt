@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package io.battlesnake.core

import mu.KLogging
import spark.Request
import spark.Response
import spark.Spark
import kotlin.system.measureTimeMillis

abstract class AbstractBattleSnake<T : AbstractGameContext> : KLogging() {

    abstract fun gameContext(): T

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
                            else -> throw IllegalAccessError("Strange call made to the snake: $uri [${req.ip()}]")
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

    private fun start(req: Request, res: Response): GameResponse =
        gameContext()
            .let { context ->
                context.assignRequestResponse(req, res)
                val startRequest = StartRequest.toObject(req.body())
                contextMap[startRequest.gameId] = context
                strategy.start.map { it.invoke(context, startRequest) }.lastOrNull() ?: StartResponse()
            }

    private fun move(req: Request, res: Response): GameResponse {
        val moveRequest = MoveRequest.toObject(req.body())
        val context = contextMap[moveRequest.gameId]
            ?: throw NoSuchElementException("Missing context for game id: ${moveRequest.gameId}")
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
        val context = contextMap.remove(endRequest.gameId)
            ?: throw NoSuchElementException("Missing context for game id: ${endRequest.gameId}")
        context.assignRequestResponse(req, res)
        return strategy.end.map { it.invoke(context, endRequest) }.lastOrNull() ?: EndResponse
    }

    fun run(port: Int = 8080) {
        val p = Integer.parseInt(System.getProperty("PORT") ?: "$port")
        logger.info { "Listening on port: $p" }

        Spark.port(p)

        Spark.get("/") { _, _ ->
            "Battlesnake documentation can be found at " +
                    "<a href=\"https://docs.battlesnake.io\">https://docs.battlesnake.io</a>."
        }
        Spark.post(
            PING,
            { request, response -> process(request, response) },
            { "{}" })
        Spark.post(
            START,
            { request, response -> process(request, response) },
            { (it as StartResponse).toJson() })
        Spark.post(
            MOVE,
            { request, response -> process(request, response) },
            { (it as MoveResponse).toJson() })
        Spark.post(
            END,
            { request, response -> process(request, response) },
            { "{}" })

        // Prime the classloader to avoid an expensive first call
        StartRequest.primeClassLoader()
    }
}
