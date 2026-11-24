package mc.carlton.freerpg.commands.sub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import mc.carlton.freerpg.commands.sub.SkillTreeCommand.MenuKind;
import org.junit.jupiter.api.Test;

class SkillTreeMenuSelectionTest {

  @Test
  void firstTenLabelsAreMain() {
    List<String> labels = List.of("digging", "woodcutting", "mining", "farming", "fishing",
        "archery", "beastMastery", "swordsmanship", "defense", "axeMastery");
    for (String label : labels) {
      assertEquals(MenuKind.MAIN, SkillTreeCommand.menuKindFor(label), label);
    }
  }

  @Test
  void lastSixLabelsArePassive() {
    for (String label : List.of("repair", "agility", "alchemy", "smelting", "enchanting", "building")) {
      assertEquals(MenuKind.PASSIVE, SkillTreeCommand.menuKindFor(label), label);
    }
  }

  @Test
  void globalIsGlobal() {
    assertEquals(MenuKind.GLOBAL, SkillTreeCommand.menuKindFor("global"));
  }

  @Test
  void unknownIsNull() {
    assertNull(SkillTreeCommand.menuKindFor("nope"));
    assertNull(SkillTreeCommand.menuKindFor(null));
    assertNull(SkillTreeCommand.menuKindFor("Smelting"));
  }
}
