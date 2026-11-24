package mc.carlton.freerpg.commands.sub;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class CommandRegistry {

  private final Map<String, SubCommand> byName = new HashMap<>();

  public void register(SubCommand command) {
    for (String name : command.names()) {
      if (byName.containsKey(name.toLowerCase(Locale.ROOT))) {
        throw new IllegalArgumentException("Duplicate sub-command name: " + name);
      }
    }
    for (String name : command.names()) {
      byName.put(name.toLowerCase(Locale.ROOT), command);
    }
  }

  public Optional<SubCommand> find(String name) {
    return Optional.ofNullable(byName.get(name.toLowerCase(Locale.ROOT)));
  }

  public Set<String> allNames() {
    return new TreeSet<>(byName.keySet());
  }
}
