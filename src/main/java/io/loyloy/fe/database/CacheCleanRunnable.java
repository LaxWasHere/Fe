package io.loyloy.fe.database;

import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;

public class CacheCleanRunnable extends BukkitRunnable {
    private final Database database;

    private static final long CACHE_TIME = 1800; // 30 mins

    CacheCleanRunnable(Database database) {
        this.database = database;
    }

    @Override
    public void run() {
        final long cacheTime = Instant.now().getEpochSecond() - CACHE_TIME;
        for (Account account : database.getCachedAccounts()) {
            if (account.getLastAccess() < cacheTime) {
                database.removeCachedAccount(account);
            }
        }
    }
}
