package org.maxgamer.quickshop.Events;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class QSCommandEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private String commandLabel;
	private Command cmd;
	private CommandSender sender;
	private String[] args;
	/**Calling when QS get a /qs or /shop command**/
	public QSCommandEvent(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		this.sender = sender;
		this.cmd = cmd;
		this.commandLabel=commandLabel;
		this.args=args;
	}

	public String[] getArgs() {
		return args;
	}
	public Command getCmd() {
		return cmd;
	}
	public CommandSender getSender() {
		return sender;
	}
	public String getCommandLabel() {
		return commandLabel;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
}