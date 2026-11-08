package mc.carlton.freerpg.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * Tab completion for /frpg: the subcommands (only those the sender may use) and the arguments of
 * the commands that take a player name, a skill name or a token type.
 */
public class FrpgTabCompleter implements TabCompleter {

  private static final List<String> SKILLS = Arrays.asList("digging", "woodcutting", "mining",
      "farming", "fishing", "beastMastery", "swordsmanship", "defense", "axeMastery",
      "repair", "agility", "alchemy", "smelting", "enchanting");

  /** The weapon skills, which the administration commands accept too. */
  private static final List<String> WEAPON_SKILLS = Arrays.asList("maceMastery", "tridentMastery",
      "spearMastery", "bowMastery", "crossbowMastery");

  /** Subcommand -> permission it needs (without the "freeRPG." prefix), null for no permission. */
  private static final Map<String, String> SUBCOMMANDS = new LinkedHashMap<>();

  static {
    SUBCOMMANDS.put("help", "help");
    SUBCOMMANDS.put("skills", "mainGUI");
    SUBCOMMANDS.put("weapons", "weaponSkills");
    SUBCOMMANDS.put("reloadWeapons", "reloadWeapons");
    SUBCOMMANDS.put("skillTree", "skillsGUI");
    SUBCOMMANDS.put("skillConfigGUI", "skillConfigGUI");
    SUBCOMMANDS.put("configGUI", "configGUI");
    SUBCOMMANDS.put("craftingGUI", "craftGUI");
    SUBCOMMANDS.put("confirmationGUI", "confirmGUI");
    SUBCOMMANDS.put("info", "info");
    SUBCOMMANDS.put("top", "leaderboard");
    SUBCOMMANDS.put("stats", "statLookup");
    SUBCOMMANDS.put("enchantItem", "enchantItem");
    for (String toggle : new String[]{"toggleFlamePick", "toggleFlint", "toggleGrapple",
        "toggleHotRod", "toggleMegaDig", "togglePotion", "toggleSpeed", "toggleVeinMiner",
        "toggleLeafBlower", "toggleHolyAxe"}) {
      SUBCOMMANDS.put(toggle, toggle); // the permission has the same name as the command
    }
    // Administration
    SUBCOMMANDS.put("saveStats", "saveStats");
    SUBCOMMANDS.put("thoroughSave", "saveStats");
    SUBCOMMANDS.put("loadInAllPlayerFiles", "saveStats");
    SUBCOMMANDS.put("giveEXP", "giveEXP");
    SUBCOMMANDS.put("setLevel", "setLevel");
    SUBCOMMANDS.put("setSouls", "setSouls");
    SUBCOMMANDS.put("setTokens", "setTokens");
    SUBCOMMANDS.put("setMultiplier", "setMultiplier");
    SUBCOMMANDS.put("addMultiplier", "setMultiplier");
    SUBCOMMANDS.put("statReset", "statReset");
    SUBCOMMANDS.put("resetCooldown", "resetCooldown");
    SUBCOMMANDS.put("createFakePlayers", "createFakePlayers");
    SUBCOMMANDS.put("deleteFakePlayers", "createFakePlayers");
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias,
      String[] args) {
    if (args.length == 0) {
      return new ArrayList<>();
    }
    if (args.length == 1) {
      List<String> allowed = new ArrayList<>();
      for (Map.Entry<String, String> entry : SUBCOMMANDS.entrySet()) {
        String permission = entry.getValue();
        if (permission == null || sender.hasPermission("freeRPG." + permission)) {
          allowed.add(entry.getKey());
        }
      }
      return filter(allowed, args[0]);
    }

    String sub = args[0].toLowerCase(Locale.ROOT);
    String last = args[args.length - 1];
    switch (sub) {
      // /frpg <command> [skill]
      case "skilltree":
      case "skilltreegui":
      case "skillconfiggui":
      case "skillconfig":
      case "skillconfigurationgui":
      case "confirmationgui":
      case "confirmgui":
        return args.length == 2 ? filter(SKILLS, last) : new ArrayList<>();
      case "top":
      case "leaderboard":
      case "statleaders":
        if (args.length == 2) {
          List<String> options = new ArrayList<>(SKILLS);
          options.add("global");
          options.add("playTime");
          options.addAll(WEAPON_SKILLS);
          return filter(options, last);
        }
        return new ArrayList<>();
      case "craftinggui":
      case "recipegui":
        return args.length == 2 ? filter(recipeIds(), last) : new ArrayList<>();
      // /frpg <command> [player]
      case "stats":
      case "statlookup":
      case "lookupstats":
      case "setsouls":
      case "soulsset":
      case "setmultiplier":
      case "addmultiplier":
        return args.length == 2 ? filter(playerNames(), last) : new ArrayList<>();
      // /frpg <command> [player] [skill]
      case "giveexp":
      case "expgive":
      case "setlevel":
      case "levelset":
      case "statreset":
      case "resetstat":
      case "resetcooldown":
        if (args.length == 2) {
          return filter(playerNames(), last);
        }
        if (args.length == 3) {
          List<String> options = new ArrayList<>(SKILLS);
          options.add("global");
          options.addAll(WEAPON_SKILLS);
          return filter(options, last);
        }
        return new ArrayList<>();
      // /frpg setTokens [player] global [amount] or [player] [skill] skill/passive [amount]
      case "settokens":
      case "tokensset":
        if (args.length == 2) {
          return filter(playerNames(), last);
        }
        if (args.length == 3) {
          List<String> options = new ArrayList<>(SKILLS);
          options.add("global");
          return filter(options, last);
        }
        if (args.length == 4 && !args[2].equalsIgnoreCase("global")) {
          return filter(Arrays.asList("skill", "passive"), last);
        }
        return new ArrayList<>();
      default:
        return new ArrayList<>();
    }
  }

  private static List<String> playerNames() {
    List<String> names = new ArrayList<>();
    for (Player player : Bukkit.getOnlinePlayers()) {
      names.add(player.getName());
    }
    return names;
  }

  private static List<String> recipeIds() {
    List<String> ids = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      ids.add("farming" + i);
      ids.add("alchemy" + i);
    }
    for (int i = 1; i <= 10; i++) {
      ids.add("enchanting" + i);
    }
    return ids;
  }

  /** Keeps the options that start with what the player typed, ignoring case. */
  private static List<String> filter(List<String> options, String typed) {
    String prefix = typed.toLowerCase(Locale.ROOT);
    List<String> result = new ArrayList<>();
    for (String option : options) {
      if (option.toLowerCase(Locale.ROOT).startsWith(prefix)) {
        result.add(option);
      }
    }
    return result;
  }
}
