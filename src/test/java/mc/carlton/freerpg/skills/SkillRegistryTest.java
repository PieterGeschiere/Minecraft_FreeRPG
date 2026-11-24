package mc.carlton.freerpg.skills;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class SkillRegistryTest {

  private static final List<String> LABELS = Arrays.asList("digging", "woodcutting", "mining",
      "farming", "fishing", "archery", "beastMastery", "swordsmanship", "defense", "axeMastery",
      "repair", "agility", "alchemy", "smelting", "enchanting", "building", "aeronautics");

  private static final List<String> TITLES = Arrays.asList("Digging", "Woodcutting", "Mining",
      "Farming", "Fishing", "Archery", "Beast Mastery", "Swordsmanship", "Defense", "Axe Mastery",
      "Repair", "Agility", "Alchemy", "Smelting", "Enchanting", "Building", "Aeronautics", "Global");

  @Test
  void labelsAreTheSeventeenSkillsInOrder() {
    assertEquals(LABELS, SkillRegistry.labels());
  }

  @Test
  void labelsWithGlobalAppendsGlobalLast() {
    List<String> expected = new ArrayList<>(LABELS);
    expected.add("global");
    assertEquals(expected, SkillRegistry.labelsWithGlobal());
  }

  @Test
  void titlesWithGlobalAreExact() {
    assertEquals(TITLES, SkillRegistry.titlesWithGlobal());
    assertEquals(TITLES.subList(0, 17), SkillRegistry.titles());
  }

  @Test
  void titleOfMapsLabels() {
    assertEquals("Axe Mastery", SkillRegistry.titleOf("axeMastery"));
    assertEquals("Global", SkillRegistry.titleOf("global"));
  }

  @Test
  void indexOf() {
    assertEquals(14, SkillRegistry.indexOf("enchanting"));
    assertEquals(15, SkillRegistry.indexOf("building"));
    assertEquals(-1, SkillRegistry.indexOf("global"));
    assertEquals(-1, SkillRegistry.indexOf("nope"));
    assertEquals(16, SkillRegistry.indexOf("aeronautics"));
    assertEquals(17, SkillRegistry.labelsWithGlobal().indexOf("global"));
    assertEquals(-1, SkillRegistry.labels().indexOf("global"));
  }

  @Test
  void classification() {
    assertTrue(SkillRegistry.isSkill("smelting"));
    assertFalse(SkillRegistry.isSkill("global"));
    assertTrue(SkillRegistry.isMain("axeMastery"));
    assertFalse(SkillRegistry.isMain("repair"));
    assertTrue(SkillRegistry.isPassive("smelting"));
    assertTrue(SkillRegistry.isPassive("building"));
    assertTrue(SkillRegistry.isPassive("aeronautics"));
    assertFalse(SkillRegistry.isMain("aeronautics"));
    assertFalse(SkillRegistry.isMain("building"));
    assertFalse(SkillRegistry.isPassive("digging"));
    assertFalse(SkillRegistry.isMain("global"));
    assertFalse(SkillRegistry.isPassive("global"));
  }

  @Test
  void labelsMatchSkillNameEnumOrder() {
    List<SkillName> mapped = new ArrayList<>();
    for (String label : SkillRegistry.labels()) {
      SkillName name = SkillName.matchSkillName(label);
      assertNotNull(name, label);
      mapped.add(name);
    }
    assertEquals(SkillName.getValuesWithoutGlobal(), mapped);
  }

  @Test
  void labelArrayIsACopy() {
    String[] arr = SkillRegistry.labelArray();
    assertEquals(LABELS, Arrays.asList(arr));
    arr[0] = "x";
    assertEquals("digging", SkillRegistry.labels().get(0));
  }

  @Test
  void listsAreImmutable() {
    assertThrows(UnsupportedOperationException.class, () -> SkillRegistry.labels().add("x"));
    assertThrows(UnsupportedOperationException.class,
        () -> SkillRegistry.labelsWithGlobal().add("x"));
    assertThrows(UnsupportedOperationException.class, () -> SkillRegistry.titles().add("x"));
    assertThrows(UnsupportedOperationException.class,
        () -> SkillRegistry.titlesWithGlobal().add("x"));
  }

  @Test
  void mainLabelsAreTheFirstTenInOrderAndImmutable() {
    assertEquals(LABELS.subList(0, 10), SkillRegistry.mainLabels());
    assertThrows(UnsupportedOperationException.class, () -> SkillRegistry.mainLabels().add("x"));
  }

  @Test
  void passiveLabelsAreTheLastSevenInOrderAndImmutable() {
    assertEquals(Arrays.asList("repair", "agility", "alchemy", "smelting", "enchanting", "building", "aeronautics"),
        SkillRegistry.passiveLabels());
    assertThrows(UnsupportedOperationException.class,
        () -> SkillRegistry.passiveLabels().add("x"));
  }
}