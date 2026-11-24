package mc.carlton.freerpg.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import mc.carlton.freerpg.commands.sub.CommandSupport;
import org.junit.jupiter.api.Test;

class CommandSupportTest {

  @Test
  void integerParsing() {
    assertEquals(12, CommandSupport.getArgumentAsInteger("12"));
    assertEquals(Integer.MAX_VALUE, CommandSupport.getArgumentAsInteger("x"));
  }

  @Test
  void doubleParsing() {
    assertEquals(1.5, CommandSupport.getArgumentAsDouble("1.5"));
    assertEquals(Double.MAX_VALUE, CommandSupport.getArgumentAsDouble("x"));
  }
}
