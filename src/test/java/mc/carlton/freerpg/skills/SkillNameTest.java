package mc.carlton.freerpg.skills;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
  void unknownInputGivesNullInsteadOfAnException() {
    assertNull(SkillName.matchSkillName("notASkill"));
    assertNull(SkillName.matchSkillName(""));
    assertNull(SkillName.matchSkillName("maceMastery")); // a weapon skill, not a main skill
  }
}
