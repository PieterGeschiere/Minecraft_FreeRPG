package mc.carlton.freerpg.quests;

import mc.carlton.freerpg.skills.SkillRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.core.info.player.ChangeStats;
import mc.carlton.freerpg.party.XpShareGuard;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import mc.carlton.freerpg.weaponskills.WeaponSkill;
import mc.carlton.freerpg.weaponskills.WeaponSkillConfig;
import mc.carlton.freerpg.weaponskills.WeaponSkillManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

/** Everything that touches the server for wheel prizes: handing them out, icons and names. */
public final class PrizeGiver {

  /** Main skills the wheel can give exp in (archery is retired). */
  private static final List<String> MAIN_SKILLS = SkillRegistry.labels().stream()
      .filter(label -> !label.equals("archery")).collect(Collectors.toUnmodifiableList());

  private static final Map<String, Material> SKILL_ICONS = Map.ofEntries(
      Map.entry("digging", Material.IRON_SHOVEL),
      Map.entry("woodcutting", Material.IRON_AXE),
      Map.entry("mining", Material.IRON_PICKAXE),
      Map.entry("farming", Material.IRON_HOE),
      Map.entry("fishing", Material.FISHING_ROD),
      Map.entry("beastMastery", Material.SADDLE),
      Map.entry("swordsmanship", Material.IRON_SWORD),
      Map.entry("defense", Material.SHIELD),
      Map.entry("axeMastery", Material.DIAMOND_AXE),
      Map.entry("repair", Material.ANVIL),
      Map.entry("agility", Material.LEATHER_BOOTS),
      Map.entry("alchemy", Material.BREWING_STAND),
      Map.entry("smelting", Material.FURNACE),
      Map.entry("enchanting", Material.ENCHANTING_TABLE),
      Map.entry("building", Material.BRICKS),
      Map.entry("aeronautics", Material.ELYTRA));

  private PrizeGiver() {
  }

  // ------------------------------------------------------------------ what can be won

  /** Every enchantment the server knows, treasure and curses included. */
  public static List<WheelRoller.EnchantChoice> enchantChoices() {
    List<WheelRoller.EnchantChoice> choices = new ArrayList<>();
    for (Enchantment enchantment : Registry.ENCHANTMENT) {
      choices.add(new WheelRoller.EnchantChoice(enchantment.getKey().toString(),
          Math.max(1, enchantment.getMaxLevel())));
    }
    return choices;
  }

  /** Skills that can receive exp: enabled main skills and, when enabled, the weapon skills. */
  public static List<String> skillNames() {
    List<String> skills = new ArrayList<>();
    Map<String, Boolean> allowed = new ConfigLoad().getAllowedSkillsMap();
    for (String skill : MAIN_SKILLS) {
      if (allowed.getOrDefault(skill, true)) {
        skills.add(skill);
      }
    }
    if (WeaponSkillConfig.isEnabled()) {
      for (WeaponSkill skill : WeaponSkill.values()) {
        skills.add(skill.getConfigKey());
      }
    }
    return skills;
  }

  // ------------------------------------------------------------------ giving

  /** Hands the prize to the player. A full inventory drops the book at their feet. */
  public static void give(Player p, WheelPrize prize) {
    switch (prize.kind()) {
      case BOOK -> {
        ItemStack book = book(prize);
        if (book == null) {
          p.giveExp(500); // the enchantment no longer exists on this server
          return;
        }
        for (ItemStack left : p.getInventory().addItem(book).values()) {
          p.getWorld().dropItemNaturally(p.getLocation(), left);
        }
      }
      case MINECRAFT_EXP -> p.giveExp(prize.amount());
      case FRPG_EXP -> XpShareGuard.share(() -> {
        WeaponSkill weaponSkill = WeaponSkill.fromInput(prize.skill());
        if (weaponSkill != null) {
          WeaponSkillManager.addExp(p, weaponSkill, prize.amount());
        } else {
          new ChangeStats(p).changeEXP(prize.skill(), prize.amount());
        }
      });
      default -> {
      }
    }
  }

  private static ItemStack book(WheelPrize prize) {
    NamespacedKey key = NamespacedKey.fromString(prize.enchantKey());
    Enchantment enchantment = key == null ? null : Registry.ENCHANTMENT.get(key);
    if (enchantment == null) {
      return null;
    }
    ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
    EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
    meta.addStoredEnchant(enchantment, prize.level(), true);
    book.setItemMeta(meta);
    return book;
  }

  // ------------------------------------------------------------------ showing

  /** The prize as it is shown on the wheel. */
  public static ItemStack icon(WheelPrize prize) {
    ItemStack stack = switch (prize.kind()) {
      case BOOK -> {
        ItemStack book = book(prize);
        yield book == null ? new ItemStack(Material.BOOK) : book;
      }
      case MINECRAFT_EXP -> new ItemStack(Material.EXPERIENCE_BOTTLE);
      case FRPG_EXP -> new ItemStack(skillIcon(prize.skill()));
    };
    ItemMeta meta = stack.getItemMeta();
    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
    stack.setItemMeta(meta);
    return stack;
  }

  private static Material skillIcon(String skill) {
    WeaponSkill weaponSkill = WeaponSkill.fromInput(skill);
    if (weaponSkill != null) {
      return weaponSkill.getIcon();
    }
    return SKILL_ICONS.getOrDefault(skill, Material.NETHER_STAR);
  }

  /** Short readable text for chat and item names, in the player's language where possible. */
  public static String describe(Player p, WheelPrize prize) {
    return switch (prize.kind()) {
      case BOOK -> QuestMessages.text(p, "questPrizeBook", "enchant",
          humanize(prize.enchantKey()) + " " + roman(prize.level()));
      case MINECRAFT_EXP -> QuestMessages.text(p, "questPrizeMcExp", "amount",
          String.valueOf(prize.amount()));
      case FRPG_EXP -> QuestMessages.text(p, "questPrizeFrpgExp", "amount",
          String.valueOf(prize.amount()), "skill", skillTitle(p, prize.skill()));
    };
  }

  private static String skillTitle(Player p, String skill) {
    WeaponSkill weaponSkill = WeaponSkill.fromInput(skill);
    if (weaponSkill != null) {
      return weaponSkill.getDisplayName();
    }
    return new LanguageSelector(p).getString(skill);
  }

  /** "minecraft:frost_walker" becomes "Frost Walker". */
  static String humanize(String key) {
    String path = key.contains(":") ? key.substring(key.indexOf(':') + 1) : key;
    StringBuilder text = new StringBuilder();
    for (String word : path.split("_")) {
      if (word.isEmpty()) {
        continue;
      }
      if (text.length() > 0) {
        text.append(' ');
      }
      text.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
    }
    return text.toString();
  }

  static String roman(int level) {
    String[] numerals = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
    return level >= 1 && level < numerals.length ? numerals[level] : String.valueOf(level);
  }
}
