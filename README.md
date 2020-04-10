# Battlesnake Quickstart 

[![Release](https://jitpack.io/v/pambrose/battlesnake-quickstart.svg)](https://jitpack.io/#pambrose/battlesnake-quickstart)
[![Build Status](https://travis-ci.org/pambrose/battlesnake-quickstart.svg?branch=master)](https://travis-ci.org/pambrose/battlesnake-quickstart)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/1abc3414ac6945ceae995618d66b45ba)](https://app.codacy.com/app/pambrose/battlesnake-quickstart?utm_source=github.com&utm_medium=referral&utm_content=pambrose/battlesnake-quickstart&utm_campaign=Badge_Grade_Dashboard)
[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/pambrose/battlesnake-quickstart)

A framework for easily creating Kotlin and Java Battlesnakes

## Motivation

Out of the box, [Battlesnake](https://battlesnake.io) requires a fair amount of JSON/REST wiring before one
can start authoring a snake. That initial exercise can prove problematic for some developers. 
This repo takes care of the wiring and communications and provides a simple framework for writing
Battlesnakes in Kotlin and Java.  

## Usage

A snake defined as a subclass of [AbstractBattleSnake](src/main/kotlin/io/battlesnake/core/AbstractBattleSnake.kt) and
implements methods to produce [SnakeContext](src/main/kotlin/io/battlesnake/core/AbstractSnakeContext.kt) 
and [Strategy](src/main/kotlin/io/battlesnake/core/Strategy.kt) objects. 

* The SnakeContext class is snake-specific. An instance is created at the start of every game for each snake 
your server is supporting and provides context between game turns. 
* The Strategy specifies responses for Ping, Start, Move, and End commands.

## Helpful Tools

* [JsonToKotlinClass](https://github.com/wuseal/JsonToKotlinClass)

## Examples

Examples of simple Battlesnakes created with this framework are [here](https://github.com/pambrose/battlesnake-examples).

### Minimal Kotlin Battlesnake

```kotlin
object ExampleSnake : AbstractBattleSnake<SnakeContext>(){

    // Add any necessary snake-specific data to SnakeContext class
    class SnakeContext(gameId: String, youId: String) : AbstractSnakeContext(gameId, youId) {
        // Context instance data goes here
    }

    // Called at the beginning of each game on Start
    override fun snakeContext(gameId: String, youId: String): SnakeContext = SnakeContext(gameId, youId)

    override fun gameStrategy() : Strategy<SnakeContext> =
        strategy(true) {

            // StartResponse describes snake color and head/tail type
            onStart { context: SnakeContext, request: StartRequest ->
                StartResponse("#ff00ff", "beluga", "bolt")
            }

            // MoveResponse can be LEFT, RIGHT, UP or DOWN
            onMove { context: SnakeContext, request: MoveRequest ->
                RIGHT
            }
        }

    @JvmStatic
    fun main(args: Array<String>) {
        run()
    }
}
```

### Minimal Java Battlesnake

```java
public class ExampleSnake extends AbstractBattleSnake<ExampleSnake.SnakeContext> {

    // SnakeContext can contain any data you want
    static class SnakeContext(String gameId, String youId) extends AbstractSnakeContext {
       public SnakeContext(@NotNull String gameId, @NotNull String youId) {
         super(gameId, youId);
       }
       // Context instance data goes here
    }

    // Called at the beginning of each game on Start
    @Override
    public SnakeContext snakeContext(String gameId, String youId) {
        return new SnakeContext(gameId, youId);
    }

    @Override
    public Strategy<SnakeContext> gameStrategy() {
        return new AbstractStrategy<SnakeContext>(true) {
            // StartResponse describes snake color and head/tail type
            @Override
            public StartResponse onStart(SnakeContext context, StartRequest request) {
                return new StartResponse("#ff00ff", "beluga", "bolt");
            }

            // MoveResponse can be LEFT, RIGHT, UP or DOWN
            @Override
            public MoveResponse onMove(SnakeContext context, MoveRequest request) {
                return RIGHT;
            }
        };
    }

    public static void main(String[] args) {
        new ExampleSnake().run(8080);
    }
}
```