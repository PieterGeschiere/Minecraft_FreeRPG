package mc.carlton.freerpg.commands.sub;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import mc.carlton.freerpg.FreeRPG;
import org.bukkit.plugin.Plugin;

/** /frpg reloadWeapons (moved verbatim from FrpgCommands). */
public class ReloadWeaponsCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("reloadWeapons");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    Plugin plugin = FreeRPG.getPlugin(FreeRPG.class);
    if (!sender.hasPermission("freeRPG.reloadWeapons")) {
      sender.sendMessage(ChatColor.RED
          + mc.carlton.freerpg.weaponskills.WeaponSkillRules.text(sender, "weaponNoPermission"));
      return true;
    }
    mc.carlton.freerpg.weaponskills.WeaponSkillConfig.load(plugin);
    sender.sendMessage(ChatColor.GREEN
        + mc.carlton.freerpg.weaponskills.WeaponSkillRules.text(sender, "weaponReloaded"));
    return true;
  }
}
