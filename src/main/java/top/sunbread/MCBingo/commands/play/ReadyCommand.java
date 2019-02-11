package top.sunbread.MCBingo.commands.play;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.sunbread.MCBingo.MCBingo;
import top.sunbread.MCBingo.commands.SubCommand;
import top.sunbread.MCBingo.commands.SubCommandInfo;
import top.sunbread.MCBingo.lobby.BingoLobby;
import top.sunbread.MCBingo.util.Utils;

@SubCommandInfo(
        name = "ready",
        permission = "mcbingo.play",
        usageTextKey = "COMMAND_READY_USAGE"
)
public final class ReadyCommand implements SubCommand {

    @Override
    public void execute(MCBingo plugin, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.getText("COMMAND_NOT_PLAYER"));
            return;
        }
        BingoLobby lobby = plugin.getLobby();
        Player player = (Player) sender;
        if (!lobby.isInLobby(player)) {
            sender.sendMessage(Utils.getText("COMMAND_READY_NOT_IN_LOBBY"));
            return;
        }
        lobby.setReady(player, !lobby.isReady(player));
    }

}
