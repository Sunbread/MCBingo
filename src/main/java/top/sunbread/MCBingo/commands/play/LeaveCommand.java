package top.sunbread.MCBingo.commands.play;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.sunbread.MCBingo.MCBingo;
import top.sunbread.MCBingo.commands.SubCommand;
import top.sunbread.MCBingo.commands.SubCommandInfo;
import top.sunbread.MCBingo.game.BingoPlayerManager;
import top.sunbread.MCBingo.lobby.BingoLobby;
import top.sunbread.MCBingo.util.Utils;

@SubCommandInfo(
        name = "leave",
        permission = "mcbingo.play",
        usageTextKey = "COMMAND_LEAVE_USAGE"
)
public final class LeaveCommand implements SubCommand {

    @Override
    public void execute(MCBingo plugin, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.getText("COMMAND_NOT_PLAYER"));
            return;
        }
        BingoPlayerManager manager = plugin.getPlayerManager();
        BingoLobby lobby = plugin.getLobby();
        Player player = (Player) sender;
        if (manager.isInGame(player)) {
            manager.leaveGame(player);
            return;
        }
        if (lobby.isInLobby(player)) {
            lobby.leaveLobby(player);
            return;
        }
        sender.sendMessage(Utils.getText("COMMAND_LEAVE_NOT_IN"));
    }

}
