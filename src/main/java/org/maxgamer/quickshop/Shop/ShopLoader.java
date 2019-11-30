package org.maxgamer.quickshop.Shop;

import com.google.gson.JsonSyntaxException;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Timer;
import org.maxgamer.quickshop.Util.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * A class allow plugin load shops fast and simply.
 */
public class ShopLoader {
    final private ArrayList<Long> loadTimes = new ArrayList<>();
    private int errors;
    private int loadAfterChunkLoaded = 0;
    private int loadAfterWorldLoaded = 0;
    private QuickShop plugin;
    private int totalLoaded = 0;
    /* This may contains broken shop, must use null check before load it. */
    @Getter private List<Shop> shopsInDatabase = new ArrayList<>();
    @Getter private List<ShopDatabaseInfoOrigin> originShopsInDatabase = new ArrayList<>();

    /**
     * The shop load allow plugin load shops fast and simply.
     *
     * @param plugin Plugin main class
     */
    public ShopLoader(@NotNull QuickShop plugin) {
        this.plugin = plugin;
    }

    private void exceptionHandler(@NotNull Exception ex, @Nullable Location shopLocation) {
        errors++;
        Logger logger = plugin.getLogger();
        logger.warning("##########FAILED TO LOAD SHOP##########");
        logger.warning("  >> Error Info:");
        logger.warning(ex.getMessage());
        logger.warning("  >> Error Trace");
        ex.printStackTrace();
        logger.warning("  >> Target Location Info");
        logger.warning("Location: " + ((shopLocation == null) ? "NULL" : shopLocation.toString()));
        logger.warning("Block: " + ((shopLocation == null) ? "NULL" : shopLocation.getBlock().getType().name()));
        logger.warning("  >> Database Info");
        try {
            logger.warning("Connected: " + plugin.getDatabase().getConnection().isClosed());
        } catch (SQLException | NullPointerException e) {
            logger.warning("Connected: " + "FALSE - Failed to load status.");
        }

        try {
            logger.warning("Readonly: " + plugin.getDatabase().getConnection().isReadOnly());
        } catch (SQLException | NullPointerException e) {
            logger.warning("Readonly: " + "FALSE - Failed to load status.");
        }

        try {
            logger.warning("ClientInfo: " + plugin.getDatabase().getConnection().getClientInfo().toString());
        } catch (SQLException | NullPointerException e) {
            logger.warning("ClientInfo: " + "FALSE - Failed to load status.");
        }

        logger.warning("#######################################");
        if (errors > 10) {
            logger.severe("QuickShop detected too many errors when loading shops, you should backup your shop database and ask the developer for help");
        }
    }

    /**
     * Load all shops
     *
     * @param worldName The world name
     */
    public void loadShops(@Nullable String worldName) {
        boolean backupedDatabaseInDeleteProcess = false;
        Timer totalLoadTimer = new Timer(true);
        try {
            this.plugin.getLogger().info("Loading shops from the database...");
            Timer fetchTimer = new Timer(true);
            ResultSet rs = plugin.getDatabaseHelper().selectAllShops();
            this.plugin.getLogger().info("Used " + fetchTimer.endTimer() + "ms to fetch all shops from the database.");
            while (rs.next()) {
                Timer singleShopLoadTimer = new Timer(true);

                ShopDatabaseInfoOrigin origin = new ShopDatabaseInfoOrigin(rs);
                originShopsInDatabase.add(origin);
                if (worldName != null && !origin.getWorld().equals(worldName)) {
                    singleShopLoaded(singleShopLoadTimer);
                    continue;
                }
                ShopDatabaseInfo data = new ShopDatabaseInfo(origin);

                Shop shop = new ContainerShop(data.getLocation(), data.getPrice(), data.getItem(), data.getModerators(), data
                        .isUnlimited(), data
                        .getType());
                shopsInDatabase.add(shop);

                if (shopNullCheck(shop)) {
                    Util.debugLog("Somethings gone wrong, skipping the loading...");
                    loadAfterWorldLoaded++;
                    singleShopLoaded(singleShopLoadTimer);
                    continue;
                }
                //Load to RAM
                plugin.getShopManager().loadShop(data.getWorld().getName(), shop);

                if (Util.isLoaded(shop.getLocation())) {
                    //Load to World
                    if (!Util.canBeShop(shop.getLocation().getBlock())) {
                        Util.debugLog("Target block can't be a shop, removing it from the database...");
                        //shop.delete();
                        plugin.getShopManager().removeShop(shop); //Remove from Mem
                        if(!backupedDatabaseInDeleteProcess){ //Only backup db one time.
                            backupedDatabaseInDeleteProcess = Util.backupDatabase();
                        }

                        if(backupedDatabaseInDeleteProcess){
                            plugin.getDatabaseHelper().removeShop(shop.getLocation().getBlockX(), shop
                                    .getLocation().getBlockY(), shop.getLocation().getBlockZ(), shop.getLocation().getWorld()
                                    .getName());
                        }

                        singleShopLoaded(singleShopLoadTimer);
                        continue;
                    }
                    shop.onLoad();
                    shop.update();

                } else {
                    loadAfterChunkLoaded++;
                }
                singleShopLoaded(singleShopLoadTimer);
            }
            long totalUsedTime = totalLoadTimer.endTimer();
            long avgPerShop = mean(loadTimes.toArray(new Long[0]));
            this.plugin.getLogger()
                    .info("Successfully loaded " + totalLoaded + " shops! (Used " + totalUsedTime + "ms, Avg " + avgPerShop + "ms per shop)");
            this.plugin.getLogger()
                    .info(this.loadAfterChunkLoaded + " shops will load after chunk have loaded, " + this.loadAfterWorldLoaded + " shops will load after the world has loaded.");
        } catch (Exception e) {
            exceptionHandler(e, null);
        }
    }

    public void loadShops() {
        loadShops(null);
    }

    private @NotNull Long mean(Long[] m) {
        long sum = 0;
        for (Long aM : m) {
            sum += aM;
        }
        if (m.length == 0) {
            return sum;
        }
        return sum / m.length;
    }

    private boolean shopNullCheck(@Nullable Shop shop) {
        if (shop == null) {
            Util.debugLog("Shop Object is null");
            return true;
        }
        if (shop.getItem() == null) {
            Util.debugLog("Shop ItemStack is null");
            return true;
        }
        if (shop.getItem().getType() == Material.AIR) {
            Util.debugLog("Shop ItemStack type can't be AIR");
            return true;
        }
        if (shop.getLocation() == null) {
            Util.debugLog("Shop Location is null");
            return true;
        }
        if (shop.getLocation().getWorld() == null) {
            Util.debugLog("Shop World is null");
            return true;
        }
        if (shop.getOwner() == null) {
            Util.debugLog("Shop Owner is null");
            return true;
        }
        return false;
    }

    private void singleShopLoaded(@NotNull Timer singleShopLoadTimer) {
        totalLoaded++;
        long singleShopLoadTime = singleShopLoadTimer.endTimer();
        loadTimes.add(singleShopLoadTime);
        Util.debugLog("Loaded shop used time " + singleShopLoadTime + "ms");
    }

   @Data
    public class ShopDatabaseInfo {
        private ItemStack item;
        private Location location;
        private ShopModerator moderators;
        private double price;
        private ShopType type;
        private boolean unlimited;
        private World world;
        private int x;
        private int y;
        private int z;

        ShopDatabaseInfo(ShopDatabaseInfoOrigin origin) {
            try {
                this.x = origin.getX();
                this.y = origin.getY();
                this.z = origin.getZ();
                this.price = origin.getPrice();
                this.unlimited = origin.isUnlimited();
                this.type = ShopType.fromID(origin.getType());
                this.world = Bukkit.getWorld(origin.getWorld());
                this.item = deserializeItem(origin.getItem());
                this.moderators = deserializeModerator(origin.getModerators());
                this.location = new Location(world, x, y, z);
            } catch (Exception ex) {
                exceptionHandler(ex, this.location);
            }
        }

        private @Nullable ItemStack deserializeItem(@NotNull String itemConfig) {
            try {
                return Util.deserialize(itemConfig);
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Failed load shop data, because target config can't deserialize the ItemStack.");
                Util.debugLog("Failed to load data to the ItemStack: " + itemConfig);
                return null;
            }
        }

        private @Nullable ShopModerator deserializeModerator(@NotNull String moderatorJson) {
            ShopModerator shopModerator;
            if (Util.isUUID(moderatorJson)) {
                Util.debugLog("Updating old shop data...");
                shopModerator = new ShopModerator(UUID.fromString(moderatorJson)); //New one
            } else {
                try {
                    shopModerator = ShopModerator.deserialize(moderatorJson);
                } catch (JsonSyntaxException ex) {
                    Util.debugLog("Updating old shop data...");
                    moderatorJson = Bukkit.getOfflinePlayer(moderatorJson).getUniqueId().toString();
                    shopModerator = new ShopModerator(UUID.fromString(moderatorJson)); //New one
                }
            }
            return shopModerator;
        }
    }

    @Data
    public class ShopDatabaseInfoOrigin {
        private String item;
        private String moderators;
        private double price;
        private int type;
        private boolean unlimited;
        private String world;
        private int x;
        private int y;
        private int z;

        ShopDatabaseInfoOrigin(ResultSet rs) {
            try {
                this.x = rs.getInt("x");
                this.y = rs.getInt("y");
                this.z = rs.getInt("z");
                this.world = rs.getString("world");
                this.item = rs.getString("itemConfig");
                this.moderators = rs.getString("owner");
                this.price = rs.getDouble("price");
                this.type = rs.getInt("type");
                this.unlimited = rs.getBoolean("unlimited");
            } catch (SQLException sqlex) {
                exceptionHandler(sqlex, null);
            }

        }
    }
}
