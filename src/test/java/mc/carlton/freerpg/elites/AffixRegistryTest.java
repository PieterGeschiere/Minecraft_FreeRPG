package mc.carlton.freerpg.elites;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;

class AffixRegistryTest {

  private static Affix fake(String id, EntityType only) {
    return new Affix() {
      @Override
      public String id() {
        return id;
      }

      @Override
      public boolean canApplyTo(EntityType type) {
        return only == null || only == type;
      }
    };
  }

  @Test
  void registeredAffixIsFoundAndUnknownIsNull() {
    Affix affix = fake("testAffixA", null);
    AffixRegistry.register(affix);
    assertSame(affix, AffixRegistry.get("testAffixA"));
    assertNull(AffixRegistry.get("doesNotExist"));
  }

  @Test
  void idsForOnlyListsAffixesThatCanApply() {
    AffixRegistry.register(fake("testOnlySpider", EntityType.SPIDER));
    assertTrue(AffixRegistry.idsFor(EntityType.SPIDER).contains("testOnlySpider"));
    assertFalse(AffixRegistry.idsFor(EntityType.ZOMBIE).contains("testOnlySpider"));
  }
}
