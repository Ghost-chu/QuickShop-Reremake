package org.maxgamer.quickshop.Command;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.maxgamer.quickshop.Database.Database;
import org.maxgamer.quickshop.Database.DatabaseHelper;
import org.maxgamer.quickshop.Database.MySQLCore;
import org.maxgamer.quickshop.Database.SQLiteCore;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.*;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Paste;
import org.maxgamer.quickshop.Util.Util;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

public class QS implements CommandExecutor{
	QuickShop plugin;

	public QS(QuickShop plugin) {
		this.plugin = plugin;
	}


	private void setUnlimited(CommandSender sender) {
		if (sender instanceof Player && sender.hasPermission("quickshop.unlimited")) {
			BlockIterator bIt = new BlockIterator((Player) sender, 10);
			while (bIt.hasNext()) {
				Block b = bIt.next();
				Shop shop = plugin.getShopManager().getShop(b.getLocation());
				if (shop != null) {
					shop.setUnlimited(!shop.isUnlimited());
					shop.setSignText();
					shop.update();
					sender.sendMessage(MsgUtil.getMessage("command.toggle-unlimited",
							(shop.isUnlimited() ? "unlimited" : "limited")));
					return;
				}
			}
			sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
			return;
		} else {
			sender.sendMessage(MsgUtil.getMessage("no-permission"));
			return;
		}
	}
	private void silentUnlimited(CommandSender sender, String[]args) {
		if (sender.hasPermission("quickshop.unlimited")) {
			
				Shop shop = plugin.getShopManager().getShop(new Location(Bukkit.getWorld(args[1]), Integer.valueOf(args[2]),
						Integer.valueOf(args[3]), Integer.valueOf(args[4])));
				if (shop != null) {
					shop.setUnlimited(!shop.isUnlimited());
					shop.setSignText();
					shop.update();
					MsgUtil.sendControlPanelInfo(sender, shop);
					sender.sendMessage(MsgUtil.getMessage("command.toggle-unlimited",
							(shop.isUnlimited() ? "unlimited" : "limited")));
					return;
				}
		} else {
			sender.sendMessage(MsgUtil.getMessage("no-permission"));
			return;
		}
	}
	private void remove(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players may use that command.");
			return;
		}
		Player p = (Player) sender;
		BlockIterator bIt = new BlockIterator(p, 10);
		while (bIt.hasNext()) {
			Block b = bIt.next();
			Shop shop = plugin.getShopManager().getShop(b.getLocation());
			if (shop != null) {
				if (shop.getOwner().equals(p.getUniqueId())||sender.hasPermission("quickshop.other.destroy")) {
					shop.onUnload();
					shop.delete();
					try {
						DatabaseHelper.removeShop(plugin.getDB(), shop.getLocation().getBlockX(),  shop.getLocation().getBlockY(),  shop.getLocation().getBlockZ(), shop.getLocation().getWorld().getName());
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else {
					sender.sendMessage(ChatColor.RED + MsgUtil.getMessage("no-permission"));
				}
				return;
			}
		}
		p.sendMessage(ChatColor.RED + "No shop found!");
	}
	private void fetchMessage(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players may use that command.");
			return;
		}
		Player p = (Player) sender;
			Bukkit.getScheduler().runTask(QuickShop.instance, new Runnable() {
				@Override
				public void run() {
					MsgUtil.flush(p);
				}
			});
	}
	private void silentRemove(CommandSender sender, String[] args) {
		// silentRemove world x y z
		if (args.length < 4)
			return;
		Player p = (Player) sender;
		Shop shop = plugin.getShopManager().getShop(new Location(Bukkit.getWorld(args[1]), Integer.valueOf(args[2]),
				Integer.valueOf(args[3]), Integer.valueOf(args[4])));
		if (shop == null)
			return;

		if (shop != null) {
			if (shop.getOwner().equals(p.getUniqueId())||sender.hasPermission("quickshop.other.destroy")) {
				shop.onUnload();
				shop.delete();
			} else {
				sender.sendMessage(ChatColor.RED + MsgUtil.getMessage("no-permission"));
			}
			return;
		}
	}
	@SuppressWarnings("unused")
	private void export(CommandSender sender, String[] args) {
		if (args.length < 2) {
			sender.sendMessage(ChatColor.RED + "Usage: /qs export mysql|sqlite");
			return;
		}
		String type = args[1].toLowerCase();
		if (type.startsWith("mysql")) {
			if (plugin.getDB().getCore() instanceof MySQLCore) {
				sender.sendMessage(ChatColor.RED + "Database is already MySQL");
				return;
			}
			ConfigurationSection cfg = plugin.getConfig().getConfigurationSection("database");
			String host = cfg.getString("host");
			String port = cfg.getString("port");
			String user = cfg.getString("user");
			String pass = cfg.getString("password");
			String name = cfg.getString("database");
			MySQLCore core = new MySQLCore(host, user, pass, name, port);
			Database target;
			try {
				target = new Database(core);
				QuickShop.instance.getDB().copyTo(target);
				sender.sendMessage(ChatColor.GREEN + "Success - Exported to MySQL " + user + "@" + host + "." + name);
			} catch (Exception e) {
				e.printStackTrace();
				sender.sendMessage(ChatColor.RED + "Failed to export to MySQL " + user + "@" + host + "." + name
						+ ChatColor.DARK_RED + " Reason: " + e.getMessage());
			}
			return;
		}
		if (type.startsWith("sql") || type.contains("file")) {
			if (plugin.getDB().getCore() instanceof SQLiteCore) {
				sender.sendMessage(ChatColor.RED + "Database is already SQLite");
				return;
			}
			File file = new File(plugin.getDataFolder(), "shops.db");
			if (file.exists()) {
				if (file.delete() == false) {
					sender.sendMessage(
							ChatColor.RED + "Warning: Failed to delete old shops.db file. This may cause errors.");
				}
			}
			SQLiteCore core = new SQLiteCore(file);
			try {
				Database target = new Database(core);
				QuickShop.instance.getDB().copyTo(target);
				sender.sendMessage(ChatColor.GREEN + "Success - Exported to SQLite: " + file.toString());
			} catch (Exception e) {
				e.printStackTrace();
				sender.sendMessage(ChatColor.RED + "Failed to export to SQLite: " + file.toString() + " Reason: "
						+ e.getMessage());
			}
			return;
		}
		sender.sendMessage(ChatColor.RED + "No target given. Usage: /qs export mysql|sqlite");
	}
	private void setOwner(CommandSender sender, String[] args) {
		if (sender instanceof Player && sender.hasPermission("quickshop.setowner")) {
			if (args.length < 2) {
				sender.sendMessage(MsgUtil.getMessage("command.no-owner-given"));
				return;
			}
			BlockIterator bIt = new BlockIterator((Player) sender, 10);
			while (bIt.hasNext()) {
				Block b = bIt.next();
				Shop shop = plugin.getShopManager().getShop(b.getLocation());
				if (shop != null) {
					@SuppressWarnings("deprecation")
					OfflinePlayer p = this.plugin.getServer().getOfflinePlayer(args[1]);
					shop.setOwner(p.getUniqueId());
					shop.update();
					sender.sendMessage(MsgUtil.getMessage("command.new-owner",
							this.plugin.getServer().getOfflinePlayer(shop.getOwner()).getName()));
					return;
				}
			}
			sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
			return;
		} else {
			sender.sendMessage(MsgUtil.getMessage("no-permission"));
			return;
		}
	}
	private void refill(CommandSender sender, String[] args) {
		if (sender instanceof Player && sender.hasPermission("quickshop.refill")) {
			if (args.length < 2) {
				sender.sendMessage(MsgUtil.getMessage("command.no-amount-given"));
				return;
			}
			int add;
			try {
				add = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(MsgUtil.getMessage("thats-not-a-number"));
				return;
			}
			BlockIterator bIt = new BlockIterator((LivingEntity) (Player) sender, 10);
			while (bIt.hasNext()) {
				Block b = bIt.next();
				Shop shop = plugin.getShopManager().getShop(b.getLocation());
				if (shop != null) {
					shop.add(shop.getItem(), add);
					sender.sendMessage(MsgUtil.getMessage("refill-success"));
					return;
				}
			}
			sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
			return;
		} else {
			sender.sendMessage(MsgUtil.getMessage("no-permission"));
			return;
		}
	}
	private void silentEmpty(CommandSender sender, String[] args) {
		if (sender.hasPermission("quickshop.refill")) {

			Shop shop = plugin.getShopManager().getShop(new Location(Bukkit.getWorld(args[1]), Integer.valueOf(args[2]),
					Integer.valueOf(args[3]), Integer.valueOf(args[4])));
			if (shop != null) {
				if (shop instanceof ContainerShop) {
					ContainerShop cs = (ContainerShop) shop;
					cs.getInventory().clear();
					MsgUtil.sendControlPanelInfo(sender, shop);
					sender.sendMessage(MsgUtil.getMessage("empty-success"));
					return;
				}
			}

		} else {
			sender.sendMessage(MsgUtil.getMessage("no-permission"));
			return;
		}
	}
	private void empty(CommandSender sender, String[] args) {
		if (sender instanceof Player && sender.hasPermission("quickshop.refill")) {
			BlockIterator bIt = new BlockIterator((LivingEntity) (Player) sender, 10);
			while (bIt.hasNext()) {
				Block b = bIt.next();
				Shop shop = plugin.getShopManager().getShop(b.getLocation());
				if (shop != null) {
					if (shop instanceof ContainerShop) {
						ContainerShop cs = (ContainerShop) shop;
						cs.getInventory().clear();
						sender.sendMessage(MsgUtil.getMessage("empty-success"));
						return;
					} else {
						sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
						return;
					}
				}
			}
			sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
			return;
		} else {
			sender.sendMessage(MsgUtil.getMessage("no-permission"));
			return;
		}
	}
	private void find(CommandSender sender, String[] args) {
		if (sender instanceof Player && sender.hasPermission("quickshop.find")) {
			if (args.length < 2) {
				sender.sendMessage(MsgUtil.getMessage("command.no-type-given"));
				return;
			}
			StringBuilder sb = new StringBuilder(args[1]);
			for (int i = 2; i < args.length; i++) {
				sb.append(" " + args[i]);
			}
			String lookFor = sb.toString();
			lookFor = lookFor.toLowerCase();
			Player p = (Player) sender;
			Location loc = p.getEyeLocation().clone();
			double minDistance = plugin.getConfig().getInt("shop.find-distance");
			double minDistanceSquared = minDistance * minDistance;
			int chunkRadius = (int) minDistance / 16 + 1;
			Shop closest = null;
			Chunk c = loc.getChunk();
			for (int x = -chunkRadius + c.getX(); x < chunkRadius + c.getX(); x++) {
				for (int z = -chunkRadius + c.getZ(); z < chunkRadius + c.getZ(); z++) {
					Chunk d = c.getWorld().getChunkAt(x, z);
					HashMap<Location, Shop> inChunk = plugin.getShopManager().getShops(d);
					if (inChunk == null)
						continue;
					for (Shop shop : inChunk.values()) {
						if (MsgUtil.getItemi18n(shop.getDataName()).toLowerCase().contains(lookFor)
								&& shop.getLocation().distanceSquared(loc) < minDistanceSquared) {
							closest = shop;
							minDistanceSquared = shop.getLocation().distanceSquared(loc);
						}
					}
				}
			}
			if (closest == null) {
				sender.sendMessage(MsgUtil.getMessage("no-nearby-shop", args[1]));
				return;
			}
			Location lookat = closest.getLocation().clone().add(0.5, 0.5, 0.5);
			// Hack fix to make /qs find not used by /back
			p.teleport(this.lookAt(loc, lookat).add(0, -1.62, 0), TeleportCause.UNKNOWN);
			p.sendMessage(
					MsgUtil.getMessage("nearby-shop-this-way", "" + (int) Math.floor(Math.sqrt(minDistanceSquared))));
			return;
		} else {
			sender.sendMessage(MsgUtil.getMessage("no-permission"));
			return;
		}
	}
	private void create(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack item = p.getInventory().getItemInMainHand();
			if (item.getType() != Material.AIR) {
				if (sender.hasPermission("quickshop.create.sell")) {
					BlockIterator bIt = new BlockIterator((LivingEntity) (Player) sender, 10);

					while (bIt.hasNext()) {
						Block b = bIt.next();
						if (Util.canBeShop(b,p.getUniqueId(),false)) {
							if (p != null && b != null && p.isOnline()) {
								BlockBreakEvent be = new BlockBreakEvent(b, p);
								Bukkit.getPluginManager().callEvent(be);
								if (be.isCancelled()) {
									return;
								}
							}

							if (!plugin.getShopManager().canBuildShop(p, b,
									Util.getYawFace(p.getLocation().getYaw()))) {
								// As of the new checking system, most plugins will tell the
								// player why they can't create a shop there.
								// So telling them a message would cause spam etc.
								return;
							}

							if (Util.getSecondHalf(b) != null && !p.hasPermission("quickshop.create.double")) {
								p.sendMessage(MsgUtil.getMessage("no-double-chests"));
								return;
							}
							if (Util.isBlacklisted(item.getType())
									&& !p.hasPermission("quickshop.bypass." + item.getType().name())) {
								p.sendMessage(MsgUtil.getMessage("blacklisted-item"));
								return;
							}

							if (args.length < 2) {
								// Send creation menu.
								Info info = new Info(b.getLocation(), ShopAction.CREATE,
										p.getInventory().getItemInMainHand(),
										b.getRelative(p.getFacing().getOppositeFace()));
								plugin.getShopManager().getActions().put(p.getUniqueId(), info);
								p.sendMessage(
										MsgUtil.getMessage("how-much-to-trade-for", Util.getName(info.getItem())));
							} else {
								plugin.getShopManager().handleChat(p, args[1]);
							}
							return;
						}
					}
				} else {
					sender.sendMessage(MsgUtil.getMessage("no-permission"));
				}
			} else {
				sender.sendMessage(MsgUtil.getMessage("no-anythings-in-your-hand"));
			}
		} else {
			sender.sendMessage("This command can't be run by console");
		}
		return;
	}
	private void amount(CommandSender sender, String[] args) {
		if (args.length < 2) {
			sender.sendMessage("Missing amount");
			return;
		}

		if (sender instanceof Player) {
			final Player player = (Player) sender;
			if (!plugin.getShopManager().getActions().containsKey(player.getUniqueId())) {
				sender.sendMessage("You do not have any pending action!");
				return;
			}
			plugin.getShopManager().handleChat(player, args[1]);
		} else {
			sender.sendMessage("This command can't be run by console");
		}
		return;
	}
	private void silentBuy(CommandSender sender, String[] args) {
		if (sender.hasPermission("quickshop.create.buy")) {
			Shop shop = plugin.getShopManager().getShop(new Location(Bukkit.getWorld(args[1]), Integer.valueOf(args[2]),
					Integer.valueOf(args[3]), Integer.valueOf(args[4])));
			if (shop != null && shop.getOwner().equals(((Player) sender).getUniqueId())) {
				shop.setShopType(ShopType.BUYING);
				shop.setSignText();
				shop.update();
				MsgUtil.sendControlPanelInfo(sender, shop);
				sender.sendMessage(MsgUtil.getMessage("command.now-buying", shop.getDataName()));
				return;
			}
		}
		sender.sendMessage(MsgUtil.getMessage("no-permission"));
		return;
	}
	private void setBuy(CommandSender sender) {
		if (sender instanceof Player && sender.hasPermission("quickshop.create.buy")) {
			BlockIterator bIt = new BlockIterator((LivingEntity) (Player) sender, 10);
			while (bIt.hasNext()) {
				Block b = bIt.next();
				Shop shop = plugin.getShopManager().getShop(b.getLocation());
				if (shop != null && shop.getOwner().equals(((Player) sender).getUniqueId())) {
					shop.setShopType(ShopType.BUYING);
					shop.setSignText();
					shop.update();
					sender.sendMessage(MsgUtil.getMessage("command.now-buying", shop.getDataName()));
					return;
				}
			}
			sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
			return;
		}
		sender.sendMessage(MsgUtil.getMessage("no-permission"));
		return;
	}
	private void silentSell(CommandSender sender, String[] args) {
		if (sender.hasPermission("quickshop.create.sell")) {
				Shop shop = plugin.getShopManager().getShop(new Location(Bukkit.getWorld(args[1]),
						Integer.valueOf(args[2]), Integer.valueOf(args[3]), Integer.valueOf(args[4])));
				if (shop != null && shop.getOwner().equals(((Player) sender).getUniqueId())) {
					shop.setShopType(ShopType.SELLING);
					shop.setSignText();
					shop.update();
					MsgUtil.sendControlPanelInfo(sender, shop);
					sender.sendMessage(MsgUtil.getMessage("command.now-selling", shop.getDataName()));
					return;
				}
			
		}
		sender.sendMessage(MsgUtil.getMessage("no-permission"));
		return;
	}
	private void setSell(CommandSender sender) {
		if (sender instanceof Player && sender.hasPermission("quickshop.create.sell")) {
			BlockIterator bIt = new BlockIterator((LivingEntity) (Player) sender, 10);
			while (bIt.hasNext()) {
				Block b = bIt.next();
				Shop shop = plugin.getShopManager().getShop(b.getLocation());
				if (shop != null && shop.getOwner().equals(((Player) sender).getUniqueId())) {
					shop.setShopType(ShopType.SELLING);
					shop.setSignText();
					shop.update();
					sender.sendMessage(MsgUtil.getMessage("command.now-selling", shop.getDataName()));
					return;
				}
			}
			sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
			return;
		}
		sender.sendMessage(MsgUtil.getMessage("no-permission"));
		return;
	}
	@SuppressWarnings("deprecation")
	private void setPrice(CommandSender sender, String[] args) {
		if (sender instanceof Player && sender.hasPermission("quickshop.create.changeprice")) {
			Player p = (Player) sender;
			if (args.length < 2) {
				sender.sendMessage(MsgUtil.getMessage("no-price-given"));
				return;
			}
			double price;
			try {
				price = Double.parseDouble(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(MsgUtil.getMessage("thats-not-a-number"));
				return;
			}
			if (price < 0.01) {
				sender.sendMessage(MsgUtil.getMessage("price-too-cheap"));
				return;
			}
			double fee = 0;
			if (plugin.priceChangeRequiresFee) {
				fee = plugin.getConfig().getDouble("shop.fee-for-price-change");
				if (fee > 0 && plugin.getEcon().getBalance(p.getUniqueId()) < fee) {
					sender.sendMessage(
							MsgUtil.getMessage("you-cant-afford-to-change-price", plugin.getEcon().format(fee)));
					return;
				}
			}
			BlockIterator bIt = new BlockIterator(p, 10);
			// Loop through every block they're looking at upto 10 blocks away
			while (bIt.hasNext()) {
				Block b = bIt.next();
				Shop shop = plugin.getShopManager().getShop(b.getLocation());
				if (shop != null && (shop.getOwner().equals(((Player) sender).getUniqueId())
						|| sender.hasPermission("quickshop.other.price"))) {
					if (shop.getPrice() == price) {
						// Stop here if there isn't a price change
						sender.sendMessage(MsgUtil.getMessage("no-price-change"));
						return;
					}
					if (fee > 0) {
						if (!plugin.getEcon().withdraw(p.getUniqueId(), fee)) {
							sender.sendMessage(MsgUtil.getMessage("you-cant-afford-to-change-price",
									plugin.getEcon().format(fee)));
							return;
						}
						sender.sendMessage(
								MsgUtil.getMessage("fee-charged-for-price-change", plugin.getEcon().format(fee)));
						try {
							plugin.getEcon().deposit(Bukkit.getOfflinePlayer(plugin.getConfig().getString("tax-account")).getUniqueId(), fee);
						} catch (Exception e) {
							e.getMessage();
							plugin.getLogger().log(Level.WARNING,
									"QuickShop can't pay tax to account in config.yml,Please set tax account name to a exist player!");
						}

					}
					// Update the shop
					shop.setPrice(price);
					shop.setSignText();
					shop.update();
					sender.sendMessage(MsgUtil.getMessage("price-is-now", plugin.getEcon().format(shop.getPrice())));
					// Chest shops can be double shops.
					if (shop instanceof ContainerShop) {
						ContainerShop cs = (ContainerShop) shop;
						if (cs.isDoubleShop()) {
							Shop nextTo = cs.getAttachedShop();
							if (cs.isSelling()) {
								if (cs.getPrice() < nextTo.getPrice()) {
									sender.sendMessage(MsgUtil.getMessage("buying-more-than-selling"));
								}
							} else {
								// Buying
								if (cs.getPrice() > nextTo.getPrice()) {
									sender.sendMessage(MsgUtil.getMessage("buying-more-than-selling"));
								}
							}
						}
					}
					return;
				}
			}
			sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
			return;
		}
		sender.sendMessage(MsgUtil.getMessage("no-permission"));
		return;
	}
	private void clean(CommandSender sender) {
		if (sender.hasPermission("quickshop.clean")) {
			sender.sendMessage(MsgUtil.getMessage("command.cleaning"));
			Iterator<Shop> shIt = plugin.getShopManager().getShopIterator();
			int i = 0;
			while (shIt.hasNext()) {
				Shop shop = shIt.next();

				try {
					if (shop.getLocation().getWorld() != null && shop.isSelling() && shop.getRemainingStock() == 0
							&& shop instanceof ContainerShop) {
						ContainerShop cs = (ContainerShop) shop;
						if (cs.isDoubleShop())
							continue;
						shIt.remove(); // Is selling, but has no stock, and is a chest shop, but is not a double shop.
										// Can be deleted safely.
						i++;
					}
				} catch (IllegalStateException e) {
					shIt.remove(); // The shop is not there anymore, remove it
				}
			}
			MsgUtil.clean();
			sender.sendMessage(MsgUtil.getMessage("command.cleaned", "" + i));
			return;
		}
		sender.sendMessage(MsgUtil.getMessage("no-permission"));
		return;
	}
	private void about(CommandSender sender) {
		sender.sendMessage("[QuickShop] About QuickShop");
		sender.sendMessage("[QuickShop] Hello, I'm Ghost_chu Author of QS reremake.");
		sender.sendMessage("[QuickShop] This plugin is remake by SunnySide Community.");
		sender.sendMessage("[QuickShop] Original author is KaiNoMood. This is QS unofficial version.");
		sender.sendMessage("[QuickShop] Have more feature, and design for 1.13 and higher version.");
		sender.sendMessage("[QuickShop] You can see our SpigotMC page to read more:");
		sender.sendMessage("[QuickShop] https://www.spigotmc.org/resources/quickshop-reremake-for-1-13.62575/");
		sender.sendMessage("[QuickShop] Thanks for use QuickShop-Reremake.");
	}
	private void reload(CommandSender sender) {
		if (sender.hasPermission("quickshop.reload")) {
			sender.sendMessage(MsgUtil.getMessage("command.reloading"));
			Bukkit.getPluginManager().disablePlugin(plugin);
			Bukkit.getPluginManager().enablePlugin(plugin);
			return;
		}
		sender.sendMessage(MsgUtil.getMessage("no-permission"));
		return;
	}
	public void paste(CommandSender sender) {
		if (sender.hasPermission("quickshop.paste")) {
			//do actions
			new BukkitRunnable() {
				@Override
				public void run() {
					try{
					    /**@TODO i18n*/
                        sender.sendMessage("Please wait...");
						Paste paste = new Paste(plugin);
						sender.sendMessage(paste.pasteTheText(paste.genNewPaste()));

					}catch (Exception err){
						sender.sendMessage("Paste failed, see console for details.");
					}
				}
			}.runTaskAsynchronously(plugin);
			return;
		}
		sender.sendMessage(MsgUtil.getMessage("no-permission"));
		return;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (args.length > 0) {
			String subArg = args[0].toLowerCase();
			if (subArg.equals("unlimited")) {
				setUnlimited(sender);
				return true;
			} else if (subArg.startsWith("silentunlimited")) {
				silentUnlimited(sender, args);
				return true;
			} else if (subArg.equals("setowner")) {
				setOwner(sender, args);
				return true;
			} else if (subArg.equals("find")) {
				find(sender, args);
				return true;
			} else if (subArg.startsWith("create")) {
				create(sender, args);
				return true;
			} else if (subArg.startsWith("amount")) {
				amount(sender, args);
				return true;
			} else if (subArg.startsWith("buy")) {
				setBuy(sender);
				return true;
			} else if (subArg.startsWith("silentbuy")) {
				silentBuy(sender, args);
				return true;
			} else if (subArg.startsWith("sell")) {
				setSell(sender);
				return true;
			} else if (subArg.startsWith("silentsell")) {
				silentSell(sender, args);
				return true;
			} else if (subArg.startsWith("price")) {
				setPrice(sender, args);
				return true;
			} else if (subArg.equals("remove")) {
				remove(sender, args);
				return true;
			} else if (subArg.startsWith("silentremove")) {
				silentRemove(sender, args);
				return true;
			} else if (subArg.equals("refill")) {
				refill(sender, args);
				return true;
			} else if (subArg.equals("empty")) {
				empty(sender, args);
				return true;
			} else if (subArg.startsWith("silentempty")) {
				silentEmpty(sender, args);
				return true;
			} else if (subArg.equals("clean")) {
				clean(sender);
				return true;
			} else if (subArg.equals("reload")) {
				reload(sender);
				return true;
			} else if (subArg.equals("export")) {
				//export(sender, args);
				return true;
			} else if (subArg.equals("about")) {
				about(sender);
				return true;
			} else if (subArg.equals("remove")) {
				remover(sender, args);
				return true;
			} else if (subArg.equals("debug")) {
				debug(sender, args);
				return true;
			} else if (subArg.equals("fetchmessage")) {
				fetchMessage(sender, args);
				return true;
			} else if (subArg.equals("info")) {
				info(sender,args);
				return true;
			} else if (subArg.equals("paste")) {
				paste(sender);
				return true;
			}
		} else {
			// Invalid arg given
			sendHelp(sender);
			return true;
		}
		// No args given
		sendHelp(sender);
		return true;
	}
	private void debug(CommandSender sender, String[] args) {
		boolean debug = plugin.getConfig().getBoolean("dev-mode");
		if(debug) {
			plugin.getConfig().set("dev-mode", false);
			plugin.saveConfig();
			sender.sendMessage(MsgUtil.getMessage("command.now-nolonger-debuging"));
			reload(sender);
		}else {
			plugin.getConfig().set("dev-mode", true);
			plugin.saveConfig();
			sender.sendMessage(MsgUtil.getMessage("command.now-debuging"));
			reload(sender);
		}
	}

	private void info(CommandSender sender, String[] args) {
		if (sender.hasPermission("quickshop.info")) {
			int buying, selling, doubles, chunks, worlds;
			buying = selling = doubles = chunks = worlds = 0;
			int nostock = 0;
			for (HashMap<ShopChunk, HashMap<Location, Shop>> inWorld : plugin.getShopManager().getShops()
					.values()) {
				worlds++;
				for (HashMap<Location, Shop> inChunk : inWorld.values()) {
					chunks++;
					for (Shop shop : inChunk.values()) {
						if (shop.isBuying()) {
							buying++;
						} else if (shop.isSelling()) {
							selling++;
						}
						if (shop instanceof ContainerShop && ((ContainerShop) shop).isDoubleShop()) {
							doubles++;
						} else if (shop.isSelling() && shop.getRemainingStock() == 0) {
							nostock++;
						}
					}
				}
			}
			sender.sendMessage(ChatColor.RED + "QuickShop Statistics...");
			sender.sendMessage(ChatColor.GREEN + "" + (buying + selling) + " shops in " + chunks
					+ " chunks spread over " + worlds + " worlds.");
			sender.sendMessage(ChatColor.GREEN + "" + doubles + " double shops. ");
			sender.sendMessage(ChatColor.GREEN + "" + nostock
					+ " nostock selling shops (excluding doubles) which will be removed by /qs clean.");
			sender.sendMessage(ChatColor.GREEN + "QuickShop "+QuickShop.getVersion());
		}else {
			sender.sendMessage(MsgUtil.getMessage("no-permission"));
		}	
	}


	private void remover(CommandSender sender, String[] args) {
		if (args.length < 5) {
			return;
		}
		ShopManager manager = plugin.getShopManager();
		try {
			Shop shop = manager.getShop(new Location(Bukkit.getWorld(args[4]), Integer.parseInt(args[1]),
					Integer.parseInt(args[2]), Integer.parseInt(args[3])));
			if (shop == null) {
				sender.sendMessage(MsgUtil.getMessage("shop-not-exist"));
				return;
			}
			if(sender instanceof Player) {
				Player player = (Player)sender;
				if(!(shop.getOwner()!=player.getUniqueId())&&!(player.hasPermission("quickshop.other.destroy"))) {
					sender.sendMessage(MsgUtil.getMessage("no-permission"));
					return;
				}
			}
			DatabaseHelper.removeShop(plugin.getDB(), Integer.parseInt(args[1]), Integer.parseInt(args[2]),
					Integer.parseInt(args[3]), args[4]);
			shop.onUnload();
			sender.sendMessage(MsgUtil.getMessage("success-removed-shop"));
		} catch (NumberFormatException | SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Returns loc with modified pitch/yaw angles so it faces lookat
	 * 
	 * @param loc    The location a players head is
	 * @param lookat The location they should be looking
	 * @return The location the player should be facing to have their crosshairs on
	 *         the location lookAt Kudos to bergerkiller for most of this function
	 */
	public Location lookAt(Location loc, Location lookat) {
		// Clone the loc to prevent applied changes to the input loc
		loc = loc.clone();
		// Values of change in distance (make it relative)
		double dx = lookat.getX() - loc.getX();
		double dy = lookat.getY() - loc.getY();
		double dz = lookat.getZ() - loc.getZ();
		// Set yaw
		if (dx != 0) {
			// Set yaw start value based on dx
			if (dx < 0) {
				loc.setYaw((float) (1.5 * Math.PI));
			} else {
				loc.setYaw((float) (0.5 * Math.PI));
			}
			loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
		} else if (dz < 0) {
			loc.setYaw((float) Math.PI);
		}
		// Get the distance from dx/dz
		double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
		float pitch = (float) -Math.atan(dy / dxz);
		// Set values, convert to degrees
		// Minecraft yaw (vertical) angles are inverted (negative)
		loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI + 360);
		// But pitch angles are normal
		loc.setPitch(pitch * 180f / (float) Math.PI);
		return loc;
	}
	public void sendDebugInfomation(CommandSender s) {
		s.sendMessage("Running " + plugin.getDescription().getVersion() + " on server "
				+ plugin.getServer().getVersion() + " for Bukkit " + plugin.getServer().getBukkitVersion());
		try {
			s.sendMessage("Database: " + plugin.getDB().getConnection().getMetaData().getDatabaseProductName() + " @ "
					+ plugin.getDB().getConnection().getMetaData().getDatabaseProductVersion());
		} catch (SQLException e) {
			s.sendMessage("Database: A error happed when getting data.");
			e.printStackTrace();
		}
	}
	public void sendHelp(CommandSender s) {

		s.sendMessage(MsgUtil.getMessage("command.description.title"));
		if (s.hasPermission("quickshop.unlimited"))
			s.sendMessage(ChatColor.GREEN + "/qs unlimited" + ChatColor.YELLOW + " - "
					+ MsgUtil.getMessage("command.description.unlimited"));
		if (s.hasPermission("quickshop.setowner"))
			s.sendMessage(ChatColor.GREEN + "/qs setowner <player>" + ChatColor.YELLOW + " - "
					+ MsgUtil.getMessage("command.description.setowner"));
		if (s.hasPermission("quickshop.create.buy"))
			s.sendMessage(ChatColor.GREEN + "/qs buy" + ChatColor.YELLOW + " - "
					+ MsgUtil.getMessage("command.description.buy"));
		if (s.hasPermission("quickshop.create.sell")) {
			s.sendMessage(ChatColor.GREEN + "/qs sell" + ChatColor.YELLOW + " - "
					+ MsgUtil.getMessage("command.description.sell"));
			s.sendMessage(ChatColor.GREEN + "/qs create [price]" + ChatColor.YELLOW + " - "
					+ MsgUtil.getMessage("command.description.create"));
		}
		if (s.hasPermission("quickshop.create.changeprice"))
			s.sendMessage(ChatColor.GREEN + "/qs price" + ChatColor.YELLOW + " - "
					+ MsgUtil.getMessage("command.description.price"));
		if (s.hasPermission("quickshop.clean"))
			s.sendMessage(ChatColor.GREEN + "/qs clean" + ChatColor.YELLOW + " - "
					+ MsgUtil.getMessage("command.description.clean"));
		if (s.hasPermission("quickshop.find"))
			s.sendMessage(ChatColor.GREEN + "/qs find <item>" + ChatColor.YELLOW + " - "
					+ MsgUtil.getMessage("command.description.find"));
		if (s.hasPermission("quickshop.refill"))
			s.sendMessage(ChatColor.GREEN + "/qs refill <amount>" + ChatColor.YELLOW + " - "
					+ MsgUtil.getMessage("command.description.refill"));
		if (s.hasPermission("quickshop.empty"))
			s.sendMessage(ChatColor.GREEN + "/qs empty" + ChatColor.YELLOW + " - "
					+ MsgUtil.getMessage("command.description.empty"));
		if (s.hasPermission("quickshop.fetchmessage"))
			s.sendMessage(ChatColor.GREEN + "/qs fetchmessage" + ChatColor.YELLOW + " - "
					+ MsgUtil.getMessage("command.description.fetchmessage"));
		if (s.hasPermission("quickshop.info"))
			s.sendMessage(ChatColor.GREEN + "/qs info" + ChatColor.YELLOW + " - "
					+ MsgUtil.getMessage("command.description.info"));
		if (s.hasPermission("quickshop.debug"))
			s.sendMessage(ChatColor.GREEN + "/qs debug" + ChatColor.YELLOW + " - "
					+ MsgUtil.getMessage("command.description.debug"));
		if (s.hasPermission("quickshop.paste"))
			s.sendMessage(ChatColor.GREEN + "/qs debug" + ChatColor.YELLOW + " - "
					+ MsgUtil.getMessage("command.description.paste"));
//		if (s.hasPermission("quickshop.export"))
//			s.sendMessage(ChatColor.GREEN + "/qs export mysql|sqlite" + ChatColor.YELLOW + " - "
//					+ MsgUtil.getMessage("command.description.export"));
	}
	
}
