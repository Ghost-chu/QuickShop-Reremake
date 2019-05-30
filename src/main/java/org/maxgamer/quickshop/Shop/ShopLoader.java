package org.maxgamer.quickshop.Shop;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

public class ShopLoader {
    private QuickShop plugin;
    private int errors;
    private ArrayList<Long> loadTimes = new ArrayList<>();
    private int totalLoaded = 0;
    private int loadAfterChunkLoaded = 0;
    private int loadAfterWorldLoaded = 0;

    public ShopLoader(QuickShop plugin) {
        this.plugin = plugin;
    }

    public void loadShops() {

        UUID totalLoadTimer = Util.setTimer();
        try {
            this.plugin.getLogger().info("Loading shops from database...");
            UUID fetchUUID = Util.setTimer();
            ResultSet rs = plugin.getDatabaseHelper().selectAllShops(this.plugin.getDatabase());
            this.plugin.getLogger().info("Used " + Util.endTimer(fetchUUID) + "ms to fetch all shops from database.");
            while (rs.next()) {
                UUID singleShopLoadTimer = Util.setTimer();
                Util.debugLog("New single timer was set: " + singleShopLoadTimer.toString());

                Util.debugLog("Reading [Database data] to [Origin shop data]");
                ShopDatabaseInfoOrigin origin = new ShopDatabaseInfoOrigin(rs);

                Util.debugLog("Reading [Origin shop data] to [Shop data]");
                ShopDatabaseInfo data = new ShopDatabaseInfo(origin);

                Util.debugLog("Creating shop object...");
                Shop shop = new ContainerShop(data.getLocation(), data.getPrice(), data.getItem(), data.getModerators(), data
                        .isUnlimited(), data
                        .getType());

                Util.debugLog("Checking shop info is exist...");
                if (shopNullCheck(shop)) {
                    Util.debugLog("Somethings went wrong, skipping loading...");
                    loadAfterWorldLoaded++;
                    singleShopLoaded(singleShopLoadTimer);
                    continue;
                }

                Util.debugLog("Load shop into RAM");
                plugin.getShopManager().loadShop(data.getWorld().getName(), shop);

                Util.debugLog("Checking shop's chunk is or not loaded");
                boolean chunkLoaded = Util.isLoaded(shop.getLocation());

                if (chunkLoaded) {
                    Util.debugLog("Check shop container type...");

                    if (!Util.canBeShop(shop.getLocation().getBlock())) {
                        Util.debugLog("Target block can't be shop, removing from database...");
                        shop.delete();
                        singleShopLoaded(singleShopLoadTimer);
                        continue;
                    }

                    Util.debugLog("Loading shop to the world");
                    plugin.getQueuedShopManager()
                            .add(new QueueShopObject(shop, new QueueAction[]{ QueueAction.LOAD, QueueAction.SETSIGNTEXT }));
                } else {
                    Util.debugLog("Shop will load after chunk loaded");
                    loadAfterChunkLoaded++;
                }
                singleShopLoaded(singleShopLoadTimer);
                continue;
            }
            long totalUsedTime = Util.endTimer(totalLoadTimer);
            long avgPerShop = mean(loadTimes.toArray(new Long[0]));
            this.plugin.getLogger()
                    .info("Successfully loaded " + totalLoaded + " shops! (Used " + totalUsedTime + "ms, Avg " + avgPerShop + "ms per shop)");
            this.plugin.getLogger()
                    .info(this.loadAfterChunkLoaded + " shops will load after chunk loaded, " + this.loadAfterWorldLoaded + " shops will load after world loaded.");
            //     while (rs.next()) {
            //         int x = 0;
            //         int y = 0;
            //         int z = 0;
            //         String worldName = null;
            //         ItemStack item = null;
            //         String moderators = null;
            //
            //         try {
            //
            //             x = rs.getInt("x");
            //             y = rs.getInt("y");
            //             z = rs.getInt("z");
            //             worldName = rs.getString("world");
            //             World world = Bukkit.getWorld(worldName);
            //             if (world == null) {
            //                 //Maybe world not loaded yet?, skipping
            //                 skipedShops++;
            //                 Util.debugLog("Found a shop can't match shop's world: " + worldName + ", it got removed or just not loaded? Ignore it...");
            //                 continue;
            //             }
            //
            //             item = Util.deserialize(rs.getString("itemConfig"));
            //             moderators = rs.getString("owner"); //Get origin data
            //             ShopModerator shopModerator = null;
            //             try {
            //                 UUID.fromString(moderators);
            //                 if (!isBackuped) {
            //                     isBackuped = Util.backupDatabase();
            //                 }
            //                 Util.debugLog("Updating old shop data...");
            //                 shopModerator = new ShopModerator(UUID.fromString(moderators)); //New one
            //                 moderators = ShopModerator.serialize(shopModerator); //Serialize
            //             } catch (IllegalArgumentException ex) {
            //                 //This expcetion is normal, cause i need check that is or not a UUID.
            //                 shopModerator = ShopModerator.deserialize(moderators);
            //             }
            //             double price = rs.getDouble("price");
            //             Location loc = new Location(world, x, y, z);
            //
            //             /* Skip invalid shops, if we know of any */
            //             if (!Util.canBeShop(loc.getBlock(), null)) {
            //                 getLogger().info("Shop is not an InventoryHolder in " + rs.getString("world") + " at: " + x
            //                         + ", " + y + ", " + z + ".  Deleting.");
            //                 if (!isBackuped) {
            //                     isBackuped = Util.backupDatabase();
            //                 }
            //                 if (isBackuped) {
            //                     DatabaseHelper.removeShop(database, x, y, z, worldName);
            //                 } else {
            //                     getLogger().warning("Skipped shop deleteion: Failed to backup database,");
            //                 }
            //                 continue;
            //             }
            //             int type = rs.getInt("type");
            //             Shop shop = new ContainerShop(loc, price, item, shopModerator);
            //             shop.setUnlimited(rs.getBoolean("unlimited"));
            //             shop.setShopType(ShopType.fromID(type));
            //             shopManager.loadShop(rs.getString("world"), shop);
            //             //if (loc.getWorld() != null && loc.getChunk().isLoaded()) {
            //             if (Util.isLoaded(loc)) {
            //                 this.getQueuedShopManager().add(new QueueShopObject(shop,new QueueAction[]{QueueAction.LOAD,QueueAction.SETSIGNTEXT}));
            //             } else {
            //                 loadAfterChunkLoaded++;
            //                 continue;
            //             }
            //             count++;
            //         } catch (Exception e) {
            //             errors++;
            //             getLogger().warning("Error loading a shop! Coords: Location[" + worldName + " (" + x + ", " + y
            //                     + ", " + z + ")] Item: " + item.getType().name() + "...");
            //             getLogger().warning("===========Error Reporting Start===========");
            //             getLogger().warning("#Java throw >>");
            //             getLogger().warning("StackTrace:");
            //             e.printStackTrace();
            //             getLogger().warning("#Shop data >>");
            //             getLogger().warning("Location: " + worldName + ";(X:" + x + ", Y:" + y + ", Z:" + z + ")");
            //             getLogger().warning(
            //                     "Item: " + item.getType().name() + " MetaData: " + item.getItemMeta().spigot().toString());
            //             getLogger().warning("Moderators: " + moderators);
            //             try {
            //                 getLogger().warning(
            //                         "BukkitWorld: " + Bukkit.getWorld(worldName).getName() + " [" + worldName + "]");
            //             } catch (Exception e2) {
            //                 getLogger().warning("BukkitWorld: WARNING:World not exist! [" + worldName + "]");
            //             }
            //             try {
            //                 getLogger().warning(
            //                         "Target Block: " + Bukkit.getWorld(worldName).getBlockAt(x, y, z).getType().name());
            //             } catch (Exception e2) {
            //                 getLogger().warning("Target Block: Can't get block!");
            //             }
            //             getLogger().warning("#Database info >>");
            //
            //             getLogger().warning("Connected:" + !getDB().getConnection().isClosed());
            //             getLogger().warning("Read Only:" + getDB().getConnection().isReadOnly());
            //
            //             if (getDB().getConnection().getClientInfo() != null) {
            //                 getLogger().warning("Client Info: " + getDB().getConnection().getClientInfo().toString());
            //             } else {
            //                 getLogger().warning("Client Info: null");
            //             }
            //             getLogger().warning("Read Only:" + getDB().getConnection().isReadOnly());
            //             getLogger().warning("#Tips >>");
            //             getLogger().warning("Please report this issues to author, And you database will auto backup!");
            //
            //             getLogger().warning("===========Error Reporting End===========");
            //
            //             if (errors < 3) {
            //                 getLogger().info("Create backup for database..");
            //                 if (!isBackuped) {
            //                     //Backup it
            //                     isBackuped = Util.backupDatabase();
            //                 }
            //                 getLogger().info("Removeing shop from database...");
            //                 if (isBackuped) {
            //                     DatabaseHelper.removeShop(database, x, y, z, worldName);
            //                 } else {
            //                     getLogger().warning("Skipped shop deleteion: Failed to backup database,");
            //                 }
            //                 getLogger().info("Trying continue loading...");
            //             } else {
            //                 getLogger().severe(
            //                         "Multiple errors in shops - Something seems to be wrong with your shops database! Please check it out immediately!");
            //                 getLogger().info("Backuping database...");
            //                 if (!isBackuped) {
            //                     //Backup
            //                     isBackuped = Util.backupDatabase();
            //                 }
            //                 getLogger().info("Removeing shop from database...");
            //                 if (isBackuped) {
            //                     DatabaseHelper.removeShop(database, x, y, z, worldName);
            //                 } else {
            //                     getLogger().warning("Skipped shop deleteion: Failed to backup database.");
            //                 }
            //                 e.printStackTrace();
            //             } /*Error reporting, this is too long so i folding it.*/
            //         }
            //     }
            // } catch (SQLException e) {
            //     e.printStackTrace();
            //     getLogger().severe("Could not load shops Because SQLException.");
            // }
            // getLogger().info("Loaded " + count + " shops (" + Util.endTimer(totalTimer) + "ms)");
            // getLogger().info("Other " + skipedShops + " shops will load when worlds loaded.");
            // getLogger().info("Have " + loadAfterChunkLoaded + " shops will load when chunks loaded.");
        } catch (Exception e) {
            exceptionHandler(e, null);
        }
    }

    private Long mean(Long[] m) {
        long sum = 0;
        for (Long aM : m) {
            sum += aM;
        }
        return sum / m.length;
    }

    private void singleShopLoaded(@NotNull UUID singleShopLoadTimer) {
        totalLoaded++;
        Util.debugLog("New single timer was ended: " + singleShopLoadTimer.toString());
        long singleShopLoadTime = Util.endTimer(singleShopLoadTimer);
        loadTimes.add(singleShopLoadTime);
        Util.debugLog("Loaded shop used time " + singleShopLoadTime + "ms");
    }

    private boolean shopNullCheck(Shop shop) {
        if (shop == null) {
            Util.debugLog("Shop Object is null");
            return true;
        }
        if (shop.getItem() == null) {
            Util.debugLog("Shop ItemStack is null");
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
        // if (shop.getLocation().getChunk() == null) {
        //     Util.debugLog("Shop Chunk is null");
        //     return true;
        // }
        // if (shop.getLocation().getBlock() == null) {
        //     Util.debugLog("Shop Block is null");
        //     return true;
        // }
        return false;
    }

    private void exceptionHandler(Exception ex, Location shopLocation) {
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
            logger.warning("Connected: " + String.valueOf(plugin.getDatabase().getConnection().isClosed()));
        } catch (SQLException | NullPointerException e) {
            logger.warning("Connected: " + "FALSE - Failed reload status.");
        }

        try {
            logger.warning("Readonly: " + String.valueOf(plugin.getDatabase().getConnection().isReadOnly()));
        } catch (SQLException | NullPointerException e) {
            logger.warning("Readonly: " + "FALSE - Failed reload status.");
        }

        try {
            logger.warning("ClientInfo: " + String.valueOf(plugin.getDatabase().getConnection().getClientInfo().toString()));
        } catch (SQLException | NullPointerException e) {
            logger.warning("ClientInfo: " + "FALSE - Failed reload status.");
        }

        try {
            logger.warning("Metadata: " + String.valueOf(plugin.getDatabase().getConnection().getMetaData().toString()));
        } catch (SQLException | NullPointerException e) {
            logger.warning("Metadata: " + "FALSE - Failed reload status.");
        }

        logger.warning("#######################################");
        if (errors > 10)
            logger.severe("QuickShop detect too many errors when loading shops, you should backup your shop database and ask developer to get help");
    }

    @Getter
    @Setter
    class ShopDatabaseInfoOrigin {
        private int x;
        private int y;
        private int z;
        private String world;
        private String item;
        private String moderators;
        private double price;
        private int type;
        private boolean unlimited;

        public ShopDatabaseInfoOrigin(ResultSet rs) {
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

    @Getter
    @Setter
    class ShopDatabaseInfo {
        private int x;
        private int y;
        private int z;
        private double price;
        private boolean unlimited;
        private ShopType type;
        private World world;
        private ItemStack item;
        private ShopModerator moderators;
        private Location location;

        public ShopDatabaseInfo(ShopDatabaseInfoOrigin origin) {
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

        private ItemStack deserializeItem(String itemConfig) {
            try {
                return Util.deserialize(itemConfig);
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Failed load shop data, cause target config can't deserialize to ItemStack.");
                Util.debugLog("Failed load data to ItemStack: " + itemConfig);
                return null;
            }
        }

        private ShopModerator deserializeModerator(String moderatorJson) {
            // try {
            //     UUID.fromString(moderators);
            //     if (!isBackuped) {
            //         isBackuped = Util.backupDatabase();
            //     }
            //
            //
            //     moderators = ShopModerator.serialize(shopModerator); //Serialize
            // } catch (IllegalArgumentException ex) {
            //     //This expcetion is normal, cause i need check that is or not a UUID.
            //     shopModerator = ShopModerator.deserialize(moderators);
            // }
            //
            ShopModerator shopModerator;
            if (Util.isUUID(moderatorJson)) {
                Util.debugLog("Updating old shop data...");
                shopModerator = new ShopModerator(UUID.fromString(moderatorJson)); //New one
            } else {
                shopModerator = ShopModerator.deserialize(moderatorJson);
            }
            return shopModerator;
        }
    }
}
