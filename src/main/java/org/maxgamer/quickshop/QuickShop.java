package org.maxgamer.quickshop;

import com.google.common.io.Files;
//import com.griefcraft.lwc.LWCPlugin;
import com.onarandombox.MultiverseCore.MultiverseCore;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.maxgamer.quickshop.Command.QS;
import org.maxgamer.quickshop.Command.Tab;
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
import org.maxgamer.quickshop.Util.Util;
import org.maxgamer.quickshop.Watcher.ItemWatcher;
import org.maxgamer.quickshop.Watcher.LogWatcher;
import org.maxgamer.quickshop.Watcher.UpdateWatcher;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;

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
//	/** Whether players are required to sneak to create/buy from a shop */
//	public boolean sneak;
//	/** Whether players are required to sneak to create a shop */
//	public boolean sneakCreate;
//	/** Whether players are required to sneak to trade with a shop */
//	public boolean sneakTrade;
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
	QS commandExecutor =null;
	public MultiverseCore mPlugin = null;
	private int displayItemCheckTicks;
	private boolean noopDisable;
	private boolean setupDBonEnableding = false;
	private String dbPrefix="";
	private Tab commandTabCompleter;
	private Metrics metrics;
	private Language language;
	//private LWCPlugin lwcPlugin;
	/** 
	 * Get the Player's Shop limit.
	 * @return int Player's shop limit
	 * @param Player p
	 * */
	public int getShopLimit(Player p) {
		int max = getConfig().getInt("limits.default");
		for (Entry<String, Integer> entry : limits.entrySet()) {
			if (entry.getValue() > max && p.hasPermission(entry.getKey()))
				max = entry.getValue();
		}
		return max;
	}
	public void onEnable() {
		instance = this;

		getLogger().info("Quickshop Reremake");
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
		if (getServer().getName().toLowerCase().contains("catserver")) {
			// Send FATAL ERROR TO CatServer's users.
			getLogger().info("NOTICE: QSRR nolonger support CatServer!");
			getLogger().info("NOTICE: The reason of detail very complex,QSRR some version support CatServer, but cuz CatServer Developing Team's attitude too bad, I decide remove all support for CatServer, Sorry user!");
			getLogger().info("NOTICE: QSRR will auto-unload in 60 secs.");
			try {
				Thread.sleep(600000);
				Bukkit.getPluginManager().disablePlugin(this);
				return;
			} catch (InterruptedException e) {
				e.printStackTrace();
				Bukkit.getPluginManager().disablePlugin(this);
				return;
			}
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


		if (getConfig().getInt("config-version") == 0)
			getConfig().set("config-version", 1);
		updateConfig(getConfig().getInt("config-version"));



		if (loadEcon() == false)
			return;
		// ProtocolLib Support
		// protocolManager = ProtocolLibrary.getProtocolManager();
		try {
			getServer().spigot();
		} catch (Throwable e) {
			getLogger().severe("You must use support Spigot or Spigot forks(eg.Paper) server not CraftBukkit");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		if (getConfig().getBoolean("plugin.Multiverse-Core")) {
			if (Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null) {
				mPlugin = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
				getLogger().info("Successfully loaded MultiverseCore support!");
			}
		}
		// added for compatibility reasons with OpenInv - see
		// https://github.com/KaiKikuchi/QuickShop/issues/139
		if (getConfig().getBoolean("plugin.OpenInv")) {
			this.openInvPlugin = Bukkit.getPluginManager().getPlugin("OpenInv");
			if (this.openInvPlugin != null)
				getLogger().info("Successfully loaded OpenInv support!");
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
		getServer().getPluginManager().registerEvents(new UpdateWatcher(), this);
		ConfigurationSection limitCfg = this.getConfig().getConfigurationSection("limits");
		if (limitCfg != null) {
			getLogger().info("Limit cfg found...");
			this.limit = limitCfg.getBoolean("use", false);
			getLogger().info("Limits.use: " + limit);
			limitCfg = limitCfg.getConfigurationSection("ranks");
			for (String key : limitCfg.getKeys(true)) {
				limits.put(key, limitCfg.getInt(key));
			}
		}
		setupDBonEnableding = true;
		setupDatabase();
		setupDBonEnableding = false;
		/* Load shops from database to memory */
		int count = 0; // Shops count
		MsgUtil.loadItemi18n();
		MsgUtil.loadEnchi18n();
		MsgUtil.loadPotioni18n();
		int skipedShops = 0;
		try {
			getLogger().info("Loading shops from database...");
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
						if(world==null){
							// Still load failed? It removed or not got loaded now?
							skipedShops++;
							Util.debugLog("Found a shop can't match shop's world: "+worldName+", it got removed or just not loaded? Ignore it...");
							continue;
						}
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
							getLogger().info("Create backup for database..");
							if (!isBackuped) {
								//Backup
								if(backupDatabase())
									isBackuped=true;
							}
							getLogger().info("Removeing shop from database...");
							if(isBackuped) {
								DatabaseHelper.removeShop(database, x, y, z, worldName);
							}else {
								getLogger().warning("Skipped shop deleteion: Failed to backup database,");
							}
							continue;
						}
					}
					step = "Loading shop price";
					double price = rs.getDouble("price");
					step = "Createing Location object";
					Location loc = new Location(world, x, y, z);
					/* Skip invalid shops, if we know of any */
					step = "Checking InventoryHolder";
					if (world != null && Util.canBeShop(loc.getBlock(),null,true) == false) {
						step = "Removeing shop in world: Because it not a correct InventoryHolder";
						getLogger().info("Shop is not an InventoryHolder in " + rs.getString("world") + " at: " + x
								+ ", " + y + ", " + z + ".  Deleting.");
							getLogger().info("Create backup for database..");
							if (!isBackuped) {
								//Backup
								if(backupDatabase())
									isBackuped=true;
							}
							getLogger().info("Removeing shop from database...");
							if(isBackuped) {
								DatabaseHelper.removeShop(database, x, y, z, worldName);
							}else {
								getLogger().warning("Skipped shop deleteion: Failed to backup database,");
							}
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

					getLogger().severe("===========Error Reporting End===========");

					if (errors < 3) {
						getLogger().info("Create backup for database..");
						if (!isBackuped) {
							//Backup
							if(backupDatabase())
								isBackuped=true;
						}
						getLogger().info("Removeing shop from database...");
						if(isBackuped) {
							DatabaseHelper.removeShop(database, x, y, z, worldName);
						}else {
							getLogger().warning("Skipped shop deleteion: Failed to backup database,");
						}
						
						getLogger().info("Trying keep loading...");
					} else {
						getLogger().severe(
								"Multiple errors in shops - Something seems to be wrong with your shops database! Please check it out immediately!");
						getLogger().info("Removeing shop from database...");
							getLogger().info("Create backup for database..");
							if (!isBackuped) {
								//Backup
								if(backupDatabase())
									isBackuped=true;
							}
							getLogger().info("Removeing shop from database...");
							if(isBackuped) {
								DatabaseHelper.removeShop(database, x, y, z, worldName);
							}else {
								getLogger().warning("Skipped shop deleteion: Failed to backup database,");
							}
						e.printStackTrace();
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			getLogger().severe("Could not load shops Because SQLException.");
		}
		getLogger().info("Loaded " + count + " shops.");
		getLogger().info("Other "+ skipedShops+" shops will load when world loaded.");

		if (getConfig().getBoolean("shop.lock")) {
			LockListener lockListener = new LockListener(this);
			Bukkit.getServer().getPluginManager().registerEvents(lockListener, this);
		}
		// Command handlers
		commandExecutor = new QS(this);
		getCommand("qs").setExecutor(commandExecutor);
		commandTabCompleter = new Tab(this);
		getCommand("qs").setTabCompleter(commandTabCompleter);
		if (getConfig().getInt("shop.find-distance") > 100) {
			getLogger().severe("Shop.find-distance is too high! It may cause lag! Pick a number under 100!");
		}
		getLogger().info("Registering Listeners");
		// Register events
		blockListener = new BlockListener(this);
		playerListener = new PlayerListener(this);
		worldListener = new WorldListener(this);
		chatListener = new ChatListener(this);
		chunkListener = new ChunkListener(this);
		inventoryListener = new DisplayProtectionListener(this);
		Bukkit.getServer().getPluginManager().registerEvents(blockListener, this);
		Bukkit.getServer().getPluginManager().registerEvents(playerListener, this);
		Bukkit.getServer().getPluginManager().registerEvents(chatListener, this);
		Bukkit.getServer().getPluginManager().registerEvents(inventoryListener, this);
		Bukkit.getServer().getPluginManager().registerEvents(chunkListener, this);
		Bukkit.getServer().getPluginManager().registerEvents(worldListener, this);
		
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
		MsgUtil.loadTransactionMessages();
		MsgUtil.clean();
		getLogger().info("QuickShop loaded!");

		if (getConfig().getBoolean("disabled-metrics") != true) {
			String serverVer = Bukkit.getVersion();
			String bukkitVer = Bukkit.getBukkitVersion();
			String serverName = Bukkit.getServer().getName();
			metrics = new Metrics(this);
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
			metrics.submitData(); // Submit now!
			getLogger().info("Metrics submited.");
		} else {
			getLogger().info("You have disabled mertics, Skipping...");
		}

		UpdateWatcher.init();
	}
	private boolean backupDatabase() {
		File sqlfile = new File(Bukkit.getPluginManager().getPlugin("QuickShop").getDataFolder()
				.getAbsolutePath().toString() + "/shop.db");
		if (!sqlfile.exists()) {
			getLogger().warning("Failed to backup! (File not found)");
			return false;
		}
		String uuid = UUID.randomUUID().toString().replaceAll("_", "");
		File bksqlfile = new File(Bukkit.getPluginManager().getPlugin("QuickShop").getDataFolder()
				.getAbsolutePath().toString() + "/shop_backup_" + uuid + ".db");
		try {
			Files.copy(sqlfile,bksqlfile);
		} catch (IOException e1) {
			e1.printStackTrace();
			getLogger().warning("Failed to backup database.");
			return false;
		}
		return true;
	}
	@Override	
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
	        List<String> commands = new ArrayList<>();
	        commands.add("unlimited");
			commands.add("buy");
			commands.add("sell");
			commands.add("create");
			commands.add("price");
			commands.add("clean");
			commands.add("range");
			commands.add("refill");
			commands.add("empty");
			commands.add("setowner");
			commands.add("fetchmessage");
	        if (args != null && args.length == 1) {
	            List<String> list = new ArrayList<>();
	            for (String s : commands) {
	                if (s.startsWith(args[0])) {
	                    list.add(s);
	                }
	            }
	            return list;
	        }
	        return null;
	    }
	private boolean setupDatabase() {
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
		if (selectedVersion == 8) {
			getConfig().set("database.use-varchar", true);
			getConfig().set("limits.old-algorithm", false);
			getConfig().set("shop.pay-player-from-unlimited-shop-owner", false);
			getConfig().set("plugin.ProtocolLib", false);
			getConfig().set("plugin.Multiverse-Core", true);
			getConfig().set("shop.ignore-unlimited",false);
			getConfig().set("config-version", 9);
			selectedVersion = 9;
			saveConfig();
			reloadConfig();
		}
		if (selectedVersion == 9) {
			getConfig().set("shop.enable-enderchest",true);
			getConfig().set("config-version", 10);
			selectedVersion = 10;
			saveConfig();
			reloadConfig();
		}
		if (selectedVersion == 10) {
			getConfig().set("shop.pay-player-from-unlimited-shop-owner",null); //Removed
			getConfig().set("config-version", 11);
			selectedVersion = 11;
			saveConfig();
			reloadConfig();
		}
		if (selectedVersion == 11) {
			getConfig().set("shop.enable-enderchest",null); //Removed
			getConfig().set("plugin.OpenInv",true);
			List<String> shoppable = getConfig().getStringList("shop-blocks");
			shoppable.add("ENDER_CHEST");
			getConfig().set("shop-blocks",shoppable);
			getConfig().set("config-version", 12);
			selectedVersion = 12;
			saveConfig();
			reloadConfig();
		}
		if (selectedVersion == 12) {
			getConfig().set("plugin.ProtocolLib",null); //Removed
			getConfig().set("plugin.BKCommonLib",null); //Removed
			getConfig().set("plugin.BKCommonLib",null); //Removed
			getConfig().set("database.use-varchar",null); //Removed
			getConfig().set("database.reconnect",null); //Removed
			getConfig().set("anonymous-metrics", false);
			getConfig().set("display-items-check-ticks", 1200);
			getConfig().set("shop.bypass-owner-check", null); //Removed
			getConfig().set("config-version", 13);
			selectedVersion = 13;
			saveConfig();
			reloadConfig();
		}
		if (selectedVersion == 13) {
			getConfig().set("plugin.AreaShop",false);
			getConfig().set("shop.special-region-only", false);
			getConfig().set("config-version", 14);
			selectedVersion = 14;
			saveConfig();
			reloadConfig();
		}
		if (selectedVersion == 14) {
			getConfig().set("plugin.AreaShop",null);
			getConfig().set("shop.special-region-only", null);
			getConfig().set("config-version", 15);
			selectedVersion = 15;
			saveConfig();
			reloadConfig();
		}
		if (selectedVersion == 15) {
			getConfig().set("ongoingfee",null);
			getConfig().set("shop.display-item-use-name",null);
			getConfig().set("shop.display-item-show-name",false);
			getConfig().set("shop.auto-fetch-shop-messages",true);
			getConfig().set("config-version", 16);
			selectedVersion = 16;
			saveConfig();
			reloadConfig();
		}
		if (selectedVersion == 16) {
			getConfig().set("ignore-cancel-chat-event", false);
			getConfig().set("config-version", 17);
			selectedVersion = 17;
			saveConfig();
			reloadConfig();
		}
		if (selectedVersion == 17) {
			getConfig().set("ignore-cancel-chat-event", false);
			getConfig().set("float", null);
			getConfig().set("config-version", 18);
			selectedVersion = 18;
			saveConfig();
			reloadConfig();
		}
		if (selectedVersion == 18) {
			getConfig().set("shop.disable-vault-format", false);
			getConfig().set("config-version", 19);
			selectedVersion = 19;
			saveConfig();
			reloadConfig();
		}
		if (selectedVersion == 19) {
			getConfig().set("shop.allow-shop-without-space-for-sign", true);
			getConfig().set("config-version", 20);
			selectedVersion = 20;
			saveConfig();
			reloadConfig();
		}
		if (selectedVersion == 20) {
			getConfig().set("shop.maximum-price", -1);
			getConfig().set("config-version", 21);
			selectedVersion = 21;
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
//		this.sneak = this.getConfig().getBoolean("shop.sneak-only");
//		this.sneakCreate = this.getConfig().getBoolean("shop.sneak-to-create");
//		this.sneakTrade = this.getConfig().getBoolean("shop.sneak-to-trade");
		this.priceChangeRequiresFee = this.getConfig().getBoolean("shop.price-change-requires-fee");
		this.displayItemCheckTicks = this.getConfig().getInt("shop.display-items-check-ticks");
		language = new Language(this); //Init locale
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
		return QuickShop.instance.getDescription().getVersion();
	}
	
	public BlockListener getBlockListener() {
		return blockListener;
	}
	public ChatListener getChatListener() {
		return chatListener;
	}
	public ChunkListener getChunkListener() {
		return chunkListener;
	}
	public DisplayProtectionListener getInventoryListener() {
		return inventoryListener;
	}
	public PlayerListener getPlayerListener() {
		return playerListener;
	}
	public WorldListener getWorldListener() {
		return worldListener;
	}
	public MultiverseCore getMVPlugin() {
		return mPlugin;
	}
	public Plugin getOpenInvPlugin() {
		return openInvPlugin;
	}
	public String getDbPrefix() {
		return dbPrefix;
	}
	public Tab getCommandTabCompleter() {
		return commandTabCompleter;
	}
	public QS getCommandExecutor() {
		return commandExecutor;
	}
    public Metrics getMetrics() {
		return metrics;
	}
    public Language getLanguage() {
		return language;
    }
    public String getFork() { return "Reremake"; }
}
