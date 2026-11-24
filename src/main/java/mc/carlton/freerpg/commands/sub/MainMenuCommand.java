package mc.carlton.freerpg.commands.sub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.core.info.player.ChangeStats;
import mc.carlton.freerpg.core.info.player.Leaderboards;
import mc.carlton.freerpg.core.info.player.PlayerStats;
import mc.carlton.freerpg.skills.SkillRegistry;
import mc.carlton.freerpg.utils.UtilityMethods;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import mc.carlton.freerpg.utils.gui.GuiDisplayStatistic;
import mc.carlton.freerpg.utils.gui.GuiItem;
import mc.carlton.freerpg.utils.gui.GuiWrapper;
import org.apache.logging.log4j.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /frpg mainGUI, /frpg skills and the /frpg main menu (moved verbatim from FrpgCommands). */
public class MainMenuCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("mainGUI", "skills");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    if (sender instanceof Player) {
      Player p = (Player) sender;
      p.performCommand("frpg");
    } else {
      FreeRPG.log(Level.WARN, "You must be a player to perform this command");
    }
    return true;
  }

  private static String getExpToNextString(Player p, String skillName) {
    ChangeStats getEXP = new ChangeStats(p);
    PlayerStats pStatClass = new PlayerStats(p);
    Map<String, ArrayList<Number>> pStat = pStatClass.getPlayerData();

    int maxLevel = getEXP.getMaxLevel(skillName);
    int EXP = pStat.get(skillName).get(1).intValue();
    int level = pStat.get(skillName).get(0).intValue();

    String EXPtoNextString;
    if (level < maxLevel) {
      int nextEXP = getEXP.getEXPfromLevel(level + 1);
      int EXPtoNext = nextEXP - EXP;
      EXPtoNextString = String.valueOf(EXPtoNext);
    } else {
      EXPtoNextString = "N/A";
    }
    return EXPtoNextString;
  }

  private static boolean hasUnspentTokens(Map<String, ArrayList<Number>> pStat, String skillName) {
    ArrayList<Number> stats = pStat.get(skillName);
    return stats != null && (stats.get(2).intValue() > 0 || stats.get(3).intValue() > 0);
  }

  private static int getTotalSkillTokens(Player p) {
    PlayerStats pStatClass = new PlayerStats(p);
    Map<String, ArrayList<Number>> pStat = pStatClass.getPlayerData();
    int totalSkillTokens = 0;
    for (String j : pStat.keySet()) {
      if (!j.equalsIgnoreCase("global")) {
        totalSkillTokens += pStat.get(j).get(3).intValue();
      }
    }
    return totalSkillTokens;
  }

  private static int getTotalPassiveTokens(Player p) {
    PlayerStats pStatClass = new PlayerStats(p);
    Map<String, ArrayList<Number>> pStat = pStatClass.getPlayerData();
    int totalPassiveTokens = 0;
    for (String j : pStat.keySet()) {
      if (!j.equalsIgnoreCase("global")) {
        totalPassiveTokens += pStat.get(j).get(2).intValue();
      }
    }
    return totalPassiveTokens;
  }

  public static void generateMainMenu(Player p) {
    //Player Information and Leaderboard
    LanguageSelector lang = new LanguageSelector(p);
    PlayerStats pStatClass = new PlayerStats(p);
    Map<String, ArrayList<Number>> pStat = pStatClass.getPlayerData();
    Leaderboards leaderboards = new Leaderboards();
    PlayerStats timeStats = new PlayerStats(p);

    //Gui Set-up
    GuiWrapper gui = new GuiWrapper(p, Bukkit.createInventory(p, 45, "Skills"));
    String nameColor = ChatColor.AQUA.toString() + ChatColor.BOLD.toString();
    String descriptionColor = ChatColor.GRAY.toString() + ChatColor.ITALIC.toString();
    String statisticNameColor = ChatColor.GRAY.toString();

    //Menu Options (Items)
    GuiItem global = new GuiItem(Material.NETHER_STAR, 4, gui);
    GuiItem digging = new GuiItem(Material.IRON_SHOVEL, 11, gui);
    GuiItem woodcutting = new GuiItem(Material.IRON_AXE, 12, gui);
    GuiItem mining = new GuiItem(Material.IRON_PICKAXE, 13, gui);
    GuiItem farming = new GuiItem(Material.IRON_HOE, 14, gui);
    GuiItem fishing = new GuiItem(Material.FISHING_ROD, 15, gui);
    GuiItem beastMastery = new GuiItem(Material.BONE, 21, gui);
    GuiItem armor = new GuiItem(Material.IRON_CHESTPLATE, 23, gui);
    GuiItem repair = new GuiItem(Material.ANVIL, 29, gui);
    GuiItem agility = new GuiItem(Material.LEATHER_LEGGINGS, 30, gui);
    GuiItem alchemy = new GuiItem(Material.POTION, 31, gui);
    GuiItem smelting = new GuiItem(Material.COAL, 32, gui);
    GuiItem enchanting = new GuiItem(Material.ENCHANTING_TABLE, 33, gui);
    GuiItem building = new GuiItem(Material.BRICKS, 34, gui);
    GuiItem info = new GuiItem(Material.MAP, 36, gui);
    GuiItem configuration = new GuiItem(Material.REDSTONE, 44, gui);

    GuiItem[] guiItems = {global, digging, woodcutting, mining, farming, fishing,
        beastMastery, armor, repair, agility, alchemy, smelting, enchanting, building,
        info, configuration};
    // Weapon skills have their own menu, so they are not part of this one.
    List<String> menuLabels = new ArrayList<>();
    menuLabels.add("global");
    for (String skill : SkillRegistry.labels()) {
      if (!skill.equals("archery") && !skill.equals("swordsmanship")
          && !skill.equals("axeMastery")) {
        menuLabels.add(skill);
      }
    }
    menuLabels.add("information");
    menuLabels.add("configuration");
    String[] labels0 = menuLabels.toArray(new String[0]);
    String[] labels = new String[labels0.length];
    for (int k = 0; k < labels0.length; k++) {
      labels[k] = lang.getString(labels0[k]);
    }

    for (int i = 0; i < guiItems.length; i++) {
      GuiItem item = guiItems[i];
      String translatedLabel = labels[i];
      item.setName(translatedLabel);
      item.setNameColor(nameColor);
      if (i == 0) { //Global
        int globalTokens = (int) pStat.get(labels0[i]).get(1);
        int totalLevel = pStat.get("global").get(0).intValue();
        int totalExperience = (int) pStat.get("global").get(29);
        int totalPlayers = leaderboards.getLeaderboardSize("global");
        int rank = leaderboards.getLeaderboardPosition(p, "global");

        item.addStatistic(lang.getString("total") + " " + lang.getString("level"),
            String.valueOf(totalLevel));
        item.addStatistic(lang.getString("total") + " " + lang.getString("exp"),
            String.valueOf(totalExperience));
        item.addSpecialLoreLine(statisticNameColor + lang.getString("rank") + ": " + ChatColor.WHITE
            + ChatColor.BOLD.toString() + "" + String.format("%,d", rank) + ChatColor.RESET
            + descriptionColor + " " + lang.getString("outOf") + " " + ChatColor.RESET
            + ChatColor.WHITE.toString() + totalPlayers);
        if (globalTokens > 0) {
          item.addEnchantmentGlow();
        }

      } else if (i < 14) { //Skills
        String skillName = labels0[i];
        int passiveTokens = (int) pStat.get(labels0[i]).get(2);
        int skillTokens = (int) pStat.get(labels0[i]).get(3);
        int level = pStat.get(labels0[i]).get(0).intValue();
        int EXP = pStat.get(labels0[i]).get(1).intValue();
        int totalPlayers = leaderboards.getLeaderboardSize(labels0[i]);
        int rank = leaderboards.getLeaderboardPosition(p, labels0[i]);
        String EXPtoNextString = getExpToNextString(p, skillName);

        GuiDisplayStatistic levelStat = new GuiDisplayStatistic(lang.getString("level"), level,
            ChatColor.GRAY.toString(), ChatColor.BLUE.toString());
        GuiDisplayStatistic expStat = new GuiDisplayStatistic(lang.getString("experience"), EXP,
            ChatColor.GRAY.toString(), ChatColor.BLUE.toString());
        GuiDisplayStatistic expToLevelStat = new GuiDisplayStatistic(lang.getString("expToLevel"),
            EXPtoNextString, ChatColor.GRAY.toString(), ChatColor.GREEN.toString());

        item.addStatistic(levelStat);
        item.addStatistic(expStat);
        item.addStatistic(expToLevelStat);
        item.addSpecialLoreLine(statisticNameColor + lang.getString("rank") + ": " + ChatColor.WHITE
            + ChatColor.BOLD.toString() + "" + String.format("%,d", rank) + ChatColor.RESET
            + descriptionColor + " " + lang.getString("outOf") + " " + ChatColor.RESET
            + ChatColor.WHITE.toString() + totalPlayers);
        if (passiveTokens > 0 || skillTokens > 0) {
          // Same line as in the weapon skills menu, so all skills look alike
          item.addSpecialLoreLine(ChatColor.LIGHT_PURPLE + lang.getString("weaponUnspentTokens")
              .replace("{passive}", Integer.toString(passiveTokens))
              .replace("{skill}", Integer.toString(skillTokens)));
          item.addEnchantmentGlow();
        }

      } else if (i == 14) { //Information
        int totalSkillTokens = getTotalSkillTokens(p);
        int totalPassiveTokens = getTotalPassiveTokens(p);
        int globalTokens = pStat.get("global").get(1).intValue();
        String playTimeString = timeStats.getPlayerPlayTimeString();
        double personalMultiplier = (double) pStat.get("global").get(23);
        int souls = (int) pStat.get("global").get(20);

        item.addStatistic(lang.getString("totalPlayTime"), playTimeString);
        item.addStatistic(lang.getString("personalMultiplier"),
            String.valueOf(personalMultiplier) + "x");
        item.addStatistic(lang.getString("globalPassiveTitle0"), globalTokens);
        item.addStatistic(lang.getString("diggingPassiveTitle2"), totalSkillTokens);
        item.addStatistic(lang.getString("diggingPassiveTitle0"), totalPassiveTokens);
        item.addStatistic(UtilityMethods.capitalizeString(lang.getString("souls")), souls);
      } else if (i == 15) {//Configuration
        item.setDescription(lang.getString("clickForOptions"));
      }
      gui.addItem(item);
    }

    //Weapon skills (separate module, see weaponskills package)
    // Swordsmanship, Axe Mastery and the newer weapon skills are all inside this menu
    GuiItem weapons = new GuiItem(Material.MACE, 22, gui);
    weapons.setName("Weapon Skills");
    weapons.setNameColor(nameColor);
    weapons.setDescription("Swordsmanship, Axe Mastery, Mace, Bow, Crossbow and more");
    if (hasUnspentTokens(pStat, "swordsmanship") || hasUnspentTokens(pStat, "axeMastery")) {
      weapons.addEnchantmentGlow();
    }
    gui.addItem(weapons);

    //Parties
    if (p.hasPermission("freerpg.party.use")) {
      GuiItem partyIcon = new GuiItem(Material.WHITE_BANNER, 40, gui);
      partyIcon.setName(lang.getString("partyMenuMain"));
      partyIcon.setNameColor(nameColor);
      partyIcon.setDescription(lang.getString("partyMenuMainDesc"));
      gui.addItem(partyIcon);
    }

    //Daily quests
    if (p.hasPermission("freerpg.quests.use")) {
      GuiItem questIcon = new GuiItem(Material.CLOCK, 41, gui);
      questIcon.setName(lang.getString("questMenuMain"));
      questIcon.setNameColor(nameColor);
      questIcon.setDescription(lang.getString("questMenuMainDesc"));
      gui.addItem(questIcon);
    }

    //Put the items in the inventory
    gui.displayGuiForPlayer();
  }
}
