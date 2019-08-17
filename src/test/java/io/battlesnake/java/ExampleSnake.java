package io.battlesnake.java;

import io.battlesnake.core.*;
import org.jetbrains.annotations.NotNull;

import static io.battlesnake.core.JavaConstants.RIGHT;

public class ExampleSnake extends AbstractBattleSnake<ExampleSnake.GameContext> {

    public static void main(String[] args) {
        new ExampleSnake().run(8080);
    }

    // Called at the beginning of each game on Start
    @Override
    public @NotNull
    GameContext gameContext() {
        return new GameContext();
    }

    @Override
    public @NotNull
    Strategy<GameContext> gameStrategy() {
        return new AbstractStrategy<GameContext>(true) {
            // StartResponse describes snake color and head/tail type
            @Override
            public @NotNull
            StartResponse onStart(@NotNull GameContext context, @NotNull StartRequest request) {
                return new StartResponse("#ff00ff", "beluga", "bolt");
            }

            // MoveResponse can be LEFT, RIGHT, UP or DOWN
            @Override
            public @NotNull
            MoveResponse onMove(@NotNull GameContext context, @NotNull MoveRequest request) {
                return RIGHT;
            }
        };
    }

    // Add any necessary snake-specific data to GameContext class
    static class GameContext extends AbstractGameContext {
    }
}
