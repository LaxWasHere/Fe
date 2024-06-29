package io.loyloy.fe;

import io.loyloy.fe.command.FeCommandCompleter;
import io.loyloy.fe.database.Account;
import io.loyloy.fe.database.Database;
import io.loyloy.fe.database.databases.sql.MySQL;
import io.loyloy.fe.database.databases.sql.SQLite;
import io.loyloy.fe.listeners.FePlayerListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

// Java Imports

public class Fe extends JavaPlugin {
    private final Set<Database> databases;
    private API api;
    private Database database;

    public Fe() {
        databases = new HashSet<>();
    }

    public void onEnable() {
        Phrase.init(this);

        databases.add(new MySQL(this));
        databases.add(new SQLite(this));

        for (Database database : databases) {
            String name = database.getConfigName();

            ConfigurationSection section = getConfig().getConfigurationSection(name);

            if (section == null) {
                section = getConfig().createSection(name);
            }

            database.getConfigDefaults(section);

            if (section.getKeys(false).isEmpty()) {
                getConfig().set(name, null);
            }
        }

        getConfig().options().copyDefaults(true);

        getConfig().options().header("Fe Config - nonit.me\n" +
                "holdings - The amount of money that players will start out with\n" +
                "prefix - The message prefix\n" +
                "currency - The single and multiple names for the currency\n" +
                "type - The type of database used (sqlite, mysql, or mongo)");

        saveConfig();

        api = new API(this);

        if (!setupDatabase()) {
            return;
        }

        FeCommand feCommand = new FeCommand(this);
        getCommand("fe").setExecutor(feCommand);
        getCommand("fe").setTabCompleter(new FeCommandCompleter(this, feCommand));

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new FePlayerListener(this), this);

        setupVault();

        reloadConfig();

        // Auto Clean On Startup
        if (api.isAutoClean()) {
            api.clean();
            log(Phrase.ACCOUNT_CLEANED);
        }
    }

    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);

        getFeDatabase().close();
    }

    public void log(String message) {
        getLogger().info(message);
    }

    public void log(Phrase phrase, String... args) {
        log(phrase.parse(args));
    }

    public Database getFeDatabase() {
        return database;
    }

    public boolean addDatabase(Database database) {
        return databases.add(database);
    }

    public Set<Database> getDatabases() {
        return new HashSet<>(databases);
    }

    public API getAPI() {
        return api;
    }

    private boolean setupDatabase() {
        String type = getConfig().getString("type");

        database = null;

        for (Database database : databases) {
            if (type.equalsIgnoreCase(database.getConfigName())) {
                this.database = database;
                break;
            }
        }

        if (database == null) {
            log(Phrase.DATABASE_TYPE_DOES_NOT_EXIST);
            return false;
        }

        if (!database.init()) {
            log(Phrase.DATABASE_FAILURE_DISABLE);
            setEnabled(false);
            return false;
        }
        return true;
    }

    private void setupPhrases() {
        File phrasesFile = new File(getDataFolder(), "phrases.yml");
        for (Phrase phrase : Phrase.values()) {
            phrase.reset();
        }
        if (!phrasesFile.exists()) {
            phrasesFile.getParentFile().mkdirs();
            saveResource("phrases.yml", false);
        }
        YamlConfiguration phrasesConfig = YamlConfiguration.loadConfiguration(phrasesFile);
        for (Phrase phrase : Phrase.values()) {
            String phraseConfigName = phrase.getConfigName();
            String phraseMessage = phrasesConfig.getString(phraseConfigName);
            if (phraseMessage == null) {
                Bukkit.getLogger().warning("[Fe] No message found for \"" + phraseConfigName + "\", using default value!");
                phraseMessage = phrase.parse();
            }
            phrase.setMessage(phraseMessage);
        }
    }

    public void reloadConfig() {
        super.reloadConfig();
        String oldCurrencySingle = getConfig().getString("currency.single");
        String oldCurrencyMultiple = getConfig().getString("currency.multiple");
        if (oldCurrencySingle != null) {
            getConfig().set("currency.major.single", oldCurrencySingle);
            getConfig().set("currency.single", null);
        }
        if (oldCurrencyMultiple != null) {
            getConfig().set("currency.major.multiple", oldCurrencyMultiple);
            getConfig().set("currency.multiple", null);
        }
        if (!getConfig().isSet("autoclean")) {
            getConfig().set("autoclean", true);
        }

        // Temporarily remove cache and updates.
        if (getConfig().isSet("cacheaccounts")) {
            getConfig().set("cacheaccounts", null);
        }
        if (getConfig().getBoolean("updatecheck")) {
            getConfig().set("updatecheck", null);
        }
        setupPhrases();
        saveConfig();
    }

    public Account getShortenedAccount(String name) {
        Account account = getAPI().getAccount(name, null);
        if (account == null) {
            Player player = getServer().getPlayer(name);
            if (player != null) {
                account = getAPI().getAccount(player.getName(), null);
            }
        }
        return account;
    }

    public String getMessagePrefix() {
        String third = Phrase.TERTIARY_COLOR.parse();
        return third + "[" + Phrase.PRIMARY_COLOR.parse() + "$1" + third + "] " + Phrase.SECONDARY_COLOR.parse();
    }

    public String getEqualMessage(String inBetween, int length) {
        return getEqualMessage(inBetween, length, length);
    }

    public String getEqualMessage(String inBetween, int length, int length2) {
        String equals = getEndEqualMessage(length);
        String end = getEndEqualMessage(length2);
        String third = Phrase.TERTIARY_COLOR.parse();
        return equals + third + "[" + Phrase.PRIMARY_COLOR.parse() + inBetween + third + "]" + end;
    }

    public String getEndEqualMessage(int length) {
        StringBuilder message = new StringBuilder(Phrase.SECONDARY_COLOR.parse() + "");
        for (int i = 0; i < length; i++) {
            message.append('=');
        }
        return message.toString();
    }

    private void setupVault() {
        Plugin vault = getServer().getPluginManager().getPlugin("Vault");
        if (vault == null) {
            return;
        }

        getServer().getServicesManager().register(Economy.class, new VaultHandler(this), this, ServicePriority.Highest);
    }
}
