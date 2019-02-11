package top.sunbread.MCBingo.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import top.sunbread.MCBingo.MCBingo;
import top.sunbread.MCBingo.commands.play.JoinCommand;
import top.sunbread.MCBingo.commands.play.LeaveCommand;
import top.sunbread.MCBingo.commands.play.ReadyCommand;
import top.sunbread.MCBingo.util.Utils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BingoCommandExecuter implements CommandExecutor {

    private MCBingo plugin;
    private Map<String, SubCommand> subCommandMap;

    public BingoCommandExecuter(MCBingo plugin) {
        this.plugin = plugin;
        this.subCommandMap = new LinkedHashMap<>();
        registerSubCommand(JoinCommand.class);
        registerSubCommand(LeaveCommand.class);
        registerSubCommand(ReadyCommand.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            reload(sender);
            return true;
        }
        if (!plugin.isLoaded()) {
            sender.sendMessage(Utils.getText("COMMAND_NOT_LOADED"));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Utils.getText("COMMAND_HELP_HINT"));
            return true;
        }
        if (args[0].equalsIgnoreCase("help") || args[0].equals("?")) {
            help(sender);
            return true;
        }
        if (!subCommandMap.containsKey(caseStandardize(args[0]))) {
            sender.sendMessage(Utils.getText("COMMAND_NOT_FOUND"));
            sender.sendMessage(Utils.getText("COMMAND_HELP_HINT"));
            return true;
        }
        String subCmdName = args[0];
        String[] subCmdArgs = Arrays.copyOfRange(args, 1, args.length);
        SubCommand subCmd = subCommandMap.get(caseStandardize(subCmdName));
        executeSubCommand(sender, subCmd, subCmdArgs);
        return true;
    }

    private void executeSubCommand(CommandSender sender, SubCommand cmd, String[] args) {
        SubCommandInfo info = cmd.getClass().getAnnotation(SubCommandInfo.class);
        if (!sender.hasPermission(info.permission())) {
            sender.sendMessage(Utils.getText("COMMAND_NO_PERMISSION"));
            return;
        }
        cmd.execute(plugin, sender, args);
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("mcbingo.reload")) {
            sender.sendMessage(Utils.getText("COMMAND_NO_PERMISSION"));
            return;
        }
        if (plugin.softReload())
            sender.sendMessage(Utils.getText("COMMAND_RELOAD_COMPLETE"));
        else
            sender.sendMessage(Utils.getText("COMMAND_RELOAD_FAIL"));
    }

    private void help(CommandSender sender) {
        sender.sendMessage(Utils.getText("COMMAND_HELP_HEADER"));
        sender.sendMessage(Utils.getText("COMMAND_HELP_USAGE"));
        for (SubCommand subCmd : subCommandMap.values()) {
            SubCommandInfo info = subCmd.getClass().getAnnotation(SubCommandInfo.class);
            if (sender.hasPermission(info.permission()))
                sender.sendMessage(Utils.getText(info.usageTextKey()));
        }
        if (sender.hasPermission("mcbingo.reload"))
            sender.sendMessage(Utils.getText("COMMAND_RELOAD_USAGE"));
    }

    private void registerSubCommand(Class<? extends SubCommand> subCmdClass) {
        SubCommandInfo info = subCmdClass.getAnnotation(SubCommandInfo.class);
        if (info == null) return;
        try {
            subCommandMap.put(caseStandardize(info.name()), subCmdClass.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String caseStandardize(String s) {
        return s.toLowerCase();
    }

}
