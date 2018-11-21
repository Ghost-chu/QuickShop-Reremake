package org.maxgamer.quickshop;

import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.maxgamer.quickshop.Command.QS;
import org.maxgamer.quickshop.Database.*;
import org.maxgamer.quickshop.Database.Database.ConnectionException;
import org.maxgamer.quickshop.Economy.Economy;
import org.maxgamer.quickshop.Economy.EconomyCore;
import org.maxgamer.quickshop.Economy.Economy_Vault;
import org.maxgamer.quickshop.Listeners.*;
import org.maxgamer.quickshop.Shop.ContainerShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopManager;
import org.maxgamer.quickshop.Shop.ShopType;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Permissions;
import org.maxgamer.quickshop.Util.Util;
import org.maxgamer.quickshop.Watcher.ItemWatcher;
import org.maxgamer.quickshop.Watcher.LogWatcher;
import org.maxgamer.quickshop.Watcher.UpdateWatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;

//import org.maxgamer.quickshop.Util.CustomPotionsName;
//import org.maxgamer.quickshop.Util.NMS;

public class QuickShop extends JavaPlugin {
	/** The active instance of QuickShop */
	public static QuickShop instance;
	/** The economy we hook into for transactions */
	private Economy economy;
	/** The Shop Manager used to store shops */
	private ShopManager shopManager;
	/**
	 * A set of players who have been warned ("Your shop isn't automatically
	 * locked")
	 */
	public HashSet<String> warnings = new HashSet<String>();
	/** The database for storing all our data for persistence */
	private Database database;
	// Listeners - We decide which one to use at runtime
	private ChatListener chatListener;
	// Listeners (These don't)
	private BlockListener blockListener;
	private PlayerListener playerListener;
	private DisplayProtectionListener inventoryListener;
	private ChunkListener chunkListener;
	private WorldListener worldListener;
	private BukkitTask itemWatcherTask;
	private LogWatcher logWatcher;
	/** Whether players are required to sneak to create/buy from a shop */
	public boolean sneak;
	/** Whether players are required to sneak to create a shop */
	public boolean sneakCreate;
	/** Whether players are required to sneak to trade with a shop */
	public boolean sneakTrade;
	/** Whether we should use display items or not */
	public boolean display = true;
	/**
	 * Whether we players are charged a fee to change the price on their shop (To
	 * help deter endless undercutting
	 */
	public boolean priceChangeRequiresFee = false;
	/** Whether or not to limit players shop amounts */
	public boolean limit = false;

	/** The plugin OpenInv (null if not present) */
	public Plugin openInvPlugin;

	private HashMap<String, Integer> limits = new HashMap<String, Integer>();
	/** Use SpoutPlugin to get item / block names */
	public boolean useSpout = false;
	// private Metrics metrics;

	MultiverseCore mPlugin = null;
	private int displayItemCheckTicks;
	private boolean noopDisable;
	private boolean setupDBonEnableding = false;
	public String dbPrefix="";
	public ProtocolLib protocolLibPlugin;
	/** The plugin metrics from Hidendra */
	// public Metrics getMetrics(){ return metrics; }
	public int getShopLimit(Player p) {
		int max = getConfig().getInt("limits.default");
		for (Entry<String, Integer> entry : limits.entrySet()) {
			if (entry.getValue() > max && p.hasPermission(entry.getKey()))
				max = entry.getValue();
		}
		return max;
	}

	@SuppressWarnings("resource")
	public void onEnable() {
		instance = this;
		getLogger().info("Quickshop Reremake by Ghost_chu(Minecraft SunnySide Server Community)");
		getLogger().info("THIS VERSION ONLY SUPPORT BUKKIT API 1.13-1.13.x VERSION!");
		getLogger().info("Author:Ghost_chu");
		getLogger().info("Original author:Netherfoam, Timtower, KaiNoMood");
		getLogger().info("Let's us start load plugin");
		// NMS.init();
		try {
			getServer().spigot();
		} catch (Exception e) {
			getLogger().severe("FATAL: QSRR only can running on Spigot and Spigot's forks server!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		saveDefaultConfig(); // Creates the config folder and copies config.yml
								// (If one doesn't exist) as required.
		reloadConfig(); // Reloads messages.yml too, aswell as config.yml and
						// others.
		getConfig().options().copyDefaults(true); // Load defaults.
		if (Util.isDevEdition()) {
			getLogger().severe("WARNING: You are running QSRR on dev-mode");
			getLogger().severe("WARNING: Keep backup and DO NOT running on production environment!");
			getLogger().severe("WARNING: Test version may destory anything!");
			getLogger().severe(
					"WARNING: QSRR won't start without you confirm, nothing will changes before you turn on dev allowed.");
			if (!getConfig().getBoolean("dev-mode")) {
				getLogger().severe(
						"WARNING: Set dev-mode: true in config.yml to allow qs load on dev mode(Maybe need add this line by your self).");
				noopDisable = true;
				Bukkit.getPluginManager().disablePlugin(this);
				return;
			}
		}
		if (loadEcon() == false)
			return;
		// ProtocolLib Support
		// protocolManager = ProtocolLibrary.getProtocolManager();

		if (Permissions.init()) {
			getLogger().info("Found permission provider.");
		} else {
			getLogger().info("Couldn't find a Vault permission provider. Some feature may be limited.");
		}

		mPlugin = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");

		if (mPlugin != null) {
			getLogger().info("Successfully loaded MultiverseCore support!");
		}
		protocolLibPlugin = (ProtocolLib) Bukkit.getPluginManager().getPlugin("ProtocolLib");
		if (protocolLibPlugin != null) {
			getLogger().info("Successfully loaded ProtocolLib support!");
		}
		if(getConfig().getInt("config-version")==0)
			getConfig().set("config-version", 1);
		updateConfig(getConfig().getInt("config-version"));

		// Initialize Util
		Util.initialize();

		// Create the shop manager.
		this.shopManager = new ShopManager(this);
		if (this.display) {
			// Display item handler thread
			getLogger().info("Starting item scheduler");
			ItemWatcher itemWatcher = new ItemWatcher(this);
			itemWatcherTask = Bukkit.getScheduler().runTaskTimer(this, itemWatcher, 600, 600);
		}
		if (this.getConfig().getBoolean("log-actions")) {
			// Logger Handler
			this.logWatcher = new LogWatcher(this, new File(this.getDataFolder(), "qs.log"));
			logWatcher.task = Bukkit.getScheduler().runTaskTimerAsynchronously(this, this.logWatcher, 150, 150);
		}
		if (getConfig().getBoolean("shop.lock")) {
			LockListener ll = new LockListener(this);
			getServer().getPluginManager().registerEvents(ll, this);
		}
		getServer().getPluginManager().registerEvents(new UpdateWatcher(), this);
//		ConfigurationSection cPotionSection = this.getConfig().getConfigurationSection("custom-potions-name");
//		if (cPotionSection!=null) {
//			CustomPotionsName.setSignFormat(new String[]{cPotionSection.getString("sign.format"), cPotionSection.getString("sign.variety.normal"), cPotionSection.getString("sign.variety.splash"), cPotionSection.getString("sign.variety.lingering")});
//			CustomPotionsName.setShopInfoFormat(new String[]{cPotionSection.getString("shopinfo.format"), cPotionSection.getString("shopinfo.variety.normal"), cPotionSection.getString("shopinfo.variety.splash"), cPotionSection.getString("shopinfo.variety.lingering")});
//
//			Map<PotionType,CustomPotionsName.Names> potionTypes = new HashMap<PotionType,CustomPotionsName.Names>();
//			for (String s : cPotionSection.getStringList("types")) {
//				try {
//					String[] a = s.split("[;]");
//					if (a.length!=3) {
//						throw new Exception("Invalid format (main args length must be 3)");
//					}
//					
//					PotionType type = PotionType.valueOf(a[0].toUpperCase());
//					if (type==null) {
//						throw new Exception("Invalid PotionType "+a[0]);
//					}
//					
//					potionTypes.put(type, new CustomPotionsName.Names(a[1], a[2]));
//				} catch (Exception e) {
//					Bukkit.getLogger().warning("Invalid potion item name definition {"+s+"} Error: "+e.getMessage());
//				}
//			}
//			CustomPotionsName.setPotionTypes(potionTypes);
//			
//			Map<PotionEffectType,String> potionEffects = new HashMap<PotionEffectType,String>();
//			for (String s : cPotionSection.getStringList("effects")) {
//				try {
//					String[] a = s.split("[;]");
//					if (a.length!=2) {
//						throw new Exception("Invalid format (main args length must be 2)");
//					}
//					
//					PotionEffectType type = PotionEffectType.getByName(a[0]);
//					if (type==null) {
//						throw new Exception("Invalid PotionEffectType "+a[0]);
//					}
//					
//					potionEffects.put(type, a[1]);
//				} catch (Exception e) {
//					Bukkit.getLogger().warning("Invalid potion effect type name definition {"+s+"} Error: "+e.getMessage());
//				}
//			}
//			CustomPotionsName.setPotionEffects(potionEffects);
//		}
//	
		ConfigurationSection limitCfg = this.getConfig().getConfigurationSection("limits");
		if (limitCfg != null) {
			getLogger().info("Limit cfg found...");
			this.limit = limitCfg.getBoolean("use", false);
			getLogger().info("Limits.use: " + limit);
			limitCfg = limitCfg.getConfigurationSection("ranks");
			for (String key : limitCfg.getKeys(true)) {
				limits.put(key, limitCfg.getInt(key));
			}
			getLogger().info(limits.toString());
		}
		setupDBonEnableding = true;
		setupDatabase();
		setupDBonEnableding = false;
		/* Load shops from database to memory */
		int count = 0; // Shops count
		MsgUtil.loadItemi18n();
		MsgUtil.loadEnchi18n();
		MsgUtil.loadPotioni18n();
		try {
			getLogger().info("Loading shops from database...");
			/*
			 * int res = Converter.convert(); if (res < 0) {
			 * plugin.getLogger().log(Level.WARNING, "Could not convert shops. Exitting.");
			 * return; } if (res > 0) { plugin.getLogger().log(Level.WARNING,
			 * "Conversion success. Continuing..."); }
			 */
//			con = database.getConnection();
//			PreparedStatement ps = con.prepareStatement("SELECT * FROM shops");
			ResultSet rs = DatabaseHelper.selectAllShops(database);
			int errors = 0;

			boolean isBackuped = false;

			while (rs.next()) {
				int x = 0;
				int y = 0;
				int z = 0;
				String worldName = null;
				ItemStack item = null;
				String owner = null;
				UUID ownerUUID = null;
				String step = "while init";
				// ==========================================================================================
				try {
					x = rs.getInt("x");
					y = rs.getInt("y");
					z = rs.getInt("z");
					worldName = rs.getString("world");
					World world = Bukkit.getWorld(worldName);
					if (world == null && mPlugin != null) {
						// Maybe world not loaded? Try call MV to load world.
						mPlugin.getCore().getMVWorldManager().loadWorld(worldName);
						world = Bukkit.getWorld(worldName);
					}
					item = Util.deserialize(rs.getString("itemConfig"));
					owner = rs.getString("owner");
					ownerUUID = null;
					step = "Covert owner to UUID";
					try {
						ownerUUID = UUID.fromString(owner);
					} catch (IllegalArgumentException e) {
						// This could be old data to be converted... check if it's a player
						step = "Update owner to UUID";
						// Because need update database, so use crossed method, Set ignore.
						@SuppressWarnings("deprecation")
						OfflinePlayer player = Bukkit.getOfflinePlayer(owner);
						if (player.hasPlayedBefore()) {
							ownerUUID = player.getUniqueId();
							DatabaseHelper.updateOwner2UUID(ownerUUID.toString(), x, y, z, worldName);
						} else {
							// Invalid shop owner
//							getDB().getConnection().createStatement()
//									.executeUpdate("DELETE FROM shops WHERE x = " + x + " AND y = " + y + " AND z = "
//											+ z + " AND world = \"" + worldName + "\""
//											+ (getDB().getCore() instanceof MySQLCore ? " LIMIT 1" : ""));
//							getDB().getConnection().createStatement()
//							.executeUpdate("DELETE FROM schedule WHERE x = " + x + " AND y = " + y + " AND z = "
//									+ z + " AND world = \"" + worldName + "\""
//									+ (getDB().getCore() instanceof MySQLCore ? " LIMIT 1" : ""));
							DatabaseHelper.removeShop(database, x, y, z, worldName);
							continue;
						}
					}
					step = "Loading shop price";
					double price = rs.getDouble("price");
					step = "Createing Location object";
					Location loc = new Location(world, x, y, z);
					/* Skip invalid shops, if we know of any */
					step = "Checking InventoryHolder";
					if (world != null && (loc.getBlock().getState() instanceof InventoryHolder) == false) {
						step = "Removeing shop in world: Because it not a correct InventoryHolder";
						getLogger().info("Shop is not an InventoryHolder in " + rs.getString("world") + " at: " + x
								+ ", " + y + ", " + z + ".  Deleting.");
//						getDB().getConnection().createStatement()
//								.executeUpdate("DELETE FROM shops WHERE x = " + x + " AND y = " + y + " AND z = " + z
//										+ " AND world = \"" + worldName + "\""
//										+ (getDB().getCore() instanceof MySQLCore ? " LIMIT 1" : ""));
//						getDB().getConnection().createStatement()
//						.executeUpdate("DELETE FROM schedule WHERE x = " + x + " AND y = " + y + " AND z = " + z
//								+ " AND world = \"" + worldName + "\""
//								+ (getDB().getCore() instanceof MySQLCore ? " LIMIT 1" : ""));
						DatabaseHelper.removeShop(database, x, y, z, worldName);
						continue;
					}
					step = "Loading shop type";
					int type = rs.getInt("type");
					step = "Loading shop in world";
					Shop shop = new ContainerShop(loc, price, item, ownerUUID);
					step = "Setting shop unlitmited status";
					shop.setUnlimited(rs.getBoolean("unlimited"));
					step = "Setting shop type";
					shop.setShopType(ShopType.fromID(type));
					step = "Loading shop to memory";
					shopManager.loadShop(rs.getString("world"), shop);

					if (getConfig().getBoolean("ongoingfee.reset-on-startup")) {
						step = "Reset schedule data";
						getDB().getConnection().createStatement()
								.executeUpdate("DELETE FROM " + QuickShop.instance.dbPrefix + "schedule WHERE x = " + x
										+ " AND y = " + y + " AND z = " + z + " AND world = \"" + worldName + "\""
										+ (getDB().getCore() instanceof MySQLCore ? " LIMIT 1" : ""));
					}

					step = "Checking shop schedule data";
					// Check shop is or not exist in schedule table
					Statement st = getDB().getConnection().createStatement();
					String checkq = "SELECT * FROM " + QuickShop.instance.dbPrefix
							+ "schedule WHERE owner ='{owner}' and world ='{world}' and x ='{x}' and y ='{y}' and z='{z}' and timestamp ='%'";
					checkq.replace("{x}", String.valueOf(loc.getBlockX()));
					checkq.replace("{y}", String.valueOf(loc.getBlockY()));
					checkq.replace("{z}", String.valueOf(loc.getBlockZ()));
					checkq.replace("{world}", String.valueOf(loc.getWorld().getName()));
					checkq.replace("{owner}", shop.getOwner().toString());
					ResultSet resultSet = st.executeQuery(checkq);
					if (!resultSet.next()) {
						// Not exist, write in
						step = "Writeing shop schedule data";
						String scheduleq = "INSERT INTO " + QuickShop.instance.dbPrefix
								+ "schedule (owner, world, x, y, z, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
						getDB().execute(scheduleq, shop.getOwner().toString(), loc.getWorld().getName(),
								loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), System.currentTimeMillis());
					}

					if (loc.getWorld() != null && loc.getChunk().isLoaded()) {
						step = "Loading shop to memory >> Chunk loaded, Loaded to memory";
						shop.onLoad();
						shop.setSignText();
					} else {
						step = "Loading shop to memory >> Chunk not loaded, Skipping";
					}
					step = "Finish";
					count++;
				} catch (Exception e) {
					errors++;
					getLogger().severe("Error loading a shop! Coords: Location[" + worldName + " (" + x + ", " + y
							+ ", " + z + ")] Item: " + item.getType().name() + "...");
					getLogger().severe("Are you deleted world included QuickShop shops? All shops will auto fixed.");

					getLogger().severe("===========Error Reporting Start===========");
					getLogger().severe("#Java throw >>");
					getLogger().severe("StackTrace:");
					e.printStackTrace();
					getLogger().severe("#Shop data >>");
					getLogger().severe("Location: " + worldName + ";(X:" + x + ", Y:" + y + ", Z:" + z + ")");
					getLogger().severe(
							"Item: " + item.getType().name() + " MetaData: " + item.getItemMeta().spigot().toString());
					getLogger().severe("Owner: " + owner + "(" + ownerUUID.toString() + ")");
					try {
						getLogger().severe(
								"BukkitWorld: " + Bukkit.getWorld(worldName).getName() + " [" + worldName + "]");
					} catch (Exception e2) {
						getLogger().severe("BukkitWorld: WARNING:World not exist! [" + worldName + "]");
					}
					try {
						getLogger().severe(
								"Target Block: " + Bukkit.getWorld(worldName).getBlockAt(x, y, z).getType().name());
					} catch (Exception e2) {
						getLogger().severe("Target Block: Can't get block!");
					}
					getLogger().severe("#Database info >>");

					getLogger().severe("Connected:" + !getDB().getConnection().isClosed());
					getLogger().severe("Read Only:" + getDB().getConnection().isReadOnly());

					if (getDB().getConnection().getClientInfo() != null) {
						getLogger().severe("Client Info: " + getDB().getConnection().getClientInfo().toString());
					} else {
						getLogger().severe("Client Info: null");
					}
					getLogger().severe("Read Only:" + getDB().getConnection().isReadOnly());

					getLogger().severe("#Debuging >>");
					getLogger().severe("Runnnig on step: " + step);

					getLogger().severe("#Tips >>");
					getLogger().severe("Please report this issues to author, And you database will auto backup!");

					if (!isBackuped) {
						File sqlfile = new File(Bukkit.getPluginManager().getPlugin("QuickShop").getDataFolder()
								.getAbsolutePath().toString() + "/shop.db");
						if (!sqlfile.exists()) {
							getLogger().severe("Failed to backup! (File not found)");
						}
						String uuid = UUID.randomUUID().toString().replaceAll("_", "");
						File bksqlfile = new File(Bukkit.getPluginManager().getPlugin("QuickShop").getDataFolder()
								.getAbsolutePath().toString() + "/shop_backup_" + uuid + ".db");
						try {
							bksqlfile.createNewFile();
						} catch (IOException e1) {
							e1.printStackTrace();
							getLogger().severe("Failed to backup! (Create)");
						}
						FileChannel inputChannel = null;
						FileChannel outputChannel = null;
						try {
							inputChannel = new FileInputStream(sqlfile).getChannel();
							outputChannel = new FileOutputStream(bksqlfile).getChannel();
							outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
						} catch (Exception e3) {
							e3.printStackTrace();
							getLogger().severe("Failed to backup! (Copy)");
						}
						try {
							inputChannel.close();
							outputChannel.close();
						} catch (IOException e1) {
							inputChannel = null;
							outputChannel = null;
						}

					}
					getLogger().severe("===========Error Reporting End===========");

					if (errors < 3) {
						getLogger().info("Removeing shop from database...");
						// return;
//						PreparedStatement delps = getDB().getConnection()
//								.prepareStatement("DELETE FROM shops WHERE x = ? AND y = ? and z = ? and world = ?");
//						delps.setInt(1, x);
//						delps.setInt(2, y);
//						delps.setInt(3, z);
//						delps.setString(4, worldName);
//						delps.execute();

						DatabaseHelper.removeShop(database, x, y, z, worldName);
//						
//						PreparedStatement scdelps = getDB().getConnection()
//								.prepareStatement("DELETE FROM schedule WHERE x = ? AND y = ? and z = ? and world = ?");
//						scdelps.setInt(1, x);
//						scdelps.setInt(2, y);
//						scdelps.setInt(3, z);
//						scdelps.setString(4, worldName);
//						scdelps.execute();
						getLogger().info("Trying keep loading...");
					} else {
						getLogger().severe(
								"Multiple errors in shops - Something seems to be wrong with your shops database! Please check it out immediately!");
						getLogger().info("Removeing shop from database...");
//						PreparedStatement delps = getDB().getConnection()
//								.prepareStatement("DELETE FROM shops WHERE x = ? AND y = ? and z = ? and world = ?");
//						delps.setInt(1, x);
//						delps.setInt(2, y);
//						delps.setInt(3, z);
//						delps.setString(4, worldName);
//						delps.execute();
//						
//						PreparedStatement scdelps = getDB().getConnection()
//								.prepareStatement("DELETE FROM schedule WHERE x = ? AND y = ? and z = ? and world = ?");
//						scdelps.setInt(1, x);
//						scdelps.setInt(2, y);
//						scdelps.setInt(3, z);
//						scdelps.setString(4, worldName);
//						scdelps.execute();
						DatabaseHelper.removeShop(database, x, y, z, worldName);
						e.printStackTrace();
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			getLogger().severe("Could not load shops Because SQLException.");
		}

		getLogger().info("Loaded " + count + " shops.");

		// Register events
		getLogger().info("Registering Listeners");
		blockListener = new BlockListener(this);
		playerListener = new PlayerListener(this);
		worldListener = new WorldListener(this);
		chatListener = new ChatListener(this);

		Bukkit.getServer().getPluginManager().registerEvents(blockListener, this);
		Bukkit.getServer().getPluginManager().registerEvents(playerListener, this);
		Bukkit.getServer().getPluginManager().registerEvents(chatListener, this);
		if (this.display) {
			inventoryListener = new DisplayProtectionListener(this);
			chunkListener = new ChunkListener(this);
			Bukkit.getServer().getPluginManager().registerEvents(inventoryListener, this);
			Bukkit.getServer().getPluginManager().registerEvents(chunkListener, this);
		}
		Bukkit.getServer().getPluginManager().registerEvents(worldListener, this);
		// Command handlers
		QS commandExecutor = new QS(this);
		getCommand("qs").setExecutor(commandExecutor);
		if (getConfig().getInt("shop.find-distance") > 100) {
			getLogger().severe("Shop.find-distance is too high! It may cause lag! Pick a number under 100!");
		}

		if (display && displayItemCheckTicks > 0) {
			new BukkitRunnable() {
				@Override
				public void run() {
					Iterator<Shop> it = getShopManager().getShopIterator();
					while (it.hasNext()) {
						Shop shop = it.next();
						if (shop instanceof ContainerShop) {
							ContainerShop cShop = (ContainerShop) shop;
							if (cShop.checkDisplayMoved()) {
								log("Display item for " + shop
										+ " is not on the correct location and has been removed. Probably someone is trying to cheat.");
								for (Player player : getServer().getOnlinePlayers()) {
									if (player.hasPermission("quickshop.alerts")) {
										player.sendMessage(ChatColor.RED + "[QuickShop] Display item for " + shop
												+ " is not on the correct location and has been removed. Probably someone is trying to cheat.");
									}
								}
								cShop.getDisplayItem().remove();
							}
						}
					}
				}
			}.runTaskTimer(this, 1L, displayItemCheckTicks);
		}

		// added for compatibility reasons with OpenInv - see
		// https://github.com/KaiKikuchi/QuickShop/issues/139
		this.openInvPlugin = Bukkit.getPluginManager().getPlugin("OpenInv");

		MsgUtil.loadTransactionMessages();
		MsgUtil.clean();
		getLogger().info("QuickShop loaded!");
		if (getConfig().getBoolean("disabled-metrics") != true) {
			String serverVer = Bukkit.getVersion();
			String bukkitVer = Bukkit.getBukkitVersion();
			String serverName = Bukkit.getServerName();
			Metrics metrics = new Metrics(this);
			// Use internal Metric class not Maven for solve plugin name issues
			String display_Items;
			if (getConfig().getBoolean("shop.display-items")) { // Maybe mod server use this plugin more? Or have big
																// number items need disabled?
				display_Items = "Enabled";
			} else {
				display_Items = "Disabled";
			}

			String locks;
			if (getConfig().getBoolean("shop.lock")) {
				locks = "Enabled";
			} else {
				locks = "Disabled";
			}

			String sneak_action;
			if (getConfig().getBoolean("shop.sneak-to-create") || getConfig().getBoolean("shop.sneak-to-trade")) {
				sneak_action = "Enabled";
			} else {
				sneak_action = "Disabled";
			}

			String use_protect_minecart;
			if (getConfig().getBoolean("protect.minecart")) {
				use_protect_minecart = "Enabled";
			} else {
				use_protect_minecart = "Disabled";
			}
			String use_protect_entity;
			if (getConfig().getBoolean("protect.entity")) {
				use_protect_entity = "Enabled";
			} else {
				use_protect_entity = "Disabled";
			}
			String use_protect_redstone;
			if (getConfig().getBoolean("protect.redstone")) {
				use_protect_redstone = "Enabled";
			} else {
				use_protect_redstone = "Disabled";
			}
			String use_protect_structuregrow;
			if (getConfig().getBoolean("protect.structuregrow")) {
				use_protect_structuregrow = "Enabled";
			} else {
				use_protect_structuregrow = "Disabled";
			}
			String use_protect_explode;
			if (getConfig().getBoolean("protect.explode")) {
				use_protect_explode = "Enabled";
			} else {
				use_protect_explode = "Disabled";
			}
			String use_protect_hopper;
			if (getConfig().getBoolean("protect.hopper")) {
				use_protect_hopper = "Enabled";
			} else {
				use_protect_hopper = "Disabled";
			}

			String shop_find_distance = getConfig().getString("shop.find-distance");

			String float_filter_enable = boolean2String(getConfig().getBoolean("float.enable"));
			String float_filter_item_enable = boolean2String(getConfig().getBoolean("float.item.enable"));
			String float_filter_lore_enable = boolean2String(getConfig().getBoolean("float.lore.enable"));
			String float_filter_displayname_enable = boolean2String(getConfig().getBoolean("float.displayname.enable"));

			String float_filter_item_blacklist = boolean2String(getConfig().getBoolean("float.item.blacklist"));
			String float_filter_lore_blacklist = boolean2String(getConfig().getBoolean("float.lore.blacklist"));
			String float_filter_displayname_blacklist = boolean2String(
					getConfig().getBoolean("float.displayname.blacklist"));

			// Version
			metrics.addCustomChart(new Metrics.SimplePie("server_version", () -> serverVer));
			metrics.addCustomChart(new Metrics.SimplePie("bukkit_version", () -> bukkitVer));
			metrics.addCustomChart(new Metrics.SimplePie("server_name", () -> serverName));
			metrics.addCustomChart(new Metrics.SimplePie("use_display_items", () -> display_Items));
			metrics.addCustomChart(new Metrics.SimplePie("use_locks", () -> locks));
			metrics.addCustomChart(new Metrics.SimplePie("use_sneak_action", () -> sneak_action));
			metrics.addCustomChart(new Metrics.SimplePie("use_protect_minecart", () -> use_protect_minecart));
			metrics.addCustomChart(new Metrics.SimplePie("use_protect_entity", () -> use_protect_entity));
			metrics.addCustomChart(new Metrics.SimplePie("use_protect_redstone", () -> use_protect_redstone));
			metrics.addCustomChart(new Metrics.SimplePie("use_protect_structuregrow", () -> use_protect_structuregrow));
			metrics.addCustomChart(new Metrics.SimplePie("use_protect_explode", () -> use_protect_explode));
			metrics.addCustomChart(new Metrics.SimplePie("use_protect_hopper", () -> use_protect_hopper));
			metrics.addCustomChart(new Metrics.SimplePie("shop_find_distance", () -> shop_find_distance));
			// Exp for stats, maybe i need improve this, so i add this.
			metrics.addCustomChart(new Metrics.SimplePie("float_filter_enable", () -> float_filter_enable));

			metrics.addCustomChart(new Metrics.SimplePie("float_filter_item_enable", () -> float_filter_item_enable));
			metrics.addCustomChart(new Metrics.SimplePie("float_filter_lore_enable", () -> float_filter_lore_enable));
			metrics.addCustomChart(
					new Metrics.SimplePie("float_filter_displayname_enable", () -> float_filter_displayname_enable));

			metrics.addCustomChart(
					new Metrics.SimplePie("float_filter_item_blacklist", () -> float_filter_item_blacklist));
			metrics.addCustomChart(
					new Metrics.SimplePie("float_filter_lore_blacklist", () -> float_filter_lore_blacklist));
			metrics.addCustomChart(new Metrics.SimplePie("float_filter_displayname_blacklist",
					() -> float_filter_displayname_blacklist));

			metrics.submitData(); // Submit now!
			getLogger().info("Mertics submited.");
		} else {
			getLogger().info("You have disabled mertics, Skipping...");
		}
		UpdateWatcher.init();
	}

	public boolean setupDatabase() {
		try {
			ConfigurationSection dbCfg = getConfig().getConfigurationSection("database");
			DatabaseCore dbCore;
			if (dbCfg.getBoolean("mysql")) {
				// MySQL database - Required database be created first.
				dbPrefix = dbCfg.getString("prefix");
				if (dbPrefix==null || dbPrefix.equals("none"))
					dbPrefix = "";
				String user = dbCfg.getString("user");
				String pass = dbCfg.getString("password");
				String host = dbCfg.getString("host");
				String port = dbCfg.getString("port");
				String database = dbCfg.getString("database");
				dbCore = new MySQLCore(host, user, pass, database, port);
			} else {
				// SQLite database - Doing this handles file creation
				dbCore = new SQLiteCore(new File(this.getDataFolder(), "shops.db"));
			}
			this.database = new Database(dbCore);
			// Make the database up to date
			DatabaseHelper.setup(getDB());
		} catch (ConnectionException e) {
			e.printStackTrace();
			if (setupDBonEnableding) {
				getLogger().severe("Error connecting to database. Aborting plugin load.");
				getServer().getPluginManager().disablePlugin(this);
			} else {
				getLogger().severe("Error connecting to database.");
			}
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
			if (setupDBonEnableding) {
				getLogger().severe("Error setting up database. Aborting plugin load.");
				getServer().getPluginManager().disablePlugin(this);
			} else {
				getLogger().severe("Error setting up database.");
			}
			return false;
		}
		return true;
	}

	public String boolean2String(boolean bool) {
		if (bool) {
			return "Enabled";
		} else {
			return "Disabled";
		}
	}


	public void updateConfig(int selectedVersion) {
		if (selectedVersion == 1) {
			getConfig().set("disabled-metrics", false);
			getConfig().set("config-version", 2);
			selectedVersion = 2;
			saveConfig();
			reloadConfig();
		}
		if (selectedVersion == 2) {
			getConfig().set("protect.minecart", true);
			getConfig().set("protect.entity", true);
			getConfig().set("protect.redstone", true);
			getConfig().set("protect.structuregrow", true);
			getConfig().set("protect.explode", true);
			getConfig().set("protect.hopper", true);
			getConfig().set("config-version", 3);
			selectedVersion = 3;
			saveConfig();
			reloadConfig();
		}
		if (selectedVersion == 3) {
			getConfig().set("shop.alternate-currency-symbol", '$');
			getConfig().set("config-version", 4);
			selectedVersion = 4;
			saveConfig();
			reloadConfig();
		}
		if (selectedVersion == 4) {
			getConfig().set("updater", true);
			getConfig().set("config-version", 5);
			selectedVersion = 5;
			saveConfig();
			reloadConfig();
		}
		if (selectedVersion == 5) {
			getConfig().set("shop.display-item-use-name", true);
			getConfig().set("config-version", 6);
			selectedVersion = 6;
			saveConfig();
			reloadConfig();
		}
		if (selectedVersion == 6) {
			getConfig().set("shop.sneak-to-control", false);
			getConfig().set("config-version", 7);
			selectedVersion = 7;
			saveConfig();
			reloadConfig();
		}
		if (selectedVersion == 7) {
			getConfig().set("database.prefix", "none");
			getConfig().set("database.reconnect", false);
			getConfig().set("database.use-varchar", false);
			getConfig().set("config-version", 8);
			selectedVersion = 8;
			saveConfig();
			reloadConfig();
		}
		
	}

	/** Reloads QuickShops config */
	@Override
	public void reloadConfig() {
		super.reloadConfig();
		// Load quick variables
		this.display = this.getConfig().getBoolean("shop.display-items");
		this.sneak = this.getConfig().getBoolean("shop.sneak-only");
		this.sneakCreate = this.getConfig().getBoolean("shop.sneak-to-create");
		this.sneakTrade = this.getConfig().getBoolean("shop.sneak-to-trade");
		this.priceChangeRequiresFee = this.getConfig().getBoolean("shop.price-change-requires-fee");
		this.displayItemCheckTicks = this.getConfig().getInt("shop.display-items-check-ticks");
		MsgUtil.loadCfgMessages();
	}

	/**
	 * Tries to load the economy and its core. If this fails, it will try to use
	 * vault. If that fails, it will return false.
	 * 
	 * @return true if successful, false if the core is invalid or is not found, and
	 *         vault cannot be used.
	 */
	public boolean loadEcon() {
		try {
			EconomyCore core = new Economy_Vault();
			if (core == null || !core.isValid()) {
				// getLogger().severe("Economy is not valid!");
				getLogger().severe("QuickShop could not hook an economy/Not found Vault!");
				getLogger().severe("QuickShop CANNOT start!");
				this.getPluginLoader().disablePlugin(this);
				// if(econ.equals("Vault"))
				// getLogger().severe("(Does Vault have an Economy to hook into?!)");
				return false;
			} else {
				this.economy = new Economy(core);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			getLogger().severe("QuickShop could not hook an economy/Not found Vault!");
			getLogger().severe("QuickShop CANNOT start!");
			this.getPluginLoader().disablePlugin(this);
			return false;
		}
	}

	public void onDisable() {
	    if (noopDisable)
	        return;
		if (itemWatcherTask != null) {
			itemWatcherTask.cancel();
		}
		if (logWatcher != null) {
			logWatcher.task.cancel();
			logWatcher.close(); // Closes the file
		}
		/* Unload UpdateWatcher */
		UpdateWatcher.uninit();
		/* Remove all display items, and any dupes we can find */
		shopManager.clear();
		/* Empty the buffer */
		database.close();
		/* Close Database */
		try {
			this.database.getConnection().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.warnings.clear();
		this.reloadConfig();
	}

	/**
	 * Returns the economy for moving currency around
	 * 
	 * @return The economy for moving currency around
	 */
	public EconomyCore getEcon() {
		return economy;
	}

	/**
	 * Logs the given string to qs.log, if QuickShop is configured to do so.
	 * 
	 * @param s The string to log. It will be prefixed with the date and time.
	 */
	public void log(String s) {
		if (this.logWatcher == null)
			return;
		Date date = Calendar.getInstance().getTime();
		Timestamp time = new Timestamp(date.getTime());
		this.logWatcher.add("[" + time.toString() + "] " + s);
	}

	/**
	 * @return Returns the database handler for queries etc.
	 */
	public Database getDB() {
		return this.database;
	}
	public ProtocolManager getProtocolLib() {
		if(this.protocolLibPlugin==null)
			return null;
		return ProtocolLibrary.getProtocolManager();
	}

	/**
	 * Returns the ShopManager. This is used for fetching, adding and removing
	 * shops.
	 * 
	 * @return The ShopManager.
	 */
	public ShopManager getShopManager() {
		return this.shopManager;
	}
	
	/**
	 * Returns QS version, this method only exist on QSRR forks If running other
	 * QSRR forks,, result may not is "Reremake x.x.x" If running QS offical, Will
	 * throw exception.
	 * 
	 * @return Plugin Version
	 */
	public static String getVersion() {
		return "Reremake "+QuickShop.instance.getDescription().getVersion();
	}

}
