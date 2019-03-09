package io.battlesnake.core

import spark.Request
import spark.Response

abstract class AbstractGameContext {
    lateinit var request: Request
    lateinit var response: Response
}