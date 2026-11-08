package mc.carlton.freerpg.weaponskills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import mc.carlton.freerpg.core.info.player.ChangeStats;
import mc.carlton.freerpg.core.info.player.Leaderboards;
import mc.carlton.freerpg.core.info.player.PlayerStats;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * The weapon skills menu (/frpg weapons). It lists the main skills Archery, Swordsmanship and Axe
 * Mastery,
 * which open their normal skill tree when clicked, and the weapon skills of this module.
 */
public class WeaponSkillsMenu implements Listener {

  private static final int ARCHERY_SLOT = 10;
  private static final int SWORDSMANSHIP_SLOT = 11;
  private static final int AXE_MASTERY_SLOT = 12;
  /** Slots of the weapon skills of this module, in the order of {@link WeaponSkill#values()}. */
  private static final int[] SKILL_SLOTS = {13, 14, 15};
  private static final int BACK_SLOT = 22;

  /** Marks the inventory as the weapon skills menu and remembers which slot runs which command. */
  private static class MenuHolder implements InventoryHolder {

    private Inventory inventory;
    private final Map<Integer, Consumer<Player>> actions = new HashMap<>();

    @Override
    public Inventory getInventory() {
      return inventory;
    }
  }

  public static void open(Player p) {
    MenuHolder holder = new MenuHolder();
    LanguageSelector lang = new LanguageSelector(p);
    Inventory inventory = Bukkit.createInventory(holder, 27,
        lang.getString("weaponSkills"));
    holder.inventory = inventory;


    inventory.setItem(ARCHERY_SLOT, createMainSkillItem(p, "archery", Material.BOW));
    holder.actions.put(ARCHERY_SLOT, pl -> pl.performCommand("frpg skillTreeGUI archery"));
    inventory.setItem(SWORDSMANSHIP_SLOT,
        createMainSkillItem(p, "swordsmanship", Material.IRON_SWORD));
    holder.actions.put(SWORDSMANSHIP_SLOT, pl -> pl.performCommand("frpg skillTreeGUI swordsmanship"));
    inventory.setItem(AXE_MASTERY_SLOT, createMainSkillItem(p, "axeMastery", Material.GOLDEN_AXE));
    holder.actions.put(AXE_MASTERY_SLOT, pl -> pl.performCommand("frpg skillTreeGUI axeMastery"));

    WeaponSkill[] skills = WeaponSkill.values();
    for (int i = 0; i < skills.length && i < SKILL_SLOTS.length; i++) {
      inventory.setItem(SKILL_SLOTS[i], createSkillItem(p, skills[i]));
      WeaponSkill skill = skills[i];
      holder.actions.put(SKILL_SLOTS[i], pl -> WeaponSkillTreeMenu.open(pl, skill));
    }

    ItemStack back = new ItemStack(Material.ARROW);
    ItemMeta backMeta = back.getItemMeta();
    backMeta.setDisplayName(ChatColor.GRAY + lang.getString("diggingPassiveDesc1"));
    back.setItemMeta(backMeta);
    inventory.setItem(BACK_SLOT, back);
    holder.actions.put(BACK_SLOT, pl -> pl.performCommand("frpg"));

    p.openInventory(inventory);
  }

  /** An item for a skill of the main FreeRPG system, opening its skill tree when clicked. */
  private static ItemStack createMainSkillItem(Player p, String skillName, Material icon) {
    LanguageSelector lang = new LanguageSelector(p);
    Map<String, ArrayList<Number>> pStat = new PlayerStats(p).getPlayerData();
    ArrayList<Number> stats = pStat.get(skillName);
    int level = stats.get(0).intValue();
    int exp = stats.get(1).intValue();
    int passiveTokens = stats.get(2).intValue();
    int skillTokens = stats.get(3).intValue();
    ChangeStats changeStats = new ChangeStats(p);
    String expToNext = level < changeStats.getMaxLevel(skillName)
        ? String.format("%,d", changeStats.getEXPfromLevel(level + 1) - exp) : "N/A";
    Leaderboards leaderboards = new Leaderboards();
    int rank = leaderboards.getLeaderboardPosition(p, skillName);
    int totalPlayers = leaderboards.getLeaderboardSize(skillName);

    ItemStack item = new ItemStack(icon);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + lang.getString(skillName));
    List<String> lore = new ArrayList<>();
    lore.add(ChatColor.GRAY + lang.getString("level") + ": " + ChatColor.BLUE + level);
    lore.add(ChatColor.GRAY + lang.getString("experience") + ": " + ChatColor.BLUE
        + String.format("%,d", exp));
    lore.add(ChatColor.GRAY + lang.getString("expToLevel") + ": " + ChatColor.GREEN + expToNext);
    lore.add(ChatColor.GRAY + lang.getString("rank") + ": " + ChatColor.WHITE + ChatColor.BOLD
        + String.format("%,d", rank) + ChatColor.RESET + ChatColor.GRAY + " / "
        + ChatColor.WHITE + totalPlayers);
    meta.setLore(lore);
    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP,
        ItemFlag.HIDE_ENCHANTS);
    item.setItemMeta(meta);
    if (passiveTokens > 0 || skillTokens > 0) {
      addGlow(item);
    }
    return item;
  }

  /** The enchantment glow the main menu uses for skills with unspent tokens. */
  private static void addGlow(ItemStack item) {
    item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 1);
  }

  private static ItemStack createSkillItem(Player p, WeaponSkill skill) {
    LanguageSelector lang = new LanguageSelector(p);
    WeaponSkillData data = WeaponSkillManager.get(p, skill);
    int level = data.getLevel();
    long exp = data.getExperience();
    int nextLevelExp = WeaponSkillManager.getExpForLevel(p, level + 1);

    ItemStack item = new ItemStack(skill.getIcon());
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + skill.getDisplayName());
    int[] rank = WeaponSkillManager.getRank(p, skill);
    List<String> lore = new ArrayList<>();
    lore.add(ChatColor.GRAY + lang.getString("level") + ": " + ChatColor.BLUE + level);
    lore.add(ChatColor.GRAY + lang.getString("experience") + ": " + ChatColor.BLUE
        + String.format("%,d", exp));
    lore.add(ChatColor.GRAY + lang.getString("expToLevel") + ": " + ChatColor.GREEN
        + String.format("%,d", Math.max(0, nextLevelExp - exp)));
    lore.add(ChatColor.GRAY + lang.getString("rank") + ": " + ChatColor.WHITE + ChatColor.BOLD
        + String.format("%,d", rank[0]) + ChatColor.RESET + ChatColor.GRAY + " / "
        + ChatColor.WHITE + rank[1]);
    int passiveTokens = WeaponSkillManager.getAvailablePassiveTokens(data);
    int skillTokens = WeaponSkillManager.getAvailableSkillTokens(data);
    if (passiveTokens > 0 || skillTokens > 0) {
      lore.add(ChatColor.LIGHT_PURPLE + WeaponSkillRules.text(p, "weaponUnspentTokens",
          "passive", Integer.toString(passiveTokens), "skill", Integer.toString(skillTokens)));
    }
    long cooldownLeft = data.getAbilityCooldownUntil() - System.currentTimeMillis();
    if (cooldownLeft > 0) {
      lore.add(ChatColor.RED + WeaponSkillRules.text(p, "weaponCooldownLeft", "ability",
          skill.getAbilityName(), "seconds", Long.toString(cooldownLeft / 1000 + 1)));
    }
    meta.setLore(lore);
    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP,
        ItemFlag.HIDE_ENCHANTS);
    item.setItemMeta(meta);
    if (WeaponSkillManager.getAvailablePassiveTokens(data) > 0
        || WeaponSkillManager.getAvailableSkillTokens(data) > 0) {
      addGlow(item);
    }
    return item;
  }

  /** Splits text into lines of at most the given length, breaking at spaces. */
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
    if (!(e.getInventory().getHolder() instanceof MenuHolder)) {
      return;
    }
    e.setCancelled(true);
    MenuHolder holder = (MenuHolder) e.getInventory().getHolder();
    Consumer<Player> action = holder.actions.get(e.getRawSlot());
    if (action != null && e.getWhoClicked() instanceof Player) {
      Player p = (Player) e.getWhoClicked();
      p.closeInventory();
      action.accept(p);
    }
  }

  @EventHandler
  public void onDrag(InventoryDragEvent e) {
    if (e.getInventory().getHolder() instanceof MenuHolder) {
      e.setCancelled(true);
    }
  }
}
