package mc.carlton.freerpg.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import mc.carlton.freerpg.weaponskills.WeaponSkill;
import mc.carlton.freerpg.weaponskills.WeaponSkillConfig;
import mc.carlton.freerpg.weaponskills.WeaponSkillRules;
import mc.carlton.freerpg.weaponskills.WeaponSkillTreeMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * One command per skill ({@code /mining}, {@code /maceMastery}, ...) that opens the skill tree of
 * that skill, the same as {@code /frpg skillTree <skill>}.
 */
public class SkillShortcutCommands implements CommandExecutor {

  /** The skills of the original FreeRPG, by the name their skill tree command expects. */
  public static final List<String> SKILLS = Arrays.asList("digging", "woodcutting", "mining",
      "farming", "fishing", "beastMastery", "swordsmanship", "defense", "axeMastery", "repair",
      "agility", "alchemy", "smelting", "enchanting");

  private final FrpgCommands frpgCommands;

  public SkillShortcutCommands(FrpgCommands frpgCommands) {
    this.frpgCommands = frpgCommands;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    String name = command.getName().toLowerCase(Locale.ROOT);
    for (WeaponSkill skill : WeaponSkill.values()) {
      if (skill.getId().toLowerCase(Locale.ROOT).equals(name)) {
        openWeaponSkill(sender, skill);
        return true;
      }
    }
    for (String skill : SKILLS) {
      if (skill.toLowerCase(Locale.ROOT).equals(name)) {
        // The normal skill tree command does the checks (permission, bed, disabled skill)
        return frpgCommands.onCommand(sender, command, label, new String[]{"skillTree", skill});
      }
    }
    return false;
  }

  private void openWeaponSkill(CommandSender sender, WeaponSkill skill) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(ChatColor.RED + WeaponSkillRules.text(sender, "weaponPlayersOnly"));
      return;
    }
    if (!sender.hasPermission("freeRPG.weaponSkills")) {
      sender.sendMessage(ChatColor.RED + WeaponSkillRules.text(sender, "weaponNoPermission"));
      return;
    }
    if (!WeaponSkillConfig.isEnabled()
        || !WeaponSkillConfig.getBoolean(skill.getConfigKey() + ".skillAllowed", true)) {
      sender.sendMessage(ChatColor.RED + WeaponSkillRules.text(sender, "disabledSkill"));
      return;
    }
    WeaponSkillTreeMenu.open((Player) sender, skill);
  }
}
