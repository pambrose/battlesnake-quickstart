package io.battlesnake.core

import spark.Request
import spark.Response

abstract class AbstractGameContext {
    lateinit var request: Request
    lateinit var response: Response
    private val gameStartTimeMillis: Long = System.currentTimeMillis()

    val elapsedTimeMillis get() = System.currentTimeMillis() - gameStartTimeMillis

    val elapsedTimeMsg: String
        get() {
            val time = elapsedTimeMillis
            return if (time > 1000)
                "${(time / 1000) as Int} secs"
            else
                "$time ms"
        }
}