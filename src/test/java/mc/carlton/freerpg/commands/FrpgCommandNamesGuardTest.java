package mc.carlton.freerpg.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Guards against losing a /frpg command name while the legacy if/else chain is migrated into
 * sub-command classes. Every pinned name must be registered.
 */
class FrpgCommandNamesGuardTest {

  // Derived from FrpgCommands.java (before migration) with
  // grep -o 'args\[0\].equalsIgnoreCase("[^"]*")' plus the alias literals on wrapped continuation
  // lines of those conditions (read by hand), covering all 43 args[0] branches and their aliases.
  private static final List<String> PINNED = List.of(
      "party", "quests", "elites", "bloodmoon", "weapons", "weaponSkills", "reloadWeapons", "help", "?",
      "saveStats", "statSave", "info", "use", "enchantItem", "enchant",
      "leaderboard", "statLeaders", "top", "statLookup", "lookupStats", "stats",
      "resetCooldown", "cooldownReset", "createFakePlayers", "createFakeProfiles",
      "deleteFakePlayers", "deleteFakeProfiles", "removeFakePlayers", "removeFakeProfiles",
      "thoroughSave", "loadInAllPlayerFiles", "loadInPlayerFiles",
      "giveEXP", "expGive", "setLevel", "levelSet", "statReset", "resetStat",
      "setSouls", "soulsSet", "setTokens", "tokensSet",
      "setMultiplier", "multiplierSet", "addMultiplier", "changeMultiplier",
      "toggleFlamePick", "flamePickToggle", "toggleFlint", "flintToggle",
      "toggleGrapple", "grappleToggle", "toggleHotRod", "hotRodToggle",
      "toggleMegaDig", "megaDigToggle", "togglePotion", "potionToggle",
      "toggleSpeed", "speedToggle", "toggleVeinMiner", "veinMinerToggle",
      "toggleLeafBlower", "leafBlowerToggle", "toggleHolyAxe", "holyAxeToggle",
      "configGUI", "configurationGUI", "skillConfigGUI", "skillConfigurationGUI", "skillConfig",
      "confirmGUI", "confirmationGUI", "craftingGUI", "recipeGUI",
      "mainGUI", "skills", "skillTree", "skillTreeGUI");

  @Test
  void pinnedListSanity() {
    assertEquals(PINNED.size(), new HashSet<>(PINNED).size(), "duplicates in pinned list");
    assertTrue(PINNED.size() >= 40, "pinned list too small: " + PINNED.size());
  }

  @Test
  void everyPinnedNameIsRegistered() {
    Set<String> registered = FrpgCommands.buildRegistry().allNames();
    List<String> missing = new ArrayList<>();
    for (String name : PINNED) {
      if (!registered.contains(name.toLowerCase(Locale.ROOT))) {
        missing.add(name);
      }
    }
    assertTrue(missing.isEmpty(), "Command names lost: " + missing);
  }

  @Test
  void registeredNamesExposedByCommandsMatchRegistry() {
    assertEquals(FrpgCommands.buildRegistry().allNames(), new FrpgCommands().registeredNames());
  }
}
