package io.loyloy.fe.database.databases.sql;

import io.loyloy.fe.Fe;
import io.loyloy.fe.database.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class SQLCore extends Database {
    private final Fe plugin;

    private Connection connection;

    public SQLCore(Fe plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    protected abstract Connection getNewConnection();

    public ArrayList<HashMap<String, String>> query(String sql, boolean hasReturn) {
        if (!checkConnection()) {
            plugin.getLogger().info("Error with database");
            return null;
        }

        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(sql);

            if (!hasReturn) {
                statement.execute();
                return null;
            }

            ResultSet set = statement.executeQuery();

            ResultSetMetaData md = set.getMetaData();
            int columns = md.getColumnCount();

            ArrayList<HashMap<String, String>> list = new ArrayList<>();

            while (set.next()) {
                HashMap<String, String> row = new HashMap<>(columns);
                for (int i = 1; i <= columns; ++i) {
                    row.put(md.getColumnName(i), nullSafeToString(set.getObject(i)));
                }
                list.add(row);
            }

            if (list.isEmpty()) {
                return null;
            }

            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean checkConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = getNewConnection();

                if (connection == null || connection.isClosed()) {
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }

    public static HashMap<String, String> getFirstRow(ArrayList<HashMap<String, String>> data) {
        if (data == null || data.size() < 1) {
            return null;
        }

        return data.get(0);
    }

    public static String getOnlyValue(ArrayList<HashMap<String, String>> data, String column) {
        if (data == null) {
            return null;
        }

        HashMap<String, String> rowOne = data.get(0);

        return rowOne.get(column);
    }

    public static String nullSafeToString(Object obj) {
        if (obj == null) {
            return null;
        }

        return obj.toString();
    }
}
