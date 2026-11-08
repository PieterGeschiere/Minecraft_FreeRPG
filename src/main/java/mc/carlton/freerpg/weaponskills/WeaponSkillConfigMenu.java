package mc.carlton.freerpg.weaponskills;

import java.util.ArrayList;
import java.util.List;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * The configuration menu of a weapon skill, laid out like the one of the main skills: the skill
 * icon on top, a toggle for the exp bar and one for the ability, and a back button.
 */
public class WeaponSkillConfigMenu implements Listener {

  private static final int ICON_SLOT = 4;
  private static final int EXP_BAR_SLOT = 10;
  private static final int ABILITY_SLOT = 11;
  private static final int BACK_SLOT = 45;

  /** Marks the inventory as a weapon skill configuration menu. */
  private static class ConfigHolder implements InventoryHolder {

    private Inventory inventory;
    private final WeaponSkill skill;

    ConfigHolder(WeaponSkill skill) {
      this.skill = skill;
    }

    @Override
    public Inventory getInventory() {
      return inventory;
    }
  }

  public static void open(Player p, WeaponSkill skill) {
    LanguageSelector lang = new LanguageSelector(p);
    ConfigHolder holder = new ConfigHolder(skill);
    Inventory gui = Bukkit.createInventory(holder, 54, skill.getDisplayName() + " Configuration");
    holder.inventory = gui;
    WeaponSkillData data = WeaponSkillManager.get(p, skill);

    gui.setItem(ICON_SLOT, item(skill.getIcon(),
        ChatColor.AQUA.toString() + ChatColor.BOLD + skill.getDisplayName(), null));
    gui.setItem(EXP_BAR_SLOT, item(Material.EXPERIENCE_BOTTLE,
        ChatColor.BOLD + lang.translateMessage("::showEXPBar::"), null));
    gui.setItem(EXP_BAR_SLOT + 9, toggle(lang, data.isExpBarEnabled()));
    gui.setItem(ABILITY_SLOT, item(skill.getIcon(),
        ChatColor.BOLD + lang.translateMessage("::triggerAbilities::"), null));
    gui.setItem(ABILITY_SLOT + 9, toggle(lang, data.isAbilityEnabled()));
    gui.setItem(BACK_SLOT, item(Material.ARROW, ChatColor.BOLD + lang.getString(
        "diggingPassiveTitle1"), lang.getString("diggingPassiveDesc1")));
    p.openInventory(gui);
  }

  private static ItemStack toggle(LanguageSelector lang, boolean on) {
    return item(on ? Material.LIME_DYE : Material.GRAY_DYE,
        (on ? ChatColor.GREEN : ChatColor.RED).toString() + ChatColor.BOLD
            + lang.getString(on ? "on0" : "off0"), null);
  }

  private static ItemStack item(Material material, String name, String description) {
    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(name);
    if (description != null) {
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString() + description);
      meta.setLore(lore);
    }
    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP,
        ItemFlag.HIDE_ENCHANTS);
    item.setItemMeta(meta);
    return item;
  }

  @EventHandler
  public void onClick(InventoryClickEvent e) {
    if (!(e.getInventory().getHolder() instanceof ConfigHolder)
        || !(e.getWhoClicked() instanceof Player)) {
      return;
    }
    e.setCancelled(true);
    WeaponSkill skill = ((ConfigHolder) e.getInventory().getHolder()).skill;
    Player p = (Player) e.getWhoClicked();
    WeaponSkillData data = WeaponSkillManager.get(p, skill);
    int slot = e.getRawSlot();
    if (slot == EXP_BAR_SLOT || slot == EXP_BAR_SLOT + 9) {
      data.setExpBarEnabled(!data.isExpBarEnabled());
      open(p, skill);
    } else if (slot == ABILITY_SLOT || slot == ABILITY_SLOT + 9) {
      data.setAbilityEnabled(!data.isAbilityEnabled());
      open(p, skill);
    } else if (slot == BACK_SLOT) {
      WeaponSkillTreeMenu.open(p, skill);
    }
  }

  @EventHandler
  public void onDrag(InventoryDragEvent e) {
    if (e.getInventory().getHolder() instanceof ConfigHolder) {
      e.setCancelled(true);
    }
  }
}
