@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package io.battlesnake.core

import io.battlesnake.core.Board.Companion.BOARD_ORIGIN
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlin.math.abs


val Int.isEven get() = this % 2 == 0
val Int.isOdd get() = this % 2 != 0

interface GameResponse

object PingResponse : GameResponse {
  override fun toString() = PingResponse::class.simpleName ?: "PingResponse"
}

@Serializable
data class StartRequest(val board: Board, val game: Game, val turn: Int, val you: You) {
  val gameId
    get() = game.id

  fun toJson() = Json.stringify(serializer(), this)

  companion object {
    fun primeClassLoader() {
      val start =
        StartRequest(
          Board(3, 4, emptyList(), emptyList()),
          Game(""),
          1,
          You("", "", emptyList(), 3, "")
                    )
      val json = start.toJson()
      toObject(json)
    }

    val json = Json(JsonConfiguration.Stable.copy(ignoreUnknownKeys = true, isLenient = true))
    fun toObject(s: String) = json.parse(serializer(), s)
  }
}

@Serializable
data class StartResponse(val color: String = "",
                         val headType: String = "",
                         val tailType: String = "") : GameResponse {
  fun toJson() = Json.stringify(serializer(), this)
}

@Serializable
data class MoveRequest(
  val board: Board,
  val game: Game,
  val turn: Int,
  val you: You
                      ) {
  val gameId
    get() = game.id

  val boardCenter
    get() = board.center

  val boardOrigin
    get() = board.origin

  val boardUpperLeft
    get() = board.upperLeft

  val boardUpperRight
    get() = board.upperRight

  val boardLowerRight
    get() = board.lowerRight

  val boardLowerLeft
    get() = board.lowerLeft

  val boardSize by lazy { Pair(board.width, board.height) }

  val isAtCenter
    get() = you.headPosition == boardCenter

  val isAtOrigin
    get() = you.headPosition == BOARD_ORIGIN

  val isAtUpperLeft
    get() = you.headPosition == boardUpperLeft

  val isAtUpperRight
    get() = you.headPosition == boardUpperRight

  val isAtLowerRight
    get() = you.headPosition == boardLowerRight

  val isAtLowerLeft
    get() = you.headPosition == boardLowerLeft

  val foodList
    get() = board.food

  val snakeList
    get() = board.snakes

  val isFoodAvailable
    get() = foodList.isNotEmpty()

  val bodyLength
    get() = you.bodyLength

  val headPosition
    get() = you.headPosition

  companion object {
    val json = Json(JsonConfiguration.Stable.copy(ignoreUnknownKeys = true, isLenient = true))
    fun toObject(s: String) = json.parse(serializer(), s)
  }
}

@Serializable
data class MoveResponse(val move: String) : GameResponse {
  fun toJson() = Json.stringify(serializer(), this)
}

@Serializable
data class EndRequest(val board: Board,
                      val game: Game,
                      val turn: Int,
                      val you: You) {
  val gameId
    get() = game.id

  companion object {
    val json = Json(JsonConfiguration.Stable.copy(ignoreUnknownKeys = true, isLenient = true))
    fun toObject(s: String) = json.parse(serializer(), s)
  }
}

object EndResponse : GameResponse {
  override fun toString() = EndResponse::class.simpleName ?: "EndResponse"
}

@Serializable
data class Game(val id: String)

@Serializable
data class Board(val height: Int,
                 val width: Int,
                 val food: List<Food>,
                 val snakes: List<Snake>) {
  @Transient
  val origin = BOARD_ORIGIN

  @Transient
  val upperLeft = BOARD_ORIGIN

  val upperRight: Position by lazy { Position(width - 1, 0) }
  val lowerRight: Position by lazy { Position(width - 1, height - 1) }
  val lowerLeft: Position by lazy { Position(0, height - 1) }

  val center by lazy {
    val centerX = (if (width.isEven) width / 2 else (width + 1) / 2) - 1
    val centerY = (if (height.isEven) height / 2 else (height + 1) / 2) - 1
    Position(centerX, centerY)
  }

  companion object {
    val BOARD_ORIGIN = Position(0, 0)
  }
}

@Serializable
data class Snake(val name: String,
                 val id: String,
                 val body: List<Body>,
                 val health: Int,
                 val shout: String) {
  val headPosition
    get() = bodyPosition(0)

  fun bodyPosition(pos: Int) = body[pos].position

  val bodyLength
    get() = body.map { it.position }.distinct().size
}

@Serializable
data class You(val name: String,
               val id: String,
               val body: List<Body>,
               val health: Int,
               val shout: String) {
  val headPosition by lazy { bodyPosition(0) }

  fun bodyPosition(pos: Int) = body[pos].position

  val bodyLength
    get() = body.map { it.position }.distinct().size
}

@Serializable
data class Body(val x: Int, val y: Int) {
  val position by lazy { Position(x, y) }
}

@Serializable
data class Food(val x: Int, val y: Int) {
  val position by lazy { Position(x, y) }
}

@Serializable
data class Position(val x: Int, val y: Int) {
  operator fun minus(other: Position) = abs(this.x - other.x) + abs(this.y - other.y)
}