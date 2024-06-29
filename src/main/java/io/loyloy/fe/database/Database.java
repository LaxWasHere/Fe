package io.loyloy.fe.database;

import io.loyloy.fe.Fe;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class Database {
    private final Fe plugin;
    private final Set<Account> cachedAccounts;
    private boolean cacheAccounts;

    public Database(Fe plugin) {
        this.plugin = plugin;
        this.cachedAccounts = new HashSet<>();
    }

    public boolean init() {
        this.cacheAccounts = plugin.getAPI().getCacheAccounts();

        if (cacheAccounts) {
            plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new CacheCleanRunnable(this), 36000L, 36000L); // 30 mins
        }

        return false;
    }

    //This is really weird and needs to be looked at
    public List<Account> getTopAccounts(int size) {
        List<Account> topAccounts = loadTopAccounts(size * 2);

        if (!cachedAccounts.isEmpty()) {
            for (Account account : cachedAccounts) {
                topAccounts.remove(account);
            }
            List<Account> cachedTopAccounts = new ArrayList<>(cachedAccounts);
            cachedTopAccounts.sort((account1, account2) -> (int) (account2.getMoney() - account1.getMoney()));
            if (cachedAccounts.size() > size) {
                cachedTopAccounts = cachedTopAccounts.subList(0, size);
            }
            topAccounts.addAll(cachedTopAccounts);
        }
        topAccounts.sort((account1, account2) -> (int) (account2.getMoney() - account1.getMoney()));

        if (topAccounts.size() > size) {
            topAccounts = topAccounts.subList(0, size);
        }
        return topAccounts;
    }

    public abstract List<Account> loadTopAccounts(int size);

    public abstract List<Account> getAccounts();

    public abstract HashMap<String, String> loadAccountData(String name, String uuid);

    protected abstract void saveAccount(String name, String uuid, double money);

    public void removeAccount(String name, String uuid) {
        Account account = getCachedAccount(name, uuid);
        if (account != null) {
            removeCachedAccount(account);
        }
    }

    public abstract void getConfigDefaults(ConfigurationSection section);

    public abstract void clean();

    public void removeAllAccounts() {
        cachedAccounts.clear();
    }

    public void close() {
        Iterator<Account> iterator = cachedAccounts.iterator();
        while (iterator.hasNext()) {
            Account account = iterator.next();
            account.save(account.getMoney());
            iterator.remove();
        }
    }

    public abstract String getName();

    public String getConfigName() {
        return getName().toLowerCase().replace(" ", "");
    }

    public ConfigurationSection getConfigSection() {
        return plugin.getConfig().getConfigurationSection(getConfigName());
    }

    public Account getAccount(String name, String uuid) {
        Account account = getCachedAccount(name, uuid);
        if (account != null) {
            return account;
        }
        HashMap<String, String> data = loadAccountData(name, uuid);
        String money_string = data.get("money");
        Double data_money;
        try {
            data_money = Double.parseDouble(money_string);
        } catch (Exception e) {
            data_money = null;
        }
        String data_name = data.get("name");
        if (data_money == null) {
            return null;
        } else {
            return createAndAddAccount(data_name, uuid, data_money);
        }
    }

    public Account updateAccount(String name, String uuid) {
        Account account = getAccount(name, uuid);
        if (account == null) {
            account = createAndAddAccount(name, uuid, plugin.getAPI().getDefaultHoldings());
        }
        if (!account.getName().equals(name)) {
            account.setName(name);
        }
        return account;
    }

    private Account createAndAddAccount(String name, String uuid, double money) {
        Account account = new Account(plugin, name, uuid, this);
        account.setMoney(money);
        if (cacheAccounts()) {
            Player player = plugin.getServer().getPlayerExact(name);
            if (player != null) {
                cachedAccounts.add(account);
            }
        }
        return account;
    }

    public boolean accountExists(String name, String uuid) {
        return getAccount(name, uuid) != null;
    }

    public boolean cacheAccounts() {
        return cacheAccounts;
    }

    public Account getCachedAccount(String name, String uuid) {
        for (Account account : cachedAccounts) {
            if (account.getName().equals(name)) {
                return account;
            }
        }
        return null;
    }

    public Set<Account> getCachedAccounts() {
        return cachedAccounts;
    }

    public boolean removeCachedAccount(Account account) {
        return cachedAccounts.remove(account);
    }

    public abstract int getVersion();

    public abstract void setVersion(int version);
}
