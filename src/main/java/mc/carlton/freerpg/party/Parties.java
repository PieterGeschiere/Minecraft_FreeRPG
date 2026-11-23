package mc.carlton.freerpg.party;

import java.io.File;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/** Entry point of the party module: starts it, stops it and gives access to its parts. */
public final class Parties {

  private static final long HOUR_TICKS = 20L * 60 * 60;
  private static final long DAY_MILLIS = 24L * 60 * 60 * 1000;

  private static Plugin plugin;
  private static PartyManager manager;
  private static InviteManager invites;
  private static TeleportRequestManager teleports;
  private static PartyStorage storage;

  private Parties() {
  }

  public static void initialize(Plugin pluginInstance) {
    plugin = pluginInstance;
    PartyConfig.load(plugin);
    storage = new PartyStorage(new File(plugin.getDataFolder(), "parties"));
    manager = new PartyManager(PartyConfig.maxSize(), Parties::onEvent);
    applyConfig();
    for (Party party : storage.loadAll(message -> plugin.getLogger().warning(message))) {
      manager.register(party);
    }
    PartyNotices.load(plugin);

    Bukkit.getPluginManager().registerEvents(new PartyListener(), plugin);
    Bukkit.getPluginManager().registerEvents(new PartyFriendlyFire(), plugin);
    Bukkit.getPluginManager().registerEvents(new PartyMenu(), plugin);
    PartyCommands commands = new PartyCommands();
    PluginCommand command = plugin.getServer().getPluginCommand("party");
    if (command != null) {
      command.setExecutor(commands);
      command.setTabCompleter(commands);
    }
    Bukkit.getScheduler().runTaskTimer(plugin, Parties::runInactivity, 20L * 30, HOUR_TICKS);
    PartyDisplay.refreshAll();
  }

  public static void shutdown() {
    if (manager == null) {
      return;
    }
    saveAll();
    PartyNotices.save();
    TeleportService.cancelAll();
    PartyDisplay.clearAll();
  }

  /** Re-reads party.yml and applies the new values (admin command). */
  public static void reload() {
    PartyConfig.reload(plugin);
    manager.setMaxSize(PartyConfig.maxSize());
    applyConfig();
    PartyDisplay.refreshAll();
  }

  private static void applyConfig() {
    invites = new InviteManager(PartyConfig.inviteExpirySeconds() * 1000L);
    teleports = new TeleportRequestManager(PartyConfig.tpaExpirySeconds() * 1000L,
        PartyConfig.tpaCooldownSeconds() * 1000L);
  }

  public static Plugin plugin() {
    return plugin;
  }

  public static PartyManager manager() {
    return manager;
  }

  public static InviteManager invites() {
    return invites;
  }

  public static TeleportRequestManager teleports() {
    return teleports;
  }

  public static boolean isOnline(UUID uuid) {
    return Bukkit.getPlayer(uuid) != null;
  }

  public static void save(Party party) {
    if (party.size() > 0) {
      storage.save(party);
    }
  }

  public static void saveAll() {
    for (Party party : manager.all()) {
      save(party);
    }
  }

  private static void onEvent(PartyEvent event) {
    PartyNotices.handle(event);
    if (event.type() == PartyEventType.DISBANDED) {
      storage.delete(event.party().name());
    } else {
      save(event.party());
    }
    PartyDisplay.refreshAll();
  }

  /** Runs now and then: remembers who is online and applies the inactivity rules. */
  private static void runInactivity() {
    long now = System.currentTimeMillis();
    for (Player p : Bukkit.getOnlinePlayers()) {
      manager.markSeen(p.getUniqueId(), p.getName(), now);
    }
    PartyInactivity.apply(manager, now, PartyConfig.ownerTransferDays() * DAY_MILLIS,
        PartyConfig.deleteDays() * DAY_MILLIS, Parties::isOnline);
    saveAll();
  }
}
