package org.maxgamer.quickshop;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.UUID;

import org.bstats.bukkit.Metrics;
import org.bstats.bukkit.Metrics.CustomChart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.maxgamer.quickshop.Command.QS;
import org.maxgamer.quickshop.Database.Database;
import org.maxgamer.quickshop.Database.Database.ConnectionException;
import org.maxgamer.quickshop.Database.DatabaseCore;
import org.maxgamer.quickshop.Database.DatabaseHelper;
import org.maxgamer.quickshop.Database.MySQLCore;
import org.maxgamer.quickshop.Database.SQLiteCore;
import org.maxgamer.quickshop.Economy.Economy;
import org.maxgamer.quickshop.Economy.EconomyCore;
import org.maxgamer.quickshop.Economy.Economy_Vault;
import org.maxgamer.quickshop.Listeners.BlockListener;
import org.maxgamer.quickshop.Listeners.ChatListener;
import org.maxgamer.quickshop.Listeners.ChunkListener;
import org.maxgamer.quickshop.Listeners.DisplayProtectionListener;
import org.maxgamer.quickshop.Listeners.LockListener;
import org.maxgamer.quickshop.Listeners.PlayerListener;
import org.maxgamer.quickshop.Listeners.WorldListener;
import org.maxgamer.quickshop.Shop.ContainerShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopManager;
import org.maxgamer.quickshop.Shop.ShopType;
//import org.maxgamer.quickshop.Util.CustomPotionsName;
import org.maxgamer.quickshop.Util.MsgUtil;
//import org.maxgamer.quickshop.Util.NMS;
import org.maxgamer.quickshop.Util.Permissions;
import org.maxgamer.quickshop.Util.Util;
import org.maxgamer.quickshop.Watcher.ItemWatcher;
import org.maxgamer.quickshop.Watcher.LogWatcher;


@SuppressWarnings("deprecation")
public class QuickShop extends JavaPlugin {
	/** The active instance of QuickShop */
	public static QuickShop instance;
	/** The economy we hook into for transactions */
	private Economy economy;
	/** The Shop Manager used to store shops */
	private ShopManager shopManager;
	/**
	 * A set of players who have been warned
	 * ("Your shop isn't automatically locked")
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
	 * Whether we players are charged a fee to change the price on their shop
	 * (To help deter endless undercutting
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
	/** Whether debug info should be shown in the console */
	public static boolean debug = false;

	
	private int displayItemCheckTicks;

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

	public void onEnable() {
		getLogger().info("Quickshop Reremake by Ghost_chu(Minecraft SunnySide Server Community)");
		getLogger().info("THIS VERSION ONLY SUPPORT BUKKIT API 1.13-1.13.x VERSION!");
		getLogger().info("Author:Ghost_chu");
		getLogger().info("Original author:Netherfoam, Timtower, KaiNoMood");
		getLogger().info("Let's us start load plugin");
		//NMS.init();
		instance = this;
		saveDefaultConfig(); // Creates the config folder and copies config.yml
								// (If one doesn't exist) as required.
		reloadConfig(); // Reloads messages.yml too, aswell as config.yml and
						// others.
		getConfig().options().copyDefaults(true); // Load defaults.
		if (getConfig().contains("debug"))
			debug = true;
		if (loadEcon() == false)
			return;

		if (Permissions.init()) {
			getLogger().info("Found permission provider.");
		} else {
			getLogger().info("Couldn't find a Vault permission provider. Some feature may be limited.");
		}
		
		//Metrics
		if(getConfig().getBoolean("metrics")) {
		PluginDescriptionFile pdf = Bukkit.getServer().getPluginManager().getPlugin("QuickShop").getDescription();
		String qsVer = pdf.getVersion();
		Metrics metrics = new Metrics(this);
		//Version
		metrics.addCustomChart(new Metrics.SimplePie("plugin_version", () -> qsVer));
		//Language Env
		Locale locale = Locale.getDefault();
		metrics.addCustomChart(new Metrics.SimplePie("country", () -> String.valueOf(locale)));
		getLogger().info("This computer using language: "+String.valueOf(locale));
		}else {
			getLogger().info("Metrics is disabled, Skipping...");
		}
		
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
		try {
			ConfigurationSection dbCfg = getConfig().getConfigurationSection("database");
			DatabaseCore dbCore;
			if (dbCfg.getBoolean("mysql")) {
				// MySQL database - Required database be created first.
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
			getLogger().severe("Error connecting to database. Aborting plugin load.");
			getServer().getPluginManager().disablePlugin(this);
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			getLogger().severe("Error setting up database. Aborting plugin load.");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		/* Load shops from database to memory */
		int count = 0; // Shops count
		Connection con;
		try {
			getLogger().info("Loading shops from database...");
			/*int res = Converter.convert();
			if (res < 0) {
				plugin.getLogger().log(Level.WARNING, "Could not convert shops. Exitting.");
				return;
			}
			if (res > 0) {
				plugin.getLogger().log(Level.WARNING, "Conversion success. Continuing...");
			}*/
			con = database.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM shops");
			ResultSet rs = ps.executeQuery();
			int errors = 0;
			//========================
			MsgUtil.loadItemi18n();
			MsgUtil.loadEnchi18n();
			MsgUtil.loadPotioni18n();
			//========================
			while (rs.next()) {
				int x = 0;
				int y = 0;
				int z = 0;
				String worldName = null;
				//==========================================================================================
				try {
					x = rs.getInt("x");
					y = rs.getInt("y");
					z = rs.getInt("z");
					worldName = rs.getString("world");
					World world = Bukkit.getWorld(worldName);
					ItemStack item = Util.deserialize(rs.getString("itemConfig"));
					String owner = rs.getString("owner");
					UUID ownerUUID = null;
					try {
						ownerUUID = UUID.fromString(owner);
					} catch (IllegalArgumentException e) {
						// This could be old data to be converted... check if it's a player
						OfflinePlayer player = Bukkit.getOfflinePlayer(owner);
						if (player.hasPlayedBefore()) {
							ownerUUID = player.getUniqueId();
							getDB().getConnection().createStatement().executeUpdate("UPDATE shops SET owner = \""+ownerUUID.toString()+"\" WHERE x = "+x+" AND y = "+y+" AND z = "+z+" AND world = \""+worldName+"\" LIMIT 1");
						} else {
							// Invalid shop owner
							getDB().getConnection().createStatement().executeUpdate("DELETE FROM shops WHERE x = "+x+" AND y = "+y+" AND z = "+z+" AND world = \""+worldName+"\""+(getDB().getCore() instanceof MySQLCore ? " LIMIT 1" : ""));
							continue;
						}
					}
					
					double price = rs.getDouble("price");
					Location loc = new Location(world, x, y, z);
					/* Skip invalid shops, if we know of any */
					if (world != null && (loc.getBlock().getState() instanceof InventoryHolder) == false) {
						getLogger().info("Shop is not an InventoryHolder in " + rs.getString("world") + " at: " + x + ", " + y + ", " + z + ".  Deleting.");
						getDB().getConnection().createStatement().executeUpdate("DELETE FROM shops WHERE x = "+x+" AND y = "+y+" AND z = "+z+" AND world = \""+worldName+"\""+(getDB().getCore() instanceof MySQLCore ? " LIMIT 1" : ""));
						continue;
					}
					int type = rs.getInt("type");
					Shop shop = new ContainerShop(loc, price, item, ownerUUID);
					shop.setUnlimited(rs.getBoolean("unlimited"));
					shop.setShopType(ShopType.fromID(type));
					shopManager.loadShop(rs.getString("world"), shop);
					if (loc.getWorld() != null && loc.getChunk().isLoaded()) {
						shop.onLoad();
					}
					count++;
				} catch (Exception e) {
					errors++;
					e.printStackTrace();
					getLogger().severe("Error loading a shop! Coords: " + worldName + " (" + x + ", " + y + ", " + z + ")...");
					if (errors < 3) {
						getLogger().info("Skipping load this shop...");
						return;
//						PreparedStatement delps = getDB().getConnection().prepareStatement("DELETE FROM shops WHERE x = ? AND y = ? and z = ? and world = ?");
//						delps.setInt(1, x);
//						delps.setInt(2, y);
//						delps.setInt(3, z);
//						delps.setString(4, worldName);
//						delps.execute();
					} else {
						getLogger().severe("Multiple errors in shops - Something seems to be wrong with your shops database! Please check it out immediately!");
						e.printStackTrace();
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			getLogger().severe("Could not load shops.");
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
			getLogger().severe("Shop.find-distance is too high! Pick a number under 100!");
		}

		if (display && displayItemCheckTicks>0) {
			new BukkitRunnable() {		
				@Override
				public void run() {
					Iterator<Shop> it = getShopManager().getShopIterator();
					while (it.hasNext()) {
						Shop shop = it.next();
						if (shop instanceof ContainerShop) {
							ContainerShop cShop = (ContainerShop) shop;
							if (cShop.checkDisplayMoved()) {
								log("Display item for "+shop+" is not on the correct location and has been removed. Probably someone is trying to cheat.");
								for (Player player : getServer().getOnlinePlayers()) {
									if (player.hasPermission("quickshop.alerts")) {
										player.sendMessage(ChatColor.RED + "[QuickShop] Display item for "+shop+" is not on the correct location and has been removed. Probably someone is trying to cheat.");
									}
								}
								cShop.getDisplayItem().remove();
							}
						}
					}
				}
			}.runTaskTimer(this, 1L, displayItemCheckTicks);
		}
		
		// added for compatibility reasons with OpenInv - see https://github.com/KaiKikuchi/QuickShop/issues/139
		this.openInvPlugin = Bukkit.getPluginManager().getPlugin("OpenInv");
		
		MsgUtil.loadTransactionMessages();
		MsgUtil.clean();
		getLogger().info("QuickShop loaded!");
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
	 * @return true if successful, false if the core is invalid or is not found,
	 *         and vault cannot be used.
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
		}catch (Exception e) {
			e.printStackTrace();
			getLogger().severe("QuickShop could not hook an economy/Not found Vault!");
			getLogger().severe("QuickShop CANNOT start!");
			return false;
		}
	}

	public void onDisable() {
		if (itemWatcherTask != null) {
			itemWatcherTask.cancel();
		}
		if (logWatcher != null) {
			logWatcher.task.cancel();
			logWatcher.close(); // Closes the file
		}
		/* Remove all display items, and any dupes we can find */
		shopManager.clear();
		/* Empty the buffer */
		database.close();
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
	 * @param s
	 *            The string to log. It will be prefixed with the date and time.
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

	/**
	 * Prints debug information if QuickShop is configured to do so.
	 * 
	 * @param s
	 *            The string to print.
	 */
	public void debug(String s) {
		if (!debug)
			return;
		this.getLogger().info(ChatColor.YELLOW + "[Debug] " + s);
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
}
