package io.battlesnake.java;

import io.battlesnake.core.AbstractBattleSnake;
import io.battlesnake.core.AbstractSnakeContext;
import io.battlesnake.core.AbstractStrategy;
import io.battlesnake.core.MoveRequest;
import io.battlesnake.core.MoveResponse;
import io.battlesnake.core.StartRequest;
import io.battlesnake.core.StartResponse;
import io.battlesnake.core.Strategy;

import static io.battlesnake.core.JavaConstants.RIGHT;

public class ExampleSnake extends AbstractBattleSnake<ExampleSnake.SnakeContext> {

  public static void main(String[] args) {
    new ExampleSnake().run(8080);
  }

  // Called at the beginning of each game on Start
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

  // Add any necessary snake-specific data to SnakeContext class
  static class SnakeContext extends AbstractSnakeContext {
  }
}
