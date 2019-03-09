package io.battlesnake.core

import mu.KLogging
import spark.Request
import spark.Response
import spark.Spark
import kotlin.system.measureTimeMillis

abstract class BattleSnake<T : AbstractGameContext> : KLogging() {

    abstract fun gameContext(): T

    abstract fun gameStrategy(): DslStrategy<T>

    internal val strategy by lazy { gameStrategy() }

    private val contextMap = mutableMapOf<String, T>()

    fun process(req: Request, res: Response) =
        try {
            val uri = req.uri()
            lateinit var gameResponse: GameResponse
            val ms =
                measureTimeMillis {
                    gameResponse =
                        when (uri) {
                            PING -> strategy.ping.map { it.invoke(req, res) }.lastOrNull() ?: PingResponse

                            START -> {
                                gameContext()
                                    .run {
                                        this.request = req
                                        this.response = res
                                        val request = StartRequest.toObject(req.body())
                                        contextMap[request.gameId] = this
                                        strategy.start.map { it.invoke(this, request) }.lastOrNull() ?: StartResponse()
                                    }
                            }

                            MOVE -> {
                                val request = MoveRequest.toObject(req.body())
                                val context = contextMap[request.gameId]
                                    ?: throw NoSuchElementException("Missing context for game id: ${request.gameId}")
                                lateinit var response: GameResponse
                                val moveTime =
                                    measureTimeMillis {
                                        response =
                                            strategy.move.map { it.invoke(context, request) }.lastOrNull() ?: RIGHT
                                    }
                                context.apply {
                                    elapsedMoveTimeMillis += moveTime
                                    moveCount += 1
                                }
                                response
                            }

                            END -> {
                                val request = EndRequest.toObject(req.body())
                                val context = contextMap.remove(request.gameId)
                                    ?: throw NoSuchElementException("Missing context for game id: ${request.gameId}")
                                strategy.end.map { it.invoke(context, request) }.lastOrNull() ?: EndResponse
                            }
                            else -> throw IllegalAccessError("Strange call made to the snake: $uri [${req.ip()}]")
                        }
                }
            strategy.afterTurn.forEach { it.invoke(req, res, gameResponse, ms) }
            gameResponse
        } catch (e: Exception) {
            logger.warn(e) { "Something went wrong with ${req.uri()}" }
            throw e
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