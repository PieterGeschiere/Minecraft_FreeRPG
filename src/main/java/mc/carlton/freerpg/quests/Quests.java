package mc.carlton.freerpg.quests;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/** Entry point of the daily quest module: starts it, stops it and gives access to its parts. */
public final class Quests {

  private static final long ACTION_BAR_GAP_MILLIS = 1000;

  private static Plugin plugin;
  private static QuestManager manager;
  private static final Map<UUID, Long> lastActionBar = new HashMap<>();

  private Quests() {
  }

  public static void initialize(Plugin pluginInstance) {
    plugin = pluginInstance;
    QuestConfig.load(plugin);
    QuestStorage storage = new QuestStorage(new File(plugin.getDataFolder(), "quests"));
    manager = new QuestManager(storage, QuestConfig::pool, QuestConfig::questsPerDay,
        System::currentTimeMillis, QuestConfig::resetZone, QuestConfig::resetHour, new Random(),
        Quests::onEvent);
    Bukkit.getPluginManager().registerEvents(new QuestListener(), plugin);
    Bukkit.getPluginManager().registerEvents(new QuestMenu(), plugin);
    Bukkit.getPluginManager().registerEvents(new WheelMenu(), plugin);
    QuestCommands commands = new QuestCommands();
    PluginCommand command = plugin.getServer().getPluginCommand("quests");
    if (command != null) {
      command.setExecutor(commands);
    }
  }

  public static void shutdown() {
    lastActionBar.clear();
  }

  /** Re-reads quests.yml (admin command). Cached player quests are kept. */
  public static void reload() {
    QuestConfig.reload(plugin);
  }

  public static Plugin plugin() {
    return plugin;
  }

  public static QuestManager manager() {
    return manager;
  }

  /** Quests only progress in survival and adventure, and not in the worlds that are turned off. */
  public static boolean countsFor(GameMode mode, String worldName, List<String> disabledWorlds) {
    if (mode != GameMode.SURVIVAL && mode != GameMode.ADVENTURE) {
      return false;
    }
    for (String disabled : disabledWorlds) {
      if (disabled.equalsIgnoreCase(worldName)) {
        return false;
      }
    }
    return true;
  }

  private static boolean counts(Player p) {
    return manager != null && QuestConfig.enabled() && p.hasPermission("freerpg.quests.use")
        && countsFor(p.getGameMode(), p.getWorld().getName(), QuestConfig.disabledWorlds());
  }

  // ------------------------------------------------------------------ progress API

  /** A player broke a block. Blocks placed by players never count. */
  public static void onBlockBreak(Player p, Block block, boolean natural) {
    if (!natural || !counts(p)) {
      return;
    }
    boolean ripe = block.getBlockData() instanceof Ageable ageable
        && ageable.getAge() >= ageable.getMaximumAge();
    manager.record(p.getUniqueId(), QuestType.BREAK_BLOCKS,
        QuestTags.forBlock(block.getType().name(), ripe), 1);
  }

  /** A player earned skill exp (base amount, before multipliers). */
  public static void onSkillExp(Player p, String skillTag, int baseExp) {
    if (!counts(p)) {
      return;
    }
    manager.record(p.getUniqueId(), QuestType.EARN_SKILL_EXP, QuestTags.forSkill(skillTag),
        baseExp);
  }

  static void record(Player p, QuestType type, Set<String> tags, int amount) {
    if (counts(p)) {
      manager.record(p.getUniqueId(), type, tags, amount);
    }
  }

  // ------------------------------------------------------------------ telling the player

  private static void onEvent(QuestManager.QuestEvent event) {
    Player p = Bukkit.getPlayer(event.player());
    if (p == null) {
      return;
    }
    ActiveQuest quest = event.quest();
    switch (event.kind()) {
      case PROGRESS -> {
        long now = System.currentTimeMillis();
        Long last = lastActionBar.get(p.getUniqueId());
        if (last == null || now - last >= ACTION_BAR_GAP_MILLIS) {
          lastActionBar.put(p.getUniqueId(), now);
          p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
              QuestMessages.text(p, "questActionBar", "quest", quest.name(), "progress",
                  String.valueOf(quest.progress()), "target", String.valueOf(quest.target()))));
        }
      }
      case QUEST_DONE -> {
        QuestMessages.send(p, "questDone", "quest", quest.name());
        if (QuestConfig.effectsEnabled()) {
          p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
        }
      }
      case ALL_DONE -> {
        TextComponent line = new TextComponent(QuestMessages.prefixed(p, "questAllDone"));
        line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quests"));
        p.spigot().sendMessage(line);
        if (QuestConfig.effectsEnabled()) {
          p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        }
      }
    }
  }
}
