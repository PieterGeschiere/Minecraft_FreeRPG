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

  MACE("maceMastery", "Mace Mastery", ChatColor.GOLD, "MACE", "Seismic Slam", "MACE");

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
