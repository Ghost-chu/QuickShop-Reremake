/*
 * This file is a part of project QuickShop, the name is QuickShop.java
 * Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 * Copyright (C) Bukkit Commons Studio and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.maxgamer.quickshop;

import lombok.Getter;
import me.minebuilders.clearlag.Clearlag;
import me.minebuilders.clearlag.listeners.ItemMergeListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Command.CommandManager;
import org.maxgamer.quickshop.Database.*;
import org.maxgamer.quickshop.Database.Database.ConnectionException;
import org.maxgamer.quickshop.Economy.*;
import org.maxgamer.quickshop.InternalListener.InternalListener;
import org.maxgamer.quickshop.Listeners.*;
import org.maxgamer.quickshop.Permission.PermissionManager;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopLoader;
import org.maxgamer.quickshop.Shop.ShopManager;
import org.maxgamer.quickshop.Util.*;
import org.maxgamer.quickshop.Util.ServerForkWrapper.BukkitAPIWrapper;
import org.maxgamer.quickshop.Util.ServerForkWrapper.PaperWrapper;
import org.maxgamer.quickshop.Util.ServerForkWrapper.SpigotWrapper;
import org.maxgamer.quickshop.Util.Timer;
import org.maxgamer.quickshop.Watcher.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Getter
public class QuickShop extends JavaPlugin {
    /**
     * The active instance of QuickShop
     */
    public static QuickShop instance;
    // Listeners (These don't)
    private BlockListener blockListener;
    /**
     * The BootError, if it not NULL, plugin will stop loading and show setted errors when use /qs
     **/
    @Nullable
    private BootError bootError;
    // Listeners - We decide which one to use at runtime
    private ChatListener chatListener;
    private ChunkListener chunkListener;
    private CommandManager commandManager;
    /**
     * WIP
     **/
    private Compatibility compatibilityTool = new Compatibility(this);
    private CustomInventoryListener customInventoryListener;
    /**
     * The database for storing all our data for persistence
     */
    private Database database;
    /**
     * Contains all SQL tasks
     **/
    private DatabaseHelper databaseHelper;
    /**
     * Queued database manager
     **/
    private DatabaseManager databaseManager;
    /**
     * Default database prefix, can overwrite by config
     **/
    private String dbPrefix = "";
    /**
     * Whether we should use display items or not
     */
    private boolean display = true;
    private DisplayBugFixListener displayBugFixListener;
    private int displayItemCheckTicks;
    private DisplayWatcher displayWatcher;
    /**
     * The economy we hook into for transactions
     */
    private Economy economy;
    private DisplayProtectionListener inventoryListener;
    private ItemMatcher itemMatcher;
    /**
     * Language manager, to select which language will loaded.
     **/
    private Language language;
    /**
     * Whether or not to limit players shop amounts
     */
    private boolean limit = false;
    /**
     * The shop limites.
     **/
    private HashMap<String, Integer> limits = new HashMap<>();
    private LockListener lockListener;
    //private BukkitTask itemWatcherTask;
    @Nullable
    private LogWatcher logWatcher;
    /**
     * bStats, good helper for metrics.
     **/
    private Metrics metrics;
    private boolean noopDisable;
    /**
     * The plugin OpenInv (null if not present)
     */
    private Plugin openInvPlugin;
    /**
     * The plugin PlaceHolderAPI(null if not present)
     */
    private Plugin placeHolderAPI;
    /**
     * A util to call to check some actions permission
     **/
    private PermissionChecker permissionChecker;
    private PlayerListener playerListener;
    private InternalListener internalListener;
    /**
     * Whether we players are charged a fee to change the price on their shop (To
     * help deter endless undercutting
     */
    private boolean priceChangeRequiresFee = false;
    /**
     * The error reporter to help devs report errors to Sentry.io
     **/
    private SentryErrorReporter sentryErrorReporter;
    /**
     * The server UniqueID, use to the ErrorReporter
     **/
    private UUID serverUniqueID;
    private boolean setupDBonEnableding = false;
    /**
     * Rewrited shoploader, more faster.
     **/
    private ShopLoader shopLoader;
    /**
     * The Shop Manager used to store shops
     */
    private ShopManager shopManager;
    private ShopProtectionListener shopProtectListener;
    private SyncTaskWatcher syncTaskWatcher;
    private ShopVaildWatcher shopVaildWatcher;
    private DisplayAutoDespawnWatcher displayAutoDespawnWatcher;
    /**
     * Use SpoutPlugin to get item / block names
     */
    private boolean useSpout = false;
    /**
     * A set of players who have been warned ("Your shop isn't automatically
     * locked")
     */
    private HashSet<String> warnings = new HashSet<>();
    private WorldListener worldListener;
    /**
     * The manager to check permissions.
     */
    private static PermissionManager permissionManager;
    private OngoingFeeWatcher ongoingFeeWatcher;
    private SignUpdateWatcher signUpdateWatcher;
    private ShopContainerWatcher shopContainerWatcher;
    private BukkitAPIWrapper bukkitAPIWrapper;

    /**
     * Get the Player's Shop limit.
     *
     * @param p The player you want get limit.
     * @return int Player's shop limit
     */
    public int getShopLimit(@NotNull Player p) {
        int max = getConfig().getInt("limits.default");
        for (Entry<String, Integer> entry : limits.entrySet()) {
            if (entry.getValue() > max && getPermissionManager().hasPermission(p, entry.getKey())) {
                max = entry.getValue();
            }
        }
        return max;
    }

    /**
     * Load 3rdParty plugin support module.
     */
    private void load3rdParty() {
        // added for compatibility reasons with OpenInv - see
        // https://github.com/KaiKikuchi/QuickShop/issues/139
        if (getConfig().getBoolean("plugin.OpenInv")) {
            this.openInvPlugin = Bukkit.getPluginManager().getPlugin("OpenInv");
            if (this.openInvPlugin != null) {
                getLogger().info("Successfully loaded OpenInv support!");
            }
        }
        if (getConfig().getBoolean("plugin.PlaceHolderAPI")) {
            this.placeHolderAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
            if (this.placeHolderAPI != null) {
                getLogger().info("Successfully loaded PlaceHolderAPI support!");
            }
        }
        if (this.display) {
            if (Bukkit.getPluginManager().getPlugin("ClearLag") != null) {
                try {
                    Clearlag clearlag = (Clearlag) Bukkit.getPluginManager().getPlugin("ClearLag");
                    for (RegisteredListener clearLagListener : ItemSpawnEvent.getHandlerList().getRegisteredListeners()) {
                        if (!clearLagListener.getPlugin().equals(clearlag)) {
                            continue;
                        }
                        int spamTimes = 500;
                        if (clearLagListener.getListener().getClass().equals(ItemMergeListener.class)) {
                            ItemSpawnEvent.getHandlerList().unregister(clearLagListener.getListener());
                            for (int i = 0; i < spamTimes; i++) {
                                getLogger().warning("+++++++++++++++++++++++++++++++++++++++++++");
                                getLogger().severe("Detected incompatible module of ClearLag-ItemMerge module, it will broken the QuickShop display, we already unregister this module listener!");
                                getLogger().severe("Please turn off it in the ClearLag config.yml or turn off the QuickShop display feature!");
                                getLogger().severe("If you didn't do that, this message will keep spam in your console every times you server boot up!");
                                getLogger().warning("+++++++++++++++++++++++++++++++++++++++++++");
                                getLogger().info("This message will spam more " + (spamTimes - i) + " times!");
                            }
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        }
    }

    /**
     * Tries to load the economy and its core. If this fails, it will try to use
     * vault. If that fails, it will return false.
     *
     * @return true if successful, false if the core is invalid or is not found, and
     * vault cannot be used.
     */
    private boolean loadEcon() {
        try {
            //EconomyCore core = new Economy_Vault();
            EconomyCore core = null;
            switch (EconomyType.fromID(getConfig().getInt("economy-type"))) {
                case UNKNOWN:
                    bootError = new BootError("Can't load the Economy provider, invaild value in config.yml.");
                    return false;
                case VAULT:
                    core = new Economy_Vault();
                    Util.debugLog("Now using the Vault economy system.");
                    break;
                case RESERVE:
                    core = new Economy_Reserve();
                    Util.debugLog("Now using the Reserve economy system.");
                    break;
                default:
                    Util.debugLog("No any economy provider selected.");
                    break;
            }
            if (!core.isValid()) {
                // getLogger().severe("Economy is not valid!");
                bootError = BuiltInSolution.econError();
                // if(econ.equals("Vault"))
                // getLogger().severe("(Does Vault have an Economy to hook into?!)");
                return false;
            } else {
                this.economy = new Economy(core);
                return true;
            }
        } catch (Exception e) {
            this.getSentryErrorReporter().ignoreThrow();
            e.printStackTrace();
            getLogger().severe("QuickShop could not hook into a economy/Not found Vault or Reserve!");
            getLogger().severe("QuickShop CANNOT start!");
            bootError = BuiltInSolution.econError();
            HandlerList.unregisterAll(this);
            getLogger().severe("Plugin listeners was disabled, please fix the economy issue.");
            return false;
        }
    }

    /**
     * Logs the given string to qs.log, if QuickShop is configured to do so.
     *
     * @param s The string to log. It will be prefixed with the date and time.
     */
    public void log(@NotNull String s) {
        if (this.getLogWatcher() == null) {
            return;
        }
        this.getLogWatcher().log(s);
    }

    /**
     * Reloads QuickShops config
     */
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        // Load quick variables
        this.display = this.getConfig().getBoolean("shop.display-items");
        this.priceChangeRequiresFee = this.getConfig().getBoolean("shop.price-change-requires-fee");
        this.displayItemCheckTicks = this.getConfig().getInt("shop.display-items-check-ticks");
        language = new Language(this); //Init locale
        if (this.getConfig().getBoolean("log-actions")) {
            logWatcher = new LogWatcher(this, new File(getDataFolder(), "qs.log"));
        } else {
            logWatcher = null;
        }
        try {
            MsgUtil.loadCfgMessages();
        }catch (Exception e){
            getLogger().warning("An error throws when loading messages");
            e.printStackTrace();
        }
    }

    /**
     * Early than onEnable, make sure instance was loaded in first time.
     */
    @Override
    public void onLoad() {
        instance = this;
        bootError = null;
    }

    @Override
    public void onDisable() {
        if (noopDisable) {
            return;
        }
        getLogger().info("QuickShop is finishing remaining work, this may need a while...");

        Util.debugLog("Closing all GUIs...");
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
        }
        Util.debugLog("Unloading all shops...");
        try {
            Objects.requireNonNull(this.getShopManager().getLoadedShops()).forEach(Shop::onUnload);
        } catch (Throwable th) {
            //ignore, we didn't care that
        }

        Util.debugLog("Cleaning up database queues...");
        if (this.getDatabaseManager() != null) {
            this.getDatabaseManager().unInit();
        }

        Util.debugLog("Unregistering tasks...");
        //if (itemWatcherTask != null)
        //    itemWatcherTask.cancel();
        if (logWatcher != null) {
            logWatcher.close(); // Closes the file
        }
        /* Unload UpdateWatcher */
        UpdateWatcher.uninit();
        Util.debugLog("Cleaning up resources and unloading all shops...");
        /* Remove all display items, and any dupes we can find */
        if (shopManager != null) {
            shopManager.clear();
        }
        /* Close Database */
        if (database != null) {
            try {
                this.database.getConnection().close();
                this.database.close();
            } catch (SQLException e) {
                if (getSentryErrorReporter() != null) {
                    this.getSentryErrorReporter().ignoreThrow();
                }
                e.printStackTrace();
            }
        }
        if (warnings != null) {
            warnings.clear();
        }
        //this.reloadConfig();
        Util.debugLog("All shutdown work is finished.");
    }

    @Override
    public void onEnable() {
        Timer enableTimer = new Timer(true);
        /* PreInit for BootError feature */
        commandManager = new CommandManager();
        //noinspection ConstantConditions
        getCommand("qs").setExecutor(commandManager);
        //noinspection ConstantConditions
        getCommand("qs").setTabCompleter(commandManager);

        getLogger().info("Quickshop "+getFork());
        getLogger().info("Reading the configuration...");
        /* Process the config */
        saveDefaultConfig();
        reloadConfig();
        getConfig().options().copyDefaults(getConfig().getBoolean("auto-fix-configuration",false)); // Load defaults.
        saveDefaultConfig();
        reloadConfig();
        //getConfig().options().copyDefaults(true);
        if (getConfig().getInt("config-version") == 0) {
            getConfig().set("config-version", 1);
        }
        updateConfig(getConfig().getInt("config-version"));


        getLogger().info("Developers: " + Util.list2String(this.getDescription().getAuthors()));
        getLogger().info("Original author: Netherfoam, Timtower, KaiNoMood");
        getLogger().info("Let's start loading the plugin");

        /* It will generate a new UUID above updateConfig */
        /* Process Metrics and Sentry error reporter. */
        metrics = new Metrics(this);
        //noinspection ConstantConditions
        serverUniqueID = UUID.fromString(getConfig().getString("server-uuid", String.valueOf(UUID.randomUUID())));
        sentryErrorReporter = new SentryErrorReporter(this);
        // loadEcon();
        switch (getConfig().getInt("server-platform",0)){
            case 1:
                bukkitAPIWrapper = new SpigotWrapper();
                getLogger().info("Plugin now running under Spigot mode. Paper performance profile is disabled, if you switch to Paper, we can use a lot paper api to improve the server performance.");
            case 2:
                bukkitAPIWrapper = new PaperWrapper();
                getLogger().info("Plugin now running under Paper mode.");
            default: //AUTO
                if(Util.isClassAvailable("com.destroystokyo.paper.PaperConfig")){
                    bukkitAPIWrapper = new PaperWrapper();
                    getLogger().info("Plugin now running under Paper mode.");
                }else{
                    bukkitAPIWrapper = new SpigotWrapper();
                    getLogger().info("Plugin now running under Spigot mode. Paper performance profile is disabled, if you switch to Paper, we can use a lot paper api to improve the server performance.");

                }
        }

        /* Initalize the Utils */
        itemMatcher = new ItemMatcher(this);
        Util.initialize();

        MsgUtil.loadItemi18n();
        MsgUtil.loadEnchi18n();
        MsgUtil.loadPotioni18n();

        /* Check the running envs is support or not. */
        try {
            runtimeCheck(this);
        } catch (RuntimeException e) {
            bootError = new BootError(e.getMessage());
            return;
        }

        /* Load 3rd party supports */
        load3rdParty();

        setupDBonEnableding = true;
        setupDatabase(); //Load the database
        setupDBonEnableding = false;

        /* Initalize the tools */
        // Create the shop manager.
        permissionManager = new PermissionManager(this);
        this.shopManager = new ShopManager(this);
        this.databaseManager = new DatabaseManager(this, database);
        this.permissionChecker = new PermissionChecker(this);


        ConfigurationSection limitCfg = this.getConfig().getConfigurationSection("limits");
        if (limitCfg != null) {
            this.limit = limitCfg.getBoolean("use", false);
            limitCfg = limitCfg.getConfigurationSection("ranks");
            for (String key : Objects.requireNonNull(limitCfg).getKeys(true)) {
                limits.put(key, limitCfg.getInt(key));
            }
        }
        if (getConfig().getInt("shop.find-distance") > 100) {
            getLogger().severe("Shop.find-distance is too high! It may cause lag! Pick a number under 100!");
        }

        /* Load all shops. */
        shopLoader = new ShopLoader(this);
        shopLoader.loadShops();

        getLogger().info("Registering Listeners...");
        // Register events

        blockListener = new BlockListener(this);
        playerListener = new PlayerListener(this);
        worldListener = new WorldListener(this);
        chatListener = new ChatListener(this);
        chunkListener = new ChunkListener(this);
        inventoryListener = new DisplayProtectionListener(this);
        customInventoryListener = new CustomInventoryListener(this);
        displayBugFixListener = new DisplayBugFixListener(this);
        shopProtectListener = new ShopProtectionListener(this);
        displayWatcher = new DisplayWatcher(this);
        syncTaskWatcher = new SyncTaskWatcher(this);
        shopVaildWatcher = new ShopVaildWatcher(this);
        ongoingFeeWatcher = new OngoingFeeWatcher(this);
        lockListener = new LockListener(this);
        internalListener = new InternalListener(this);
        signUpdateWatcher = new SignUpdateWatcher(this);
        shopContainerWatcher = new ShopContainerWatcher(this);
        Bukkit.getPluginManager().registerEvents(blockListener, this);
        Bukkit.getPluginManager().registerEvents(playerListener, this);
        Bukkit.getPluginManager().registerEvents(chatListener, this);
        Bukkit.getPluginManager().registerEvents(inventoryListener, this);
        Bukkit.getPluginManager().registerEvents(chunkListener, this);
        Bukkit.getPluginManager().registerEvents(worldListener, this);
        Bukkit.getPluginManager().registerEvents(customInventoryListener, this);
        Bukkit.getPluginManager().registerEvents(displayBugFixListener, this);
        Bukkit.getPluginManager().registerEvents(shopProtectListener, this);
        Bukkit.getPluginManager().registerEvents(internalListener, this);
        if (getConfig().getBoolean("shop.lock")) {
            Bukkit.getPluginManager().registerEvents(lockListener, this);
        }
        if (Bukkit.getPluginManager().getPlugin("ClearLag") != null) {
            Bukkit.getPluginManager().registerEvents(new ClearLaggListener(), this);
        }
        getLogger().info("Cleaning MsgUtils...");
        MsgUtil.loadTransactionMessages();
        MsgUtil.clean();
        getLogger().info("Registering UpdateWatcher...");
        UpdateWatcher.init();
        getLogger().info("Registering BStats Mertics...");
        submitMeritcs();
        getLogger().info("QuickShop Loaded! " + enableTimer.endTimer() + " ms.");
        /* Delay the Ecoonomy system load, give a chance to let economy system regiser. */
        /* And we have a listener to listen the ServiceRegisterEvent :) */
        Util.debugLog("Loading economy system...");
        new BukkitRunnable() {
            @Override
            public void run() {
                loadEcon();
            }
        }.runTaskLater(this, 1);
        Util.debugLog("Registering shop watcher...");
        shopVaildWatcher.runTaskTimer(this, 0, 20 * 60);
        signUpdateWatcher.runTaskTimer(this,0,120);
        shopContainerWatcher.runTaskTimer(this,0,20);
        if (logWatcher != null) {
            logWatcher.runTaskTimerAsynchronously(this, 10, 10);
            getLogger().info("Log actions is enabled, actions will log in the qs.log file!");
        }
        if (getConfig().getBoolean("shop.ongoing-fee.enable")) {
            getLogger().info("Ongoine fee feature is enabled.");
            ongoingFeeWatcher.runTaskTimerAsynchronously(this, getConfig().getInt("shop.ongoing-fee.ticks"), getConfig().getInt("shop.ongoing-fee.ticks"));
        }

        if (this.display) {
            if (getConfig().getBoolean("shop.display-auto-despawn")) {
                this.displayAutoDespawnWatcher = new DisplayAutoDespawnWatcher(this);
                this.displayAutoDespawnWatcher.runTaskTimerAsynchronously(this, 0, getConfig().getInt("shop.display-check-time"));
            }
        }
    }

    /**
     * Check the env plugin running.
     *
     * @throws RuntimeException The error message, use this to create a BootError.
     */
    private void runtimeCheck(QuickShop shop) throws RuntimeException {
        if (Util.isClassAvailable("org.maxgamer.quickshop.Util.NMS")) {
            getLogger().severe("FATAL: Old QuickShop is installed, You must remove old quickshop jar from plugins folder!");
            throw new RuntimeException("FATAL: Old QuickShop is installed, You must remove old quickshop jar from plugins folder!");
        }
        try {
            getServer().spigot();
        } catch (Throwable e) {
            getLogger().severe("FATAL: QSRR can only be run on Spigot servers and forks of Spigot!");
            throw new RuntimeException("Server must be Spigot based, Don't use CraftBukkit!");
        }

        if (getServer().getName().toLowerCase().contains("catserver")) {
            // Send FATAL ERROR TO CatServer's users.
            getLogger().severe("FATAL: QSRR can't run on CatServer Community/Personal/Pro/Async");
            throw new RuntimeException("QuickShop doen't support CatServer");
        }

        if (Util.isDevEdition()) {
            getLogger().severe("WARNING: You are running QSRR in dev-mode");
            getLogger().severe("WARNING: Keep backup and DO NOT run this in a production environment!");
            getLogger().severe("WARNING: Test version may destroy everything!");
            getLogger().severe(
                    "WARNING: QSRR won't start without your confirmation, nothing will change before you turn on dev allowed.");
            if (!getConfig().getBoolean("dev-mode")) {
                getLogger().severe(
                        "WARNING: Set dev-mode: true in config.yml to allow qs load in dev mode(You may need add this line to the config yourself).");
                noopDisable = true;
                throw new RuntimeException("Snapshot cannot run when dev-mode is false in the config");
            }
        }
        String nmsVersion = Util.getNMSVersion();
        IncompatibleChecker incompatibleChecker = new IncompatibleChecker();
        getLogger().info("Running QuickShop-Reremake on Minecraft version " + nmsVersion);
        if (incompatibleChecker.isIncompatible(nmsVersion)) {
            throw new RuntimeException("Your Minecraft version is nolonger supported: " + nmsVersion);
        }
    }

    /**
     * Setup the database
     *
     * @return The setup result
     */
    private boolean setupDatabase() {
        try {
            ConfigurationSection dbCfg = getConfig().getConfigurationSection("database");
            DatabaseCore dbCore;
            if (Objects.requireNonNull(dbCfg).getBoolean("mysql")) {
                // MySQL database - Required database be created first.
                dbPrefix = dbCfg.getString("prefix");
                if (dbPrefix == null || "none".equals(dbPrefix)) {
                    dbPrefix = "";
                }
                String user = dbCfg.getString("user");
                String pass = dbCfg.getString("password");
                String host = dbCfg.getString("host");
                String port = dbCfg.getString("port");
                String database = dbCfg.getString("database");
                boolean useSSL = dbCfg.getBoolean("usessl");
                dbCore = new MySQLCore(Objects.requireNonNull(host,"MySQL host can't be null"), Objects.requireNonNull(user,"MySQL username can't be null"), Objects.requireNonNull(pass,"MySQL password can't be null"), Objects.requireNonNull(database,"MySQL database name can't be null"), Objects.requireNonNull(port,"MySQL port can't be null"), useSSL);
            } else {
                // SQLite database - Doing this handles file creation
                dbCore = new SQLiteCore(new File(this.getDataFolder(), "shops.db"));
            }
            this.database = new Database(dbCore);
            // Make the database up to date
            databaseHelper = new DatabaseHelper(this, database);
        } catch (ConnectionException e) {
            e.printStackTrace();
            if (setupDBonEnableding) {
                bootError = BuiltInSolution.databaseError();
                return false;
            } else {
                getLogger().severe("Error connecting to the database.");
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            if (setupDBonEnableding) {
                bootError = BuiltInSolution.databaseError();
                return false;
            } else {
                getLogger().severe("Error setting up the database.");
            }
            return false;
        }
        return true;
    }

    private void submitMeritcs() {
        if (!getConfig().getBoolean("disabled-metrics")) {
            String serverVer = Bukkit.getVersion();
            String bukkitVer = Bukkit.getBukkitVersion();
            String serverName = Bukkit.getServer().getName();
            String vaultVer;
            Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
            if (vault != null) {
                vaultVer = vault.getDescription().getVersion();
            } else {
                vaultVer = "Vault not found";
            }
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
            String shop_find_distance = getConfig().getString("shop.find-distance");
            String economyType = Economy.getNowUsing().name();
            String useDisplayAutoDespawn = String.valueOf(getConfig().getBoolean("shop.display-auto-despawn"));
            String useEnhanceDisplayProtect = String.valueOf(getConfig().getBoolean("shop.enchance-display-protect"));
            String useEnhanceShopProtect = String.valueOf(getConfig().getBoolean("shop.enchance-shop-protect"));
            String useOngoingFee = String.valueOf(getConfig().getBoolean("shop.ongoing-fee.enable"));
            String disableDebugLoggger = String.valueOf(getConfig().getBoolean("disable-debuglogger"));

            // Version
            metrics.addCustomChart(new Metrics.SimplePie("server_version", () -> serverVer));
            metrics.addCustomChart(new Metrics.SimplePie("bukkit_version", () -> bukkitVer));
            metrics.addCustomChart(new Metrics.SimplePie("vault_version", () -> vaultVer));
            metrics.addCustomChart(new Metrics.SimplePie("server_name", () -> serverName));
            metrics.addCustomChart(new Metrics.SimplePie("use_display_items", () -> display_Items));
            metrics.addCustomChart(new Metrics.SimplePie("use_locks", () -> locks));
            metrics.addCustomChart(new Metrics.SimplePie("use_sneak_action", () -> sneak_action));
            metrics.addCustomChart(new Metrics.SimplePie("shop_find_distance", () -> shop_find_distance));
            metrics.addCustomChart(new Metrics.SimplePie("economy_type", () -> economyType));
            metrics.addCustomChart(new Metrics.SimplePie("use_display_auto_despawn", () -> useDisplayAutoDespawn));
            metrics.addCustomChart(new Metrics.SimplePie("use_enhance_display_protect", () -> useEnhanceDisplayProtect));
            metrics.addCustomChart(new Metrics.SimplePie("use_enhance_shop_protect", () -> useEnhanceShopProtect));
            metrics.addCustomChart(new Metrics.SimplePie("use_ongoing_fee", () -> useOngoingFee));
            metrics.addCustomChart(new Metrics.SimplePie("disable_background_debug_logger", ()->disableDebugLoggger));
            // Exp for stats, maybe i need improve this, so i add this.
            metrics.submitData(); // Submit now!
            getLogger().info("Metrics submitted.");
        } else {
            getLogger().info("You have disabled mertics, Skipping...");
        }

    }

    @SuppressWarnings("UnusedAssignment")
    private void updateConfig(int selectedVersion) {
        String serverUUID = getConfig().getString("server-uuid");
        if (serverUUID == null || serverUUID.isEmpty()) {
            UUID uuid = UUID.randomUUID();
            serverUUID = uuid.toString();
            getConfig().set("server-uuid", serverUUID);
        }
        if (selectedVersion == 1) {
            getConfig().set("disabled-metrics", false);
            getConfig().set("config-version", 2);
            selectedVersion = 2;
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
        }
        if (selectedVersion == 3) {
            getConfig().set("shop.alternate-currency-symbol", '$');
            getConfig().set("config-version", 4);
            selectedVersion = 4;
        }
        if (selectedVersion == 4) {
            getConfig().set("updater", true);
            getConfig().set("config-version", 5);
            selectedVersion = 5;
        }
        if (selectedVersion == 5) {
            getConfig().set("shop.display-item-use-name", true);
            getConfig().set("config-version", 6);
            selectedVersion = 6;
        }
        if (selectedVersion == 6) {
            getConfig().set("shop.sneak-to-control", false);
            getConfig().set("config-version", 7);
            selectedVersion = 7;
        }
        if (selectedVersion == 7) {
            getConfig().set("database.prefix", "none");
            getConfig().set("database.reconnect", false);
            getConfig().set("database.use-varchar", false);
            getConfig().set("config-version", 8);
            selectedVersion = 8;
        }
        if (selectedVersion == 8) {
            getConfig().set("database.use-varchar", true);
            getConfig().set("limits.old-algorithm", false);
            getConfig().set("shop.pay-player-from-unlimited-shop-owner", false);
            getConfig().set("plugin.ProtocolLib", false);
            getConfig().set("plugin.Multiverse-Core", true);
            getConfig().set("shop.ignore-unlimited", false);
            getConfig().set("config-version", 9);
            selectedVersion = 9;
        }
        if (selectedVersion == 9) {
            getConfig().set("shop.enable-enderchest", true);
            getConfig().set("config-version", 10);
            selectedVersion = 10;
        }
        if (selectedVersion == 10) {
            getConfig().set("shop.pay-player-from-unlimited-shop-owner", null); //Removed
            getConfig().set("config-version", 11);
            selectedVersion = 11;
        }
        if (selectedVersion == 11) {
            getConfig().set("shop.enable-enderchest", null); //Removed
            getConfig().set("plugin.OpenInv", true);
            List<String> shoppable = getConfig().getStringList("shop-blocks");
            shoppable.add("ENDER_CHEST");
            getConfig().set("shop-blocks", shoppable);
            getConfig().set("config-version", 12);
            selectedVersion = 12;
        }
        if (selectedVersion == 12) {
            getConfig().set("plugin.ProtocolLib", null); //Removed
            getConfig().set("plugin.BKCommonLib", null); //Removed
            getConfig().set("plugin.BKCommonLib", null); //Removed
            getConfig().set("database.use-varchar", null); //Removed
            getConfig().set("database.reconnect", null); //Removed
            getConfig().set("anonymous-metrics", false);
            getConfig().set("display-items-check-ticks", 1200);
            getConfig().set("shop.bypass-owner-check", null); //Removed
            getConfig().set("config-version", 13);
            selectedVersion = 13;
        }
        if (selectedVersion == 13) {
            getConfig().set("plugin.AreaShop", false);
            getConfig().set("shop.special-region-only", false);
            getConfig().set("config-version", 14);
            selectedVersion = 14;
        }
        if (selectedVersion == 14) {
            getConfig().set("plugin.AreaShop", null);
            getConfig().set("shop.special-region-only", null);
            getConfig().set("config-version", 15);
            selectedVersion = 15;
        }
        if (selectedVersion == 15) {
            getConfig().set("ongoingfee", null);
            getConfig().set("shop.display-item-use-name", null);
            getConfig().set("shop.display-item-show-name", false);
            getConfig().set("shop.auto-fetch-shop-messages", true);
            getConfig().set("config-version", 16);
            selectedVersion = 16;
        }
        if (selectedVersion == 16) {
            getConfig().set("ignore-cancel-chat-event", false);
            getConfig().set("config-version", 17);
            selectedVersion = 17;
        }
        if (selectedVersion == 17) {
            getConfig().set("ignore-cancel-chat-event", false);
            getConfig().set("float", null);
            getConfig().set("config-version", 18);
            selectedVersion = 18;
        }
        if (selectedVersion == 18) {
            getConfig().set("shop.disable-vault-format", false);
            getConfig().set("config-version", 19);
            selectedVersion = 19;
        }
        if (selectedVersion == 19) {
            getConfig().set("shop.allow-shop-without-space-for-sign", true);
            getConfig().set("config-version", 20);
            selectedVersion = 20;
        }
        if (selectedVersion == 20) {
            getConfig().set("shop.maximum-price", -1);
            getConfig().set("config-version", 21);
            selectedVersion = 21;
        }
        if (selectedVersion == 21) {
            getConfig().set("shop.sign-material", "OAK_WALL_SIGN");
            getConfig().set("config-version", 22);
            selectedVersion = 22;
        }
        if (selectedVersion == 22) {
            getConfig().set("include-offlineplayer-list", "false");
            getConfig().set("config-version", 23);
            selectedVersion = 23;
        }
        if (selectedVersion == 23) {
            getConfig().set("lockette.enable", null);
            getConfig().set("lockette.item", null);
            getConfig().set("lockette.lore", null);
            getConfig().set("lockette.displayname", null);
            getConfig().set("float", null);
            getConfig().set("lockette.enable", true);
            ArrayList<String> blackListWorld = new ArrayList<>();
            blackListWorld.add("disabled_world_name");
            getConfig().set("shop.blacklist-world", blackListWorld);
            getConfig().set("config-version", 24);
            selectedVersion = 24;
        }
        if (selectedVersion == 24) {
            getConfig().set("shop.strict-matches-check", false);
            getConfig().set("config-version", 25);
            selectedVersion = 25;
        }
        if (selectedVersion == 25) {
            String language = getConfig().getString("language");
            if (language == null || language.isEmpty() || "default".equals(language)) {
                getConfig().set("language", "en");
            }
            getConfig().set("config-version", 26);
            selectedVersion = 26;
        }
        if (selectedVersion == 26) {
            getConfig().set("database.usessl", false);
            getConfig().set("config-version", 27);
            selectedVersion = 27;
        }
        if (selectedVersion == 27) {
            getConfig().set("queue.enable", true);
            getConfig().set("queue.shops-per-tick", 20);
            getConfig().set("config-version", 28);
            selectedVersion = 28;
        }
        if (selectedVersion == 28) {
            getConfig().set("database.queue", true);
            getConfig().set("config-version", 29);
            selectedVersion = 29;
        }
        if (selectedVersion == 29) {
            getConfig().set("plugin.Multiverse-Core", null);
            getConfig().set("shop.protection-checking", true);
            getConfig().set("config-version", 30);
            selectedVersion = 30;
        }
        if (selectedVersion == 30) {
            getConfig().set("auto-report-errors", true);
            getConfig().set("config-version", 31);
            selectedVersion = 31;
        }
        if (selectedVersion == 31) {
            getConfig().set("shop.display-type", 0);
            getConfig().set("config-version", 32);
            selectedVersion = 32;
        }
        if (selectedVersion == 32) {
            getConfig().set("effect.sound.ontabcomplete", true);
            getConfig().set("effect.sound.oncommand", true);
            getConfig().set("effect.sound.ononclick", true);
            getConfig().set("config-version", 33);
            selectedVersion = 33;
        }
        if (selectedVersion == 33) {
            getConfig().set("matcher.item.damage", true);
            getConfig().set("matcher.item.displayname", true);
            getConfig().set("matcher.item.lores", true);
            getConfig().set("matcher.item.enchs", true);
            getConfig().set("matcher.item.potions", true);
            getConfig().set("matcher.item.attributes", true);
            getConfig().set("matcher.item.itemflags", true);
            getConfig().set("matcher.item.custommodeldata", true);
            getConfig().set("config-version", 34);
            selectedVersion = 34;
        }
        if (selectedVersion == 34) {
            getConfig().set("queue.enable", false); // Close it for everyone
            if (getConfig().getInt("shop.display-items-check-ticks") == 1200) {
                getConfig().set("shop.display-items-check-ticks", 6000);
            }
            getConfig().set("config-version", 35);
            selectedVersion = 35;
        }
        if (selectedVersion == 35) {
            getConfig().set("queue", null); // Close it for everyone
            getConfig().set("config-version", 36);
            selectedVersion = 36;
        }
        if (selectedVersion == 36) {
            getConfig().set("economy-type", 0); // Close it for everyone
            getConfig().set("config-version", 37);
            selectedVersion = 37;
        }
        if (selectedVersion == 37) {
            getConfig().set("shop.ignore-cancel-chat-event", true);
            getConfig().set("config-version", 38);
            selectedVersion = 38;
        }
        if (selectedVersion == 38) {
            getConfig().set("protect.inventorymove", true);
            getConfig().set("protect.spread", true);
            getConfig().set("protect.fromto", true);
            getConfig().set("protect.minecart", null);
            getConfig().set("protect.hopper", null);
            getConfig().set("config-version", 39);
            selectedVersion = 39;
        }
        if (selectedVersion == 39) {
            getConfig().set("update-sign-when-inventory-moving", true);
            getConfig().set("config-version", 40);
            selectedVersion = 39;
        }
        if (selectedVersion == 40) {
            getConfig().set("allow-economy-loan", false);
            getConfig().set("config-version", 41);
            selectedVersion = 41;
        }
        if (selectedVersion == 41) {
            getConfig().set("send-display-item-protection-alert", true);
            getConfig().set("config-version", 42);
            selectedVersion = 42;
        }
        if (selectedVersion == 42) {
            getConfig().set("langutils-language", "en_us");
            getConfig().set("config-version", 43);
            selectedVersion = 43;
        }
        if (selectedVersion == 43) {
            getConfig().set("permission-type", 0);
            getConfig().set("config-version", 44);
            selectedVersion = 44;
        }
        if (selectedVersion == 44) {
            getConfig().set("matcher.item.repaircost", false);
            getConfig().set("config-version", 45);
            selectedVersion = 45;
        }
        if (selectedVersion == 45) {
            getConfig().set("shop.display-item-use-name", true);
            getConfig().set("shop.protection-checking-filter", new ArrayList<>());
            getConfig().set("config-version", 46);
            selectedVersion = 46;
        }
        if (selectedVersion == 46) {
            getConfig().set("shop.use-protection-checking-filter", true);
            getConfig().set("shop.max-shops-checks-in-once", 100);
            getConfig().set("config-version", 47);
            selectedVersion = 47;
        }
        if (selectedVersion == 47) {
            getConfig().set("shop.use-protection-checking-filter", false);
            getConfig().set("config-version", 48);
            selectedVersion = 48;
        }
        if (selectedVersion == 48) {
            getConfig().set("permission-type", null);
            getConfig().set("shop.use-protection-checking-filter", null);
            getConfig().set("shop.protection-checking-filter", null);
            getConfig().set("config-version", 49);
            selectedVersion = 49;
        }
        if (selectedVersion == 49 || selectedVersion == 50) {
            getConfig().set("shop.enchance-display-protect", false);
            getConfig().set("shop.enchance-shop-protect", false);
            getConfig().set("protect", null);
            getConfig().set("config-version", 51);
            selectedVersion = 51;
        }
        if (selectedVersion < 60) { //Ahhh fuck versions
            getConfig().set("matcher.use-bukkit-matcher", false);
            getConfig().set("config-version", 60);
            selectedVersion = 60;
        }
        if (selectedVersion == 60) { //Ahhh fuck versions
            getConfig().set("matcher.use-bukkit-matcher", null);
            getConfig().set("shop.strict-matches-check", null);
            getConfig().set("matcher.work-type", 0);
            getConfig().set("shop.display-auto-despawn", true);
            getConfig().set("shop.display-despawn-range", 10);
            getConfig().set("shop.display-check-time", 10);
            getConfig().set("config-version", 61);
            selectedVersion = 61;
        }
        if (selectedVersion == 61) { //Ahhh fuck versions
            getConfig().set("shop.word-for-sell-all-items", "all");
            getConfig().set("plugin.PlaceHolderAPI", true);
            getConfig().set("config-version", 62);
            selectedVersion = 62;
        }
        if (selectedVersion == 62) { //Ahhh fuck versions
            getConfig().set("shop.display-auto-despawn", false);
            getConfig().set("shop.word-for-trade-all-items", getConfig().getString("shop.word-for-sell-all-items"));

            getConfig().set("config-version", 63);
            selectedVersion = 63;
        }
        if (selectedVersion == 63) { //Ahhh fuck versions
            getConfig().set("shop.ongoing-fee.enable", false);
            getConfig().set("shop.ongoing-fee.ticks", 42000);
            getConfig().set("shop.ongoing-fee.cost-per-shop", 2);
            getConfig().set("shop.ongoing-fee.ignore-unlimited", true);
            getConfig().set("config-version", 64);
            selectedVersion = 64;
        }
        if (selectedVersion == 64) {
            getConfig().set("shop.allow-free-shop", false);
            getConfig().set("config-version", 65);
            selectedVersion = 65;
        }
        if (selectedVersion == 65) {
            getConfig().set("shop.minimum-price", 0.01);
            getConfig().set("config-version", 66);
            selectedVersion = 66;
        }
        if (selectedVersion == 66) {
            getConfig().set("use-decimal-format", false);
            getConfig().set("decimal-format", "#,###.##");
            getConfig().set("shop.show-owner-uuid-in-controlpanel-if-op", false);
            getConfig().set("config-version", 67);
            selectedVersion = 67;
        }
        if (selectedVersion == 67) {
            getConfig().set("disable-debuglogger", false);
            getConfig().set("matcher.use-bukkit-matcher", null);
            getConfig().set("config-version", 68);
            selectedVersion = 68;
        }
        if (selectedVersion == 68){
            ArrayList<String> temp = new ArrayList<>();
            temp.add("SoulBound");
            getConfig().set("shop.blacklist-lores", temp);
            getConfig().set("config-version", 69);
            selectedVersion = 69;
        }
        if (selectedVersion == 69){
            getConfig().set("shop.display-item-use-name", false);
            getConfig().set("config-version", 70);
            selectedVersion = 70;
        }
        if (selectedVersion == 70){
            getConfig().set("cachingpool.enable", false);
            getConfig().set("cachingpool.maxsize", 100000000);
            getConfig().set("config-version", 71);
            selectedVersion = 71;
        }
        if(selectedVersion == 71){
            if(Objects.equals(getConfig().getString("language"), "en")){
                getConfig().set("language","en-US");
            }
            getConfig().set("server-platform",0);
            getConfig().set("config-version", 72);
        }


        saveConfig();
        reloadConfig();
        File file = new File(getDataFolder(), "example.config.yml");
        file.delete();
        try {
            Files.copy(Objects.requireNonNull(getResource("config.yml")), file.toPath());
        } catch (IOException ioe) {
            getLogger().warning("Error on spawning the example config file: " + ioe.getMessage());
        }
        configVaildate();
    }

    public void configVaildate(){
        YamlConfiguration attached = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(getResource("config.yml"))));
        Set<String> keysA = new HashSet<>(attached.getKeys(true));
        Set<String> keysB = new HashSet<>(getConfig().getKeys(true));
        Set<String> ignoreCheckKeys = new HashSet<>();
        ignoreCheckKeys.add("server-uuid");
        final String msgForConfiguration = "Missing options in config.yml, the key [%key%] not exist in config.yml, it may cause plugin errors, please fix it! There is guide for you to fix: \n" +
                ".....\n" +
                "%data%\n" +
                ".....\n" +
                "Tips: Add \"auto-fix-configuration: true\" in config.yml to allow QuickShop automatic fix your configuration!";
        keysA.stream().filter((key)->!keysB.contains(key)).filter((key)->!ignoreCheckKeys.contains(key)).collect(Collectors.toList()).forEach((miss)->{
            String theMsg = msgForConfiguration;
            theMsg = theMsg.replace("%key%",miss);
            List<String> tiers = new ArrayList<>(Arrays.asList(miss.split("\\.")));
            StringBuilder miss2Yaml = new StringBuilder();
            int spaces = 2;
            Iterator iterator = tiers.iterator();
            while (true){
                String tier = (String) iterator.next();
                miss2Yaml.append(tier);
                if(iterator.hasNext()){
                    miss2Yaml.append(": ");
                    miss2Yaml.append("\n");
                    for (int i = 0; i < spaces; i++) {
                        miss2Yaml.append(" ");
                    }
                    spaces += 2;
                }else{
                    miss2Yaml.append(": ");
                    miss2Yaml.append(attached.get(miss));
                   break;
                }
            }
            theMsg = theMsg.replace("%data%",miss2Yaml.toString());
            getLogger().warning(theMsg);
        });
    }

    /**
     * Return the QSRR's fork edition name, you can modify this if you want create yourself fork.
     *
     * @return The fork name.
     */
    public String getFork() {
        return "Reremake";
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

    /**
     * Get the permissionManager as static
     *
     * @return the permission Manager.
     */
    public static PermissionManager getPermissionManager() {
        return permissionManager;
    }
}
