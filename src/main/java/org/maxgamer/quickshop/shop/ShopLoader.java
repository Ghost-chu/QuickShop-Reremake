/*
 * This file is a part of project QuickShop, the name is ShopLoader.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.shop;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.api.shop.ShopType;
import org.maxgamer.quickshop.database.JavaWarpedResultSet;
import org.maxgamer.quickshop.util.JsonUtil;
import org.maxgamer.quickshop.util.Timer;
import org.maxgamer.quickshop.util.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class allow plugin load shops fast and simply.
 */
public class ShopLoader {
    private final List<Long> loadTimes = new ArrayList<>();

    private final Map<Timer, Double> timeCostCache = new HashMap<>();

    private final QuickShop plugin;
    /* This may contains broken shop, must use null check before load it. */
    private int errors;
    //private final WarningSender warningSender;

    /**
     * The shop load allow plugin load shops fast and simply.
     *
     * @param plugin Plugin main class
     */
    public ShopLoader(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        //this.warningSender = new WarningSender(plugin, 15000);
    }

    public void loadShops() {
        loadShops(null);
    }

    /**
     * Load all shops in the specified world
     *
     * @param worldName The world name, null if load all shops
     */
    public void loadShops(@Nullable String worldName) {
        //boolean backupedDatabaseInDeleteProcess = false;
        this.plugin.getLogger().info("Fetching shops from the database...If plugin stuck there, check your database connection.");
        int loadAfterChunkLoaded = 0;
        int loadAfterWorldLoaded = 0;
        List<Shop> pendingLoadShops = new ArrayList<>();
        try (JavaWarpedResultSet warpRS = plugin.getDatabaseHelper().selectAllShops(); ResultSet rs = warpRS.getResultSet()) {
            this.plugin.getLogger().info("Loading shops from the database...");
            while (rs.next()) {
                ShopRawDatabaseInfo origin = new ShopRawDatabaseInfo(rs);
                if (worldName != null && !origin.getWorld().equals(worldName)) {
                    continue;
                }
                ShopDatabaseInfo data;
                try {
                    data = new ShopDatabaseInfo(origin);
                } catch (Exception e) {
                    exceptionHandler(e, null);
                    continue;
                }
                //World unloaded and not found
                if (data.getWorld() == null) {
                    ++loadAfterWorldLoaded;
                    continue;
                }
                Shop shop =
                        new ContainerShop(plugin,
                                data.getLocation(),
                                data.getPrice(),
                                data.getItem(),
                                data.getModerators(),
                                data.isUnlimited(),
                                data.getType(),
                                data.getExtra(),
                                data.getCurrency(),
                                data.isDisableDisplay(),
                                data.getTaxAccount());
                if (data.needUpdate.get()) {
                    shop.setDirty();
                }
                if (shopNullCheck(shop)) {
                    if (plugin.getConfig().getBoolean("debug.delete-corrupt-shops", false)) {
                        plugin.getLogger().warning("Deleting shop " + shop + " caused by corrupted.");
                        plugin.getDatabaseHelper().removeShop(origin.getWorld(), origin.getX(), origin.getY(), origin.getZ());
                    } else {
                        Util.debugLog("Trouble database loading debug: " + data);
                        Util.debugLog("Somethings gone wrong, skipping the loading...");
                    }
                    continue;
                }
                //World unloaded but found
                if (!shop.getLocation().isWorldLoaded()) {
                    ++loadAfterWorldLoaded;
                    continue;
                }
                // Load to RAM
                plugin.getShopManager().loadShop(data.getWorld().getName(), shop);
                if (Util.isLoaded(shop.getLocation())) {
                    // Load to World
                    if (!Util.canBeShop(shop.getLocation().getBlock())) {
                        Util.debugLog("Target block can't be a shop, removing it from the memory...");
                        // shop.delete();
                        plugin.getShopManager().removeShop(shop); // Remove from Mem
                        //TODO: Only remove from memory, so if it actually is a bug, user won't lost all shops.
                        //TODO: Old shop will be deleted when in same location creating new shop.
                    } else {
                        pendingLoadShops.add(shop);
                    }
                } else {
                    loadAfterChunkLoaded++;
                }
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                this.plugin.getLogger().info("Loading the shops in worlds...");
                for (Shop shop : pendingLoadShops) {
                    shop.onLoad();
                    shop.update();
                }
                this.plugin
                        .getLogger()
                        .info(
                                "Successfully loaded "
                                        + pendingLoadShops.size()
                                        + " shops!");
            }, 1);
            this.plugin.getLogger().info("Scheduled " + pendingLoadShops.size() + " shops to load in next tick, " + loadAfterChunkLoaded
                    + " shops will load after chunk have loaded, "
                    + loadAfterWorldLoaded
                    + " shops will load after the world has loaded.");
        } catch (Exception e) {
            exceptionHandler(e, null);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private boolean shopNullCheck(@Nullable Shop shop) {
        if (shop == null) {
            Util.debugLog("Shop object is null");
            return true;
        }
        if (shop.getItem() == null) {
            Util.debugLog("Shop itemStack is null");
            return true;
        }
        if (shop.getItem().getType() == Material.AIR) {
            Util.debugLog("Shop itemStack type can't be AIR");
            return true;
        }
        if (shop.getLocation() == null) {
            Util.debugLog("Shop location is null");
            return true;
        }
        if (shop.getOwner() == null) {
            Util.debugLog("Shop owner is null");
            return true;
        }
        if (plugin.getServer().getOfflinePlayer(shop.getOwner()).getName() == null) {
            Util.debugLog("Shop owner not exist on this server, did you have reset the playerdata?");
        }
        return false;
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

    @NotNull
    private YamlConfiguration extraUpgrade(@NotNull String extraString) {
        if (!StringUtils.isEmpty(extraString) && !"QuickShop: {}".equalsIgnoreCase(extraString)) {
            Util.debugLog("Extra API -> Upgrading -> " + extraString.replaceAll("\n", ""));
        }
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        JsonConfiguration jsonConfiguration = new JsonConfiguration();
        try {
            jsonConfiguration.loadFromString(extraString);
        } catch (InvalidConfigurationException e) {
            plugin.getLogger().log(Level.WARNING, "Cannot upgrade extra data: " + extraString, e);
        }
        for (String key : jsonConfiguration.getKeys(true)) {
            yamlConfiguration.set(key, jsonConfiguration.get(key));
        }
        return yamlConfiguration;
    }

    private @NotNull YamlConfiguration deserializeExtra(@NotNull String extraString, @NotNull AtomicBoolean needUpdate) {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        try {
            if (extraString.startsWith("{")) {
                yamlConfiguration = extraUpgrade(extraString);
                needUpdate.set(true);
            } else {
                yamlConfiguration.loadFromString(extraString);
            }
        } catch (InvalidConfigurationException e) {
            yamlConfiguration = extraUpgrade(extraString);
            needUpdate.set(true);
        }
        return yamlConfiguration;
    }

    private void exceptionHandler(@NotNull Exception ex, @Nullable Location shopLocation) {
        errors++;
        Logger logger = plugin.getLogger();
        logger.warning("##########FAILED TO LOAD SHOP##########");
        logger.warning("  >> Error Info:");
        String err = ex.getMessage();
        if (err == null) {
            err = "null";
        }
        logger.warning(err);
        logger.warning("  >> Error Trace");
        ex.printStackTrace();
        logger.warning("  >> Target Location Info");
        logger.warning("Location: " + ((shopLocation == null) ? "NULL" : shopLocation.toString()));
        logger.warning(
                "Block: " + ((shopLocation == null) ? "NULL" : shopLocation.getBlock().getType().name()));
        logger.warning("#######################################");
        if (errors > 10) {
            logger.severe(
                    "QuickShop detected too many errors when loading shops, you should backup your shop database and ask the developer for help");
        }
    }

    public synchronized void recoverFromFile(@NotNull String fileContent) {
        plugin.getLogger().info("Processing the shop data...");
        String[] shopsPlain = fileContent.split("\n");
        plugin.getLogger().info("Recovering shops...");
        Gson gson = JsonUtil.getGson();
        int total = shopsPlain.length;
        for (int i = 0; i < total; i++) {
            String shopStr = shopsPlain[i].trim();
            boolean success = false;
            try {
                ShopRawDatabaseInfo shopDatabaseInfoOrigin = gson.fromJson(shopStr, ShopRawDatabaseInfo.class);
                ShopDatabaseInfo data = new ShopDatabaseInfo(shopDatabaseInfoOrigin);
                Shop shop =
                        new ContainerShop(plugin,
                                data.getLocation(),
                                data.getPrice(),
                                data.getItem(),
                                data.getModerators(),
                                data.isUnlimited(),
                                data.getType(),
                                data.getExtra(),
                                data.getCurrency(),
                                data.isDisableDisplay(),
                                data.getTaxAccount());
                if (shopNullCheck(shop)) {
                    continue;
                }
                // Load to RAM
                Util.mainThreadRun(() -> {
                    plugin.getDatabaseHelper().createShop(shop, null, null);
                    plugin.getShopManager().loadShop(data.getWorld().getName(), shop);
                    shop.update();
                });

                success = true;
            } catch (JsonSyntaxException ignore) {
            }
            plugin.getLogger().info("Processed " + i + "/" + total + " - [" + success + "]");
        }
    }

//    @NotNull
//    public List<Shop> getShopsInDatabase() {
//        errors = 0;
//        List<Shop> shopsInDatabaseList = new ArrayList<>();
//        this.plugin.getLogger().info("Loading shops from the database...");
//        for (ShopRawDatabaseInfo shopRawDatabaseInfo : getOriginShopsInDatabase()) {
//            try {
//                ShopDatabaseInfo databaseInfo = new ShopDatabaseInfo(shopRawDatabaseInfo);
//                Shop shop = new ContainerShop(plugin,
//                        databaseInfo.getLocation(),
//                        databaseInfo.getPrice(),
//                        databaseInfo.getItem(),
//                        databaseInfo.getModerators(),
//                        databaseInfo.isUnlimited(),
//                        databaseInfo.getType(),
//                        databaseInfo.getExtra(),
//                        databaseInfo.getCurrency(),
//                        databaseInfo.isDisableDisplay(),
//                        databaseInfo.getTaxAccount());
//                shopsInDatabaseList.add(shop);
//            } catch (Exception e) {
//                exceptionHandler(e, null);
//            }
//        }
//        return shopsInDatabaseList;
//    }

    @NotNull
    public List<ShopRawDatabaseInfo> getOriginShopsInDatabase() {
        errors = 0;
        List<ShopRawDatabaseInfo> shopRawDatabaseInfoList = new ArrayList<>();
        try (JavaWarpedResultSet warpRS = plugin.getDatabaseHelper().selectAllShops(); ResultSet rs = warpRS.getResultSet()) {
            this.plugin.getLogger().info("Getting shops from the database...");
            while (rs.next()) {
                ShopRawDatabaseInfo origin = new ShopRawDatabaseInfo(rs);
                shopRawDatabaseInfoList.add(origin);
            }
        } catch (SQLException e) {
            exceptionHandler(e, null);
            return Collections.emptyList();
        }
        return shopRawDatabaseInfoList;
    }

    @Getter
    @Setter
    static public class ShopRawDatabaseInfo {
        private String item;

        private String moderators;

        private double price;

        private int type;

        private boolean unlimited;

        private String world;

        private int x;

        private int y;

        private int z;

        private String extra;

        private String currency;

        private boolean disableDisplay;

        private String taxAccount;

        ShopRawDatabaseInfo(ResultSet rs) throws SQLException {
            this.x = rs.getInt("x");
            this.y = rs.getInt("y");
            this.z = rs.getInt("z");
            this.world = rs.getString("world");
            this.item = rs.getString("itemConfig");
            this.moderators = rs.getString("owner");
            this.price = rs.getDouble("price");
            this.type = rs.getInt("type");
            this.unlimited = rs.getBoolean("unlimited");
            this.extra = rs.getString("extra");
            //handle old shops
            if (extra == null) {
                extra = "";
            }
            this.currency = rs.getString("currency");
            this.disableDisplay = rs.getInt("disableDisplay") == 0;
            this.taxAccount = rs.getString("taxAccount");
        }

        ShopRawDatabaseInfo(int x, int y, int z, String world, String itemConfig, String owner, double price, int type, boolean unlimited, String extra, String currency, boolean disableDisplay, String taxAccount) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.world = world;
            this.item = itemConfig;
            this.moderators = owner;
            this.price = price;
            this.type = type;
            this.unlimited = unlimited;
            this.extra = extra;
            this.currency = currency;
            this.taxAccount = taxAccount;
            this.disableDisplay = disableDisplay;
        }

        ShopRawDatabaseInfo() {

        }

        @Override
        public String toString() {
            return JsonUtil.getGson().toJson(this);
        }
    }

    @Getter
    @Setter
    public class ShopDatabaseInfo {
        private ItemStack item;

        private Location location;

        private JavaShopModerator moderators;

        private double price;

        private ShopType type;

        private boolean unlimited;

        private World world;

        private int x;

        private int y;

        private int z;

        private YamlConfiguration extra;

        private AtomicBoolean needUpdate = new AtomicBoolean(false);

        private String currency;

        private UUID taxAccount;

        private boolean disableDisplay;

        ShopDatabaseInfo(ShopRawDatabaseInfo origin) {
            try {
                this.x = origin.getX();
                this.y = origin.getY();
                this.z = origin.getZ();
                this.world = plugin.getServer().getWorld(origin.getWorld());
                this.location = new Location(world, x, y, z);
                this.price = origin.getPrice();
                this.unlimited = origin.isUnlimited();
                this.moderators = deserializeModerator(origin.getModerators(), needUpdate);
                this.type = ShopType.fromID(origin.getType());
                this.item = deserializeItem(origin.getItem());
                this.extra = deserializeExtra(origin.getExtra(), needUpdate);
                this.currency = origin.getCurrency();
                this.disableDisplay = origin.isDisableDisplay();
                this.taxAccount = origin.getTaxAccount() != null ? UUID.fromString(origin.getTaxAccount()) : null;
            } catch (Exception ex) {
                exceptionHandler(ex, this.location);
            }
        }

        private @Nullable ItemStack deserializeItem(@NotNull String itemConfig) {
            try {
                return Util.deserialize(itemConfig);
            } catch (InvalidConfigurationException e) {
                plugin.getLogger().log(Level.WARNING, "Failed load shop data, because target config can't deserialize the ItemStack", e);
                Util.debugLog("Failed to load data to the ItemStack: " + itemConfig);
                return null;
            }
        }

        private @Nullable JavaShopModerator deserializeModerator(@NotNull String moderatorJson, AtomicBoolean needUpdate) {
            JavaShopModerator shopModerator;
            if (Util.isUUID(moderatorJson)) {
                Util.debugLog("Updating old shop data... for " + moderatorJson);
                shopModerator = new JavaShopModerator(UUID.fromString(moderatorJson)); // New one
                needUpdate.set(true);
            } else {
                try {
                    shopModerator = JavaShopModerator.deserialize(moderatorJson);
                } catch (JsonSyntaxException ex) {
                    Util.debugLog("Updating old shop data... for " + moderatorJson);
                    //noinspection deprecation
                    moderatorJson = plugin.getServer().getOfflinePlayer(moderatorJson).getUniqueId().toString();
                    shopModerator = new JavaShopModerator(UUID.fromString(moderatorJson)); // New one
                    needUpdate.set(true);
                }
            }
            return shopModerator;
        }

    }

}
