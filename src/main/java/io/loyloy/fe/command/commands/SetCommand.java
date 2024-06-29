package io.loyloy.fe.command.commands;

import io.loyloy.fe.Fe;
import io.loyloy.fe.Phrase;
import io.loyloy.fe.command.CommandType;
import io.loyloy.fe.command.SubCommand;
import io.loyloy.fe.database.Account;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetCommand extends SubCommand {
    private final Fe plugin;

    public SetCommand(Fe plugin) {
        super("set", "fe.set", "set [name] [amount]", Phrase.COMMAND_SET, CommandType.CONSOLE);
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length < 2) {
            return false;
        }
        double money;
        try {
            money = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            return false;
        }
        Account victim = plugin.getShortenedAccount(args[0]);
        if (victim == null) {
            Phrase.ACCOUNT_DOES_NOT_EXIST.sendWithPrefix(sender);
            return true;
        }
        if (!victim.canReceive(money)) {
            Phrase.MAX_BALANCE_REACHED.sendWithPrefix(sender, victim.getName());
            return true;
        }
        String formattedMoney = plugin.getAPI().format(money);
        victim.setMoney(money);
        Phrase.PLAYER_SET_MONEY.sendWithPrefix(sender, victim.getName(), formattedMoney);
        return true;
    }
}
