package org.maxgamer.quickshop.economy;

import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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


    /**
     * Create a transaction
     *
     * @param from The account that money from, be null is ignored.
     * @param to   The account that money to, be null is ignored.
     * @param core The economy core
     */
    @Builder
    public EconomyTransaction(@Nullable UUID from, @Nullable UUID to, double amount, @NotNull EconomyCore core) {
        this.from = from;
        this.to = to;
        this.core = core;
        this.amount = amount;
        this.steps = TransactionSteps.WAIT;
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
     * Commit the transaction
     *
     * @return The transaction success.
     */
    public boolean commit() {
        Util.debugLog("Transaction begin: " + from + " => " + to + "; Amount: " + amount + ", EconomyCore: " + core.getName());
        steps = TransactionSteps.WITHDRAW;
        if (from != null && !core.withdraw(from, amount)) {
            Util.debugLog("Failed to withdraw " + amount + " from player " + from.toString() + " account");
            return false;
        }
        steps = TransactionSteps.DEPOSIT;
        if (to != null && !core.deposit(to, amount)) {
            Util.debugLog("Failed to deposit " + amount + " to player " + to.toString() + " account");
            return false;
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
    @NotNull
    public List<RollbackSteps> rollback(boolean continueWhenFailed) {
        List<RollbackSteps> rollbackSteps = new ArrayList<>(3);
        if (to != null && !core.withdraw(to, amount)) { //Rollback deposit
            if (!continueWhenFailed) {
                rollbackSteps.add(RollbackSteps.ROLLBACK_DEPOSIT);
                return rollbackSteps;
            }
        }
        if (to != null && !core.deposit(to, amount)) { //Rollback withdraw
            if (!continueWhenFailed) {
                rollbackSteps.add(RollbackSteps.ROLLBACK_WITHDRAW);
                return rollbackSteps;
            }
        }
        rollbackSteps.add(RollbackSteps.ROLLBACK_DONE);
        return rollbackSteps;
    }

    private enum RollbackSteps {
        ROLLBACK_WITHDRAW,
        ROLLBACK_DEPOSIT,
        ROLLBACK_DONE
    }

    private enum TransactionSteps {
        WAIT,
        WITHDRAW,
        DEPOSIT,
        DONE
    }

    public static class EconomyTransactionException extends RuntimeException {
        public EconomyTransactionException(String msg) {
            super(msg);
        }
    }
}
