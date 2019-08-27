package org.maxgamer.quickshop.Economy;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.*;
import net.tnemc.core.Reserve;
import net.tnemc.core.economy.EconomyAPI;
import org.bukkit.Bukkit;
import org.maxgamer.quickshop.QuickShop;

/**
 * @author creatorfromhell
 */
public class Economy_Reserve implements EconomyCore {
    final private String errorMsg = "QuickShop got an error when calling your Economy system, this is NOT a QuickShop error, please do not report this issue to the QuickShop's Issue tracker, ask your Economy plugin's author.";
    private QuickShop plugin = QuickShop.instance;
    @Getter
    @Setter
    private EconomyAPI reserve = null;

    public Economy_Reserve() {
        setup();
    }

    private String formatInternal(double balance) {
        try {
            return QuickShop.instance.getConfig().getString("shop.alternate-currency-symbol") + balance;
        } catch (Exception e) {
            return String.valueOf('$' + balance);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setup() {
        try {
            if (((Reserve) Bukkit.getPluginManager().getPlugin("Reserve")).economyProvided()) {
                reserve = ((Reserve) Bukkit.getPluginManager().getPlugin("Reserve")).economy();
            }
        } catch (Throwable throwable) {
            reserve = null;
        }
    }

    /**
     * Checks that this economy is valid. Returns false if it is not valid.
     *
     * @return True if this economy will work, false if it will not.
     */
    @Override
    public boolean isValid() {
        return reserve != null;
    }

    /**
     * Formats the given number... E.g. 50.5 becomes $50.5 Dollars, or 50 Dollars 5 Cents
     *
     * @param balance The given number
     * @return The balance in human readable text.
     */
    @Override
    public String format(double balance) {
        try {
            return reserve.format(new BigDecimal(balance));
        } catch (Throwable throwable) {
            plugin.getSentryErrorReporter().ignoreThrow();
            throwable.printStackTrace();
            plugin.getLogger().warning(this.errorMsg);
            return formatInternal(balance);
        }
    }

    /**
     * Deposits a given amount of money from thin air to the given username.
     *
     * @param name   The exact (case insensitive) username to give money to
     * @param amount The amount to give them
     * @return True if success (Should be almost always)
     */
    @Override
    public boolean deposit(UUID name, double amount) {
        try {
            return reserve.addHoldings(name, new BigDecimal(amount));
        } catch (Throwable throwable) {
            plugin.getSentryErrorReporter().ignoreThrow();
            throwable.printStackTrace();
            plugin.getLogger().warning(this.errorMsg);
            return false;
        }
    }

    /**
     * Withdraws a given amount of money from the given username and turns it to thin air.
     *
     * @param name   The exact (case insensitive) username to take money from
     * @param amount The amount to take from them
     * @return True if success, false if they didn't have enough cash
     */
    @Override
    public boolean withdraw(UUID name, double amount) {
        try {
            if (!plugin.getConfig().getBoolean("shop.allow-economy-loan")) {
                if (getBalance(name) < amount) {
                    return false;
                }
            }
            return reserve.removeHoldings(name, new BigDecimal(amount));
        } catch (Throwable throwable) {
            plugin.getSentryErrorReporter().ignoreThrow();
            throwable.printStackTrace();
            plugin.getLogger().warning(this.errorMsg);
            return false;
        }
    }

    /**
     * Transfers the given amount of money from Player1 to Player2
     *
     * @param from   The player who is paying money
     * @param to     The player who is receiving money
     * @param amount The amount to transfer
     * @return true if success (Payer had enough cash, receiver was able to receive the funds)
     */
    @Override
    public boolean transfer(UUID from, UUID to, double amount) {
        try {
            return reserve.transferHoldings(from, to, new BigDecimal(amount));
        } catch (Throwable throwable) {
            plugin.getSentryErrorReporter().ignoreThrow();
            throwable.printStackTrace();
            plugin.getLogger().warning(this.errorMsg);
            return false;
        }
    }

    /**
     * Fetches the balance of the given account name
     *
     * @param name The name of the account
     * @return Their current balance.
     */
    @Override
    public double getBalance(UUID name) {
        try {
            return reserve.getHoldings(name).doubleValue();
        } catch (Throwable throwable) {
            plugin.getSentryErrorReporter().ignoreThrow();
            throwable.printStackTrace();
            plugin.getLogger().warning(this.errorMsg);
            return 0.0;
        }
    }
}
