package mc.carlton.freerpg.skills;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Matching of user input to skill names. */
class SkillNameTest {

  @Test
  void camelCaseNamesMatchTheirSkill() {
    assertEquals(SkillName.DIGGING, SkillName.matchSkillName("digging"));
    assertEquals(SkillName.AXE_MASTERY, SkillName.matchSkillName("axeMastery"));
    assertEquals(SkillName.BEAST_MASTERY, SkillName.matchSkillName("beastMastery"));
  }

  @Test
  void buildingIsAPassiveSkillWithoutGlobal() {
    assertEquals(SkillName.BUILDING, SkillName.matchSkillName("building"));
    assertTrue(SkillName.getPassiveSkillValues().contains(SkillName.BUILDING));
    assertTrue(SkillName.getValuesWithoutGlobal().contains(SkillName.BUILDING));
    assertFalse(SkillName.getValuesWithoutGlobal().contains(SkillName.GLOBAL));
    assertFalse(SkillName.getMainSkillValues().contains(SkillName.BUILDING));
  }

  @Test
  void enumOrderMatchesRegistryOrder() {
    List<SkillName> mapped = new ArrayList<>();
    for (String label : SkillRegistry.labelsWithGlobal()) {
      mapped.add(SkillName.matchSkillName(label));
    }
    assertEquals(SkillName.getValues(), mapped);
  }

  @Test
  void unknownInputGivesNullInsteadOfAnException() {
    assertNull(SkillName.matchSkillName("notASkill"));
    assertNull(SkillName.matchSkillName(""));
    assertNull(SkillName.matchSkillName("maceMastery")); // a weapon skill, not a main skill
  }

  @Test
  void aeronauticsIsAPassiveSkillWithoutGlobal() {
    assertEquals(SkillName.AERONAUTICS, SkillName.matchSkillName("aeronautics"));
    assertTrue(SkillName.getPassiveSkillValues().contains(SkillName.AERONAUTICS));
    assertTrue(SkillName.getValuesWithoutGlobal().contains(SkillName.AERONAUTICS));
    assertFalse(SkillName.getMainSkillValues().contains(SkillName.AERONAUTICS));
  }
}
