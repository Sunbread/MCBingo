package top.sunbread.MCBingo.commands;

import org.bukkit.command.CommandSender;
import top.sunbread.MCBingo.MCBingo;

public interface SubCommand {

    void execute(MCBingo plugin, CommandSender sender, String[] args);

}
