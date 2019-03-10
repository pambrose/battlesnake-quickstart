package io.battlesnake.core

import spark.Request
import spark.Response

abstract class AbstractGameContext {
    private val gameStartTimeMillis: Long = System.currentTimeMillis()

    var elapsedMoveTimeMillis = 0L
    var moveCount = 0L

    lateinit var request: Request
        internal set
    lateinit var response: Response
        internal set

    internal fun update(req: Request, res: Response) {
        request = req
        response = res
    }

    val elapsedGameTimeMillis get() = System.currentTimeMillis() - gameStartTimeMillis

    val elapsedGameTimeMsg: String
        get() {
            val time = elapsedGameTimeMillis
            return if (time > 1000)
                "${time / 1000} secs"
            else
                "$time ms"
        }
}