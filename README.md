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

* The SnakeContext class is snake-specific. The framework creates an instance at the start of every game (for each snake 
your server is supporting), and it provides context between game turns. 
* The Strategy specifies responses for the `Ping`, `Start`, `Move`, and `End` commands.

## Examples

Examples of simple Battlesnakes created with this framework are [here](https://github.com/pambrose/battlesnake-examples).

### Minimal Kotlin Battlesnake

```kotlin
object ExampleSnake : AbstractBattleSnake<SnakeContext>(){

    // Add any necessary snake-specific data to the SnakeContext class
    class SnakeContext : AbstractSnakeContext() {
        // Snake-specific context data goes here
    }

    // Called at the beginning of each game on Start for each snake
    override fun snakeContext(): SnakeContext = SnakeContext()

    override fun gameStrategy() : Strategy<SnakeContext> =
        strategy(verbose = true) {

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

    // Add any necessary snake-specific data to the SnakeContext class
    static class SnakeContext extends AbstractSnakeContext {
        // Snake-specific context data goes here
    }

    // Called at the beginning of each game on Start for each snake
    @Override
    public SnakeContext snakeContext() {
        return new SnakeContext();
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

## Helpful Tools

* [JsonToKotlinClass](https://github.com/wuseal/JsonToKotlinClass)