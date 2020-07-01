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

2) Implement the two abstract classes of AbstractBattleSnake: `snakeContext()` and `gameStrategy()`.

3) Define a [SnakeContext](src/main/kotlin/io/battlesnake/core/SnakeContext.kt) object to maintain
state between game moves. The framework creates SnakeContext instances at the start of every game 
(one for each snake your server is supporting).
                     
4) Define a [GameStrategy](src/main/kotlin/io/battlesnake/core/GameStrategy.kt) object to produce responses 
for the `/`, `/start`, `/move`, and `/end` requests. The framework creates a single GameStrategy 
instance when the server launches.

## Examples

Examples of simple Battlesnakes created with this framework are [here](https://github.com/pambrose/battlesnake-examples).

### Minimal Kotlin Battlesnake

```kotlin
object ExampleSnake : AbstractBattleSnake<MySnakeContext>(){
  
    // Called once during server launch
    override fun gameStrategy() : GameStrategy<MySnakeContext> =
        strategy(verbose = true) {

            // DescribeResponse describes snake color and head/tail type
            onDescribe { call: ApplicationCall ->
                DescribeResponse("#ff00ff", "beluga", "bolt")
            }

            // MoveResponse can be LEFT, RIGHT, UP or DOWN
            onMove { context: MySnakeContext, request: MoveRequest ->
                RIGHT
            }
        }

    // Called at the beginning of each game on Start for each snake
    override fun snakeContext(): MySnakeContext = MySnakeContext()

    // Add any necessary snake-specific data to the SnakeContext class
    class MySnakeContext : SnakeContext() {
        // Snake-specific context data goes here
    }

    @JvmStatic
    fun main(args: Array<String>) {
        run()
    }
}
```

### Minimal Java Battlesnake

```java
public class ExampleSnake extends AbstractBattleSnake<ExampleSnake.MySnakeContext> {

    // Called at the beginning of each game on Start for each snake
    @Override
    public MySnakeContext snakeContext() {
        return new MySnakeContext();
    }

    // Called once during server launch
    @Override
    public MyGameStrategy gameStrategy() {
        return new MyGameStrategy(true);
    }

    // Add any necessary snake-specific data to the SnakeContext class
    static class MySnakeContext extends SnakeContext {
        // Snake-specific context data goes here
    }

    static class MyGameStrategy extends AbstractGameStrategy<MySnakeContext> {
        public MyGameStrategy(boolean verbose) {
            super(verbose);
        }
        
        // DescibeResponse describes snake color and head/tail type
        @Override
        public DescibeResponse onDescribe(MySnakeContext context, StartRequest request) {
            return new DescibeResponse("me", "#ff00ff", "beluga", "bolt");
        }
        
        // MoveResponse can be LEFT, RIGHT, UP or DOWN
        @Override
        public MoveResponse onMove(MySnakeContext context, MoveRequest request) {
            return RIGHT;
        }
    }              
    
    public static void main(String[] args) {
        new ExampleSnake().run(8080);
    }
}
```

## Helpful Tools

* [JsonToKotlinClass](https://github.com/wuseal/JsonToKotlinClass)