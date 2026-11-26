package mc.carlton.freerpg.quests;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Turns what happened in the game into tags that quest filters are matched against. */
public final class QuestTags {

  private QuestTags() {
  }

  public static Set<String> forBlock(String materialName, boolean ripeCrop) {
    String name = materialName.toUpperCase(Locale.ROOT);
    Set<String> tags = new HashSet<>();
    tags.add("ANY");
    tags.add(name); // a filter can also be one exact block, like DIAMOND_ORE
    if (name.endsWith("_ORE") || name.equals("ANCIENT_DEBRIS")) {
      tags.add("ORES");
    }
    if (name.endsWith("_LOG") || name.endsWith("_STEM") || name.endsWith("_WOOD")
        || name.endsWith("_HYPHAE")) {
      tags.add("LOGS");
    }
    if (ripeCrop) {
      tags.add("RIPE_CROPS");
    }
    return tags;
  }

  public static Set<String> forMob(String entityTypeName, boolean hostile) {
    Set<String> tags = new HashSet<>();
    tags.add("ANY");
    tags.add(entityTypeName.toUpperCase(Locale.ROOT));
    if (hostile) {
      tags.add("HOSTILE");
    }
    return tags;
  }

  public static Set<String> forSkill(String skillName) {
    Set<String> tags = new HashSet<>();
    tags.add("ANY");
    tags.add(skillName.toUpperCase(Locale.ROOT));
    return tags;
  }
}
