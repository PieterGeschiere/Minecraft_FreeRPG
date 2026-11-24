package mc.carlton.freerpg.commands;

import java.util.Optional;
import java.util.Set;
import mc.carlton.freerpg.commands.sub.AddMultiplierCommand;
import mc.carlton.freerpg.commands.sub.CommandRegistry;
import mc.carlton.freerpg.commands.sub.ConfigGuiCommand;
import mc.carlton.freerpg.commands.sub.ConfirmGuiCommand;
import mc.carlton.freerpg.commands.sub.CraftingGuiCommand;
import mc.carlton.freerpg.commands.sub.CreateFakePlayersCommand;
import mc.carlton.freerpg.commands.sub.DeleteFakePlayersCommand;
import mc.carlton.freerpg.commands.sub.EnchantItemCommand;
import mc.carlton.freerpg.commands.sub.GiveExpCommand;
import mc.carlton.freerpg.commands.sub.HelpCommand;
import mc.carlton.freerpg.commands.sub.InfoCommand;
import mc.carlton.freerpg.commands.sub.LeaderboardCommand;
import mc.carlton.freerpg.commands.sub.LoadInAllPlayerFilesCommand;
import mc.carlton.freerpg.commands.sub.MainMenuCommand;
import mc.carlton.freerpg.commands.sub.PartyCommand;
import mc.carlton.freerpg.commands.sub.QuestsCommand;
import mc.carlton.freerpg.commands.sub.ReloadWeaponsCommand;
import mc.carlton.freerpg.commands.sub.ResetCooldownCommand;
import mc.carlton.freerpg.commands.sub.SaveStatsCommand;
import mc.carlton.freerpg.commands.sub.SetLevelCommand;
import mc.carlton.freerpg.commands.sub.SetMultiplierCommand;
import mc.carlton.freerpg.commands.sub.SetSoulsCommand;
import mc.carlton.freerpg.commands.sub.SetTokensCommand;
import mc.carlton.freerpg.commands.sub.SkillConfigGuiCommand;
import mc.carlton.freerpg.commands.sub.SkillTreeCommand;
import mc.carlton.freerpg.commands.sub.StatLookupCommand;
import mc.carlton.freerpg.commands.sub.StatResetCommand;
import mc.carlton.freerpg.commands.sub.SubCommand;
import mc.carlton.freerpg.commands.sub.ThoroughSaveCommand;
import mc.carlton.freerpg.commands.sub.TogglesCommand;
import mc.carlton.freerpg.commands.sub.WeaponsCommand;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import mc.carlton.freerpg.weaponskills.WeaponSkillCommands;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FrpgCommands implements CommandExecutor {

  private final CommandRegistry registry = buildRegistry();

  static CommandRegistry buildRegistry() {
    CommandRegistry r = new CommandRegistry();
    r.register(new HelpCommand());
    r.register(new SaveStatsCommand());
    r.register(new InfoCommand());
    r.register(new EnchantItemCommand());
    r.register(new CreateFakePlayersCommand());
    r.register(new DeleteFakePlayersCommand());
    r.register(new ThoroughSaveCommand());
    r.register(new LoadInAllPlayerFilesCommand());
    r.register(new PartyCommand());
    r.register(new QuestsCommand());
    r.register(new mc.carlton.freerpg.commands.sub.ElitesCommand());
    r.register(new mc.carlton.freerpg.commands.sub.BloodMoonCommand());
    r.register(new WeaponsCommand());
    r.register(new ReloadWeaponsCommand());
    r.register(new LeaderboardCommand());
    r.register(new StatLookupCommand());
    r.register(new ResetCooldownCommand());
    r.register(new GiveExpCommand());
    r.register(new SetLevelCommand());
    r.register(new StatResetCommand());
    r.register(new SetSoulsCommand());
    r.register(new SetTokensCommand());
    r.register(new SetMultiplierCommand());
    r.register(new AddMultiplierCommand());
    r.register(new MainMenuCommand());
    r.register(new ConfigGuiCommand());
    r.register(new SkillConfigGuiCommand());
    r.register(new ConfirmGuiCommand());
    r.register(new CraftingGuiCommand());
    r.register(new SkillTreeCommand());
    TogglesCommand.all().forEach(r::register);
    return r;
  }

  public Set<String> registeredNames() {
    return registry.allNames();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    // Admin commands for the weapon skills (Mace, Trident, Spear, Bow, Crossbow)
    if (WeaponSkillCommands.handle(sender, args)) {
      return true;
    }

    // Migrated sub-commands (see commands/sub); anything not registered falls through to the legacy chain below
    if (args.length > 0) {
      Optional<SubCommand> sub = registry.find(args[0]);
      if (sub.isPresent()) {
        return sub.get().execute(sender, args);
      }
    }

    // /frpg aka MainGUI
    if (args.length == 0) {
      CommandHelper commandHelper = new CommandHelper(sender, args, 0, "");
      commandHelper.setCheckInBed(true);
      commandHelper.setPlayerOnlyCommand(true);
      commandHelper.setPermissionName("mainGUI");
      if (!commandHelper.isProperCommand()) {
        return true; //Command Restricted or Improper
      }
      Player p = (Player) sender;
      MainMenuCommand.generateMainMenu(p); //Generates the Main GUI
      return true;
    }
    else {
      if (sender instanceof Player) {
        Player p = (Player) sender;
        LanguageSelector lang = new LanguageSelector(p);
        p.sendMessage(ChatColor.RED + lang.getString("unknownCommand"));
      } else {
        sender.sendMessage("Unknown command");
      }
    }

    return true;
  }
}
