package io.loyloy.fe.command.commands;

import io.loyloy.fe.Fe;
import io.loyloy.fe.Phrase;
import io.loyloy.fe.command.CommandType;
import io.loyloy.fe.command.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class RemoveCommand extends SubCommand {
    private final Fe plugin;

    public RemoveCommand(Fe plugin) {
        super("remove", "fe.remove", "remove [name]", Phrase.COMMAND_REMOVE, CommandType.CONSOLE);
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length < 1) {
            return false;
        }
        String name = args[0];
        if (!plugin.getAPI().accountExists(name, null)) {
            Phrase.ACCOUNT_DOES_NOT_EXIST.sendWithPrefix(sender);
            return true;
        }

        plugin.getAPI().removeAccount(name, null);
        Phrase.ACCOUNT_REMOVED.sendWithPrefix(sender, Phrase.PRIMARY_COLOR.parse() + name + Phrase.SECONDARY_COLOR.parse());
        return true;
    }
}
