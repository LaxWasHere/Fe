package io.loyloy.fe.command.commands;

import io.loyloy.fe.Fe;
import io.loyloy.fe.Phrase;
import io.loyloy.fe.command.CommandType;
import io.loyloy.fe.command.SubCommand;
import io.loyloy.fe.database.Account;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeductCommand extends SubCommand {
    private final Fe plugin;

    public DeductCommand(Fe plugin) {
        super("deduct", "fe.deduct", "deduct [name] [amount]", Phrase.COMMAND_DEDUCT, CommandType.CONSOLE);
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
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
        String formattedMoney = plugin.getAPI().format(money);
        victim.withdraw(money);
        Phrase.PLAYER_DEDUCT_MONEY.sendWithPrefix(sender, formattedMoney, victim.getName());
        Player receiverPlayer = plugin.getServer().getPlayerExact(victim.getName());
        if (receiverPlayer != null) {
            Phrase.PLAYER_DEDUCTED_MONEY.sendWithPrefix(receiverPlayer, formattedMoney, sender.getName());
        }
        return true;
    }
}	
