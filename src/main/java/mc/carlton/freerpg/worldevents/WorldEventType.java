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
      "bloodMoonTitle", "bloodMoonSubtitle", "bloodMoonBossBar", "bloodMoonEnded"),
  BOUNTIFUL_HARVEST("bountifulHarvest", "bountiful_harvest", Scope.WORLD, BarColor.GREEN,
      "worldEventHarvestTitle", "worldEventHarvestSubtitle", "worldEventHarvestBossBar",
      "worldEventHarvestEnded"),
  GOLD_RUSH("goldRush", "gold_rush", Scope.WORLD, BarColor.YELLOW,
      "worldEventGoldRushTitle", "worldEventGoldRushSubtitle", "worldEventGoldRushBossBar",
      "worldEventGoldRushEnded"),
  ECLIPSE("eclipse", "eclipse", Scope.WORLD, BarColor.PURPLE,
      "worldEventEclipseTitle", "worldEventEclipseSubtitle", "worldEventEclipseBossBar",
      "worldEventEclipseEnded"),
  FISHING_FESTIVAL("fishingFestival", "fishing_festival", Scope.SERVER, BarColor.BLUE,
      "worldEventFestivalTitle", "worldEventFestivalSubtitle", "worldEventFestivalBossBar",
      "worldEventFestivalEnded");

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

  WorldEventType(String configKey, String commandName, Scope scope, BarColor barColor,
      String titleKey, String subtitleKey, String barKey, String endedKey) {
    this.configKey = configKey;
    this.commandName = commandName;
    this.scope = scope;
    this.barColor = barColor;
    this.titleKey = titleKey;
    this.subtitleKey = subtitleKey;
    this.barKey = barKey;
    this.endedKey = endedKey;
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
