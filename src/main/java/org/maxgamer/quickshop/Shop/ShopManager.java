package org.maxgamer.quickshop.Shop;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Sign;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Database.Database;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Permissions;
import org.maxgamer.quickshop.Util.Util;

public class ShopManager {
	private QuickShop plugin;
	private HashMap<UUID, Info> actions = new HashMap<UUID, Info>();
	private HashMap<String, HashMap<ShopChunk, HashMap<Location, Shop>>> shops = new HashMap<String, HashMap<ShopChunk, HashMap<Location, Shop>>>();
	final private static ItemStack AIR = new ItemStack(Material.AIR);

	public ShopManager(QuickShop plugin) {
		this.plugin = plugin;
	}

	public Database getDatabase() {
		return plugin.getDB();
	}

	/**
	 * @return Returns the HashMap<Player name, shopInfo>. Info contains what
	 *         their last question etc was.
	 */
	public HashMap<UUID, Info> getActions() {
		return this.actions;
	}

	public void createShop(Shop shop) {
		Location loc = shop.getLocation();
		ItemStack item = shop.getItem();
		try {
			// Write it to the database
			String q = "INSERT INTO shops (owner, price, itemConfig, x, y, z, world, unlimited, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			plugin.getDB().execute(q, shop.getOwner().toString(), shop.getPrice(), Util.serialize(item), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName(), (shop.isUnlimited() ? 1 : 0), shop.getShopType().toID());
			// Add it to the world
			addShop(loc.getWorld().getName(), shop);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Could not create shop! Changes will revert after a reboot!");
		}
	}

	/**
	 * Loads the given shop into storage. This method is used for loading data
	 * from the database. Do not use this method to create a shop.
	 * 
	 * @param world
	 *            The world the shop is in
	 * @param shop
	 *            The shop to load
	 */
	public void loadShop(String world, Shop shop) {
		this.addShop(world, shop);
	}

	/**
	 * Returns a hashmap of World -> Chunk -> Shop
	 * 
	 * @return a hashmap of World -> Chunk -> Shop
	 */
	public HashMap<String, HashMap<ShopChunk, HashMap<Location, Shop>>> getShops() {
		return this.shops;
	}

	/**
	 * Returns a hashmap of Chunk -> Shop
	 * 
	 * @param world
	 *            The name of the world (case sensitive) to get the list of
	 *            shops from
	 * @return a hashmap of Chunk -> Shop
	 */
	public HashMap<ShopChunk, HashMap<Location, Shop>> getShops(String world) {
		return this.shops.get(world);
	}

	/**
	 * Returns a hashmap of Shops
	 * 
	 * @param c
	 *            The chunk to search. Referencing doesn't matter, only
	 *            coordinates and world are used.
	 * @return
	 */
	public HashMap<Location, Shop> getShops(Chunk c) {
		// long start = System.nanoTime();
		HashMap<Location, Shop> shops = getShops(c.getWorld().getName(), c.getX(), c.getZ());
		// long end = System.nanoTime();
		// System.out.println("Chunk lookup in " + ((end - start)/1000000.0) +
		// "ms.");
		return shops;
	}

	public HashMap<Location, Shop> getShops(String world, int chunkX, int chunkZ) {
		HashMap<ShopChunk, HashMap<Location, Shop>> inWorld = this.getShops(world);
		if (inWorld == null) {
			return null;
		}
		ShopChunk shopChunk = new ShopChunk(world, chunkX, chunkZ);
		return inWorld.get(shopChunk);
	}

	/**
	 * Gets a shop in a specific location
	 * 
	 * @param loc
	 *            The location to get the shop from
	 * @return The shop at that location
	 */
	public Shop getShop(Location loc) {
		HashMap<Location, Shop> inChunk = getShops(loc.getChunk());
		if (inChunk == null) {
			return null;
		}
		// We can do this because WorldListener updates the world reference so
		// the world in loc is the same as world in inChunk.get(loc)
		return inChunk.get(loc);
	}

	/**
	 * Adds a shop to the world. Does NOT require the chunk or world to be
	 * loaded
	 * 
	 * @param world
	 *            The name of the world
	 * @param shop
	 *            The shop to add
	 */
	private void addShop(String world, Shop shop) {
		HashMap<ShopChunk, HashMap<Location, Shop>> inWorld = this.getShops().get(world);
		// There's no world storage yet. We need to create that hashmap.
		if (inWorld == null) {
			inWorld = new HashMap<ShopChunk, HashMap<Location, Shop>>(3);
			// Put it in the data universe
			this.getShops().put(world, inWorld);
		}
		// Calculate the chunks coordinates. These are 1,2,3 for each chunk, NOT
		// location rounded to the nearest 16.
		int x = (int) Math.floor((shop.getLocation().getBlockX()) / 16.0);
		int z = (int) Math.floor((shop.getLocation().getBlockZ()) / 16.0);
		// Get the chunk set from the world info
		ShopChunk shopChunk = new ShopChunk(world, x, z);
		HashMap<Location, Shop> inChunk = inWorld.get(shopChunk);
		// That chunk data hasn't been created yet - Create it!
		if (inChunk == null) {
			inChunk = new HashMap<Location, Shop>(1);
			// Put it in the world
			inWorld.put(shopChunk, inChunk);
		}
		// Put the shop in its location in the chunk list.
		inChunk.put(shop.getLocation(), shop);
	}

	/**
	 * Removes a shop from the world. Does NOT remove it from the database. *
	 * REQUIRES * the world to be loaded
	 * 
	 * @param shop
	 *            The shop to remove
	 */
	public void removeShop(Shop shop) {
		Location loc = shop.getLocation();
		String world = loc.getWorld().getName();
		HashMap<ShopChunk, HashMap<Location, Shop>> inWorld = this.getShops().get(world);
		int x = (int) Math.floor((shop.getLocation().getBlockX()) / 16.0);
		int z = (int) Math.floor((shop.getLocation().getBlockZ()) / 16.0);
		ShopChunk shopChunk = new ShopChunk(world, x, z);
		HashMap<Location, Shop> inChunk = inWorld.get(shopChunk);
		inChunk.remove(loc);
	}

	/**
	 * Removes all shops from memory and the world. Does not delete them from
	 * the database. Call this on plugin disable ONLY.
	 */
	public void clear() {
		if (plugin.display) {
			for (World world : Bukkit.getWorlds()) {
				for (Chunk chunk : world.getLoadedChunks()) {
					HashMap<Location, Shop> inChunk = this.getShops(chunk);
					if (inChunk == null)
						continue;
					for (Shop shop : inChunk.values()) {
						shop.onUnload();
					}
				}
			}
		}
		this.actions.clear();
		this.shops.clear();
	}

	/**
	 * Checks other plugins to make sure they can use the chest they're making a
	 * shop.
	 * 
	 * @param p
	 *            The player to check
	 * @param b
	 *            The block to check
	 * @return True if they're allowed to place a shop there.
	 */
	public boolean canBuildShop(Player p, Block b, BlockFace bf) {
		if (plugin.limit) {
			int owned = 0;
			Iterator<Shop> it = getShopIterator();
			while (it.hasNext()) {
				if (it.next().getOwner().equals(p.getUniqueId())) {
					owned++;
				}
			}
			int max = plugin.getShopLimit(p);
			if (owned + 1 > max) {
				p.sendMessage(ChatColor.RED + "You have already created a maximum of " + owned + "/" + max + " shops!");
				return false;
			}
		}
		PlayerInteractEvent pie = new PlayerInteractEvent(p, Action.RIGHT_CLICK_BLOCK, AIR, b, bf); // PIE = PlayerInteractEvent -  What else?
		Bukkit.getPluginManager().callEvent(pie);
		pie.getPlayer().closeInventory(); // If the player has chat open, this
		// will close their chat.
		if (pie.isCancelled()) {
			return false;
		}
		ShopPreCreateEvent spce = new ShopPreCreateEvent(p, b.getLocation());
		Bukkit.getPluginManager().callEvent(spce);
		if (spce.isCancelled()) {
			return false;
		}
		return true;
	}

	public void handleChat(final Player p, String msg) {
		final String message = ChatColor.stripColor(msg).replaceAll("[^\\d]+", "");
		// Use from the main thread, because Bukkit hates life
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				HashMap<UUID, Info> actions = getActions();
				// They wanted to do something.
				Info info = actions.remove(p.getUniqueId());
				if (info == null)
					return; // multithreaded means this can happen
				if (info.getLocation().getWorld() != p.getLocation().getWorld()) {
					p.sendMessage(MsgUtil.getMessage("shop-creation-cancelled"));
					return;
				}
				if (info.getLocation().distanceSquared(p.getLocation()) > 25) {
					p.sendMessage(MsgUtil.getMessage("shop-creation-cancelled"));
					return;
				}
				/* Creation handling */
				if (info.getAction() == ShopAction.CREATE) {
					try {
						// Checking the shop can be created
						if (plugin.getShopManager().getShop(info.getLocation()) != null) {
							p.sendMessage(MsgUtil.getMessage("shop-already-owned"));
							return;
						}
						if (Util.getSecondHalf(info.getLocation().getBlock()) != null && !p.hasPermission("quickshop.create.double")) {
							p.sendMessage(MsgUtil.getMessage("no-double-chests"));
							return;
						}
						if (Util.canBeShop(info.getLocation().getBlock()) == false) {
							p.sendMessage(MsgUtil.getMessage("chest-was-removed"));
							return;
						}
						// Price per item
						double price;
						if (plugin.getConfig().getBoolean("whole-number-prices-only")) {
							price = Integer.parseInt(message);
						} else {
							price = Double.parseDouble(message);
						}
						if (price < 0.01) {
							p.sendMessage(MsgUtil.getMessage("price-too-cheap"));
							return;
						}
						// Check price restriction
						Entry<Double,Double> priceRestriction = Util.getPriceRestriction(info.getItem().getType());
						if (priceRestriction!=null) {
							if (price<priceRestriction.getKey() || price>priceRestriction.getValue()) {
								p.sendMessage(ChatColor.RED+"Restricted prices for "+info.getItem().getType()+": min "+priceRestriction.getKey()+", max "+priceRestriction.getValue());
							}
						}

						double tax = plugin.getConfig().getDouble("shop.cost");
						// Tax refers to the cost to create a shop. Not actual
						// tax, that would be silly
						if (tax != 0 && plugin.getEcon().getBalance(p.getName()) < tax) {
							p.sendMessage(MsgUtil.getMessage("you-cant-afford-a-new-shop", format(tax)));
							return;
						}
						// Create the sample shop.
						Shop shop = new ContainerShop(info.getLocation(), price, info.getItem(), p.getUniqueId());
						shop.onLoad();
						ShopCreateEvent e = new ShopCreateEvent(shop, p);
						Bukkit.getPluginManager().callEvent(e);
						if (e.isCancelled()) {
							shop.onUnload();
							return;
						}
						// This must be called after the event has been called.
						// Else, if the event is cancelled, they won't get their
						// money back.
						if (tax != 0) {
							if (!plugin.getEcon().withdraw(p.getName(), tax)) {
								p.sendMessage(MsgUtil.getMessage("you-cant-afford-a-new-shop", format(tax)));
								shop.onUnload();
								return;
							}
							plugin.getEcon().deposit(plugin.getConfig().getString("tax-account"), tax);
						}
						/* The shop has hereforth been successfully created */
						createShop(shop);
						Location loc = shop.getLocation();
						plugin.log(p.getName() + " created a " + shop.getDataName() + " shop at (" + loc.getWorld().getName() + " - " + loc.getX() + "," + loc.getY() + "," + loc.getZ() + ")");
						if (!plugin.getConfig().getBoolean("shop.lock")) {
							// Warn them if they haven't been warned since
							// reboot
							if (!plugin.warnings.contains(p.getName())) {
								p.sendMessage(MsgUtil.getMessage("shops-arent-locked"));
								plugin.warnings.add(p.getName());
							}
						}
						// Figures out which way we should put the sign on and
						// sets its text.
						if (info.getSignBlock() != null && info.getSignBlock().getType() == Material.AIR && plugin.getConfig().getBoolean("shop.auto-sign")) {
							BlockState bs = info.getSignBlock().getState();
							BlockFace bf = info.getLocation().getBlock().getFace(info.getSignBlock());
							bs.setType(Material.WALL_SIGN);
							Sign sign = (Sign) bs.getData();
							sign.setFacingDirection(bf);
							bs.update(true);
							shop.setSignText();
							/*
							 * Block b = shop.getLocation().getBlock();
							 * ItemFrame iFrame = (ItemFrame)
							 * b.getWorld().spawnEntity(b.getLocation(),
							 * EntityType.ITEM_FRAME);
							 * 
							 * BlockFace[] faces = new
							 * BlockFace[]{BlockFace.NORTH, BlockFace.EAST,
							 * BlockFace.SOUTH, BlockFace.WEST}; for(BlockFace
							 * face : faces){ if(face == bf) continue; //This is
							 * the sign's location iFrame.setFacingDirection(bf,
							 * true); //iFrame.setItem(shop.getItem());
							 * ItemStack iStack = shop.getItem().clone();
							 * iStack.setAmount(0); iFrame.setItem(iStack); /*
							 * Field handleField =
							 * iFrame.getClass().getField("entity");
							 * handleField.setAccessible(true); Object handle =
							 * handleField.get(iFrame);
							 * 
							 * ItemStack bukkitStack = shop.getItem();
							 * 
							 * Field itemStackHandle =
							 * 
							 * Method setItemStack =
							 * handle.getClass().getMethod("a", Object.class);
							 * setItemStack.
							 */
							// }
						}
						if (shop instanceof ContainerShop) {
							ContainerShop cs = (ContainerShop) shop;
							if (cs.isDoubleShop()) {
								Shop nextTo = cs.getAttachedShop();
								if (nextTo.getPrice() > shop.getPrice()) {
									// The one next to it must always be a
									// buying shop.
									p.sendMessage(MsgUtil.getMessage("buying-more-than-selling"));
								}
							}
						}
					}
					/* They didn't enter a number. */
					catch (NumberFormatException ex) {
						p.sendMessage(MsgUtil.getMessage("shop-creation-cancelled"));
						return;
					}
				}
				/* Purchase Handling */
				else if (info.getAction() == ShopAction.BUY) {
					int amount = 0;
					try {
						amount = Integer.parseInt(message);
					} catch (NumberFormatException e) {
						p.sendMessage(MsgUtil.getMessage("shop-purchase-cancelled"));
						return;
					}
					// Get the shop they interacted with
					Shop shop = plugin.getShopManager().getShop(info.getLocation());
					// It's not valid anymore
					if (shop == null || Util.canBeShop(info.getLocation().getBlock()) == false) {
						p.sendMessage(MsgUtil.getMessage("chest-was-removed"));
						return;
					}
					if (info.hasChanged(shop)) {
						p.sendMessage(MsgUtil.getMessage("shop-has-changed"));
						return;
					}
					if (shop.isSelling()) {
						int stock = shop.getRemainingStock();
						if (stock < amount) {
							p.sendMessage(MsgUtil.getMessage("shop-stock-too-low", "" + shop.getRemainingStock(), shop.getDataName()));
							return;
						}
						if (amount == 0) {
							// Dumb.
							MsgUtil.sendPurchaseSuccess(p, shop, amount);
							return;
						} else if (amount < 0) {
							// & Dumber
							p.sendMessage(MsgUtil.getMessage("negative-amount"));
							return;
						}
						int pSpace = Util.countSpace(p.getInventory(), shop.getItem());
						if (amount > pSpace) {
							p.sendMessage(MsgUtil.getMessage("not-enough-space", "" + pSpace));
							return;
						}
						ShopPurchaseEvent e = new ShopPurchaseEvent(shop, p, amount);
						Bukkit.getPluginManager().callEvent(e);
						if (e.isCancelled())
							return; // Cancelled
						// Money handling
						if (!p.getUniqueId().equals(shop.getOwner())) {
							// Check their balance. Works with *most* economy
							// plugins*
							if (plugin.getEcon().getBalance(p.getName()) < amount * shop.getPrice()) {
								p.sendMessage(MsgUtil.getMessage("you-cant-afford-to-buy", format(amount * shop.getPrice()), format(plugin.getEcon().getBalance(p.getName()))));
								return;
							}
							// Don't tax them if they're purchasing from
							// themselves.
							// Do charge an amount of tax though.
							double tax = plugin.getConfig().getDouble("tax");
							if (shop.getOwner().equals(p.getUniqueId()) || Permissions.hasPermission(Bukkit.getOfflinePlayer(shop.getOwner()), "quickshop.taxexemption")) {
								tax = 0;
							}
							
							double total = amount * shop.getPrice();
							if (!plugin.getEcon().withdraw(p.getUniqueId(), total)) {
								p.sendMessage(MsgUtil.getMessage("you-cant-afford-to-buy", format(amount * shop.getPrice()), format(plugin.getEcon().getBalance(p.getName()))));
								return;
							}
							if (!shop.isUnlimited() || plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners")) {
								plugin.getEcon().deposit(shop.getOwner(), total * (1 - tax));
								if (tax != 0) {
									plugin.getEcon().deposit(plugin.getConfig().getString("tax-account"), total * tax);
								}
							}
							// Notify the shop owner
							if (plugin.getConfig().getBoolean("show-tax")) {
								String msg = MsgUtil.getMessage("player-bought-from-your-store-tax", p.getName(), "" + amount, shop.getDataName(), Util.format((tax * total)));
								if (stock == amount)
									msg += "\n" + MsgUtil.getMessage("shop-out-of-stock", "" + shop.getLocation().getBlockX(), "" + shop.getLocation().getBlockY(), "" + shop.getLocation().getBlockZ(), shop.getDataName());
								MsgUtil.send(shop.getOwner(), msg);
							} else {
								String msg = MsgUtil.getMessage("player-bought-from-your-store", p.getName(), "" + amount, shop.getDataName());
								if (stock == amount)
									msg += "\n" + MsgUtil.getMessage("shop-out-of-stock", "" + shop.getLocation().getBlockX(), "" + shop.getLocation().getBlockY(), "" + shop.getLocation().getBlockZ(), shop.getDataName());
								MsgUtil.send(shop.getOwner(), msg);
							}
						}
						// Transfers the item from A to B
						shop.sell(p, amount);
						MsgUtil.sendPurchaseSuccess(p, shop, amount);
						plugin.log(p.getName() + " bought " + amount + " for " + (shop.getPrice() * amount) + " from " + shop.toString());
					} else if (shop.isBuying()) {
						int space = shop.getRemainingSpace();
						if (space < amount) {
							p.sendMessage(MsgUtil.getMessage("shop-has-no-space", "" + space, shop.getDataName()));
							return;
						}
						int count = Util.countItems(p.getInventory(), shop.getItem());
						// Not enough items
						if (amount > count) {
							p.sendMessage(MsgUtil.getMessage("you-dont-have-that-many-items", "" + count, shop.getDataName()));
							return;
						}
						if (amount == 0) {
							// Dumb.
							MsgUtil.sendPurchaseSuccess(p, shop, amount);
							return;
						} else if (amount < 0) {
							// & Dumber
							p.sendMessage(MsgUtil.getMessage("negative-amount"));
							return;
						}
						// Money handling
						if (!p.getUniqueId().equals(shop.getOwner())) {
							// Don't tax them if they're purchasing from
							// themselves.
							// Do charge an amount of tax though.
							double tax = plugin.getConfig().getDouble("tax");
							double total = amount * shop.getPrice();
							if (!shop.isUnlimited() || plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners")) {
								// Tries to check their balance nicely to see if
								// they can afford it.
								if (plugin.getEcon().getBalance(shop.getOwner()) < amount * shop.getPrice()) {
									p.sendMessage(MsgUtil.getMessage("the-owner-cant-afford-to-buy-from-you", format(amount * shop.getPrice()), format(plugin.getEcon().getBalance(shop.getOwner()))));
									return;
								}
								// Check for plugins faking econ.has(amount)
								if (!plugin.getEcon().withdraw(shop.getOwner(), total)) {
									p.sendMessage(MsgUtil.getMessage("the-owner-cant-afford-to-buy-from-you", format(amount * shop.getPrice()), format(plugin.getEcon().getBalance(shop.getOwner()))));
									return;
								}
								if (tax != 0) {
									plugin.getEcon().deposit(plugin.getConfig().getString("tax-account"), total * tax);
								}
							}
							// Give them the money after we know we succeeded
							plugin.getEcon().deposit(p.getName(), total * (1 - tax));
							// Notify the owner of the purchase.
							String msg = MsgUtil.getMessage("player-sold-to-your-store", p.getName(), "" + amount, shop.getDataName());
							if (space == amount)
								msg += "\n" + MsgUtil.getMessage("shop-out-of-space", "" + shop.getLocation().getBlockX(), "" + shop.getLocation().getBlockY(), "" + shop.getLocation().getBlockZ());
							MsgUtil.send(shop.getOwner(), msg);
						}
						shop.buy(p, amount);
						MsgUtil.sendSellSuccess(p, shop, amount);
						plugin.log(p.getName() + " sold " + amount + " for " + (shop.getPrice() * amount) + " to " + shop.toString());
					}
					shop.setSignText(); // Update the signs count
				}
				/* If it was already cancelled (from destroyed) */
				else {
					return; // It was cancelled, go away.
				}
			}
		});
	}

	/**
	 * Returns a new shop iterator object, allowing iteration over shops easily,
	 * instead of sorting through a 3D hashmap.
	 * 
	 * @return a new shop iterator object.
	 */
	public Iterator<Shop> getShopIterator() {
		return new ShopIterator();
	}

	public String format(double d) {
		return plugin.getEcon().format(d);
	}

	public class ShopIterator implements Iterator<Shop> {
		private Iterator<Shop> shops;
		private Iterator<HashMap<Location, Shop>> chunks;
		private Iterator<HashMap<ShopChunk, HashMap<Location, Shop>>> worlds;
		private Shop current;

		public ShopIterator() {
			worlds = getShops().values().iterator();
		}

		/**
		 * Returns true if there is still more shops to iterate over.
		 */
		@Override
		public boolean hasNext() {
			if (shops == null || !shops.hasNext()) {
				if (chunks == null || !chunks.hasNext()) {
					if (!worlds.hasNext()) {
						return false;
					} else {
						chunks = worlds.next().values().iterator();
						return hasNext();
					}
				} else {
					shops = chunks.next().values().iterator();
					return hasNext();
				}
			}
			return true;
		}

		/**
		 * Fetches the next shop. Throws NoSuchElementException if there are no
		 * more shops.
		 */
		@Override
		public Shop next() {
			if (shops == null || !shops.hasNext()) {
				if (chunks == null || !chunks.hasNext()) {
					if (!worlds.hasNext()) {
						throw new NoSuchElementException("No more shops to iterate over!");
					}
					chunks = worlds.next().values().iterator();
				}
				shops = chunks.next().values().iterator();
			}
			if (!shops.hasNext())
				return this.next(); // Skip to the next one (Empty iterator?)
			current = shops.next();
			return current;
		}

		/**
		 * Removes the current shop. This method will delete the shop from
		 * memory and the database.
		 */
		@Override
		public void remove() {
			current.delete(false);
			shops.remove();
		}
	}
}