package mc.carlton.freerpg.party;

import java.util.function.BiConsumer;
import mc.carlton.freerpg.core.info.player.ChangeStats;
import mc.carlton.freerpg.weaponskills.WeaponSkill;
import mc.carlton.freerpg.weaponskills.WeaponSkillManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * Bonus XP sharing. The earner keeps everything; every eligible party member gets a percentage of
 * the base XP of the action in the same skill, through the normal XP code of the receiver (their
 * own multipliers, level cap and XP boss bar). Shared XP is never shared again.
 */
public final class PartyXpShare {

  private PartyXpShare() {
  }

  /** Called when a player earns XP in a main skill (base amount, before multipliers). */
  public static void onMainSkillExp(Player earner, String skillName, int baseExp) {
    share(earner, baseExp, (member, amount) -> new ChangeStats(member).changeEXP(skillName,
        amount));
  }

  /** Called when a player earns XP in a weapon skill (base amount, before multipliers). */
  public static void onWeaponSkillExp(Player earner, WeaponSkill skill, int baseExp) {
    share(earner, baseExp, (member, amount) -> WeaponSkillManager.addExp(member, skill, amount));
  }

  private static void share(Player earner, int baseExp, BiConsumer<Player, Integer> grant) {
    if (XpShareGuard.isSharing() || !PartyConfig.xpShareEnabled() || Parties.manager() == null) {
      return;
    }
    Party party = Parties.manager().partyOf(earner.getUniqueId()).orElse(null);
    if (party == null || !party.xpShareEnabled()) {
      return;
    }
    int amount = XpShareMath.share(baseExp, PartyConfig.xpSharePercent());
    if (amount <= 0) {
      return;
    }
    long now = System.currentTimeMillis();
    double radius = PartyConfig.xpShareRadius();
    XpShareGuard.share(() -> {
      for (PartyMember member : party.members()) {
        if (member.uuid().equals(earner.getUniqueId())) {
          continue;
        }
        Player receiver = Bukkit.getPlayer(member.uuid());
        if (receiver == null) {
          continue;
        }
        boolean sameWorld = receiver.getWorld().equals(earner.getWorld());
        double distanceSquared = sameWorld
            ? receiver.getLocation().distanceSquared(earner.getLocation()) : Double.MAX_VALUE;
        boolean afk = PartyListener.isAfk(member.uuid(), now);
        boolean spectator = receiver.getGameMode() == GameMode.SPECTATOR;
        if (XpShareMath.eligible(party.xpShareEnabled(), member.receiveSharedXp(), sameWorld,
            distanceSquared, radius, true, afk, spectator)) {
          grant.accept(receiver, amount);
        }
      }
    });
  }
}
