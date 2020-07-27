package org.maxgamer.quickshop.economy;

import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.util.CalculateUtil;
import org.maxgamer.quickshop.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class EconomyTransaction {
    @Nullable
    private final UUID from;
    @Nullable
    private final UUID to;
    private final double amount;
    @NotNull
    private final EconomyCore core;
    private TransactionSteps steps; //For rollback
    private final double total; //
    private final double tax;
    private final UUID taxAccount;
    private final boolean allowLoan;


    /**
     * Create a transaction
     *
     * @param from The account that money from, be null is ignored.
     * @param to   The account that money to, be null is ignored.
     * @param core The economy core
     */
    @Builder
    public EconomyTransaction(@Nullable UUID from, @Nullable UUID to, double amount, double taxModifier, @Nullable UUID taxAccount, @NotNull EconomyCore core, boolean allowLoan) {
        this.from = from;
        this.to = to;
        this.core = core;
        this.amount = amount;
        this.steps = TransactionSteps.WAIT;
        this.taxAccount = taxAccount;
        this.allowLoan = allowLoan;
        if (taxModifier != 0.0d) { //Calc total money and apply tax
            this.total = CalculateUtil.multiply(taxModifier, amount);
        } else {
            this.total = amount;
        }
        this.tax = CalculateUtil.subtract(amount, total); //Calc total tax
        if (from == null && to == null) {
            throw new IllegalArgumentException("From and To cannot be null in same time.");
        }
        //Fetch some stupid plugin caching
        if (from != null) {
            core.getBalance(from);
        }
        if (to != null) {
            core.getBalance(to);
        }
    }

    /**
     * Commit the transaction by the Fail-Safe way
     * Automatic rollback when commit failed
     *
     * @return The transaction success.
     */
    public boolean failSafeCommit() {
        Util.debugLog("Transaction begin: FailSafe Commit --> " + from + " => " + to + "; Amount: " + amount + ", EconomyCore: " + core.getName());
        boolean result = commit();
        if (!result) {
            rollback(true);
        }
        return result;
    }

    /**
     * Commit the transaction
     *
     * @return The transaction success.
     */
    public boolean commit() {
        Util.debugLog("Transaction begin: Regular Commit --> " + from + " => " + to + "; Amount: " + amount + " Total(include tax): " + total + " Tax: " + tax + ", EconomyCore: " + core.getName());
        if (core.getBalance(from) < total && !allowLoan) {
            return false;
        }
        steps = TransactionSteps.WITHDRAW;
        if (from != null && !core.withdraw(from, amount)) {
            Util.debugLog("Failed to withdraw " + amount + " from player " + from.toString() + " account");
            return false;
        }
        steps = TransactionSteps.DEPOSIT;
        if (to != null && !core.deposit(to, total)) {
            Util.debugLog("Failed to deposit " + total + " to player " + to.toString() + " account");
            return false;
        }
        steps = TransactionSteps.TAX;
        if (taxAccount != null && !core.deposit(taxAccount, tax)) {
            Util.debugLog("Failed to deposit tax account: " + tax); //Ignore
        }
        steps = TransactionSteps.DONE;
        return true;
    }

    /**
     * Rolling back the transaction
     *
     * @param continueWhenFailed Continue when some parts of the rollback fails.
     * @return A list contains all steps executed. If "continueWhenFailed" is false, it only contains all success steps before hit the error. Else all.
     */
    @SuppressWarnings("UnusedReturnValue")
    @NotNull
    public List<RollbackSteps> rollback(boolean continueWhenFailed) {
        List<RollbackSteps> rollbackSteps = new ArrayList<>(3);
        if (steps == TransactionSteps.WAIT) {
            return rollbackSteps; //We did nothing, just checks balance
        }
        if (steps == TransactionSteps.DEPOSIT || steps == TransactionSteps.WITHDRAW) {
            if (from != null && !core.deposit(from, amount)) { //Rollback withdraw
                if (!continueWhenFailed) {
                    rollbackSteps.add(RollbackSteps.ROLLBACK_WITHDRAW);
                    return rollbackSteps;
                }
            }
        }
        if (steps == TransactionSteps.DEPOSIT) {
            if (to != null && !core.withdraw(to, total)) { //Rollback deposit
                if (!continueWhenFailed) {
                    rollbackSteps.add(RollbackSteps.ROLLBACK_DEPOSIT);
                    return rollbackSteps;
                }
            }
        }

        if (taxAccount != null && !core.deposit(taxAccount, tax)) { //Rollback withdraw
            rollbackSteps.add(RollbackSteps.ROLLBACK_TAX); //Ignore fails
        }
        rollbackSteps.add(RollbackSteps.ROLLBACK_DONE);
        return rollbackSteps;
    }

    private enum RollbackSteps {
        ROLLBACK_WITHDRAW,
        ROLLBACK_DEPOSIT,
        ROLLBACK_TAX,
        ROLLBACK_DONE
    }

    private enum TransactionSteps {
        WAIT,
        WITHDRAW,
        DEPOSIT,
        TAX,
        DONE
    }

}
