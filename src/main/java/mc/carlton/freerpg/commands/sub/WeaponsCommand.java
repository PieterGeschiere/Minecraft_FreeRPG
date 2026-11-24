package mc.carlton.freerpg.commands.sub;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import mc.carlton.freerpg.FreeRPG;

/** /frpg weapons (moved verbatim from FrpgCommands). */
public class WeaponsCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("weapons", "weaponSkills");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(ChatColor.RED
          + mc.carlton.freerpg.weaponskills.WeaponSkillRules.text(sender, "weaponPlayersOnly"));
      return true;
    }
    if (!sender.hasPermission("freeRPG.weaponSkills")) {
      sender.sendMessage(ChatColor.RED
          + mc.carlton.freerpg.weaponskills.WeaponSkillRules.text(sender, "weaponNoPermission"));
      return true;
    }
    mc.carlton.freerpg.weaponskills.WeaponSkills.openMenu((Player) sender);
    return true;
  }
}
