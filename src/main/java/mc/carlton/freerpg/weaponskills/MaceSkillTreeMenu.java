package mc.carlton.freerpg.weaponskills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.core.info.player.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * The Mace Mastery skill tree. It has the same layout as the skill trees of the main skills: token
 * counters and the three passives (dyes) in the left column, seven skills (terracotta) connected by
 * glass panes, a back button, a refund button and a configuration button.
 */
public class MaceSkillTreeMenu implements Listener {

  private static final WeaponSkill SKILL = WeaponSkill.MACE;

  // Slots of the main skill trees
  private static final int PASSIVE_TOKEN_SLOT = 0;
  private static final int SKILL_TOKEN_SLOT = 9;
  private static final int PASSIVE_1_SLOT = 18;
  private static final int PASSIVE_2_SLOT = 27;
  private static final int PASSIVE_3_SLOT = 36;
  private static final int BACK_SLOT = 45;
  private static final int REFUND_SLOT = 47;
  private static final int CONFIG_SLOT = 53;
  private static final int[] CONNECTOR_SLOTS = {6, 12, 14, 24, 25, 30, 32, 42};

  /** The skills of the tree in the order 1a, 1b, 2a, 2b, 3a, 3b, M. */
  private static final String[] SKILL_KEYS = {MaceMastery.WIDE_SHOCKWAVE, MaceMastery.STAGGER,
      MaceMastery.AFTERSHOCK, MaceMastery.RATTLE, MaceMastery.EARTHSHAKER, MaceMastery.ADRENALINE,
      MaceMastery.TITANS_WRATH};
  private static final int[] SKILL_SLOTS = {11, 29, 13, 31, 7, 43, 26};
  private static final int[] SKILL_MAX_LEVELS = {5, 5, 5, 5, 1, 1, 1};
  private static final String[] SKILL_NAMES = {"Wide Shockwave", "Stagger", "Aftershock", "Rattle",
      "Earthshaker", "Adrenaline", "Titan's Wrath"};

  /** Marks the inventory as this skill tree and maps slots to the passive or skill they hold. */
  private static class TreeHolder implements InventoryHolder {

    private Inventory inventory;

    @Override
    public Inventory getInventory() {
      return inventory;
    }
  }

  private static String[] skillDescriptions() {
    return new String[]{
        "Increases the radius of Seismic Slam's shockwave by "
            + number(MaceMastery.perk("wideShockwaveRadiusPerLevel", 0.5)) + " blocks per level.",
        "Smash attacks slow the target for " + number(MaceMastery.perk("staggerSecondsPerLevel", 1.0))
            + " second(s) per level.",
        "Increases the damage of Seismic Slam's shockwave by "
            + percent(MaceMastery.perk("aftershockPerLevel", 0.06))
            + " of the smash damage per level.",
        "Smash attacks weaken the target for " + number(MaceMastery.perk("rattleSecondsPerLevel", 2.0))
            + " seconds per level.",
        "Seismic Slam launches mobs into the air and its shockwave deals "
            + percent(MaceMastery.perk("earthshakerDamage", 0.2)) + " more damage.",
        "Smash attacks grant you Speed and Resistance for "
            + number(MaceMastery.perk("adrenalineSeconds", 4)) + " seconds.",
        "Smash attacks deal " + percent(MaceMastery.perk("titansWrathDamage", 0.25))
            + " more damage and Seismic Slam's shockwave is "
            + percent(MaceMastery.perk("titansWrathRadius", 1.5) - 1) + " wider."};
  }

  public static void open(Player p) {
    TreeHolder holder = new TreeHolder();
    Inventory gui = Bukkit.createInventory(holder, 54, "Mace Mastery");
    holder.inventory = gui;
    WeaponSkillData data = WeaponSkillManager.get(p, SKILL);
    int passiveTokens = WeaponSkillManager.getAvailablePassiveTokens(data);
    int skillTokens = WeaponSkillManager.getAvailableSkillTokens(data);

    // Skills
    String[] descriptions = skillDescriptions();
    for (int i = 0; i < SKILL_KEYS.length; i++) {
      int level = data.getSkill(SKILL_KEYS[i]);
      ItemStack item = new ItemStack(skillMaterial(data, i));
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(ChatColor.BOLD + SKILL_NAMES[i]);
      List<String> lore = new ArrayList<>();
      String levelColor = i < 4 ? ChatColor.GREEN.toString()
          : i == 6 ? ChatColor.DARK_PURPLE.toString() : ChatColor.BLUE.toString();
      lore.add(ChatColor.GRAY + "Level " + levelColor + level + "/" + SKILL_MAX_LEVELS[i]);
      for (String line : wrap(descriptions[i], 40)) {
        lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString() + line);
      }
      meta.setLore(lore);
      item.setItemMeta(meta);
      gui.setItem(SKILL_SLOTS[i], item);
    }

    // Token counters
    gui.setItem(PASSIVE_TOKEN_SLOT, tokenItem(Material.IRON_NUGGET, "Passive Tokens", passiveTokens,
        "Earned by leveling up. Invest them in the dyes below."));
    gui.setItem(SKILL_TOKEN_SLOT, tokenItem(Material.GOLD_NUGGET, "Skill Tokens", skillTokens,
        "Earned every " + (int) WeaponSkillConfig.getDouble("tokens.levelsPerSkillToken", 100)
            + " levels. Spend them on the skills."));

    // Passives
    double duration = Math.round(MaceMastery.getAbilityDuration(data) * 100) / 100.0;
    gui.setItem(PASSIVE_1_SLOT, passiveItem(Material.RED_DYE, 1, "Slam Duration",
        ChatColor.GRAY + "Duration: " + ChatColor.AQUA + duration + " s" + ChatColor.GRAY
            + " (max " + WeaponSkillConfig.getDouble("mace.ability.maxDurationSeconds", 12) + " s)",
        "Increases how long Seismic Slam stays ready.",
        "Seismic Slam: right-click a mace to ready it. For a short time (up to "
            + MaceMastery.getAbilityCharges() + " smashes) your smash attacks send a shockwave "
            + "through nearby hostile mobs. Cooldown " + MaceMastery.getAbilityCooldown() + " s."));
    gui.setItem(PASSIVE_2_SLOT, passiveItem(Material.GREEN_DYE, 2, "Smash Power",
        ChatColor.GRAY + "Bonus: " + ChatColor.AQUA + percent(
            data.getPassive(MaceMastery.SMASH_POWER) * WeaponSkillConfig.getDouble(
                "mace.perks.smashPowerPerLevel", 0.005)),
        "Increases the damage of your smash attacks.", null));
    gui.setItem(PASSIVE_3_SLOT, passiveItem(Material.BLUE_DYE, 3, "Soft Landing",
        ChatColor.GRAY + "Reduction: " + ChatColor.AQUA + percent(
            MaceMastery.getFallReduction(data)),
        "Reduces the fall damage you take while holding a mace.", null));

    // Back, refund and configuration
    ItemStack back = new ItemStack(Material.ARROW);
    ItemMeta backMeta = back.getItemMeta();
    backMeta.setDisplayName(ChatColor.BOLD + "Back");
    back.setItemMeta(backMeta);
    gui.setItem(BACK_SLOT, back);
    gui.setItem(REFUND_SLOT, refundItem(p));
    gui.setItem(CONFIG_SLOT, configItem(data));

    // Connectors
    ItemStack connector = new ItemStack(Material.GLASS_PANE);
    ItemMeta connectorMeta = connector.getItemMeta();
    connectorMeta.setDisplayName(ChatColor.WHITE.toString());
    connector.setItemMeta(connectorMeta);
    for (int slot : CONNECTOR_SLOTS) {
      gui.setItem(slot, connector);
    }

    p.openInventory(gui);
  }

  /** Terracotta colour: red = locked, pink = can be learned, yellow = in progress, green = done. */
  private static Material skillMaterial(WeaponSkillData data, int index) {
    int level = data.getSkill(SKILL_KEYS[index]);
    int max = SKILL_MAX_LEVELS[index];
    if (level >= max) {
      return Material.GREEN_TERRACOTTA;
    }
    if (level > 0) {
      return Material.YELLOW_TERRACOTTA;
    }
    return requirementMet(data, index) ? Material.PINK_TERRACOTTA : Material.RED_TERRACOTTA;
  }

  /** The tree rules of the main skills: a skill needs two levels in the one before it. */
  private static boolean requirementMet(WeaponSkillData data, int index) {
    switch (index) {
      case 2:
        return data.getSkill(MaceMastery.WIDE_SHOCKWAVE) >= 2;
      case 3:
        return data.getSkill(MaceMastery.STAGGER) >= 2;
      case 4:
        return data.getSkill(MaceMastery.AFTERSHOCK) >= 2;
      case 5:
        return data.getSkill(MaceMastery.RATTLE) >= 2;
      case 6:
        int total = 0;
        for (int i = 0; i < 6; i++) {
          total += data.getSkill(SKILL_KEYS[i]);
        }
        return total >= 10;
      default:
        return true;
    }
  }

  private static ItemStack tokenItem(Material material, String name, int tokens, String text) {
    ItemStack item = new ItemStack(material, Math.max(1, Math.min(64, tokens)));
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(ChatColor.BOLD + name);
    List<String> lore = new ArrayList<>();
    lore.add(ChatColor.BLUE + "Total: " + ChatColor.GOLD + tokens);
    for (String line : wrap(text, 40)) {
      lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString() + line);
    }
    meta.setLore(lore);
    item.setItemMeta(meta);
    return item;
  }

  private static ItemStack passiveItem(Material material, int amount, String name,
      String statLine, String description, String abilityDescription) {
    ItemStack item = new ItemStack(material, amount);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(ChatColor.BOLD + name);
    List<String> lore = new ArrayList<>();
    lore.add(statLine);
    for (String line : wrap(description, 40)) {
      lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString() + line);
    }
    if (abilityDescription != null) {
      lore.add("");
      lore.add(ChatColor.UNDERLINE + "Ability");
      for (String line : wrap(abilityDescription, 40)) {
        lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString() + line);
      }
    }
    meta.setLore(lore);
    item.setItemMeta(meta);
    return item;
  }

  private static ItemStack refundItem(Player p) {
    ItemStack soul = new ItemStack(Material.COMPOSTER);
    ItemMeta meta = soul.getItemMeta();
    meta.setDisplayName(ChatColor.BOLD + "Refund Skill Tree");
    List<String> lore = new ArrayList<>();
    ArrayList<Number> global = new PlayerStats(p).getPlayerData().get("global");
    if (global.get(9).intValue() < 1) {
      lore.add(ChatColor.RED + ChatColor.ITALIC.toString() + "Locked");
    } else {
      int cost = new ConfigLoad().getSoulsInfo().get(1);
      lore.add("Souls: " + ChatColor.AQUA + ChatColor.ITALIC + global.get(20).intValue() + "/"
          + cost);
      lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString()
          + "Gives all invested tokens back so you can spend them again.");
      lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString() + "(-" + cost + " souls)");
    }
    meta.setLore(lore);
    soul.setItemMeta(meta);
    return soul;
  }

  private static ItemStack configItem(WeaponSkillData data) {
    ItemStack item = new ItemStack(Material.REDSTONE);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(ChatColor.BOLD + "Configuration");
    List<String> lore = new ArrayList<>();
    lore.add(ChatColor.GRAY + "Seismic Slam activation: "
        + (data.isAbilityEnabled() ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
    lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString() + "Click to turn right-click activation "
        + "of the ability on or off.");
    meta.setLore(lore);
    item.setItemMeta(meta);
    return item;
  }

  private static String number(double value) {
    return value == Math.rint(value) ? String.valueOf((int) value) : String.valueOf(value);
  }

  private static String percent(double fraction) {
    double value = Math.round(fraction * 1000) / 10.0;
    return value + "%";
  }

  private static List<String> wrap(String text, int width) {
    List<String> lines = new ArrayList<>();
    StringBuilder line = new StringBuilder();
    for (String word : text.split(" ")) {
      if (line.length() > 0 && line.length() + word.length() + 1 > width) {
        lines.add(line.toString());
        line = new StringBuilder();
      }
      if (line.length() > 0) {
        line.append(' ');
      }
      line.append(word);
    }
    if (line.length() > 0) {
      lines.add(line.toString());
    }
    return lines;
  }

  @EventHandler
  public void onClick(InventoryClickEvent e) {
    if (!(e.getInventory().getHolder() instanceof TreeHolder)
        || !(e.getWhoClicked() instanceof Player)) {
      return;
    }
    e.setCancelled(true);
    Player p = (Player) e.getWhoClicked();
    WeaponSkillData data = WeaponSkillManager.get(p, SKILL);
    int slot = e.getRawSlot();
    switch (slot) {
      case BACK_SLOT:
        WeaponSkillsMenu.open(p);
        return;
      case PASSIVE_1_SLOT:
        investInPassive(p, data, MaceMastery.SLAM_DURATION, e);
        return;
      case PASSIVE_2_SLOT:
        investInPassive(p, data, MaceMastery.SMASH_POWER, e);
        return;
      case PASSIVE_3_SLOT:
        investInPassive(p, data, MaceMastery.SOFT_LANDING, e);
        return;
      case REFUND_SLOT:
        refund(p, data);
        return;
      case CONFIG_SLOT:
        data.setAbilityEnabled(!data.isAbilityEnabled());
        p.sendMessage(ChatColor.GREEN + "Seismic Slam activation is now "
            + (data.isAbilityEnabled() ? "ON" : "OFF") + ".");
        open(p);
        return;
      default:
        break;
    }
    for (int i = 0; i < SKILL_SLOTS.length; i++) {
      if (SKILL_SLOTS[i] == slot) {
        learnSkill(p, data, i);
        return;
      }
    }
  }

  private void investInPassive(Player p, WeaponSkillData data, String key,
      InventoryClickEvent e) {
    int available = WeaponSkillManager.getAvailablePassiveTokens(data);
    if (available <= 0) {
      p.sendMessage(ChatColor.RED + "You have no passive tokens.");
      return;
    }
    int room = MaceMastery.getPassiveMax(key) - data.getPassive(key);
    if (room <= 0) {
      p.sendMessage(ChatColor.RED + "This perk is already at its maximum level.");
      return;
    }
    int investment;
    if (e.isShiftClick() && e.isRightClick()) {
      investment = available;
    } else if (e.isShiftClick()) {
      investment = Math.min(available, WeaponSkillConfig.getInt("tokens.shiftClickInvestment", 100));
    } else if (e.isRightClick()) {
      investment = Math.min(available, WeaponSkillConfig.getInt("tokens.rightClickInvestment", 10));
    } else {
      investment = 1;
    }
    investment = Math.min(investment, room);
    data.setPassive(key, data.getPassive(key) + investment);
    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.2f);
    open(p);
  }

  private void learnSkill(Player p, WeaponSkillData data, int index) {
    String key = SKILL_KEYS[index];
    if (data.getSkill(key) >= SKILL_MAX_LEVELS[index]) {
      p.sendMessage(ChatColor.RED + "This perk is already maxed out.");
      return;
    }
    if (!requirementMet(data, index)) {
      p.sendMessage(ChatColor.RED + (index == 6
          ? "You need at least 10 levels in the other perks first."
          : "You need level 2 in the perk before this one first."));
      return;
    }
    if (WeaponSkillManager.getAvailableSkillTokens(data) <= 0) {
      p.sendMessage(ChatColor.RED + "You have no skill tokens.");
      return;
    }
    data.setSkill(key, data.getSkill(key) + 1);
    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
    open(p);
  }

  private void refund(Player p, WeaponSkillData data) {
    ArrayList<Number> global = new PlayerStats(p).getPlayerData().get("global");
    if (global.get(9).intValue() < 1) {
      p.sendMessage(ChatColor.RED + "You need to unlock the refund perk in the global skill tree "
          + "first.");
      return;
    }
    int cost = new ConfigLoad().getSoulsInfo().get(1);
    int souls = global.get(20).intValue();
    if (souls < cost) {
      p.sendMessage(ChatColor.RED + "You need " + cost + " souls to refund the skill tree.");
      return;
    }
    global.set(20, souls - cost);
    data.resetTree();
    p.sendMessage(ChatColor.GREEN + "Your Mace Mastery skill tree was refunded (-" + cost
        + " souls).");
    open(p);
  }

  @EventHandler
  public void onDrag(InventoryDragEvent e) {
    if (e.getInventory().getHolder() instanceof TreeHolder) {
      e.setCancelled(true);
    }
  }
}
