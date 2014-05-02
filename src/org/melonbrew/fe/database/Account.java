package org.melonbrew.fe.database;

import org.melonbrew.fe.API;
import org.melonbrew.fe.Fe;

public class Account {
    private final String name;

    private final API api;

    private final Database database;

    private Double money;

    public Account(String name, Fe plugin, Database database) {
        this.name = name;

        this.api = plugin.getAPI();

        this.database = database;

        this.money = null;
    }

    public String getName() {
        return name;
    }

    public Double getMoney() {
        if (database.cacheAccounts()) {
            if (money != null) {
                return money;
            }

            money = database.loadAccountMoney(name);

            return money;
        }

        return database.loadAccountMoney(name);
    }

    public void setMoney(double money) {
        Double currentMoney = getMoney();

        if (currentMoney != null && currentMoney == money) {
            return;
        }

        if (money < 0 && !api.isCurrencyNegative()) {
            money = 0;
        }

        currentMoney = api.getMoneyRounded(money);

        if (api.getMaxHoldings() > 0 && currentMoney > api.getMaxHoldings()) {
            currentMoney = api.getMoneyRounded(api.getMaxHoldings());
        }

        if (!database.cacheAccounts()) {
            save(currentMoney);
        } else {
            this.money = currentMoney;
        }
    }

    public void withdraw(double amount) {
        setMoney(getMoney() - amount);
    }

    public void deposit(double amount) {
        setMoney(getMoney() + amount);
    }

    public boolean canReceive(double amount) {
        return api.getMaxHoldings() == -1 || amount + getMoney() < api.getMaxHoldings();

    }

    public boolean has(double amount) {
        return getMoney() >= amount;
    }

    public void save(double money) {
        database.saveAccount(name, money);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Account)) {
            return false;
        }

        Account account = (Account) object;

        return account.getName().equals(getName());
    }
}
