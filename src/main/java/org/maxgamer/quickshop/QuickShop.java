package org.maxgamer.quickshop;

import java.io.File;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;

import javafx.scene.control.Tab;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Command.CommandManager;
import org.maxgamer.quickshop.Database.*;
import org.maxgamer.quickshop.Database.Database.ConnectionException;
import org.maxgamer.quickshop.Economy.*;
import org.maxgamer.quickshop.Listeners.*;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopLoader;
import org.maxgamer.quickshop.Shop.ShopManager;
import org.maxgamer.quickshop.Util.*;
import org.maxgamer.quickshop.Util.Timer;
import org.maxgamer.quickshop.Watcher.LogWatcher;
import org.maxgamer.quickshop.Watcher.UpdateWatcher;

//import com.griefcraft.lwc.LWCPlugin;
@Getter
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
    private HashSet<String> warnings = new HashSet<>();
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
    //private BukkitTask itemWatcherTask;
    private LogWatcher logWatcher;
    private ItemMatcher itemMatcher;
    private DisplayBugFixListener displayBugFixListener;
    private LockListener lockListener;
//	/** Whether players are required to sneak to create/buy from a shop */
//	public boolean sneak;
//	/** Whether players are required to sneak to create a shop */
//	public boolean sneakCreate;
//	/** Whether players are required to sneak to trade with a shop */
//	public boolean sneakTrade;
    /** Whether we should use display items or not */
    private boolean display = true;
    /**
     * Whether we players are charged a fee to change the price on their shop (To
     * help deter endless undercutting
     */
    private boolean priceChangeRequiresFee = false;
    /** Whether or not to limit players shop amounts */
    private boolean limit = false;

    /** The plugin OpenInv (null if not present) */
    private Plugin openInvPlugin;
    /** The shop limites. **/
    private HashMap<String, Integer> limits = new HashMap<>();
    /** Use SpoutPlugin to get item / block names */
    private boolean useSpout = false;
    // private Metrics metrics;
    private int displayItemCheckTicks;
    private boolean noopDisable;
    private boolean setupDBonEnableding = false;
    /** Default database prefix, can overwrite by config **/
    private String dbPrefix = "";
    private Tab commandTabCompleter;
    /** bStats, good helper for metrics. **/
    private Metrics metrics;
    /** Language manager, to select which language will loaded. **/
    private Language language;
    /** The BootError, if it not NULL, plugin will stop loading and show setted errors when use /qs **/
    private BootError bootError;
    private CustomInventoryListener customInventoryListener;
    /** WIP **/
    private Compatibility compatibilityTool = new Compatibility(this);
    /** Rewrited shoploader, more faster. **/
    private ShopLoader shopLoader;
    /** Contains all SQL tasks **/
    private DatabaseHelper databaseHelper;
    /** Queued database manager **/
    private DatabaseManager databaseManager;
    /** A util to call to check some actions permission **/
    private PermissionChecker permissionChecker;
    /** The server UniqueID, use to the ErrorReporter **/
    private UUID serverUniqueID;
    /** The error reporter to help devs report errors to Sentry.io **/
    private SentryErrorReporter sentryErrorReporter;
    private CommandManager commandManager;

    /**
     * Get the Player's Shop limit.
     *
     * @param p The player you want get limit.
     * @return int Player's shop limit
     */
    public int getShopLimit(@NotNull Player p) {
        int max = getConfig().getInt("limits.default");
        for (Entry<String, Integer> entry : limits.entrySet()) {
            if (entry.getValue() > max && p.hasPermission(entry.getKey()))
                max = entry.getValue();
        }
        return max;
    }

    /**
     * Check the env plugin running.
     *
     * @throws RuntimeException The error message, use this to create a BootError.
     */
    private void runtimeCheck() throws RuntimeException {
        try {
            getServer().spigot();
        } catch (Throwable e) {
            getLogger().severe("FATAL: QSRR can only be run on Spigot servers and forks of Spigot!");
            throw new RuntimeException("Server must be Spigot based, Don't use CraftBukkit!");
        }

        if (getServer().getName().toLowerCase().contains("catserver")) {
            // Send FATAL ERROR TO CatServer's users.
            getLogger().severe("FATAL: QSRR can't run on CatServer Community/Personal/Pro");
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
        if (incompatibleChecker.isIncompatible(nmsVersion))
            throw new RuntimeException("Your Minecraft version is nolonger supported: " + nmsVersion);
    }

    /**
     * Load 3rdParty plugin support module.
     */
    private void load3rdParty() {
        // added for compatibility reasons with OpenInv - see
        // https://github.com/KaiKikuchi/QuickShop/issues/139
        if (getConfig().getBoolean("plugin.OpenInv")) {
            this.openInvPlugin = Bukkit.getPluginManager().getPlugin("OpenInv");
            if (this.openInvPlugin != null)
                getLogger().info("Successfully loaded OpenInv support!");
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
    public void onEnable() {
        Timer enableTimer = new Timer(true);
        /* PreInit for BootError feature */
        commandManager = new CommandManager();
        //noinspection ConstantConditions
        getCommand("qs").setExecutor(commandManager);
        //noinspection ConstantConditions
        getCommand("qs").setTabCompleter(commandManager);

        getLogger().info("Quickshop Reremake");
        getLogger().info("Developers: " + Util.list2String(this.getDescription().getAuthors()));
        getLogger().info("Original author: Netherfoam, Timtower, KaiNoMood");
        getLogger().info("Let's start loading the plugin");
        /* Check the running envs is support or not. */
        try {
            runtimeCheck();
        } catch (RuntimeException e) {
            bootError = new BootError(e.getMessage());
            return;
        }
        /* Process the config */
        saveDefaultConfig();

        reloadConfig(); //Plugin support reload, so need reload config here.
        this.reloadConfig();

        getConfig().options().copyDefaults(true); // Load defaults.

        if (getConfig().getInt("config-version") == 0)
            getConfig().set("config-version", 1);

        updateConfig(getConfig().getInt("config-version"));
        /* It will generate a new UUID above updateConfig */
        //noinspection ConstantConditions
        /* Process Metrics and Sentry error reporter. */
        metrics = new Metrics(this);
        serverUniqueID = UUID.fromString(getConfig().getString("server-uuid"));
        sentryErrorReporter = new SentryErrorReporter(this);



        /* Process the Economy system. */
        if (!loadEcon()) {
            bootError = BuiltInSolution.econError();
            return;
        }

        /* Load 3rd party supports */
        load3rdParty();

        /* Initalize the Utils */
        itemMatcher = new ItemMatcher(this);
        Util.initialize();

        MsgUtil.loadItemi18n();
        MsgUtil.loadEnchi18n();
        MsgUtil.loadPotioni18n();

        setupDBonEnableding = true;
        setupDatabase(); //Load the database
        setupDBonEnableding = false;

        /* Initalize the tools */
        // Create the shop manager.
        this.shopManager = new ShopManager(this);
        this.databaseManager = new DatabaseManager(this, database);
        this.permissionChecker = new PermissionChecker(this);


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

        if (getConfig().getInt("shop.find-distance") > 100) {
            getLogger().severe("Shop.find-distance is too high! It may cause lag! Pick a number under 100!");
        }
        if (getConfig().getInt("shop.display-items-check-ticks") < 3000) {
            getLogger().severe("Shop.display-items-check-ticks is too low! It may cause lag! Pick a number > 3000");
        }
        /* Load all shops. */
        shopLoader = new ShopLoader(this);
        shopLoader.loadShops();

        getLogger().info("Registering Listeners...");
        // Register events
        if (getConfig().getBoolean("shop.lock")) {
            LockListener lockListener = new LockListener(this);
            Bukkit.getServer().getPluginManager().registerEvents(lockListener, this);
        }
        blockListener = new BlockListener(this);
        playerListener = new PlayerListener(this);
        worldListener = new WorldListener(this);
        chatListener = new ChatListener(this);
        chunkListener = new ChunkListener(this);
        inventoryListener = new DisplayProtectionListener(this);
        customInventoryListener = new CustomInventoryListener(this);
        displayBugFixListener = new DisplayBugFixListener(this);
        Bukkit.getServer().getPluginManager().registerEvents(blockListener, this);
        Bukkit.getServer().getPluginManager().registerEvents(playerListener, this);
        Bukkit.getServer().getPluginManager().registerEvents(chatListener, this);
        Bukkit.getServer().getPluginManager().registerEvents(inventoryListener, this);
        Bukkit.getServer().getPluginManager().registerEvents(chunkListener, this);
        Bukkit.getServer().getPluginManager().registerEvents(worldListener, this);
        Bukkit.getServer().getPluginManager().registerEvents(customInventoryListener, this);
        Bukkit.getServer().getPluginManager().registerEvents(displayBugFixListener, this);
        getLogger().info("Registering DisplayCheck Task....");
        if (display && displayItemCheckTicks > 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Iterator<Shop> it = getShopManager().getShopIterator();
                    while (it.hasNext()) {
                        Shop shop = it.next();
                        if (shop == null)
                            continue;
                        shop.checkDisplay();
                    }
                }
            }.runTaskTimer(this, 1L, displayItemCheckTicks);
        }
        getLogger().info("Cleaning MsgUtils...");
        MsgUtil.loadTransactionMessages();
        MsgUtil.clean();
        getLogger().info("Registering UpdateWatcher...");
        UpdateWatcher.init();
        getLogger().info("Registering BStats Mertics...");
        submitMeritcs();
        getLogger().info("QuickShop Loaded! " + enableTimer.endTimer() + " ms.");
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
            // Version
            metrics.addCustomChart(new Metrics.SimplePie("server_version", () -> serverVer));
            metrics.addCustomChart(new Metrics.SimplePie("bukkit_version", () -> bukkitVer));
            metrics.addCustomChart(new Metrics.SimplePie("vault_version", () -> vaultVer));
            metrics.addCustomChart(new Metrics.SimplePie("server_name", () -> serverName));
            metrics.addCustomChart(new Metrics.SimplePie("use_display_items", () -> display_Items));
            metrics.addCustomChart(new Metrics.SimplePie("use_locks", () -> locks));
            metrics.addCustomChart(new Metrics.SimplePie("use_sneak_action", () -> sneak_action));
            metrics.addCustomChart(new Metrics.SimplePie("shop_find_distance", () -> shop_find_distance));

            // Exp for stats, maybe i need improve this, so i add this.
            metrics.submitData(); // Submit now!
            getLogger().info("Metrics submitted.");
        } else {
            getLogger().info("You have disabled mertics, Skipping...");
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
            if (dbCfg.getBoolean("mysql")) {
                // MySQL database - Required database be created first.
                dbPrefix = dbCfg.getString("prefix");
                if (dbPrefix == null || dbPrefix.equals("none"))
                    dbPrefix = "";
                String user = dbCfg.getString("user");
                String pass = dbCfg.getString("password");
                String host = dbCfg.getString("host");
                String port = dbCfg.getString("port");
                String database = dbCfg.getString("database");
                boolean useSSL = dbCfg.getBoolean("usessl");
                dbCore = new MySQLCore(host, user, pass, database, port, useSSL);
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

    @SuppressWarnings("UnusedAssignment")
    private void updateConfig(int selectedVersion) {
        String serverUUID = getConfig().getString("server-uuid");
        if (serverUUID == null || serverUUID.isEmpty()) {
            UUID uuid = UUID.randomUUID();
            serverUUID = uuid.toString();
            getConfig().set("server-uuid", serverUUID);
            saveConfig();
            reloadConfig();
        }
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
            getConfig().set("shop.ignore-unlimited", false);
            getConfig().set("config-version", 9);
            selectedVersion = 9;
            saveConfig();
            reloadConfig();
        }
        if (selectedVersion == 9) {
            getConfig().set("shop.enable-enderchest", true);
            getConfig().set("config-version", 10);
            selectedVersion = 10;
            saveConfig();
            reloadConfig();
        }
        if (selectedVersion == 10) {
            getConfig().set("shop.pay-player-from-unlimited-shop-owner", null); //Removed
            getConfig().set("config-version", 11);
            selectedVersion = 11;
            saveConfig();
            reloadConfig();
        }
        if (selectedVersion == 11) {
            getConfig().set("shop.enable-enderchest", null); //Removed
            getConfig().set("plugin.OpenInv", true);
            List<String> shoppable = getConfig().getStringList("shop-blocks");
            shoppable.add("ENDER_CHEST");
            getConfig().set("shop-blocks", shoppable);
            getConfig().set("config-version", 12);
            selectedVersion = 12;
            saveConfig();
            reloadConfig();
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
            saveConfig();
            reloadConfig();
        }
        if (selectedVersion == 13) {
            getConfig().set("plugin.AreaShop", false);
            getConfig().set("shop.special-region-only", false);
            getConfig().set("config-version", 14);
            selectedVersion = 14;
            saveConfig();
            reloadConfig();
        }
        if (selectedVersion == 14) {
            getConfig().set("plugin.AreaShop", null);
            getConfig().set("shop.special-region-only", null);
            getConfig().set("config-version", 15);
            selectedVersion = 15;
            saveConfig();
            reloadConfig();
        }
        if (selectedVersion == 15) {
            getConfig().set("ongoingfee", null);
            getConfig().set("shop.display-item-use-name", null);
            getConfig().set("shop.display-item-show-name", false);
            getConfig().set("shop.auto-fetch-shop-messages", true);
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
        if (selectedVersion == 21) {
            getConfig().set("shop.sign-material", "OAK_WALL_SIGN");
            getConfig().set("config-version", 22);
            selectedVersion = 22;
            saveConfig();
            reloadConfig();
        }
        if (selectedVersion == 22) {
            getConfig().set("include-offlineplayer-list", "false");
            getConfig().set("config-version", 23);
            selectedVersion = 23;
            saveConfig();
            reloadConfig();
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
            saveConfig();
            reloadConfig();
        }
        if (selectedVersion == 24) {
            getConfig().set("shop.strict-matches-check", false);
            getConfig().set("config-version", 25);
            selectedVersion = 25;
            saveConfig();
            reloadConfig();
        }
        if (selectedVersion == 25) {
            if (getConfig().getString("language").equals("default"))
                getConfig().set("language", "en");
            getConfig().set("config-version", 26);
            selectedVersion = 26;
            saveConfig();
            reloadConfig();
        }
        if (selectedVersion == 26) {
            getConfig().set("database.usessl", false);
            getConfig().set("config-version", 27);
            selectedVersion = 27;
            saveConfig();
            reloadConfig();

        }
        if (selectedVersion == 27) {
            getConfig().set("queue.enable", true);
            getConfig().set("queue.shops-per-tick", 20);
            getConfig().set("config-version", 28);
            selectedVersion = 28;
            saveConfig();
            reloadConfig();

        }
        if (selectedVersion == 28) {
            getConfig().set("database.queue", true);
            getConfig().set("config-version", 29);
            selectedVersion = 29;
            saveConfig();
            reloadConfig();

        }
        if (selectedVersion == 29) {
            getConfig().set("plugin.Multiverse-Core", null);
            getConfig().set("shop.protection-checking", true);
            getConfig().set("config-version", 30);
            selectedVersion = 30;
            saveConfig();
            reloadConfig();
        }
        if (selectedVersion == 30) {
            getConfig().set("auto-report-errors", true);
            getConfig().set("config-version", 31);
            selectedVersion = 31;
            saveConfig();
            reloadConfig();
        }
        if (selectedVersion == 31) {
            getConfig().set("shop.display-type", 0);
            getConfig().set("config-version", 32);
            selectedVersion = 32;
            saveConfig();
            reloadConfig();
        }
        if (selectedVersion == 32) {
            getConfig().set("effect.sound.ontabcomplete", true);
            getConfig().set("effect.sound.oncommand", true);
            getConfig().set("effect.sound.ononclick", true);
            getConfig().set("config-version", 33);
            selectedVersion = 33;
            saveConfig();
            reloadConfig();
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
            saveConfig();
            reloadConfig();
        }
        if (selectedVersion == 34) {
            getConfig().set("queue.enable", false); // Close it for everyone
            if (getConfig().getInt("shop.display-items-check-ticks") == 1200)
                getConfig().set("shop.display-items-check-ticks", 6000);
            getConfig().set("config-version", 35);
            selectedVersion = 35;
            saveConfig();
            reloadConfig();
        }
        if (selectedVersion == 35) {
            getConfig().set("queue", null); // Close it for everyone
            getConfig().set("config-version", 36);
            selectedVersion = 36;
            saveConfig();
            reloadConfig();
        }
        if (selectedVersion == 36) {
            getConfig().set("economy-type", 0); // Close it for everyone
            getConfig().set("config-version", 37);
            selectedVersion = 37;
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
        this.priceChangeRequiresFee = this.getConfig().getBoolean("shop.price-change-requires-fee");
        this.displayItemCheckTicks = this.getConfig().getInt("shop.display-items-check-ticks");
        language = new Language(this); //Init locale
        if (this.getConfig().getBoolean("log-actions")) {
            logWatcher = new LogWatcher(this, new File(getDataFolder(), "qs.log"));
        } else {
            logWatcher = null;
        }
        MsgUtil.loadCfgMessages();
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
            }

            // if(Bukkit.getPluginManager().isPluginEnabled("Reserve")) {
            //     final EconomyCore reserveCore = new Economy_Reserve();
            //     if(reserveCore.isValid()) {
            //         core = reserveCore;
            //         Util.debugLog("QuickShop now using Reserve for economy system.");
            //     }
            // }

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
            return false;
        }
    }

    @Override
    public void onDisable() {
        if (noopDisable)
            return;
        getLogger().info("QuickShop is finishing remaining work, this may need a while...");
        // Util.debugLog("Cleaning up shop queues...");
        // if (this.getQueuedShopManager() != null)
        //     this.getQueuedShopManager().uninit();

        Util.debugLog("Closing all GUIs...");
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
        }

        Util.debugLog("Cleaning up database queues...");
        if (this.getShopManager() != null)
            this.getDatabaseManager().uninit();

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
        shopManager.clear();
        /* Empty the buffer */
        database.close();
        /* Close Database */
        try {
            this.database.getConnection().close();
        } catch (SQLException e) {
            this.getSentryErrorReporter().ignoreThrow();
            e.printStackTrace();
        }

        this.warnings.clear();
        //this.reloadConfig();
        Util.debugLog("All shutdown work is finished.");
    }

    /**
     * Logs the given string to qs.log, if QuickShop is configured to do so.
     *
     * @param s The string to log. It will be prefixed with the date and time.
     */
    public void log(@NotNull String s) {
        if (this.logWatcher == null)
            return;
        Date date = Calendar.getInstance().getTime();
        Timestamp time = new Timestamp(date.getTime());
        this.logWatcher.add("[" + time.toString() + "] " + s);
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
     * Return the QSRR's fork edition name, you can modify this if you want create yourself fork.
     *
     * @return The fork name.
     */
    public String getFork() { return "Reremake"; }

}