package io.battlesnake.core

import spark.Request
import spark.Response

abstract class AbstractGameContext {
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
            return if (time > 1000)
                "${time / 1000} secs"
            else
                "$time ms"
        }
}