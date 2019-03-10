package io.battlesnake.java;

import io.battlesnake.core.*;

import static io.battlesnake.core.JavaConstants.RIGHT;

public class ExampleSnake extends AbstractBattleSnake<ExampleSnake.GameContext> {

    public static void main(String[] args) {
        new ExampleSnake().run(8080);
    }

    // Called at the beginning of each game on Start
    @Override
    public GameContext gameContext() {
        return new GameContext();
    }

    @Override
    public Strategy<GameContext> gameStrategy() {
        return new AbstractStrategy<GameContext>(true) {
            // StartResponse describes snake color and head/tail type
            @Override
            public StartResponse onStart(GameContext context, StartRequest request) {
                return new StartResponse("#ff00ff", "beluga", "bolt");
            }

            // MoveResponse can be LEFT, RIGHT, UP or DOWN
            @Override
            public MoveResponse onMove(GameContext context, MoveRequest request) {
                return RIGHT;
            }
        };
    }

    // Add any necessary snake-specific data to GameContext class
    static class GameContext extends AbstractGameContext {
    }
}
