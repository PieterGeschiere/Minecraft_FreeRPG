package mc.carlton.freerpg.weaponskills;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.core.info.player.PlayerStats;
import mc.carlton.freerpg.utils.game.LanguageSelector;
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
 * The skill tree menu of the weapon skills. It has the same layout as the skill trees of the main
 * skills: token counters and the three passives (dyes) in the left column, seven skills
 * (terracotta) connected by glass panes, a back button, a refund button and a configuration
 * button. The texts come from the {@link SkillTreeInfo} of the skill.
 */
public class WeaponSkillTreeMenu implements Listener {

  // Slots of the main skill trees
  private static final int PASSIVE_TOKEN_SLOT = 0;
  private static final int SKILL_TOKEN_SLOT = 9;
  private static final int[] PASSIVE_SLOTS = {18, 27, 36};
  private static final Material[] PASSIVE_MATERIALS = {Material.RED_DYE, Material.GREEN_DYE,
      Material.BLUE_DYE};
  private static final String[] PASSIVE_KEYS = {"p1", "p2", "p3"};
  private static final int BACK_SLOT = 45;
  private static final int REFUND_SLOT = 47;
  private static final int CONFIG_SLOT = 53;
  private static final int[] CONNECTOR_SLOTS = {6, 12, 14, 24, 25, 30, 32, 42};

  /** The skills of the tree in the order 1a, 1b, 2a, 2b, 3a, 3b, M. */
  private static final String[] SKILL_KEYS = {"1a", "1b", "2a", "2b", "3a", "3b", "M"};
  private static final int[] SKILL_SLOTS = {11, 29, 13, 31, 7, 43, 26};
  private static final int[] SKILL_MAX_LEVELS = {5, 5, 5, 5, 1, 1, 1};

  private static final Map<WeaponSkill, SkillTreeInfo> trees = new EnumMap<>(WeaponSkill.class);

  /** Marks the inventory as a skill tree and remembers which skill it belongs to. */
  private static class TreeHolder implements InventoryHolder {

    private Inventory inventory;
    private final WeaponSkill skill;

    TreeHolder(WeaponSkill skill) {
      this.skill = skill;
    }

    @Override
    public Inventory getInventory() {
      return inventory;
    }
  }

  public static void register(SkillTreeInfo info) {
    trees.put(info.getSkill(), info);
  }

  public static void open(Player p, WeaponSkill skill) {
    SkillTreeInfo info = trees.get(skill);
    if (info == null) {
      p.sendMessage(ChatColor.RED + WeaponSkillRules.text(p, "weaponNoTree"));
      return;
    }
    TreeHolder holder = new TreeHolder(skill);
    Inventory gui = Bukkit.createInventory(holder, 54, skill.getDisplayName());
    holder.inventory = gui;
    LanguageSelector lang = new LanguageSelector(p);
    WeaponSkillData data = WeaponSkillManager.get(p, skill);
    int passiveTokens = WeaponSkillManager.getAvailablePassiveTokens(data);
    int skillTokens = WeaponSkillManager.getAvailableSkillTokens(data);

    // Skills
    String[] names = info.getSkillNames(p);
    String[] descriptions = info.getSkillDescriptions(p);
    for (int i = 0; i < SKILL_KEYS.length; i++) {
      int level = data.getSkill(SKILL_KEYS[i]);
      ItemStack item = new ItemStack(skillMaterial(data, i));
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(ChatColor.BOLD + names[i]);
      List<String> lore = new ArrayList<>();
      String levelColor = i < 4 ? ChatColor.GREEN.toString()
          : i == 6 ? ChatColor.DARK_PURPLE.toString() : ChatColor.BLUE.toString();
      lore.add(ChatColor.GRAY + lang.getString("level") + " " + levelColor + level + "/" + SKILL_MAX_LEVELS[i]);
      for (String line : TreeText.wrap(descriptions[i], 40)) {
        lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString() + line);
      }
      meta.setLore(lore);
      item.setItemMeta(meta);
      gui.setItem(SKILL_SLOTS[i], item);
    }

    // Token counters
    gui.setItem(PASSIVE_TOKEN_SLOT, tokenItem(Material.IRON_NUGGET, "Passive Tokens", passiveTokens,
        "Earned by leveling up. Invest them in the dyes below.", lang));
    gui.setItem(SKILL_TOKEN_SLOT, tokenItem(Material.GOLD_NUGGET, "Skill Tokens", skillTokens,
        "Earned every " + (int) WeaponSkillConfig.getDouble("tokens.levelsPerSkillToken", 100)
            + " levels. Spend them on the skills.", lang));

    // Passives
    String[] passiveNames = info.getPassiveNames(p);
    String[] passiveDescriptions = info.getPassiveDescriptions(p);
    for (int i = 0; i < 3; i++) {
      ItemStack item = new ItemStack(PASSIVE_MATERIALS[i], i + 1);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(ChatColor.BOLD + passiveNames[i]);
      List<String> lore = new ArrayList<>();
      lore.add(info.getPassiveStat(p, i, data));
      for (String line : TreeText.wrap(passiveDescriptions[i], 40)) {
        lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString() + line);
      }
      if (i == 0) {
        lore.add("");
        lore.add(ChatColor.UNDERLINE + lang.getString("weaponAbility"));
        for (String line : TreeText.wrap(info.getAbilityDescription(p), 40)) {
          lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString() + line);
        }
      }
      meta.setLore(lore);
      item.setItemMeta(meta);
      gui.setItem(PASSIVE_SLOTS[i], item);
    }

    // Back, refund and configuration
    ItemStack back = new ItemStack(Material.ARROW);
    ItemMeta backMeta = back.getItemMeta();
    backMeta.setDisplayName(ChatColor.BOLD + lang.getString("diggingPassiveTitle1"));
    back.setItemMeta(backMeta);
    gui.setItem(BACK_SLOT, back);
    gui.setItem(REFUND_SLOT, refundItem(p, lang));
    gui.setItem(CONFIG_SLOT, configItem(skill, data));

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
    if (level >= SKILL_MAX_LEVELS[index]) {
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
        return data.getSkill("1a") >= 2;
      case 3:
        return data.getSkill("1b") >= 2;
      case 4:
        return data.getSkill("2a") >= 2;
      case 5:
        return data.getSkill("2b") >= 2;
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

  private static ItemStack tokenItem(Material material, String name, int tokens, String text,
      LanguageSelector lang) {
    ItemStack item = new ItemStack(material, Math.max(1, Math.min(64, tokens)));
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(ChatColor.BOLD + name);
    List<String> lore = new ArrayList<>();
    lore.add(ChatColor.BLUE + lang.getString("total") + ": " + ChatColor.GOLD + tokens);
    for (String line : TreeText.wrap(text, 40)) {
      lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString() + line);
    }
    meta.setLore(lore);
    item.setItemMeta(meta);
    return item;
  }

  private static ItemStack refundItem(Player p, LanguageSelector lang) {
    ItemStack soul = new ItemStack(Material.COMPOSTER);
    ItemMeta meta = soul.getItemMeta();
    meta.setDisplayName(ChatColor.BOLD + lang.getString("refundSkillTitle"));
    List<String> lore = new ArrayList<>();
    ArrayList<Number> global = new PlayerStats(p).getPlayerData().get("global");
    if (global.get(9).intValue() < 1) {
      lore.add(ChatColor.RED + ChatColor.ITALIC.toString() + lang.getString("locked"));
    } else {
      int cost = new ConfigLoad().getSoulsInfo().get(1);
      lore.add(lang.getString("souls") + ": " + ChatColor.AQUA + ChatColor.ITALIC + global.get(20).intValue() + "/"
          + cost);
      lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString()
          + "Gives all invested tokens back so you can spend them again.");
      lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString() + "(-" + cost + " souls)");
    }
    meta.setLore(lore);
    soul.setItemMeta(meta);
    return soul;
  }

  private static ItemStack configItem(WeaponSkill skill, WeaponSkillData data) {
    ItemStack item = new ItemStack(Material.REDSTONE);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(ChatColor.BOLD + "Configuration");
    List<String> lore = new ArrayList<>();
    lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString()
        + "Turn the exp bar and the activation of the ability on or off.");
    meta.setLore(lore);
    item.setItemMeta(meta);
    return item;
  }

  @EventHandler
  public void onClick(InventoryClickEvent e) {
    if (!(e.getInventory().getHolder() instanceof TreeHolder)
        || !(e.getWhoClicked() instanceof Player)) {
      return;
    }
    e.setCancelled(true);
    TreeHolder holder = (TreeHolder) e.getInventory().getHolder();
    WeaponSkill skill = holder.skill;
    SkillTreeInfo info = trees.get(skill);
    Player p = (Player) e.getWhoClicked();
    WeaponSkillData data = WeaponSkillManager.get(p, skill);
    int slot = e.getRawSlot();
    if (slot == BACK_SLOT) {
      WeaponSkillsMenu.open(p);
      return;
    }
    if (slot == REFUND_SLOT) {
      refund(p, skill, data);
      return;
    }
    if (slot == CONFIG_SLOT) {
      WeaponSkillConfigMenu.open(p, skill);
      return;
    }
    for (int i = 0; i < PASSIVE_SLOTS.length; i++) {
      if (PASSIVE_SLOTS[i] == slot) {
        investInPassive(p, skill, info, data, PASSIVE_KEYS[i], e);
        return;
      }
    }
    for (int i = 0; i < SKILL_SLOTS.length; i++) {
      if (SKILL_SLOTS[i] == slot) {
        learnSkill(p, skill, data, i);
        return;
      }
    }
  }

  private void investInPassive(Player p, WeaponSkill skill, SkillTreeInfo info,
      WeaponSkillData data, String key, InventoryClickEvent e) {
    int available = WeaponSkillManager.getAvailablePassiveTokens(data);
    if (available <= 0) {
      p.sendMessage(ChatColor.RED + WeaponSkillRules.text(p, "weaponNoPassiveTokens"));
      return;
    }
    int room = info.getPassiveMax(key) - data.getPassive(key);
    if (room <= 0) {
      p.sendMessage(ChatColor.RED + WeaponSkillRules.text(p, "weaponPerkMaxed"));
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
    open(p, skill);
  }

  private void learnSkill(Player p, WeaponSkill skill, WeaponSkillData data, int index) {
    String key = SKILL_KEYS[index];
    if (data.getSkill(key) >= SKILL_MAX_LEVELS[index]) {
      p.sendMessage(ChatColor.RED + WeaponSkillRules.text(p, "weaponPerkMaxed"));
      return;
    }
    if (!requirementMet(data, index)) {
      p.sendMessage(ChatColor.RED + WeaponSkillRules.text(p,
          index == 6 ? "weaponNeedMasteryReq" : "weaponNeedPreviousPerk"));
      return;
    }
    if (WeaponSkillManager.getAvailableSkillTokens(data) <= 0) {
      p.sendMessage(ChatColor.RED + WeaponSkillRules.text(p, "weaponNoSkillTokens"));
      return;
    }
    data.setSkill(key, data.getSkill(key) + 1);
    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
    open(p, skill);
  }

  private void refund(Player p, WeaponSkill skill, WeaponSkillData data) {
    ArrayList<Number> global = new PlayerStats(p).getPlayerData().get("global");
    if (global.get(9).intValue() < 1) {
      p.sendMessage(ChatColor.RED + WeaponSkillRules.text(p, "weaponNeedRefundPerk"));
      return;
    }
    if (data.getTotalPassiveLevels() + data.getTotalSkillLevels() == 0) {
      p.sendMessage(ChatColor.RED + WeaponSkillRules.text(p, "weaponNothingToRefund", "skill",
          skill.getDisplayName()));
      return;
    }
    int cost = new ConfigLoad().getSoulsInfo().get(1);
    int souls = global.get(20).intValue();
    if (souls < cost) {
      p.sendMessage(ChatColor.RED + WeaponSkillRules.text(p, "weaponNeedSouls", "cost",
          Integer.toString(cost)));
      return;
    }
    global.set(20, souls - cost);
    data.resetTree();
    p.sendMessage(ChatColor.GREEN + WeaponSkillRules.text(p, "weaponRefunded", "skill",
        skill.getDisplayName(), "cost", Integer.toString(cost)));
    open(p, skill);
  }

  @EventHandler
  public void onDrag(InventoryDragEvent e) {
    if (e.getInventory().getHolder() instanceof TreeHolder) {
      e.setCancelled(true);
    }
  }
}
