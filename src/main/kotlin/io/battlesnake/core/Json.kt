package io.battlesnake.core

import io.battlesnake.core.Board.Companion.BOARD_ORIGIN
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import java.lang.Math.abs

fun Int.isEven() = this % 2 == 0
fun Int.isOdd() = this % 2 != 0

interface GameResponse

object PingResponse : GameResponse {
    override fun toString() = PingResponse::class.simpleName!!
}

@Serializable
data class StartRequest(val board: Board, val game: Game, val turn: Int, val you: You) {
    @Transient
    val gameId
        get() = game.id

    fun toJson() = Json.stringify(StartRequest.serializer(), this)

    companion object {
        fun primeClassLoader() {
            val start =
                StartRequest(
                    Board(emptyList(), 3, emptyList(), 4),
                    Game(""),
                    1,
                    You(emptyList(), 3, "", "")
                )
            val json = start.toJson()
            StartRequest.toObject(json)
        }

        fun toObject(json: String) = Json.parse(StartRequest.serializer(), json)
    }
}

@Serializable
data class StartResponse(
    val color: String = "",
    val headType: String = "",
    val tailType: String = ""
) : GameResponse {
    fun toJson() = Json.stringify(StartResponse.serializer(), this)
}

@Serializable
data class MoveRequest(
    val board: Board,
    val game: Game,
    val turn: Int,
    val you: You
) {
    @Transient
    val gameId
        get() = game.id
    @Transient
    val boardCenter
        get() = board.center
    @Transient
    val boardOrigin
        get() = board.origin
    @Transient
    val boardUpperLeft
        get() = board.upperLeft
    @Transient
    val boardUpperRight
        get() = board.upperRight
    @Transient
    val boardLowerRight
        get() = board.lowerRight
    @Transient
    val boardLowerLeft
        get() = board.lowerLeft
    @Transient
    val boardSize by lazy { Pair(board.width, board.height) }
    @Transient
    val isAtCenter
        get() = you.headPosition == boardCenter
    @Transient
    val isAtOrigin
        get() = you.headPosition == BOARD_ORIGIN
    @Transient
    val isAtUpperLeft
        get() = you.headPosition == boardUpperLeft
    @Transient
    val isAtUpperRight
        get() = you.headPosition == boardUpperRight
    @Transient
    val isAtLowerRight
        get() = you.headPosition == boardLowerRight
    @Transient
    val isAtLowerLeft
        get() = you.headPosition == boardLowerLeft
    @Transient
    val foodList
        get() = board.food
    @Transient
    val snakeList
        get() = board.snakes
    @Transient
    val isFoodAvailable
        get() = foodList.isNotEmpty()
    @Transient
    val nearestFood by lazy { you.nearestFood(foodList) }
    @Transient
    val nearestFoodPosition
        get() = nearestFood.position
    @Transient
    val bodyLength
        get() = you.bodyLength

    @Transient
    val headPosition
        get() = you.headPosition

    companion object {
        fun toObject(json: String) = Json.parse(MoveRequest.serializer(), json)
    }
}

@Serializable
data class MoveResponse(val move: String) : GameResponse {
    fun toJson() = Json.stringify(MoveResponse.serializer(), this)
}

@Serializable
data class EndRequest(
    val board: Board,
    val game: Game,
    val turn: Int,
    val you: You
) {
    @Transient
    val gameId
        get() = game.id

    companion object {
        fun toObject(json: String) = Json.parse(EndRequest.serializer(), json)
    }
}

object EndResponse : GameResponse {
    override fun toString() = EndResponse::class.simpleName!!
}

@Serializable
data class Game(val id: String)

@Serializable
data class Board(
    val food: List<Food>,
    val height: Int,
    val snakes: List<Snake>,
    val width: Int
) {
    @Transient
    val origin = BOARD_ORIGIN

    @Transient
    val upperLeft = BOARD_ORIGIN

    @Transient
    val upperRight: Position  by lazy { Position(width - 1, 0) }

    @Transient
    val lowerRight: Position  by lazy { Position(width - 1, height - 1) }

    @Transient
    val lowerLeft: Position  by lazy { Position(0, height - 1) }

    @Transient
    val center by lazy {
        val centerX =
            (if (width.isEven())
                width / 2
            else
                (width + 1) / 2) - 1

        val centerY =
            (if (height.isEven())
                height / 2
            else
                (height + 1) / 2) - 1
        Position(centerX, centerY)
    }

    companion object {
        val BOARD_ORIGIN = Position(0, 0)
    }
}

@Serializable
data class Snake(
    val body: List<Body>,
    val health: Int,
    val id: String,
    val name: String
) {
    @Transient
    val bodyLength
        get() = body.map { it.position }.distinct().size

    @Transient
    val headPosition
        get() = bodyPosition(0)

    fun bodyPosition(pos: Int) = body[pos].position
}

@Serializable
data class You(
    val body: List<Body>,
    val health: Int,
    val id: String,
    val name: String
) {
    @Transient
    val bodyLength
        get() = body.map { it.position }.distinct().size

    @Transient
    val headPosition by lazy { bodyPosition(0) }

    fun nearestFood(foodList: List<Food>) =
        foodList
            .map { Pair(it, movesTo(it)) }
            .maxBy { it.second }!!
            .first

    fun bodyPosition(pos: Int) = body[pos].position

    fun movesTo(food: Food) = headPosition - food.position
}

@Serializable
data class Body(
    val x: Int,
    val y: Int
) {
    @Transient
    val position by lazy { Position(x, y) }
}

@Serializable
data class Food(
    val x: Int,
    val y: Int
) {
    @Transient
    val position by lazy { Position(x, y) }
}

@Serializable
data class Position(
    val x: Int,
    val y: Int
) {
    operator fun minus(other: Position) = abs(this.x - other.x) + abs(this.y - other.y)
}