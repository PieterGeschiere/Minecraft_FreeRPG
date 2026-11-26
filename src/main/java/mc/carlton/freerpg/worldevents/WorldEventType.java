package mc.carlton.freerpg.worldevents;

import java.util.Locale;
import java.util.Optional;
import org.bukkit.boss.BarColor;

/**
 * The world events. The declaration order is the order in which their daily rolls are made.
 * Text keys are written out in full so the language-key tests can see them.
 */
public enum WorldEventType {
  BLOOD_MOON("bloodMoon", "blood_moon", Scope.WORLD, BarColor.RED,
      "bloodMoonTitle", "bloodMoonSubtitle", "bloodMoonBossBar", "bloodMoonEnded",
      "bloodMoonInfo"),
  BOUNTIFUL_HARVEST("bountifulHarvest", "bountiful_harvest", Scope.WORLD, BarColor.GREEN,
      "worldEventHarvestTitle", "worldEventHarvestSubtitle", "worldEventHarvestBossBar",
      "worldEventHarvestEnded", "worldEventHarvestInfo"),
  GOLD_RUSH("goldRush", "gold_rush", Scope.WORLD, BarColor.YELLOW,
      "worldEventGoldRushTitle", "worldEventGoldRushSubtitle", "worldEventGoldRushBossBar",
      "worldEventGoldRushEnded", "worldEventGoldRushInfo"),
  ECLIPSE("eclipse", "eclipse", Scope.WORLD, BarColor.PURPLE,
      "worldEventEclipseTitle", "worldEventEclipseSubtitle", "worldEventEclipseBossBar",
      "worldEventEclipseEnded", "worldEventEclipseInfo"),
  FISHING_FESTIVAL("fishingFestival", "fishing_festival", Scope.SERVER, BarColor.BLUE,
      "worldEventFestivalTitle", "worldEventFestivalSubtitle", "worldEventFestivalBossBar",
      "worldEventFestivalEnded", "worldEventFestivalInfo"),
  LUMBERJACKS_DAY("lumberjacksDay", "lumberjacks_day", Scope.WORLD, BarColor.GREEN,
      "worldEventLumberjackTitle", "worldEventLumberjackSubtitle", "worldEventLumberjackBossBar",
      "worldEventLumberjackEnded", "worldEventLumberjackInfo"),
  FORGE_FEVER("forgeFever", "forge_fever", Scope.WORLD, BarColor.PINK,
      "worldEventForgeTitle", "worldEventForgeSubtitle", "worldEventForgeBossBar",
      "worldEventForgeEnded", "worldEventForgeInfo"),
  SCHOLARS_MOON("scholarsMoon", "scholars_moon", Scope.WORLD, BarColor.WHITE,
      "worldEventScholarTitle", "worldEventScholarSubtitle", "worldEventScholarBossBar",
      "worldEventScholarEnded", "worldEventScholarInfo"),
  ADVENTURERS_WIND("adventurersWind", "adventurers_wind", Scope.WORLD, BarColor.BLUE,
      "worldEventWindTitle", "worldEventWindSubtitle", "worldEventWindBossBar",
      "worldEventWindEnded", "worldEventWindInfo"),
  TITAN_NIGHT("titanNight", "titan_night", Scope.WORLD, BarColor.YELLOW,
      "worldEventTitanTitle", "worldEventTitanSubtitle", "worldEventTitanBossBar",
      "worldEventTitanEnded", "worldEventTitanInfo"),
  MUTATOR_NIGHT("mutatorNight", "mutator_night", Scope.WORLD, BarColor.PINK,
      "worldEventMutatorTitle", "worldEventMutatorSubtitle", "worldEventMutatorBossBar",
      "worldEventMutatorEnded", "worldEventMutatorInfo");

  /** Whether an event happens in one world or everywhere on the server. */
  public enum Scope { WORLD, SERVER }

  private final String configKey;
  private final String commandName;
  private final Scope scope;
  private final BarColor barColor;
  private final String titleKey;
  private final String subtitleKey;
  private final String barKey;
  private final String endedKey;
  private final String infoKey;

  WorldEventType(String configKey, String commandName, Scope scope, BarColor barColor,
      String titleKey, String subtitleKey, String barKey, String endedKey, String infoKey) {
    this.configKey = configKey;
    this.commandName = commandName;
    this.scope = scope;
    this.barColor = barColor;
    this.titleKey = titleKey;
    this.subtitleKey = subtitleKey;
    this.barKey = barKey;
    this.endedKey = endedKey;
    this.infoKey = infoKey;
  }

  public String configKey() {
    return configKey;
  }

  public String commandName() {
    return commandName;
  }

  public Scope scope() {
    return scope;
  }

  public BarColor barColor() {
    return barColor;
  }

  public String titleKey() {
    return titleKey;
  }

  public String subtitleKey() {
    return subtitleKey;
  }

  public String barKey() {
    return barKey;
  }

  public String endedKey() {
    return endedKey;
  }

  /** The chat text that says what the event does. */
  public String infoKey() {
    return infoKey;
  }

  public static Optional<WorldEventType> fromCommandName(String name) {
    if (name == null) {
      return Optional.empty();
    }
    String wanted = name.toLowerCase(Locale.ROOT);
    for (WorldEventType type : values()) {
      if (type.commandName.equals(wanted)) {
        return Optional.of(type);
      }
    }
    return Optional.empty();
  }
}
