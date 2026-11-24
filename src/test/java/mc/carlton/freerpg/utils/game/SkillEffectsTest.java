package mc.carlton.freerpg.utils.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

/** The effects of the main skill abilities and perks: coverage and the config switches. */
class SkillEffectsTest {

  private static YamlConfiguration weaponSkillsYml() throws Exception {
    try (InputStream stream = SkillEffectsTest.class.getClassLoader()
        .getResourceAsStream("weaponSkills.yml")) {
      assertNotNull(stream, "weaponSkills.yml is not on the classpath");
      YamlConfiguration yaml = new YamlConfiguration();
      yaml.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
      return yaml;
    }
  }

  @Test
  void everyAbilitySoundSkillAlsoHasParticles() {
    Set<String> withParticles = new HashSet<>(AbilityVisuals.SKILLS);
    for (String skill : AbilitySounds.skills()) {
      assertTrue(withParticles.contains(skill), skill + " has a sound but no particles");
    }
  }

  @Test
  void everyParticleSkillAlsoHasASound() {
    for (String skill : AbilityVisuals.SKILLS) {
      assertTrue(AbilitySounds.skills().contains(skill), skill + " has particles but no sound");
    }
  }

  @Test
  void allNineMainSkillAbilitiesHaveEffects() {
    assertEquals(9, AbilityVisuals.SKILLS.size());
    assertEquals(9, new HashSet<>(AbilityVisuals.SKILLS).size());
  }

  @Test
  void everyEffectSwitchIsInTheBundledConfigAndOnByDefault() throws Exception {
    YamlConfiguration yaml = weaponSkillsYml();
    for (String key : new String[]{EffectSettings.SKILL_ABILITY_SOUNDS,
        EffectSettings.SKILL_ABILITY_PARTICLES, EffectSettings.PERK_SOUNDS,
        EffectSettings.PERK_PARTICLES}) {
      assertTrue(yaml.contains(key), key + " is missing in weaponSkills.yml");
      assertTrue(yaml.getBoolean(key), key + " should be on by default");
    }
  }

  @Test
  void theSwitchesAreOnWhenNoConfigIsLoaded() {
    assertTrue(EffectSettings.skillAbilitySounds());
    assertTrue(EffectSettings.skillAbilityParticles());
    assertTrue(EffectSettings.perkSounds());
    assertTrue(EffectSettings.perkParticles());
  }
}
