# BattleSnake QuickStart 

A framework for creating Kotlin and Java BattleSnakes

[![Build Status](https://travis-ci.org/pambrose/battlesnake-dispatch.svg?branch=master)](https://travis-ci.org/pambrose/simple-battlesnake)
[![Release](https://jitpack.io/v/pambrose/battlesnake-dispatch.svg)](https://jitpack.io/#pambrose/battlesnake-dispatch)

## Motivation

Out of the box, [BattleSnake](https://battlesnake.io) requires a fair amount of JSON/REST wiring before one 
can start authoring a snake. That initial exercise can prove problematic for some developers. 
This repo takes care of the wiring and communications and provides a simple framework for writing 
BattleSnakes in Kotlin and Java.  

## Usage

A snake defined as a subclass of [AbstractBattleSnake](src/main/kotlin/io/battlesnake/core/AbstractBattleSnake.kt) and 
implements methods to produce [GameContext](src/main/kotlin/io/battlesnake/core/AbstractGameContxt.kt) 
and [Strategy](src/main/kotlin/io/battlesnake/core/Strategy.kt) objects. 

* A GameContext object is snake-specific and is created at the start of every game and provides context between game turns. 
* A Strategy specifies responses for Ping, Start, Move, and End commands.

## Examples

### Minimal Kotlin BattleSnake

```kotlin
object ExampleSnake : AbstractBattleSnake<GameContext>(){

    // Add any necessary snake-specific data to GameContext class
    class GameContext : AbstractGameContext()

    // Called at the beginning of each game on Start
    override fun gameContext(): GameContext = GameContext()

    override fun gameStrategy() : Strategy<GameContext> =
        strategy(true) {

            // StartResponse describes snake color and head/tail type
            onStart { context: GameContext, request: StartRequest ->
                StartResponse("#ff00ff", "beluga", "bolt")
            }

            // MoveResponse can be LEFT, RIGHT, UP or DOWN
            onMove { context: GameContext, request: MoveRequest ->
                RIGHT
            }
        }

    @JvmStatic
    fun main(args: Array<String>) {
        run()
    }
}
```

### Minimal Java BattleSnake

```java
public class ExampleSnake extends AbstractBattleSnake<ExampleSnake.GameContext> {

    // GameContext can contain any data you want
    static class GameContext extends AbstractGameContext {
    }

    // Called at the beginning of each game on Start
    @Override
    public GameContext gameContext() {
        return new GameContext();
    }

    @Override
    public Strategy<GameContext> gameStrategy() {
        return new AbstractStrategy<GameContext>(true) {
            // StartReponse describes snake color and head/tail type
            @Override
            public StartResponse onStart(GameContext context, StartRequest request) {
                return new StartResponse("#ff00ff", "beluga", "bolt");
            }

            // MoveReponse can be LEFT, RIGHT, UP or DOWN
            @Override
            public MoveResponse onMove(GameContext context, MoveRequest request) {
                return RIGHT;
            }
        };
    }

    public static void main(String[] args) {
        new ExampleSnake().run(8080);
    }
}
```

