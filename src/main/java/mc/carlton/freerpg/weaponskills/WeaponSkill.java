package mc.carlton.freerpg.weaponskills;

import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * The weapon skills of the self-contained weapon skill module. They are stored and displayed
 * separately from the main FreeRPG skills (see {@link WeaponSkills}).
 */
public enum WeaponSkill {

  MACE("maceMastery", "Mace Mastery", ChatColor.GOLD, "MACE", "Seismic Slam", "MACE"),
  TRIDENT("tridentMastery", "Trident Mastery", ChatColor.AQUA, "TRIDENT", "Maelstrom",
      "TRIDENT"),
  SPEAR("spearMastery", "Spear Mastery", ChatColor.GREEN, "IRON_SPEAR", "Piercing Rush",
      "WOODEN_SPEAR", "STONE_SPEAR", "COPPER_SPEAR", "IRON_SPEAR", "GOLDEN_SPEAR",
      "DIAMOND_SPEAR", "NETHERITE_SPEAR");

  private final String id;
  private final String displayName;
  private final ChatColor color;
  private final String iconName;
  private final String abilityName;
  private final List<String> weaponNames;

  WeaponSkill(String id, String displayName, ChatColor color, String iconName,
      String abilityName, String... weaponNames) {
    this.id = id;
    this.displayName = displayName;
    this.color = color;
    this.iconName = iconName;
    this.abilityName = abilityName;
    this.weaponNames = Arrays.asList(weaponNames);
  }

  /** Finds a skill by its id ("maceMastery") or short name ("mace"), ignoring case. */
  public static WeaponSkill fromInput(String input) {
    for (WeaponSkill skill : values()) {
      String shortName = skill.id.replace("Mastery", "");
      if (skill.id.equalsIgnoreCase(input) || shortName.equalsIgnoreCase(input)) {
        return skill;
      }
    }
    return null;
  }

  /** Name of the section in weaponSkills.yml ("mace", "trident", "spear"). */
  public String getConfigKey() {
    return id.replace("Mastery", "");
  }

  /** Key used in files and the config. */
  public String getId() {
    return id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public ChatColor getColor() {
    return color;
  }

  public String getAbilityName() {
    return abilityName;
  }

  /** Icon of the skill in the menu. */
  public Material getIcon() {
    Material material = Material.matchMaterial(iconName);
    return material == null ? Material.IRON_SWORD : material;
  }

  /** True if the given material is a weapon of this skill. */
  public boolean isWeapon(Material material) {
    return material != null && weaponNames.contains(material.name());
  }
}
