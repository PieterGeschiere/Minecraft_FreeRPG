package mc.carlton.freerpg.weaponskills;

import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Makes the weapon skills work with the administration commands giveEXP, setLevel, statReset and
 * resetCooldown (setTokens only explains that tokens come from levels). Called first by /frpg,
 * it only handles commands that name a weapon skill and leaves everything else to the normal
 * command code.
 */
public final class WeaponSkillCommands {

  private WeaponSkillCommands() {
  }

  /** Prints a page (10 players) of the leaderboard of a weapon skill. */
  private static void showLeaderboard(CommandSender sender, WeaponSkill skill, int page) {
    java.util.List<WeaponSkillBoard.Entry> sorted = WeaponSkillBoard.getSorted(skill);
    int totalPages = Math.max(1, (int) Math.ceil(sorted.size() / 10.0));
    if (page > totalPages || page < 1) {
      page = 1;
    }
    sender.sendMessage(ChatColor.AQUA + "------| " + ChatColor.WHITE + ChatColor.BOLD
        + skill.getDisplayName() + " Leaderboard" + ChatColor.RESET + ChatColor.WHITE + " Page ["
        + page + "/" + totalPages + "]" + ChatColor.AQUA + " |-----");
    for (int i = 10 * (page - 1); i < Math.min(10 * page, sorted.size()); i++) {
      WeaponSkillBoard.Entry entry = sorted.get(i);
      sender.sendMessage(ChatColor.WHITE + Integer.toString(i + 1) + ". " + ChatColor.AQUA
          + entry.getName() + ChatColor.WHITE + " - " + ChatColor.WHITE + String.format("%,d",
          entry.getLevel()) + " (" + ChatColor.GRAY + String.format("%,d", entry.getExperience())
          + ChatColor.WHITE + ")");
    }
  }

  /** Adds the weapon skills to the end of the /frpg stats screen of a player. */
  public static void sendStatLines(CommandSender sender, String playerName) {
    for (WeaponSkill skill : WeaponSkill.values()) {
      int rank = WeaponSkillBoard.getRank(skill, playerName);
      if (rank < 0) {
        continue;
      }
      WeaponSkillBoard.Entry entry = WeaponSkillBoard.getSorted(skill).get(rank - 1);
      String rankString = mc.carlton.freerpg.utils.UtilityMethods.intToRankingString(rank);
      sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + skill.getDisplayName()
          + ChatColor.RESET + ChatColor.GRAY + " - " + ChatColor.WHITE + String.format("%,d",
          entry.getLevel()) + " (" + ChatColor.GRAY + rankString + ChatColor.WHITE + ")");
    }
  }

  /** Returns true if the command was about a weapon skill and has been handled. */
  public static boolean handle(CommandSender sender, String[] args) {
    if (args.length < 2) {
      return false;
    }
    String sub = args[0].toLowerCase(Locale.ROOT);
    if (sub.equals("top") || sub.equals("leaderboard") || sub.equals("statleaders")) {
      WeaponSkill boardSkill = WeaponSkill.fromInput(args[1]);
      if (boardSkill == null) {
        return false;
      }
      if (!sender.hasPermission("freeRPG.leaderboard")) {
        sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
        return true;
      }
      int page = 1;
      if (args.length > 2) {
        try {
          page = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
          sender.sendMessage(ChatColor.RED + "Usage: /frpg top " + boardSkill.getId() + " [page]");
          return true;
        }
      }
      showLeaderboard(sender, boardSkill, page);
      return true;
    }
    String permission;
    boolean needsValue;
    switch (sub) {
      case "giveexp":
      case "expgive":
        permission = "giveEXP";
        needsValue = true;
        break;
      case "setlevel":
      case "levelset":
        permission = "setLevel";
        needsValue = true;
        break;
      case "statreset":
      case "resetstat":
        permission = "statReset";
        needsValue = false;
        break;
      case "resetcooldown":
      case "cooldownreset":
        permission = "resetCooldown";
        needsValue = false;
        break;
      case "settokens":
      case "tokensset":
        permission = "setTokens";
        needsValue = false;
        break;
      default:
        return false;
    }

    // The skill is the second argument, or the third if a player name comes first
    int skillIndex = -1;
    WeaponSkill skill = null;
    for (int i = 1; i <= 2 && i < args.length; i++) {
      WeaponSkill found = WeaponSkill.fromInput(args[i]);
      if (found != null) {
        skill = found;
        skillIndex = i;
        break;
      }
    }
    if (skill == null) {
      return false;
    }

    if (!sender.hasPermission("freeRPG." + permission)) {
      sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
      return true;
    }
    if (permission.equals("setTokens")) {
      sender.sendMessage(ChatColor.RED + "Weapon skill tokens come from levels. Use /frpg setLevel "
          + "to change them.");
      return true;
    }

    Player target;
    if (skillIndex == 2) {
      target = Bukkit.getPlayerExact(args[1]);
      if (target == null) {
        sender.sendMessage(ChatColor.RED + args[1] + " is not online.");
        return true;
      }
    } else if (sender instanceof Player) {
      target = (Player) sender;
    } else {
      sender.sendMessage(ChatColor.RED + "Usage: /frpg " + args[0] + " [playerName] "
          + skill.getId() + (needsValue ? " [amount]" : ""));
      return true;
    }

    int amount = 0;
    if (needsValue) {
      if (args.length <= skillIndex + 1) {
        sender.sendMessage(ChatColor.RED + "Usage: /frpg " + args[0] + " [playerName] "
            + skill.getId() + " [amount]");
        return true;
      }
      try {
        amount = Integer.parseInt(args[skillIndex + 1]);
      } catch (NumberFormatException e) {
        sender.sendMessage(ChatColor.RED + "\"" + args[skillIndex + 1] + "\" is not a number.");
        return true;
      }
    }

    String name = target.getName() + "'s " + skill.getDisplayName();
    switch (permission) {
      case "giveEXP":
        WeaponSkillManager.giveExp(target, skill, amount);
        sender.sendMessage(ChatColor.GREEN + "Gave " + amount + " exp to " + name + " (level "
            + WeaponSkillManager.get(target, skill).getLevel() + ").");
        break;
      case "setLevel":
        WeaponSkillManager.setLevel(target, skill, amount);
        sender.sendMessage(ChatColor.GREEN + "Set " + name + " level to "
            + WeaponSkillManager.get(target, skill).getLevel() + ".");
        break;
      case "statReset":
        WeaponSkillManager.reset(target, skill);
        sender.sendMessage(ChatColor.GREEN + "Reset " + name + ".");
        break;
      default:
        WeaponSkillManager.resetCooldown(target, skill);
        sender.sendMessage(ChatColor.GREEN + "Reset the " + skill.getAbilityName()
            + " cooldown of " + target.getName() + ".");
        break;
    }
    return true;
  }
}
