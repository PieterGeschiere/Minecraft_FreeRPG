package mc.carlton.freerpg.elites;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import mc.carlton.freerpg.FreeRPG;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/** /frpg elites reload | spawn &lt;mob&gt; &lt;affix[,affix]&gt; */
public final class EliteAdminCommands {

  private EliteAdminCommands() {
  }

  /** Handles {@code /frpg elites ...}; args[0] is "elites". */
  public static boolean handle(CommandSender sender, String[] args) {
    Player player = sender instanceof Player p ? p : null;
    if (!sender.hasPermission("freerpg.elites.admin")) {
      sender.sendMessage(EliteMessages.prefixed(player, "eliteNoPermission"));
      return true;
    }
    if (args.length < 2) {
      sender.sendMessage(EliteMessages.prefixed(player, "eliteUsage"));
      return true;
    }
    switch (args[1].toLowerCase(Locale.ROOT)) {
      case "reload" -> {
        Elites.reload(FreeRPG.getPlugin(FreeRPG.class));
        sender.sendMessage(EliteMessages.prefixed(player, "eliteReloaded"));
      }
      case "spawn" -> spawn(sender, player, args);
      default -> sender.sendMessage(EliteMessages.prefixed(player, "eliteUsage"));
    }
    return true;
  }

  private static void spawn(CommandSender sender, Player player, String[] args) {
    if (player == null) {
      sender.sendMessage(EliteMessages.prefixed(null, "eliteMustBePlayer"));
      return;
    }
    if (args.length < 4) {
      player.sendMessage(EliteMessages.prefixed(player, "eliteUsage"));
      return;
    }
    EntityType type = parseType(args[2]);
    if (type == null) {
      player.sendMessage(EliteMessages.prefixed(player, "eliteUnknownType", "type", args[2]));
      return;
    }
    List<String> affixes = new ArrayList<>();
    for (String id : EliteData.decode(args[3])) {
      if (AffixRegistry.get(id) == null) {
        player.sendMessage(EliteMessages.prefixed(player, "eliteUnknownAffix", "affix", id,
            "list", String.join(", ", AffixRegistry.allIds())));
        return;
      }
      affixes.add(id);
    }
    Location where = player.getLocation().add(player.getLocation().getDirection().setY(0)
        .normalize().multiply(3));
    Entity spawned = player.getWorld().spawnEntity(where, type);
    if (spawned instanceof LivingEntity mob) {
      EliteManager.makeElite(mob, affixes);
      player.sendMessage(EliteMessages.prefixed(player, "eliteSpawned", "type",
          EliteManager.displayName(type), "affixes", String.join(", ", affixes)));
    } else {
      spawned.remove();
      player.sendMessage(EliteMessages.prefixed(player, "eliteUnknownType", "type", args[2]));
    }
  }

  /** A living mob type from its name (any case), or null. */
  static EntityType parseType(String name) {
    try {
      EntityType type = EntityType.valueOf(name.toUpperCase(Locale.ROOT));
      Class<? extends Entity> entityClass = type.getEntityClass();
      return entityClass != null && LivingEntity.class.isAssignableFrom(entityClass)
          && type != EntityType.PLAYER ? type : null;
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /** Mob names for tab completion. */
  public static List<String> mobNames() {
    List<String> names = new ArrayList<>();
    for (EntityType type : EntityType.values()) {
      if (parseType(type.name()) != null) {
        names.add(type.name().toLowerCase(Locale.ROOT));
      }
    }
    return names;
  }
}
