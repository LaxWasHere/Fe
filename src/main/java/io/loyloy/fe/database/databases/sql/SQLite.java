package io.loyloy.fe.database.databases.sql;

import io.loyloy.fe.Fe;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

public class SQLite extends SQLFunctions {
    private final Fe plugin;

    public SQLite(Fe plugin) {
        super(plugin);

        this.plugin = plugin;
    }

    public Connection getNewConnection() {
        try {
            Class.forName("org.sqlite.JDBC");

            return DriverManager.getConnection("jdbc:sqlite:" + new File(plugin.getDataFolder(), "database.db").getAbsolutePath());
        } catch (Exception e) {
            return null;
        }
    }

    public void getConfigDefaults(ConfigurationSection section) {

    }

    public String getName() {
        return "SQLite";
    }
}
