package io.loyloy.fe.database.databases.sql;

import io.loyloy.fe.Fe;
import io.loyloy.fe.database.Account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class SQLFunctions extends SQLCore {
    private final Fe plugin;

    private String accountsName = "fe_accounts";
    private String versionName = "fe_version";
    private String accountsColumnUser = "name";
    private String accountsColumnMoney = "money";
    private String accountsColumnUUID = "uuid";

    private static final String SYSTEM_NAME_MONEY = "money";
    private static final String SYSTEM_NAME_USER = "name";

    public SQLFunctions(Fe plugin) {
        super(plugin);

        this.plugin = plugin;
    }

    public void setAccountTable(String accountsName) {
        this.accountsName = accountsName;
    }

    public void setVersionTable(String versionName) {
        this.versionName = versionName;
    }

    public void setAccountsColumnUser(String accountsColumnUser) {
        this.accountsColumnUser = accountsColumnUser;
    }

    public void setAccountsColumnMoney(String accountsColumnMoney) {
        this.accountsColumnMoney = accountsColumnMoney;
    }

    public void setAccountsColumnUUID(String accountsColumnUUID) {
        this.accountsColumnUUID = accountsColumnUUID;
    }

    public boolean init() {
        super.init();
        updateTables();
        return checkConnection();
    }

    //+++ SQL Database Functions +++

    private void updateTables() {
        query("CREATE TABLE IF NOT EXISTS " + accountsName + " (" + accountsColumnUser + " varchar(64) NOT NULL, " + accountsColumnUUID + " varchar(36), " + accountsColumnMoney + " double NOT NULL);", false);
        query("CREATE TABLE IF NOT EXISTS " + versionName + " (version int NOT NULL);", false);
    }

    public int getVersion() {
        String versionString = getOnlyValue(query("SELECT * from " + versionName + ";", true), "version");
        return Integer.parseInt(versionString);
    }

    public void setVersion(int version) {
        query("DELETE FROM " + versionName + ";", false);
        query("INSERT INTO " + versionName + " (version) VALUES (" + version + ");", false);
    }

    public List<Account> loadTopAccounts(int size) {
        String sql = "SELECT * FROM " + accountsName + " ORDER BY money DESC limit " + size + ";";
        return convertToAccounts(query(sql, true));
    }

    public List<Account> getAccounts() {
        String sql = "SELECT * from " + accountsName;
        return convertToAccounts(query(sql, true));
    }

    public HashMap<String, String> loadAccountData(String name, String uuid) {
        String sql = "SELECT * FROM " + accountsName + " WHERE UPPER(" + (uuid != null ? accountsColumnUUID : accountsColumnUser) + ") LIKE UPPER('" + (uuid != null ? uuid : name) + "');";
        HashMap<String, String> accountData = getFirstRow(query(sql, true));

        if (accountData == null) {
            return new HashMap<>();
        }

        //Filter out custom names on tables
        if (!accountsColumnMoney.equals(SYSTEM_NAME_MONEY)) {
            accountData.put(SYSTEM_NAME_MONEY, accountData.get(accountsColumnMoney));
            accountData.remove(accountsColumnMoney);
        }
        if (!accountsColumnUser.equals(SYSTEM_NAME_USER)) {
            accountData.put(SYSTEM_NAME_USER, accountData.get(accountsColumnUser));
            accountData.remove(accountsColumnUser);
        }

        return accountData;
    }

    public void removeAccount(String name, String uuid) {
        super.removeAccount(name, uuid);

        String sql = "DELETE FROM " + accountsName + " WHERE UPPER(" + (uuid != null ? accountsColumnUUID : accountsColumnUser) + ") LIKE UPPER('" + (uuid != null ? uuid : name) + "');";
        query(sql, false);
    }

    protected void saveAccount(String name, String uuid, double money) {
        String selectSQL = "SELECT * FROM " + accountsName + " WHERE UPPER(" + (uuid != null ? accountsColumnUUID : accountsColumnUser) + ") LIKE UPPER('" + (uuid != null ? uuid : name) + "');";
        String saveSQL;

        if (getFirstRow(query(selectSQL, true)) == null) {
            if (uuid == null) {
                saveSQL = "INSERT INTO " + accountsName + " (" + accountsColumnUser + ", " + accountsColumnMoney + ") VALUES ('" + name + "', " + money + ");";
            } else {
                saveSQL = "INSERT INTO " + accountsName + " (" + accountsColumnUser + ", " + accountsColumnUUID + ", " + accountsColumnMoney + ") VALUES ('" + name + "', '" + uuid + "', " + money + ");";
            }
        } else {
            if (uuid == null) {
                saveSQL = "UPDATE " + accountsName + " SET " + accountsColumnUser + " = '" + name + "', " + accountsColumnMoney + " = " + money + " WHERE UPPER(" + accountsColumnUser + ") LIKE UPPER('" + name + "');";
            } else {
                saveSQL = "UPDATE " + accountsName + " SET " + accountsColumnUser + " = '" + name + "', " + accountsColumnUUID + " = '" + uuid + "', " + accountsColumnMoney + " = " + money + " WHERE UPPER(" + accountsColumnUUID + ") LIKE UPPER('" + uuid + "');";
            }
        }

        query(saveSQL, false);
    }

    public void clean() {
        String sql = "DELETE FROM " + accountsName + " WHERE " + accountsColumnMoney + " = " + plugin.getAPI().getDefaultHoldings();
        query(sql, false);
    }

    public void removeAllAccounts() {
        super.removeAllAccounts();
        query("DELETE FROM " + accountsName, false);
    }

    //+++ Helper Functions +++

    public List<Account> convertToAccounts(ArrayList<HashMap<String, String>> data) {
        List<Account> accounts = new ArrayList<>();

        if (data == null || data.size() == 0) {
            return accounts;
        }

        for (HashMap<String, String> row : data) {
            Account account = new Account(plugin, row.get(accountsColumnUser), row.get(accountsColumnUUID), this);
            account.setMoney(Double.parseDouble(row.get(accountsColumnMoney)));
            accounts.add(account);
        }

        return accounts;
    }
}
