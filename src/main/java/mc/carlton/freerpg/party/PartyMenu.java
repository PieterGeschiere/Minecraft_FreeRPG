package mc.carlton.freerpg.party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

/** The /party menu: the party, its members and every setting. */
public final class PartyMenu implements Listener {

  private static final int INFO_SLOT = 4;
  private static final int FIRST_MEMBER_SLOT = 9;
  private static final int LAST_MEMBER_SLOT = 35;
  private static final int OPEN_SLOT = 37;
  private static final int XP_SLOT = 38;
  private static final int FRIENDLY_SLOT = 39;
  private static final int SET_SPAWN_SLOT = 40;
  private static final int SPAWN_SLOT = 41;
  private static final int RECEIVE_XP_SLOT = 42;
  private static final int TP_REQUESTS_SLOT = 43;
  private static final int BACK_SLOT = 45;
  private static final int LEAVE_SLOT = 53;

  /** Marks an inventory as a party menu and remembers what each slot does. */
  private static final class MenuHolder implements InventoryHolder {

    private Inventory inventory;
    private final Map<Integer, Consumer<Player>> actions = new HashMap<>();

    @Override
    public Inventory getInventory() {
      return inventory;
    }
  }

  // ------------------------------------------------------------------ opening

  public static void open(Player p) {
    Party party = Parties.manager().partyOf(p.getUniqueId()).orElse(null);
    if (party == null) {
      openNoParty(p);
    } else {
      openParty(p, party);
    }
  }

  private static void reopenLater(Player p) {
    Bukkit.getScheduler().runTask(Parties.plugin(), () -> open(p));
  }

  private static MenuHolder holder(Player p, int size) {
    MenuHolder holder = new MenuHolder();
    holder.inventory = Bukkit.createInventory(holder, size,
        PartyMessages.text(p, "partyMenuTitle"));
    return holder;
  }

  private static ItemStack item(Material material, String name, List<String> lore) {
    ItemStack stack = new ItemStack(material);
    ItemMeta meta = stack.getItemMeta();
    meta.setDisplayName(name);
    meta.setLore(lore);
    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    stack.setItemMeta(meta);
    return stack;
  }

  private static ItemStack item(Material material, String name, String... lore) {
    return item(material, name, new ArrayList<>(List.of(lore)));
  }

  private static ItemStack backItem(Player p) {
    return item(Material.ARROW, "§7" + PartyMessages.text(p, "partyMenuBack"));
  }

  private static void openNoParty(Player p) {
    MenuHolder holder = holder(p, 27);
    holder.inventory.setItem(4, item(Material.WHITE_BANNER,
        "§9§l" + PartyMessages.text(p, "partyMenuMain"),
        PartyMessages.text(p, "partyMenuNoParty")));
    holder.inventory.setItem(11, item(Material.WRITABLE_BOOK, "§e/party create <name>",
        PartyMessages.text(p, "partyMenuCreateHelp")));
    holder.inventory.setItem(13, item(Material.OAK_DOOR, "§e/party join <party|owner>",
        PartyMessages.text(p, "partyMenuJoinHelp")));
    holder.inventory.setItem(15, item(Material.PAPER, "§e/party accept",
        PartyMessages.text(p, "partyMenuAcceptHelp")));
    holder.inventory.setItem(22, backItem(p));
    holder.actions.put(22, pl -> pl.performCommand("frpg"));
    p.openInventory(holder.inventory);
  }

  private static String state(Player p, boolean on) {
    return PartyMessages.text(p, on ? "partyWordOn" : "partyWordOff");
  }

  private static void openParty(Player p, Party party) {
    UUID viewerId = p.getUniqueId();
    PartyMember viewer = party.member(viewerId);
    PartyRole role = viewer.role();
    MenuHolder holder = holder(p, 54);
    Inventory inv = holder.inventory;

    inv.setItem(INFO_SLOT, item(Material.WHITE_BANNER, "§9§l" + party.name(),
        PartyMessages.text(p, "partyMenuInfoMembers", "size", String.valueOf(party.size()), "max",
            String.valueOf(Parties.manager().maxSize())),
        PartyMessages.text(p, party.open() ? "partyWordOpen" : "partyWordClosed")));

    int slot = FIRST_MEMBER_SLOT;
    for (PartyMember member : party.members()) {
      if (slot > LAST_MEMBER_SLOT) {
        break;
      }
      inv.setItem(slot, memberHead(p, role, member, viewerId));
      final UUID target = member.uuid();
      if (!availableActions(role, member, viewerId).isEmpty()) {
        holder.actions.put(slot, pl -> openMemberActions(pl, target));
      }
      slot++;
    }

    boolean manage = PartyRules.can(PartyAction.TOGGLE_OPEN, role);
    String needRights = PartyMessages.text(p, "partyMenuNeedRights");
    String clickToggle = PartyMessages.text(p, "partyMenuClickToggle");
    String clickUse = PartyMessages.text(p, "partyMenuClickUse");

    inv.setItem(OPEN_SLOT, item(party.open() ? Material.OAK_DOOR : Material.IRON_DOOR,
        "§e" + PartyMessages.text(p, "partyMenuOpenName"),
        PartyMessages.text(p, party.open() ? "partyWordOpen" : "partyWordClosed"),
        manage ? clickToggle : needRights));
    inv.setItem(XP_SLOT, item(Material.EXPERIENCE_BOTTLE,
        "§e" + PartyMessages.text(p, "partyMenuXpName"),
        "§7" + PartyMessages.text(p, "partyMenuXpDesc"), state(p, party.xpShareEnabled()),
        manage ? clickToggle : needRights));
    inv.setItem(FRIENDLY_SLOT, item(Material.IRON_SWORD,
        "§e" + PartyMessages.text(p, "partyMenuFriendlyName"),
        "§7" + PartyMessages.text(p, "partyMenuFriendlyDesc"), state(p, party.friendlyFire()),
        manage ? clickToggle : needRights));
    inv.setItem(SET_SPAWN_SLOT, item(Material.COMPASS,
        "§e" + PartyMessages.text(p, "partyMenuSpawnSetName"),
        "§7" + PartyMessages.text(p, "partyMenuSpawnSetDesc"),
        party.spawn().isPresent() ? "" : PartyMessages.text(p, "partyMenuSpawnNone"),
        manage ? clickUse : needRights));
    inv.setItem(SPAWN_SLOT, item(Material.ENDER_PEARL,
        "§e" + PartyMessages.text(p, "partyMenuSpawnTpName"),
        "§7" + PartyMessages.text(p, "partyMenuSpawnTpDesc"),
        party.spawn().isPresent() ? clickUse : PartyMessages.text(p, "partyMenuSpawnNone")));
    inv.setItem(RECEIVE_XP_SLOT, item(Material.GOLD_NUGGET,
        "§e" + PartyMessages.text(p, "partyMenuReceiveXpName"), state(p, viewer.receiveSharedXp()),
        clickToggle));
    inv.setItem(TP_REQUESTS_SLOT, item(Material.ENDER_EYE,
        "§e" + PartyMessages.text(p, "partyMenuTpRequestsName"),
        state(p, viewer.acceptTeleportRequests()), clickToggle));
    inv.setItem(BACK_SLOT, backItem(p));
    inv.setItem(LEAVE_SLOT, item(Material.BARRIER,
        "§c" + PartyMessages.text(p, "partyMenuLeaveName")));

    if (manage) {
      holder.actions.put(OPEN_SLOT, pl -> {
        PartyCommands.report(pl, Parties.manager().setOpen(viewerId, !party.open()), "", false);
        reopenLater(pl);
      });
      holder.actions.put(XP_SLOT, pl -> {
        PartyCommands.report(pl, Parties.manager().setXpShare(viewerId, !party.xpShareEnabled()),
            "", false);
        reopenLater(pl);
      });
      holder.actions.put(FRIENDLY_SLOT, pl -> {
        PartyCommands.report(pl,
            Parties.manager().setFriendlyFire(viewerId, !party.friendlyFire()), "", false);
        reopenLater(pl);
      });
      holder.actions.put(SET_SPAWN_SLOT, pl -> {
        pl.closeInventory();
        pl.performCommand("party setspawn");
      });
    }
    holder.actions.put(SPAWN_SLOT, pl -> {
      pl.closeInventory();
      pl.performCommand("party spawn");
    });
    holder.actions.put(RECEIVE_XP_SLOT, pl -> {
      viewer.setReceiveSharedXp(!viewer.receiveSharedXp());
      Parties.save(party);
      reopenLater(pl);
    });
    holder.actions.put(TP_REQUESTS_SLOT, pl -> {
      viewer.setAcceptTeleportRequests(!viewer.acceptTeleportRequests());
      Parties.save(party);
      reopenLater(pl);
    });
    holder.actions.put(BACK_SLOT, pl -> pl.performCommand("frpg"));
    holder.actions.put(LEAVE_SLOT, pl -> openConfirm(pl, PartyMessages.text(pl, "partyMenuLeaveName"),
        () -> PartyCommands.report(pl, Parties.manager().leave(viewerId,
            System.currentTimeMillis()), "", false)));
    p.openInventory(inv);
  }

  // ------------------------------------------------------------------ members

  private static ItemStack memberHead(Player viewer, PartyRole viewerRole, PartyMember member,
      UUID viewerId) {
    ItemStack head = new ItemStack(Material.PLAYER_HEAD);
    SkullMeta meta = (SkullMeta) head.getItemMeta();
    OfflinePlayer owner = Bukkit.getOfflinePlayer(member.uuid());
    meta.setOwningPlayer(owner);
    String crown = member.role() == PartyRole.OWNER ? "§6♛ "
        : member.role() == PartyRole.OFFICER ? "§e★ " : "§f";
    meta.setDisplayName(crown + member.lastName());
    List<String> lore = new ArrayList<>();
    lore.add("§7" + PartyMessages.text(viewer, PartyCommands.roleKey(member.role())));
    lore.add(PartyMessages.text(viewer,
        Parties.isOnline(member.uuid()) ? "partyWordOnline" : "partyWordOffline"));
    lore.add(availableActions(viewerRole, member, viewerId).isEmpty()
        ? PartyMessages.text(viewer, "partyMenuMemberNoActions")
        : PartyMessages.text(viewer, "partyMenuMemberClick"));
    meta.setLore(lore);
    head.setItemMeta(meta);
    return head;
  }

  private enum MemberAction { KICK, PROMOTE, DEMOTE, TRANSFER }

  private static List<MemberAction> availableActions(PartyRole viewerRole, PartyMember target,
      UUID viewerId) {
    List<MemberAction> actions = new ArrayList<>();
    if (target.uuid().equals(viewerId) || target.role() == PartyRole.OWNER) {
      return actions;
    }
    boolean canKick = PartyRules.can(PartyAction.KICK_MEMBER, viewerRole)
        && (target.role() != PartyRole.OFFICER
        || PartyRules.can(PartyAction.KICK_OFFICER, viewerRole));
    if (canKick) {
      actions.add(MemberAction.KICK);
    }
    if (PartyRules.can(PartyAction.PROMOTE, viewerRole) && target.role() == PartyRole.MEMBER) {
      actions.add(MemberAction.PROMOTE);
    }
    if (PartyRules.can(PartyAction.DEMOTE, viewerRole) && target.role() == PartyRole.OFFICER) {
      actions.add(MemberAction.DEMOTE);
    }
    if (PartyRules.can(PartyAction.TRANSFER, viewerRole)) {
      actions.add(MemberAction.TRANSFER);
    }
    return actions;
  }

  private static void openMemberActions(Player p, UUID targetId) {
    Party party = Parties.manager().partyOf(p.getUniqueId()).orElse(null);
    PartyMember target = party == null ? null : party.member(targetId);
    if (target == null) {
      open(p);
      return;
    }
    UUID viewerId = p.getUniqueId();
    List<MemberAction> actions = availableActions(party.member(viewerId).role(), target, viewerId);
    if (actions.isEmpty()) {
      open(p);
      return;
    }
    MenuHolder holder = holder(p, 27);
    holder.inventory.setItem(4, item(Material.PLAYER_HEAD, "§f" + target.lastName()));
    int slot = 10;
    for (MemberAction action : actions) {
      String label;
      Material icon;
      Runnable run;
      switch (action) {
        case KICK:
          label = "partyMenuKick";
          icon = Material.IRON_BOOTS;
          run = () -> PartyCommands.report(p, Parties.manager().kick(viewerId, targetId),
              target.lastName(), false);
          break;
        case PROMOTE:
          label = "partyMenuPromote";
          icon = Material.GOLDEN_HELMET;
          run = () -> PartyCommands.report(p, Parties.manager().promote(viewerId, targetId),
              target.lastName(), true);
          break;
        case DEMOTE:
          label = "partyMenuDemote";
          icon = Material.LEATHER_HELMET;
          run = () -> PartyCommands.report(p, Parties.manager().demote(viewerId, targetId),
              target.lastName(), true);
          break;
        default:
          label = "partyMenuTransfer";
          icon = Material.GOLDEN_APPLE;
          run = () -> PartyCommands.report(p, Parties.manager().transfer(viewerId, targetId),
              target.lastName(), true);
          break;
      }
      String name = PartyMessages.text(p, label);
      holder.inventory.setItem(slot, item(icon, "§e" + name, "§7" + target.lastName()));
      holder.actions.put(slot, pl -> openConfirm(pl, name + ": " + target.lastName(), run));
      slot += 2;
    }
    holder.inventory.setItem(22, backItem(p));
    holder.actions.put(22, PartyMenu::open);
    p.openInventory(holder.inventory);
  }

  // ------------------------------------------------------------------ confirm

  private static void openConfirm(Player p, String what, Runnable action) {
    MenuHolder holder = holder(p, 27);
    holder.inventory.setItem(4, item(Material.PAPER,
        "§e" + PartyMessages.text(p, "partyMenuConfirmTitle"), "§7" + what));
    holder.inventory.setItem(11, item(Material.LIME_CONCRETE,
        "§a" + PartyMessages.text(p, "partyMenuConfirm")));
    holder.inventory.setItem(15, item(Material.RED_CONCRETE,
        "§c" + PartyMessages.text(p, "partyMenuCancel")));
    holder.actions.put(11, pl -> {
      pl.closeInventory();
      action.run();
      reopenLater(pl);
    });
    holder.actions.put(15, PartyMenu::open);
    p.openInventory(holder.inventory);
  }

  // ----------------------------------------------------------------- clicking

  @EventHandler
  public void onClick(InventoryClickEvent e) {
    if (!(e.getView().getTopInventory().getHolder() instanceof MenuHolder holder)) {
      return;
    }
    e.setCancelled(true);
    if (e.getClickedInventory() != e.getView().getTopInventory()
        || e.getClick() != ClickType.LEFT || !(e.getWhoClicked() instanceof Player p)) {
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
