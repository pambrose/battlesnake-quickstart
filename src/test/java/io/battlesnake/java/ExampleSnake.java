package io.battlesnake.java;

import io.battlesnake.core.AbstractBattleSnake;
import io.battlesnake.core.AbstractDefaultGameStrategy;
import io.battlesnake.core.GameStrategy;
import io.battlesnake.core.MoveRequest;
import io.battlesnake.core.MoveResponse;
import io.battlesnake.core.SnakeContext;
import io.battlesnake.core.StartRequest;
import io.battlesnake.core.StartResponse;

import static io.battlesnake.core.JavaConstants.RIGHT;
import static io.battlesnake.java.ExampleSnake.MySnakeContext;

public class ExampleSnake extends AbstractBattleSnake<MySnakeContext> {

  // Add any additional data and methods to SnakeContext class
  static class MySnakeContext extends SnakeContext {
  }

  static class MyGameStrategy extends AbstractDefaultGameStrategy<MySnakeContext> {
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

  // Called at the beginning of each game on Start
  @Override
  public MySnakeContext snakeContext() {
    return new MySnakeContext();
  }

  @Override
  public GameStrategy gameStrategy() {
    return new MyGameStrategy(true);
  }

  public static void main(String[] args) {
    new ExampleSnake().run(8080);
  }
}
