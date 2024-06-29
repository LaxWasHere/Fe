package io.loyloy.fe.listeners;

import io.loyloy.fe.Fe;
import io.loyloy.fe.database.Account;
import io.loyloy.fe.database.Database;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class FePlayerListener implements Listener {
    private final Fe plugin;

    public FePlayerListener(Fe plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Player player = event.getPlayer();
            plugin.getAPI().updateAccount(player.getName(), player.getUniqueId().toString());
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
        {
            Database database = plugin.getFeDatabase();
            Player player = event.getPlayer();
            Account account = database.getCachedAccount(player.getName(), player.getUniqueId().toString());
            if (account != null) {
                account.save(account.getMoney());
                database.removeCachedAccount(account);
            }
        });
    }
}
