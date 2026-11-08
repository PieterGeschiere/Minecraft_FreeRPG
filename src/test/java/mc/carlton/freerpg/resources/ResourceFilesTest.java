package mc.carlton.freerpg.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import mc.carlton.freerpg.weaponskills.WeaponSkill;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

/** The bundled config files must be valid and consistent with the code. */
class ResourceFilesTest {

  private static YamlConfiguration load(String name) throws Exception {
    try (InputStream stream = ResourceFilesTest.class.getClassLoader().getResourceAsStream(name)) {
      assertNotNull(stream, name + " is not on the classpath");
      YamlConfiguration yaml = new YamlConfiguration();
      yaml.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
      return yaml;
    }
  }

  @Test
  void everyBundledYamlFileParses() throws Exception {
    for (String name : new String[]{"plugin.yml", "config.yml", "advancedConfig.yml",
        "weaponSkills.yml", "perkConfig.yml", "languages.yml"}) {
      assertFalse(load(name).getKeys(false).isEmpty(), name + " is empty");
    }
  }

  @Test
  void everyWeaponSkillHasItsSectionInWeaponSkillsYml() throws Exception {
    YamlConfiguration yaml = load("weaponSkills.yml");
    for (WeaponSkill skill : WeaponSkill.values()) {
      ConfigurationSection section = yaml.getConfigurationSection(skill.getConfigKey());
      assertNotNull(section, "weaponSkills.yml has no section " + skill.getConfigKey());
      for (String part : new String[]{"exp", "ability", "perks", "passives"}) {
        assertNotNull(section.getConfigurationSection(part),
            skill.getConfigKey() + " has no '" + part + "' section");
      }
      assertTrue(section.getBoolean("skillAllowed"), skill.getConfigKey() + " is off by default");
    }
  }

  @Test
  void everyWeaponSkillHasItsPermissionsInPluginYml() throws Exception {
    YamlConfiguration plugin = load("plugin.yml");
    for (WeaponSkill skill : WeaponSkill.values()) {
      for (String suffix : new String[]{"EXP", "Ability"}) {
        String key = "permissions.freeRPG." + skill.getId() + suffix;
        assertNotNull(plugin.get(key), "plugin.yml is missing " + key);
      }
    }
  }

  @Test
  void weaponSkillPermissionsAreGrantedByTheParentPermissions() throws Exception {
    YamlConfiguration plugin = load("plugin.yml");
    ConfigurationSection exp = plugin.getConfigurationSection("permissions.freeRPG.getEXP.children");
    ConfigurationSection abilities = plugin.getConfigurationSection(
        "permissions.freeRPG.useAbilities.children");
    assertNotNull(exp, "plugin.yml has no freeRPG.getEXP parent permission");
    assertNotNull(abilities, "plugin.yml has no freeRPG.useAbilities parent permission");
    for (WeaponSkill skill : WeaponSkill.values()) {
      assertTrue(exp.contains("freeRPG." + skill.getId() + "EXP"), skill.getId() + " exp parent");
      assertTrue(abilities.contains("freeRPG." + skill.getId() + "Ability"),
          skill.getId() + " ability parent");
    }
  }

  @Test
  void newContentHasItsExpKeysInAdvancedConfig() throws Exception {
    YamlConfiguration config = load("advancedConfig.yml");
    String[][] keys = {
        {"woodcutting", "breakPoplar_Log"}, {"woodcutting", "breakMushroom_Stem"},
        {"woodcutting", "breakWart_Block"}, {"mining", "breakCinnabar"},
        {"mining", "breakSulfur"}, {"mining", "breakMagma_Block"}, {"mining", "breakPrismarine"},
        {"mining", "breakBone_Block"}, {"mining", "breakSculk"},
        {"digging", "breakSuspicious_Block"}, {"farming", "breakNew_Plants"},
        {"farming", "breakLeaf_Litter"}, {"farming", "breakVines"}, {"farming", "breakSeagrass"},
        {"farming", "breakDried_Kelp_Block"}};
    for (String[] key : keys) {
      assertTrue(config.contains(key[0] + ".expDrops." + key[1]),
          "advancedConfig.yml has no " + key[0] + ".expDrops." + key[1]);
    }
  }

  @Test
  void toggleCommandsHaveTheirPermissionsDeclared() throws Exception {
    YamlConfiguration plugin = load("plugin.yml");
    for (String toggle : new String[]{"toggleFlamePick", "toggleFlint", "toggleGrapple",
        "toggleHotRod", "toggleMegaDig", "togglePotion", "toggleSpeed", "toggleVeinMiner",
        "toggleLeafBlower", "toggleHolyAxe"}) {
      assertNotNull(plugin.get("permissions.freeRPG." + toggle), "plugin.yml lacks " + toggle);
    }
  }

  @Test
  void combatSkillsHaveKillExpForTheNewerMobs() throws Exception {
    YamlConfiguration config = load("advancedConfig.yml");
    for (String skill : new String[]{"swordsmanship", "axeMastery", "beastMastery", "defense"}) {
      for (String key : new String[]{"killBogged", "killBreeze", "killCamel", "killAllay",
          "killFrog", "killWarden", "killCopper_Golem", "killTadpole"}) {
        assertTrue(config.contains(skill + ".expDrops." + key), skill + " has no " + key);
      }
    }
  }

  @Test
  void advancedConfigHasNoDuplicateKeysInASection() throws Exception {
    java.util.List<String> keysByPath = new java.util.ArrayList<>();
    java.util.Deque<String> path = new java.util.ArrayDeque<>();
    java.util.Deque<Integer> indents = new java.util.ArrayDeque<>();
    java.util.Set<String> seen = new java.util.HashSet<>();
    try (InputStream stream = ResourceFilesTest.class.getClassLoader()
        .getResourceAsStream("advancedConfig.yml")) {
      for (String line : new String(stream.readAllBytes(), StandardCharsets.UTF_8).split("\n")) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^( *)([A-Za-z0-9_]+):(.*)$")
            .matcher(line);
        if (!m.matches() || line.trim().startsWith("#") || line.trim().startsWith("-")) {
          continue;
        }
        int indent = m.group(1).length();
        while (!indents.isEmpty() && indents.peek() >= indent) {
          indents.pop();
          path.pop();
        }
        String full = String.join(".", new java.util.ArrayList<>(
            new java.util.ArrayDeque<>(path).reversed())) + "." + m.group(2);
        assertTrue(seen.add(full), "duplicate key " + full);
        path.push(m.group(2));
        indents.push(indent);
      }
    }
  }

  @Test
  void archeryIsRetiredInTheDefaultConfig() throws Exception {
    assertEquals(false, load("config.yml").getBoolean("archery.skillAllowed", true));
  }
}
