package io.loyloy.fe.command.commands;

import io.loyloy.fe.Fe;
import io.loyloy.fe.Phrase;
import io.loyloy.fe.command.CommandType;
import io.loyloy.fe.command.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends SubCommand {
    private final Fe plugin;

    public ReloadCommand(Fe plugin) {
        super("reload", "fe.reload", "reload", Phrase.COMMAND_RELOAD, CommandType.CONSOLE);
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        plugin.reloadConfig();
        Phrase.CONFIG_RELOADED.sendWithPrefix(sender);
        return true;
    }
}
