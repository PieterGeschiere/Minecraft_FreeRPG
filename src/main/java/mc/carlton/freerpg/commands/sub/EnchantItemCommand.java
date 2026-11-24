package mc.carlton.freerpg.commands.sub;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.commands.CommandHelper;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import mc.carlton.freerpg.utils.game.PsuedoEnchanting;
import org.bukkit.inventory.ItemStack;

/** /frpg enchantItem (moved verbatim from FrpgCommands). */
public class EnchantItemCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("enchantItem", "enchant");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg enchantItem [::level::]";
    CommandHelper commandHelper = new CommandHelper(sender, args, 2, IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPlayerOnlyCommand(true);
    commandHelper.setPermissionName("enchantItem");
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }
    Player p = (Player) sender;
    int level = CommandSupport.getArgumentAsInteger(args[1]);
    if (level == Integer.MAX_VALUE) {
      commandHelper.sendImproperArgumentsMessage();
      return true; //Improper input
    }
    if (level > 40) {
      LanguageSelector lang = new LanguageSelector(p);
      p.sendMessage(ChatColor.RED + lang.getString("levelArgument"));
      return true; //Level input too high
    }
    ItemStack item = p.getInventory().getItemInMainHand();
    PsuedoEnchanting enchant = new PsuedoEnchanting();
    item = enchant.enchantItem(item, level, false);
    p.getInventory().setItemInMainHand(item);
    return true;
  }
}
