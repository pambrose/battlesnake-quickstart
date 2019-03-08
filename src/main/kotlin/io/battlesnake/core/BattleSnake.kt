package io.battlesnake.core

import mu.KLogging
import spark.Request
import spark.Response
import spark.Spark
import kotlin.system.measureTimeMillis

abstract class BattleSnake<T> : KLogging() {

    abstract fun gameContext(): T

    abstract fun gameStrategy(): Strategy<T>

    open fun moveTo(context: T, request: MoveRequest, position: Position) = RIGHT

    protected fun moveToOrigin(context: T, request: MoveRequest) =
        moveTo(context, request, Board.BOARD_ORIGIN)

    protected fun moveToCenter(context: T, request: MoveRequest) =
        moveTo(context, request, request.board.center)

    internal val strategy by lazy { gameStrategy() }

    private val contextMap = mutableMapOf<String, T>()

    fun process(req: Request, res: Response) =
        try {
            //logger.info{ "$uri called with: ${req.body()}" }
            strategy.beforeTurn.invoke(req, res)
            val uri = req.uri()
            lateinit var resp: GameResponse
            val ms =
                measureTimeMillis {
                    resp =
                        when (uri) {
                            PING -> strategy.ping.invoke()

                            START -> {
                                gameContext()
                                    .run {
                                        val request = StartRequest.toObject(req.body())
                                        contextMap[request.gameId] = this
                                        logger.info { "Starting game ${request.gameId}" }
                                        strategy.start.invoke(this, request)
                                    }
                            }

                            MOVE -> {
                                val request = MoveRequest.toObject(req.body())
                                val context = contextMap[request.gameId]
                                    ?: throw NoSuchElementException("Missing context for game id: ${request.gameId}")
                                strategy.move.invoke(context, request)
                            }

                            END -> {
                                val request = EndRequest.toObject(req.body())
                                val context = contextMap.remove(request.gameId)
                                    ?: throw NoSuchElementException("Missing context for game id: ${request.gameId}")
                                logger.info { "Game ${request.gameId} ended in ${request.turn} moves" }
                                strategy.end.invoke(context, request)
                            }
                            else -> throw IllegalAccessError("Strange call made to the snake: $uri")
                        }
                }
            logger.info { "Responded to $uri in ${ms}ms with: $resp" }
            strategy.afterTurn.invoke(resp, ms)
            resp
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