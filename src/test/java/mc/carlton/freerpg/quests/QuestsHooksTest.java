package mc.carlton.freerpg.quests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.bukkit.GameMode;
import org.junit.jupiter.api.Test;

class QuestsHooksTest {

  @Test
  void disabledWorldCheckIsCaseInsensitive() {
    assertFalse(Quests.countsFor(GameMode.SURVIVAL, "Creative_World", List.of("creative_world")));
    assertTrue(Quests.countsFor(GameMode.SURVIVAL, "world", List.of("creative_world")));
  }

  @Test
  void survivalAndAdventureCountButCreativeAndSpectatorDoNot() {
    assertTrue(Quests.countsFor(GameMode.SURVIVAL, "world", List.of()));
    assertTrue(Quests.countsFor(GameMode.ADVENTURE, "world", List.of()));
    assertFalse(Quests.countsFor(GameMode.CREATIVE, "world", List.of()));
    assertFalse(Quests.countsFor(GameMode.SPECTATOR, "world", List.of()));
  }
}
