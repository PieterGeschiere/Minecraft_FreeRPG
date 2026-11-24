package mc.carlton.freerpg.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import mc.carlton.freerpg.commands.sub.CommandRegistry;
import mc.carlton.freerpg.commands.sub.SubCommand;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

class CommandRegistryTest {

  private static SubCommand cmd(String... names) {
    return new SubCommand() {
      @Override
      public List<String> names() {
        return Arrays.asList(names);
      }

      @Override
      public boolean execute(CommandSender sender, String[] args) {
        return true;
      }
    };
  }

  @Test
  void lookupIsCaseInsensitive() {
    CommandRegistry r = new CommandRegistry();
    SubCommand c = cmd("giveEXP");
    r.register(c);
    assertSame(c, r.find("GIVEexp").get());
  }

  @Test
  void aliasesResolveToSameCommand() {
    CommandRegistry r = new CommandRegistry();
    SubCommand c = cmd("giveEXP", "expGive");
    r.register(c);
    assertSame(r.find("giveEXP").get(), r.find("EXPGIVE").get());
  }

  @Test
  void duplicateAliasRejectedCaseInsensitively() {
    CommandRegistry r = new CommandRegistry();
    r.register(cmd("a", "b"));
    assertThrows(IllegalArgumentException.class, () -> r.register(cmd("c", "B")));
  }

  @Test
  void unknownReturnsEmpty() {
    assertTrue(new CommandRegistry().find("nope").isEmpty());
  }

  @Test
  void allNamesAreLowerCase() {
    CommandRegistry r = new CommandRegistry();
    r.register(cmd("GiveEXP", "expGive"));
    assertEquals(Set.of("giveexp", "expgive"), r.allNames());
  }
}
