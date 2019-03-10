package io.battlesnake.example.java;

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

    // GameContext can contain any data you want
    static class GameContext extends AbstractGameContext {
    }
}
