package io.loyloy.fe.command.commands;

import io.loyloy.fe.Fe;
import io.loyloy.fe.Phrase;
import io.loyloy.fe.command.CommandType;
import io.loyloy.fe.command.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CleanCommand extends SubCommand {
    private final Fe plugin;

    public CleanCommand(Fe plugin) {
        super("clean", "fe.clean", "clean", Phrase.COMMAND_CLEAN, CommandType.CONSOLE);
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        plugin.getAPI().clean();
        Phrase.ACCOUNT_CLEANED.sendWithPrefix(sender);
        return true;
    }
}
