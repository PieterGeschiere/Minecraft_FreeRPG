package mc.carlton.freerpg.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
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
 * The reward wheel: a ring of prizes around a 54-slot chest. The prize is rolled and handed out
 * the moment the spin starts; the animation after that is only for show, so closing the menu or
 * logging out can never lose or repeat a prize.
 */
public final class WheelMenu implements Listener {

  private static final int MIN_STEPS = 50;

  /** Marks an inventory as the wheel. */
  private static final class WheelHolder implements InventoryHolder {

    private Inventory inventory;
    private boolean finished;

    @Override
    public Inventory getInventory() {
      return inventory;
    }
  }

  /** Uses the player's spin (if they have one), gives the prize and plays the wheel. */
  public static void start(Player p) {
    start(p, QuestPeriod.DAILY);
  }

  /** The same for the spin earned by finishing the quests of a period; bigger periods pay more. */
  public static void start(Player p, QuestPeriod period) {
    if (!QuestConfig.enabled()) {
      QuestMessages.send(p, "questDisabled");
      return;
    }
    if (!Quests.manager(period).claimSpin(p.getUniqueId())) {
      QuestMessages.send(p, "questNoSpin");
      return;
    }
    play(p, QuestConfig.rewardMultiplier(period));
  }

  /** Uses one of the player's won (bonus) spins and plays the wheel. */
  public static void startBonus(Player p) {
    if (!QuestConfig.enabled()) {
      QuestMessages.send(p, "questDisabled");
      return;
    }
    if (!Quests.bonusSpins().consume(p.getUniqueId())) {
      QuestMessages.send(p, "questNoBonusSpin");
      return;
    }
    play(p, 1);
  }

  /** Rolls and gives the prize, then plays the wheel. The spin itself is already paid for. */
  private static void play(Player p, int multiplier) {
    Random random = new Random();
    int size = WheelAnimation.RING_SLOTS.length;
    List<WheelPrize> ring = WheelRoller.rollRing(random, PrizeGiver.enchantChoices(),
        PrizeGiver.skillNames(), QuestConfig.weightBook(), QuestConfig.weightMinecraftExp(),
        QuestConfig.weightFrpgExp(), scaled(QuestConfig.minecraftExpRange(), multiplier),
        scaled(QuestConfig.frpgExpRange(), multiplier), size);
    int landing = WheelRoller.pickLanding(random, size);
    WheelPrize prize = ring.get(landing);
    try {
      PrizeGiver.give(p, prize);
    } catch (RuntimeException e) {
      Quests.plugin().getLogger().severe("Could not give wheel prize " + prize + " to "
          + p.getName() + ": " + e.getMessage());
      p.giveExp(500);
    }

    WheelHolder holder = new WheelHolder();
    holder.inventory = Bukkit.createInventory(holder, 54,
        QuestMessages.text(p, "questWheelTitle"));
    ItemStack filler = named(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), " ", false);
    for (int slot = 0; slot < 54; slot++) {
      holder.inventory.setItem(slot, filler);
    }
    List<ItemStack> icons = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      ItemStack icon = named(PrizeGiver.icon(ring.get(i)),
          PrizeGiver.describe(p, ring.get(i)), false);
      icons.add(icon);
      holder.inventory.setItem(WheelAnimation.RING_SLOTS[i], icon);
    }
    p.openInventory(holder.inventory);

    int[] sequence = WheelAnimation.stepSequence(size, landing, MIN_STEPS + random.nextInt(size));
    spin(p, holder, icons, sequence, 0, -1, prize);
  }

  /** An exp range with both ends multiplied (a multiplier below 1 counts as 1). */
  static int[] scaled(int[] range, int multiplier) {
    int m = Math.max(1, multiplier);
    return new int[] {(int) Math.min(Integer.MAX_VALUE, (long) range[0] * m),
        (int) Math.min(Integer.MAX_VALUE, (long) range[1] * m)};
  }

  private static void spin(Player p, WheelHolder holder, List<ItemStack> icons, int[] sequence,
                           int step, int previous, WheelPrize prize) {
    if (!p.isOnline() || p.getOpenInventory().getTopInventory().getHolder() != holder) {
      finish(p, holder, prize, false);
      return;
    }
    if (previous >= 0) {
      holder.inventory.setItem(WheelAnimation.RING_SLOTS[previous], icons.get(previous));
    }
    int position = sequence[step];
    boolean last = step == sequence.length - 1;
    if (last) {
      holder.inventory.setItem(WheelAnimation.RING_SLOTS[position], named(
          icons.get(position).clone(), QuestMessages.text(p, "questWheelWinner", "prize",
              PrizeGiver.describe(p, prize)), true));
      finish(p, holder, prize, true);
      return;
    }
    holder.inventory.setItem(WheelAnimation.RING_SLOTS[position], named(
        new ItemStack(Material.LIME_STAINED_GLASS_PANE),
        icons.get(position).getItemMeta().getDisplayName(), true));
    if (QuestConfig.effectsEnabled()) {
      float pitch = 0.8f + 1.2f * step / sequence.length;
      p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, pitch);
    }
    Bukkit.getScheduler().runTaskLater(Quests.plugin(), () ->
        spin(p, holder, icons, sequence, step + 1, position, prize),
        WheelAnimation.delayTicks(step, sequence.length));
  }

  private static void finish(Player p, WheelHolder holder, WheelPrize prize, boolean landed) {
    if (holder.finished) {
      return;
    }
    holder.finished = true;
    if (!p.isOnline()) {
      return;
    }
    QuestMessages.send(p, "questWon", "prize", PrizeGiver.describe(p, prize));
    if (landed && QuestConfig.effectsEnabled()) {
      p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
      p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation().add(0, 1, 0), 40, 0.4,
          0.6, 0.4, 0.2);
    }
  }

  private static ItemStack named(ItemStack stack, String name, boolean glow) {
    ItemMeta meta = stack.getItemMeta();
    meta.setDisplayName(name);
    if (glow) {
      meta.setEnchantmentGlintOverride(true);
    }
    stack.setItemMeta(meta);
    return stack;
  }

  @EventHandler
  public void onClick(InventoryClickEvent e) {
    if (e.getView().getTopInventory().getHolder() instanceof WheelHolder) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onDrag(InventoryDragEvent e) {
    if (e.getView().getTopInventory().getHolder() instanceof WheelHolder) {
      e.setCancelled(true);
    }
  }
}
