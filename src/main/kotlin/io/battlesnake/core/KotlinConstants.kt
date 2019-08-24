package io.battlesnake.core

const val PING = "/ping"
const val START = "/start"
const val MOVE = "/move"
const val END = "/end"

val UP = MoveResponse("up")
val DOWN = MoveResponse("down")
val LEFT = MoveResponse("left")
val RIGHT = MoveResponse("right")
