/*
 * Copyright © 2020 Paul Ambrose (pambrose@mac.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.battlesnake.java;

import io.battlesnake.core.AbstractBattleSnake;
import io.battlesnake.core.AbstractGameStrategy;
import io.battlesnake.core.DescribeResponse;
import io.battlesnake.core.MoveRequest;
import io.battlesnake.core.MoveResponse;
import io.battlesnake.core.SnakeContext;
import io.ktor.application.ApplicationCall;

import static io.battlesnake.core.JavaConstants.RIGHT;
import static io.battlesnake.java.ExampleSnake.MySnakeContext;

public class ExampleSnake extends AbstractBattleSnake<MySnakeContext> {

  // Called at the beginning of each game on Start
  @Override
  public MySnakeContext snakeContext() {
    return new MySnakeContext();
  }

  // Called once during server launch
  @Override
  public MyGameStrategy gameStrategy() {
    return new MyGameStrategy(true);
  }

  // Add any additional data and methods to SnakeContext class
  static class MySnakeContext extends SnakeContext {
  }

  static class MyGameStrategy extends AbstractGameStrategy<MySnakeContext> {
    public MyGameStrategy(boolean verbose) {
      super(verbose);
    }

    // DescribeResponse describes snake color and head/tail type
    @Override
    public DescribeResponse onDescribe(ApplicationCall call) {
      return new DescribeResponse("me", "#ff00ff", "beluga", "bolt");
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
