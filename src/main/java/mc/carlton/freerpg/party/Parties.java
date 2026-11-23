package mc.carlton.freerpg.party;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
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
    long now = System.currentTimeMillis();
    long previousHeartbeat = readHeartbeat();
    if (previousHeartbeat > 0) {
      PartyInactivity.compensateDowntime(manager, previousHeartbeat, now);
    }
    writeHeartbeat(now);

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
    writeHeartbeat(System.currentTimeMillis());
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

  /** Creates the invite and request managers, or updates their values (keeping what is pending). */
  private static void applyConfig() {
    long inviteMillis = PartyConfig.inviteExpirySeconds() * 1000L;
    long requestMillis = PartyConfig.tpaExpirySeconds() * 1000L;
    long cooldownMillis = PartyConfig.tpaCooldownSeconds() * 1000L;
    if (invites == null) {
      invites = new InviteManager(inviteMillis);
      teleports = new TeleportRequestManager(requestMillis, cooldownMillis);
    } else {
      invites.setExpiryMillis(inviteMillis);
      teleports.setExpiryMillis(requestMillis);
      teleports.setCooldownMillis(cooldownMillis);
    }
  }

  // Server downtime must not count as inactivity: the time the server last ran is remembered.

  private static File stateFile() {
    return new File(plugin.getDataFolder(), "partyState.yml");
  }

  private static void writeHeartbeat(long now) {
    YamlConfiguration yaml = new YamlConfiguration();
    yaml.set("heartbeat", now);
    try {
      yaml.save(stateFile());
    } catch (IOException e) {
      plugin.getLogger().warning("Could not save partyState.yml: " + e.getMessage());
    }
  }

  private static long readHeartbeat() {
    File file = stateFile();
    return file.exists() ? YamlConfiguration.loadConfiguration(file).getLong("heartbeat", 0) : 0;
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

  /** Saves a party that still exists. A failing save is logged and never breaks the caller. */
  public static void save(Party party) {
    if (party.size() == 0 || !manager.isRegistered(party)) {
      return; // a disbanded party must not be written back to disk by a stale menu or command
    }
    try {
      storage.save(party);
    } catch (RuntimeException e) {
      plugin.getLogger().warning("Could not save party " + party.name() + ": " + e.getMessage());
    }
  }

  public static void saveAll() {
    for (Party party : manager.all()) {
      save(party);
    }
  }

  private static void onEvent(PartyEvent event) {
    try {
      PartyNotices.handle(event);
    } catch (RuntimeException e) {
      plugin.getLogger().warning("Could not tell the party about " + event.type() + ": "
          + e.getMessage());
    }
    if (event.type() == PartyEventType.DISBANDED) {
      storage.delete(event.party().name());
    } else {
      save(event.party());
    }
    try {
      PartyDisplay.refreshAll();
    } catch (RuntimeException e) {
      plugin.getLogger().warning("Could not update the party display: " + e.getMessage());
    }
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
    writeHeartbeat(now);
  }
}
