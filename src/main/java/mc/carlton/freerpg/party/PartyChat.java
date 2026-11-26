package mc.carlton.freerpg.party;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;

/** Chat messages with clickable [Accept] and [Deny] buttons. */
public final class PartyChat {

  private PartyChat() {
  }

  public static void sendInvite(Player target, String inviter, String party) {
    PartyMessages.send(target, "partyInviteReceived", "player", inviter, "party", party);
    target.spigot().sendMessage(buttons(target, "/party accept " + party, "/party deny " + party));
    PartyEffects.invited(target);
  }

  public static void sendTpaRequest(Player target, String requester) {
    PartyMessages.send(target, "partyTpaReceived", "player", requester);
    target.spigot().sendMessage(buttons(target, "/party tpaccept", "/party tpdeny"));
    PartyEffects.requested(target);
  }

  private static BaseComponent buttons(Player p, String acceptCommand, String denyCommand) {
    TextComponent line = new TextComponent("");
    line.addExtra(button(PartyMessages.text(p, "partyButtonAccept"), acceptCommand,
        PartyMessages.text(p, "partyButtonAcceptHover")));
    line.addExtra(new TextComponent("  "));
    line.addExtra(button(PartyMessages.text(p, "partyButtonDeny"), denyCommand,
        PartyMessages.text(p, "partyButtonDenyHover")));
    return line;
  }

  private static BaseComponent button(String label, String command, String hover) {
    TextComponent button = new TextComponent("");
    for (BaseComponent part : TextComponent.fromLegacyText(label)) {
      button.addExtra(part);
    }
    button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
    button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hover)));
    return button;
  }
}
