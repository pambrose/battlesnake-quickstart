# Battlesnake Quickstart 

[![Release](https://jitpack.io/v/pambrose/battlesnake-quickstart.svg)](https://jitpack.io/#pambrose/battlesnake-quickstart)
[![Build Status](https://travis-ci.org/pambrose/battlesnake-quickstart.svg?branch=master)](https://travis-ci.org/pambrose/battlesnake-quickstart)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/1abc3414ac6945ceae995618d66b45ba)](https://app.codacy.com/app/pambrose/battlesnake-quickstart?utm_source=github.com&utm_medium=referral&utm_content=pambrose/battlesnake-quickstart&utm_campaign=Badge_Grade_Dashboard)

A framework for easily creating Kotlin and Java Battlesnakes

[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/pambrose/battlesnake-quickstart)

## Motivation

Out of the box, [Battlesnake](https://battlesnake.io) requires a fair amount of JSON/REST wiring before one
can start authoring a snake. That initial exercise can prove problematic for some developers. 
This repo takes care of the wiring and communications and provides a simple framework for writing
Battlesnakes in Kotlin and Java.  

## Usage

1) Define a snake as a subclass of [AbstractBattleSnake](src/main/kotlin/io/battlesnake/core/AbstractBattleSnake.kt).

2) Define a [SnakeContext](src/main/kotlin/io/battlesnake/core/SnakeContext.kt) object to maintain
state between game moves. The framework creates SnakeContext instances at the start of every game, one for each snake 
your server is supporting.
                     
3) Define a [GameStrategy](src/main/kotlin/io/battlesnake/core/GameStrategy.kt) object to produce responses 
for the `Ping`, `Start`, `Move`, and `End` requests. The framework creates a single GameStrategy 
instance when the server launches.

## Examples

Examples of simple Battlesnakes created with this framework are [here](https://github.com/pambrose/battlesnake-examples).

### Minimal Kotlin Battlesnake

```kotlin
object ExampleSnake : AbstractBattleSnake<MySnakeContext>(){

    // Add any necessary snake-specific data to the SnakeContext class
    class MySnakeContext : SnakeContext() {
        // Snake-specific context data goes here
    }

    // Called at the beginning of each game on Start for each snake
    override fun snakeContext(): MySnakeContext = MySnakeContext()

    override fun gameStrategy() : GameStrategy<MySnakeContext> =
        strategy(verbose = true) {

            // StartResponse describes snake color and head/tail type
            onStart { context: MySnakeContext, request: StartRequest ->
                StartResponse("#ff00ff", "beluga", "bolt")
            }

            // MoveResponse can be LEFT, RIGHT, UP or DOWN
            onMove { context: MySnakeContext, request: MoveRequest ->
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
    static class MySnakeContext extends SnakeContext {
        // Snake-specific context data goes here
    }

    static class MyGameStrategy extends AbstractGameStrategy<MySnakeContext> {
        public MyGameStrategy(boolean verbose) {
            super(verbose);
        }
        
        // StartResponse describes snake color and head/tail type
        @Override
        public StartResponse onStart(MySnakeContext context, StartRequest request) {
            return new StartResponse("#ff00ff", "beluga", "bolt");
        }
        
        // MoveResponse can be LEFT, RIGHT, UP or DOWN
        @Override
        public MoveResponse onMove(MySnakeContext context, MoveRequest request) {
            return RIGHT;
        }
    }              
    
    // Called at the beginning of each game on Start for each snake
    @Override
    public MySnakeContext snakeContext() {
        return new MySnakeContext();
    }

    @Override
    public MyGameStrategy gameStrategy() {
        return new MyGameStrategy(true);
    }

    public static void main(String[] args) {
        new ExampleSnake().run(8080);
    }
}
```

## Helpful Tools

* [JsonToKotlinClass](https://github.com/wuseal/JsonToKotlinClass)