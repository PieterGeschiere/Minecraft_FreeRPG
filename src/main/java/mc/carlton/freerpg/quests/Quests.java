package mc.carlton.freerpg.quests;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
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
  private static QuestManager partyManager;
  private static BonusSpins bonusSpins;
  private static final Map<UUID, String> partyNames = new HashMap<>();
  private static final Map<QuestPeriod, QuestManager> managers = new EnumMap<>(QuestPeriod.class);
  private static final Map<UUID, Long> lastActionBar = new HashMap<>();

  private Quests() {
  }

  public static void initialize(Plugin pluginInstance) {
    plugin = pluginInstance;
    QuestConfig.load(plugin);
    managers.clear();
    for (QuestPeriod period : QuestPeriod.values()) {
      String folder = period == QuestPeriod.DAILY ? "quests" : "quests-" + period.id();
      QuestStorage storage = new QuestStorage(new File(plugin.getDataFolder(), folder));
      managers.put(period, new QuestManager(period, storage, () -> QuestConfig.pool(period),
          () -> QuestConfig.questsPer(period), System::currentTimeMillis, QuestConfig::resetZone,
          QuestConfig::resetHour, new Random(), event -> onEvent(period, event)));
    }
    manager = managers.get(QuestPeriod.DAILY);
    bonusSpins = new BonusSpins(new File(plugin.getDataFolder(), "bonus-spins.yml"));
    // Party quests: one set of quests per party, found by a key made from the party's name
    partyManager = new QuestManager(QuestPeriod.DAILY,
        new QuestStorage(new File(plugin.getDataFolder(), "quests-party")),
        QuestConfig::partyPool, QuestConfig::partyQuestsPerDay, System::currentTimeMillis,
        QuestConfig::resetZone, QuestConfig::resetHour, new Random(), Quests::onPartyEvent);
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

  /** The manager of today's (daily) quests. */
  public static QuestManager manager() {
    return manager;
  }

  /** The wheel spins that were won, for example at a world event. */
  public static BonusSpins bonusSpins() {
    return bonusSpins;
  }

  /** The manager of one period's quests. */
  public static QuestManager manager(QuestPeriod period) {
    return managers.get(period);
  }

  /** The managers of the periods that are turned on, daily first. */
  public static List<QuestManager> activeManagers() {
    List<QuestManager> active = new ArrayList<>();
    for (QuestPeriod period : QuestPeriod.values()) {
      QuestManager m = managers.get(period);
      if (m != null && QuestConfig.periodEnabled(period)) {
        active.add(m);
      }
    }
    return active;
  }

  /** Counts something for the daily, weekly and seasonal quests at once. */
  private static void recordAll(UUID uuid, QuestType type, Set<String> tags, int amount) {
    for (QuestManager m : activeManagers()) {
      m.record(uuid, type, tags, amount);
    }
    if (partyManager != null && QuestConfig.partyQuestsEnabled()
        && mc.carlton.freerpg.party.Parties.manager() != null) {
      mc.carlton.freerpg.party.Parties.manager().partyOf(uuid).ifPresent(party -> {
        UUID key = partyKey(party.name());
        partyNames.put(key, party.name());
        partyManager.record(key, type, tags, amount);
      });
    }
  }

  /** The id a party's quests are saved under (the same name always gives the same id). */
  public static UUID partyKey(String partyName) {
    return UUID.nameUUIDFromBytes(("party:" + partyName.toLowerCase(java.util.Locale.ROOT))
        .getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }

  /** Today's quests of a party, or null when party quests are not available. */
  public static PlayerQuests partyQuests(String partyName) {
    if (partyManager == null || !QuestConfig.partyQuestsEnabled()) {
      return null;
    }
    UUID key = partyKey(partyName);
    partyNames.put(key, partyName);
    return partyManager.today(key);
  }

  /** Tells the online members of a party that one of its quests, or all of them, were finished. */
  private static void onPartyEvent(QuestManager.QuestEvent event) {
    if (event.kind() == QuestManager.QuestEvent.Kind.PROGRESS) {
      return;
    }
    String partyName = partyNames.get(event.player());
    if (partyName == null || mc.carlton.freerpg.party.Parties.manager() == null) {
      return;
    }
    mc.carlton.freerpg.party.Party party = null;
    for (mc.carlton.freerpg.party.Party candidate
        : mc.carlton.freerpg.party.Parties.manager().all()) {
      if (candidate.name().equalsIgnoreCase(partyName)) {
        party = candidate;
        break;
      }
    }
    if (party == null) {
      return;
    }
    Random random = new Random();
    int multiplier = QuestConfig.partyRewardMultiplier();
    for (mc.carlton.freerpg.party.PartyMember member : party.members()) {
      Player p = Bukkit.getPlayer(member.uuid());
      if (p == null) {
        continue;
      }
      if (event.kind() == QuestManager.QuestEvent.Kind.QUEST_DONE) {
        mc.carlton.freerpg.party.PartyMessages.send(p, "partyQuestDone", "quest",
            event.quest().name());
      } else {
        WheelPrize prize = WheelRoller.rollRing(random, PrizeGiver.enchantChoices(),
            PrizeGiver.skillNames(), QuestConfig.weightBook(), QuestConfig.weightMinecraftExp(),
            QuestConfig.weightFrpgExp(),
            WheelMenu.scaled(QuestConfig.minecraftExpRange(), multiplier),
            WheelMenu.scaled(QuestConfig.frpgExpRange(), multiplier), 1).get(0);
        try {
          PrizeGiver.give(p, prize);
          mc.carlton.freerpg.party.PartyMessages.send(p, "partyQuestAllDone", "prize",
              PrizeGiver.describe(p, prize));
        } catch (RuntimeException e) {
          plugin.getLogger().severe("Could not give party quest prize " + prize + " to "
              + p.getName() + ": " + e.getMessage());
          p.giveExp(500);
        }
        if (QuestConfig.effectsEnabled()) {
          p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        }
        QuestEffects.allDone(p, QuestPeriod.DAILY);
      }
    }
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
    recordAll(p.getUniqueId(), QuestType.BREAK_BLOCKS,
        QuestTags.forBlock(block.getType().name(), ripe), 1);
  }

  /** A player earned skill exp (base amount, before multipliers). */
  public static void onSkillExp(Player p, String skillTag, int baseExp) {
    if (!counts(p)) {
      return;
    }
    recordAll(p.getUniqueId(), QuestType.EARN_SKILL_EXP, QuestTags.forSkill(skillTag),
        baseExp);
  }

  static void record(Player p, QuestType type, Set<String> tags, int amount) {
    if (counts(p)) {
      recordAll(p.getUniqueId(), type, tags, amount);
    }
  }

  // ------------------------------------------------------------------ telling the player

  private static void onEvent(QuestPeriod period, QuestManager.QuestEvent event) {
    Player p = Bukkit.getPlayer(event.player());
    if (p == null) {
      return;
    }
    ActiveQuest quest = event.quest();
    switch (event.kind()) {
      case PROGRESS -> {
        if (period != QuestPeriod.DAILY) {
          return; // the action bar only follows today's quests, weekly ones would never stop
        }
        long now = System.currentTimeMillis();
        Long last = lastActionBar.get(p.getUniqueId());
        if (last == null || now - last >= ACTION_BAR_GAP_MILLIS) {
          lastActionBar.put(p.getUniqueId(), now);
          QuestEffects.progress(p, quest.progress(), quest.target());
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
        QuestEffects.questDone(p);
      }
      case ALL_DONE -> {
        String key = switch (period) {
          case DAILY -> "questAllDone";
          case WEEKLY -> "questAllDoneWeekly";
          case SEASONAL -> "questAllDoneSeasonal";
        };
        TextComponent line = new TextComponent(QuestMessages.prefixed(p, key));
        line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
            period == QuestPeriod.DAILY ? "/quests" : "/quests " + period.id()));
        p.spigot().sendMessage(line);
        if (QuestConfig.effectsEnabled()) {
          p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        }
        QuestEffects.allDone(p, period);
      }
    }
  }
}
