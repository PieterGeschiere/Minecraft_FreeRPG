package mc.carlton.freerpg.quests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
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

/** The /quests menu: today's quests with their progress and the wheel button. */
public final class QuestMenu implements Listener {

  private static final int INFO_SLOT = 4;
  private static final int[] QUEST_SLOTS = {11, 13, 15};
  private static final int WHEEL_SLOT = 22;
  private static final int BACK_SLOT = 18;
  private static final int BAR_LENGTH = 10;

  /** Marks an inventory as the quest menu and remembers what each slot does. */
  private static final class MenuHolder implements InventoryHolder {

    private Inventory inventory;
    private final Map<Integer, Consumer<Player>> actions = new HashMap<>();

    @Override
    public Inventory getInventory() {
      return inventory;
    }
  }

  public static void open(Player p) {
    if (!QuestConfig.enabled()) {
      QuestMessages.send(p, "questDisabled");
      return;
    }
    PlayerQuests data = Quests.manager().today(p.getUniqueId());
    MenuHolder holder = new MenuHolder();
    holder.inventory = Bukkit.createInventory(holder, 27, QuestMessages.text(p, "questMenuTitle"));
    Inventory inv = holder.inventory;

    ItemStack filler = item(Material.GRAY_STAINED_GLASS_PANE, " ", List.of(), false);
    for (int slot = 0; slot < inv.getSize(); slot++) {
      inv.setItem(slot, filler);
    }
    inv.setItem(INFO_SLOT, item(Material.WRITABLE_BOOK, QuestMessages.text(p, "questMenuInfo"),
        List.of(QuestMessages.text(p, "questMenuInfoLore")), false));

    for (int i = 0; i < QUEST_SLOTS.length; i++) {
      if (i < data.quests().size()) {
        inv.setItem(QUEST_SLOTS[i], questItem(p, data.quests().get(i)));
      } else {
        inv.setItem(QUEST_SLOTS[i], item(Material.LIGHT_GRAY_STAINED_GLASS_PANE,
            QuestMessages.text(p, "questNone"), List.of(), false));
      }
    }

    inv.setItem(WHEEL_SLOT, wheelItem(p, data));
    holder.actions.put(WHEEL_SLOT, pl -> {
      PlayerQuests current = Quests.manager().today(pl.getUniqueId());
      if (current.spin() == SpinState.READY) {
        pl.closeInventory();
        // start on the next tick: a menu should not be opened from inside a click on another one
        Bukkit.getScheduler().runTask(Quests.plugin(), () -> WheelMenu.start(pl));
      } else {
        QuestMessages.send(pl, "questNoSpin");
      }
    });

    inv.setItem(BACK_SLOT, item(Material.ARROW, "§7" + QuestMessages.text(p, "questBack"),
        List.of(), false));
    holder.actions.put(BACK_SLOT, pl -> pl.performCommand("frpg"));
    p.openInventory(inv);
  }

  private static ItemStack questItem(Player p, ActiveQuest quest) {
    List<String> lore = new ArrayList<>();
    lore.add(QuestMessages.text(p, "questProgress", "progress", String.valueOf(quest.progress()),
        "target", String.valueOf(quest.target())));
    lore.add(bar(quest));
    if (quest.done()) {
      lore.add(QuestMessages.text(p, "questStatusDone"));
    }
    return item(icon(quest.type()), (quest.done() ? "§a" : "§e") + quest.name(), lore,
        quest.done());
  }

  private static String bar(ActiveQuest quest) {
    int filled = (int) Math.round((double) quest.progress() / quest.target() * BAR_LENGTH);
    StringBuilder bar = new StringBuilder("§a");
    for (int i = 0; i < BAR_LENGTH; i++) {
      if (i == filled) {
        bar.append("§8");
      }
      bar.append('■');
    }
    return bar.toString();
  }

  private static Material icon(QuestType type) {
    return switch (type) {
      case BREAK_BLOCKS -> Material.IRON_PICKAXE;
      case KILL_MOBS -> Material.IRON_SWORD;
      case CATCH_FISH -> Material.FISHING_ROD;
      case CRAFT_ITEMS -> Material.CRAFTING_TABLE;
      case BREED_ANIMALS -> Material.WHEAT;
      case EARN_SKILL_EXP -> Material.EXPERIENCE_BOTTLE;
    };
  }

  private static ItemStack wheelItem(Player p, PlayerQuests data) {
    switch (data.spin()) {
      case READY:
        return item(Material.NETHER_STAR, QuestMessages.text(p, "questWheelReady"),
            List.of(QuestMessages.text(p, "questWheelReadyLore")), true);
      case CLAIMED:
        return item(Material.BARRIER, QuestMessages.text(p, "questWheelUsed"),
            List.of(QuestMessages.text(p, "questWheelUsedLore")), false);
      default:
        int done = (int) data.quests().stream().filter(ActiveQuest::done).count();
        return item(Material.CLOCK, QuestMessages.text(p, "questWheelLocked"),
            List.of(QuestMessages.text(p, "questWheelLockedLore", "done", String.valueOf(done),
                "total", String.valueOf(data.quests().size()))), false);
    }
  }

  private static ItemStack item(Material material, String name, List<String> lore, boolean glow) {
    ItemStack stack = new ItemStack(material);
    ItemMeta meta = stack.getItemMeta();
    meta.setDisplayName(name);
    meta.setLore(new ArrayList<>(lore));
    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    if (glow) {
      meta.setEnchantmentGlintOverride(true);
    }
    stack.setItemMeta(meta);
    return stack;
  }

  @EventHandler
  public void onClick(InventoryClickEvent e) {
    if (!(e.getView().getTopInventory().getHolder() instanceof MenuHolder holder)) {
      return;
    }
    e.setCancelled(true);
    if (e.getClickedInventory() == null || e.getClickedInventory() != e.getView()
        .getTopInventory() || !(e.getWhoClicked() instanceof Player p)) {
      return;
    }
    Consumer<Player> action = holder.actions.get(e.getSlot());
    if (action != null) {
      action.accept(p);
    }
  }

  @EventHandler
  public void onDrag(InventoryDragEvent e) {
    if (e.getView().getTopInventory().getHolder() instanceof MenuHolder) {
      e.setCancelled(true);
    }
  }
}
