package mc.carlton.freerpg.utils.gui;

import java.util.List;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * The back arrow every menu uses, so it looks the same everywhere. It always sits in the bottom
 * left corner: slot 45 in the 54-slot menus, slot 18 in the 27-slot ones.
 */
public final class BackButton {

  /** Bottom left slot of the 54-slot menus (skill trees, configuration, crafting...). */
  public static final int SLOT_LARGE = 45;
  /** Bottom left slot of the 27-slot menus (weapon skills, quests, party...). */
  public static final int SLOT_SMALL = 18;

  private BackButton() {
  }

  public static ItemStack create(LanguageSelector lang) {
    ItemStack back = new ItemStack(Material.ARROW);
    ItemMeta meta = back.getItemMeta();
    meta.setDisplayName(ChatColor.BOLD + lang.getString("diggingPassiveTitle1"));
    meta.setLore(List.of(ChatColor.GRAY.toString() + ChatColor.ITALIC
        + lang.getString("diggingPassiveDesc1")));
    back.setItemMeta(meta);
    return back;
  }
}
