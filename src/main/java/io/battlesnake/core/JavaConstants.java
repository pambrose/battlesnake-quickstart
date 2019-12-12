package io.battlesnake.core;

import static io.battlesnake.core.KotlinConstantsKt.getDOWN;
import static io.battlesnake.core.KotlinConstantsKt.getLEFT;
import static io.battlesnake.core.KotlinConstantsKt.getRIGHT;
import static io.battlesnake.core.KotlinConstantsKt.getUP;

public interface JavaConstants {
  MoveResponse UP = getUP();
  MoveResponse DOWN = getDOWN();
  MoveResponse LEFT = getLEFT();
  MoveResponse RIGHT = getRIGHT();
}
