package org.maxgamer.quickshop.Economy;

import java.util.UUID;

public class Economy implements EconomyCore {
	private EconomyCore core;

	public Economy(EconomyCore core) {
		this.core = core;
	}

	/**
	 * Checks that this economy is valid. Returns false if it is not valid.
	 * 
	 * @return True if this economy will work, false if it will not.
	 */
	public boolean isValid() {
		return core.isValid();
	}

	/**
	 * Deposits a given amount of money from thin air to the given username.
	 * 
	 * @param name
	 *            The exact (case insensitive) username to give money to
	 * @param amount
	 *            The amount to give them
	 * @return True if success (Should be almost always)
	 */
	@Deprecated
	public boolean deposit(String name, double amount) {
		return core.deposit(name, amount);
	}

	/**
	 * Withdraws a given amount of money from the given username and turns it to
	 * thin air.
	 * 
	 * @param name
	 *            The exact (case insensitive) username to take money from
	 * @param amount
	 *            The amount to take from them
	 * @return True if success, false if they didn't have enough cash
	 */
	@Deprecated
	public boolean withdraw(String name, double amount) {
		return core.withdraw(name, amount);
	}

	/**
	 * Transfers the given amount of money from Player1 to Player2
	 * 
	 * @param from
	 *            The player who is paying money
	 * @param to
	 *            The player who is receiving money
	 * @param amount
	 *            The amount to transfer
	 * @return true if success (Payer had enough cash, receiver was able to
	 *         receive the funds)
	 */
	@Deprecated
	public boolean transfer(String from, String to, double amount) {
		return core.transfer(from, to, amount);
	}

	/**
	 * Fetches the balance of the given account name
	 * 
	 * @param name
	 *            The name of the account
	 * @return Their current balance.
	 */
	@Deprecated
	public double getBalance(String name) {
		return core.getBalance(name);
	}

	/**
	 * Formats the given number... E.g. 50.5 becomes $50.5 Dollars, or 50
	 * Dollars 5 Cents
	 * 
	 * @param balance
	 *            The given number
	 * @return The balance in human readable text.
	 */
	public String format(double balance) {
		return core.format(balance);
	}
	@Deprecated
	public boolean has(String name, double amount) {
		return core.getBalance(name) >= amount;
	}

	@Override
	public String toString() {
		return core.getClass().getName().split("_")[1];
	}

	@Override
	public boolean deposit(UUID name, double amount) {
		return core.deposit(name,amount);
	}

	@Override
	public boolean withdraw(UUID name, double amount) {
		return core.withdraw(name, amount);
	}

	@Override
	public boolean transfer(UUID from, UUID to, double amount) {
		return core.transfer(from, to, amount);
	}

	@Override
	public double getBalance(UUID name) {
		return core.getBalance(name);
	}
}