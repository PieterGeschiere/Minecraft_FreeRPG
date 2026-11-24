package mc.carlton.freerpg.elites;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.EntityType;

/** All known affixes by id, in the order they were registered. */
public final class AffixRegistry {

  private static final Map<String, Affix> AFFIXES = new LinkedHashMap<>();

  private AffixRegistry() {
  }

  /** Registers the ten built-in affixes. */
  public static void registerDefaults() {
    register(new mc.carlton.freerpg.elites.affix.Volatile());
    register(new mc.carlton.freerpg.elites.affix.Vampiric());
    register(new mc.carlton.freerpg.elites.affix.Swift());
    register(new mc.carlton.freerpg.elites.affix.Armored());
    register(new mc.carlton.freerpg.elites.affix.Splitting());
    register(new mc.carlton.freerpg.elites.affix.Thorns());
    register(new mc.carlton.freerpg.elites.affix.Summoner());
    register(new mc.carlton.freerpg.elites.affix.Frostbound());
    register(new mc.carlton.freerpg.elites.affix.Burning());
    register(new mc.carlton.freerpg.elites.affix.Teleporter());
  }

  public static void register(Affix affix) {
    AFFIXES.put(affix.id(), affix);
  }

  /** The affix with this id, or null. */
  public static Affix get(String id) {
    return AFFIXES.get(id);
  }

  /** The ids of every affix that can be used on this type of mob. */
  public static List<String> idsFor(EntityType type) {
    List<String> ids = new ArrayList<>();
    for (Affix affix : AFFIXES.values()) {
      if (affix.canApplyTo(type)) {
        ids.add(affix.id());
      }
    }
    return ids;
  }

  /** Every registered id (for tab completion). */
  public static List<String> allIds() {
    return new ArrayList<>(AFFIXES.keySet());
  }
}
