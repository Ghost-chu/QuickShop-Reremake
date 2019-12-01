package org.maxgamer.quickshop.Util;

import com.google.common.io.Files;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Database.MySQLCore;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.DisplayItem;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Watcher.InventoryEditContainer;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author MACHENIKE
 */
@SuppressWarnings("WeakerAccess")
public class Util {
    private static EnumSet<Material> blacklist = EnumSet.noneOf(Material.class);
    @Getter
    private static List<String> debugLogs = new ArrayList<>();
    private static boolean devMode;
    private static QuickShop plugin;
    private static EnumMap<Material, Entry<Double, Double>> restrictedPrices = new EnumMap<>(Material.class);
    private static Object serverInstance;
    private static EnumSet<Material> shoppables = EnumSet.noneOf(Material.class);
    private static Field tpsField;
    private static List<String> worldBlacklist = new ArrayList<>();

    /**
     * Convert strArray to String.
     * E.g "Foo, Bar"
     *
     * @param strArray Target array
     * @return str
     */
    public static String array2String(@NotNull String[] strArray) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strArray.length; i++) {
            builder.append(strArray[i]);
            if (i + 1 != strArray.length) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    /**
     * Backup shops.db
     *
     * @return The result for backup
     */
    public static boolean backupDatabase() {
        if (plugin.getDatabase().getCore() instanceof MySQLCore) {
            return true; //Backup and logs by MySQL
        }
        File dataFolder = plugin.getDataFolder();
        File sqlfile = new File(dataFolder, "shops.db");
        if (!sqlfile.exists()) {
            plugin.getLogger().warning("Failed to backup! (File not found)");
            return false;
        }
        String uuid = UUID.randomUUID().toString().replaceAll("_", "");
        File bksqlfile = new File(dataFolder, "/shops_backup_" + uuid + ".db");
        try {
            Files.copy(sqlfile, bksqlfile);
        } catch (Exception e1) {
            e1.printStackTrace();
            plugin.getLogger().warning("Failed to backup the database.");
            return false;
        }
        return true;
    }

    /**
     * Returns true if the given block could be used to make a shop out of.
     *
     * @param b The block to check, Possibly a chest, dispenser, etc.
     * @return True if it can be made into a shop, otherwise false.
     */
    public static boolean canBeShop(@NotNull Block b) {
        BlockState bs = b.getState();

        if (!isShoppables(b.getType())) {
            return false;
        }
        //noinspection RedundantIfStatement
        if (isBlacklistWorld(b.getWorld())) {
            return false;
        }
        if (b.getType() == Material.ENDER_CHEST) {
            return plugin.getOpenInvPlugin() != null;
        }
        return bs instanceof InventoryHolder;
    }

    /**
     * Counts the number of items in the given inventory where
     * Util.matches(inventory item, item) is true.
     *
     * @param inv  The inventory to search
     * @param item The ItemStack to search for
     * @return The number of items that match in this inventory.
     */
    public static int countItems(@Nullable Inventory inv, @NotNull ItemStack item) {
        if (inv == null) {
            return 0;
        }
        int items = 0;
        for (ItemStack iStack : inv.getStorageContents()) {
            //noinspection ConstantConditions
            if (iStack == null) {
                continue;
            }
            if (plugin.getItemMatcher().matches(item, iStack)) {
                items += iStack.getAmount();
            }
        }
        return items;
    }

    /**
     * Returns the number of items that can be given to the inventory safely.
     *
     * @param inv  The inventory to count
     * @param item The item prototype. Material, durabiltiy and enchants must
     *             match for 'stackability' to occur.
     * @return The number of items that can be given to the inventory safely.
     */
    public static int countSpace(@Nullable Inventory inv, @NotNull ItemStack item) {
        if (inv == null) {
            return 0;
        }
        int space = 0;

        ItemStack[] contents = inv.getStorageContents();
        for (ItemStack iStack : contents) {
            if (iStack == null || iStack.getType() == Material.AIR) {
                space += item.getMaxStackSize();
            } else if (plugin.getItemMatcher().matches(item, iStack)) {
                space += item.getMaxStackSize() - iStack.getAmount();
            }
        }
        return space;
    }

    static short tookLongTimeCostTimes;

    /**
     * Print debug log when plugin running on dev mode.
     *
     * @param logs logs
     */
    public static void debugLog(@NotNull String... logs) {
        if (!devMode) {
            if (QuickShop.instance.getConfig().getBoolean("disable-debuglogger", false)) {
                return;
            }
        }
        long startTime = System.currentTimeMillis();
        String className = Thread.currentThread().getStackTrace()[2].getClassName();
        try {
            Class c = Class.forName(className);
            className = c.getSimpleName();
            if (!c.getSimpleName().isEmpty()) {
                className = c.getSimpleName();
            }
        } catch (ClassNotFoundException e) {
            //Ignore
        }
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        int codeLine = Thread.currentThread().getStackTrace()[2].getLineNumber();

        for (String log : logs) {
            String text = "[DEBUG] [" + className + "]" + " [" + methodName + "] (" + codeLine + ") " + log;
            debugLogs.add(text);
            if (debugLogs.size() > 500000) /* Keep debugLogs max can have 500k lines. */ {
                debugLogs.remove(0);
            }
            if (devMode) {
                QuickShop.instance.getLogger().info(text);
            }
        }
        long debugLogCost = System.currentTimeMillis() - startTime;
        if (!devMode) {
            if (debugLogCost > 2) {
                tookLongTimeCostTimes++;
                if (tookLongTimeCostTimes > 30) {
                    QuickShop.instance.getConfig().set("disable-debuglogger", true);
                    QuickShop.instance.saveConfig();
                    QuickShop.instance.getLogger().warning("Detected the debug logger tooked time keep too lang, QuickShop already auto-disable debug logger, your server performance should back to normal. But you must re-enable it if you want to report any bugs.");
                }
            }
        }
    }

    /**
     * Covert YAML string to ItemStack.
     *
     * @param config serialized ItemStack
     * @return ItemStack iStack
     * @throws InvalidConfigurationException when failed deserialize config
     */
    public static ItemStack deserialize(@NotNull String config) throws InvalidConfigurationException {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.loadFromString(config);
        cfg.getString("item");
        return cfg.getItemStack("item");
    }

    /**
     * Check two location is or not equals for the BlockPosition on 2D
     *
     * @param b1 block 1
     * @param b2 block 2
     * @return Equals or not.
     */
    private static boolean equalsBlockStateLocation(@NotNull Location b1, @NotNull Location b2) {
        return (b1.getBlockX() == b2.getBlockX()) && (b1.getBlockY() == b2.getBlockY()) && (b1.getBlockZ() == b2
                .getBlockZ()) && (b1.getWorld().getName().equals(b2.getWorld().getName()));
    }

    /**
     * First uppercase for every words the first char for a text.
     *
     * @param string text
     * @return Processed text.
     */
    public static String firstUppercase(@NotNull String string) {
        if (string.length() > 1) {
            return Character.toUpperCase(string.charAt(0)) + string.substring(1).toLowerCase();
        } else {
            return string.toUpperCase();
        }
    }

    /**
     * Formats the given number according to how vault would like it. E.g. $50 or 5
     * dollars.
     *
     * @param n price
     * @return The formatted string.
     */
    public static String format(double n) {
        if (plugin.getConfig().getBoolean("shop.disable-vault-format")) {
            return plugin.getConfig().getString("shop.alternate-currency-symbol") + n;
        }
        try {
            String formated = plugin.getEconomy().format(n);
            if (formated == null || formated.isEmpty()) {
                Util.debugLog("Use alternate-currency-symbol to formatting, Cause economy plugin returned null");
                return plugin.getConfig().getString("shop.alternate-currency-symbol") + n;
            } else {
                return formated;
            }
        } catch (NumberFormatException | NullPointerException e) {
            Util.debugLog("format", e.getMessage());
            Util.debugLog("format", "Use alternate-currency-symbol to formatting, Cause NumberFormatException");
            return plugin.getConfig().getString("shop.alternate-currency-symbol") + n;
        }
    }

    /**
     * Fetches the block which the given sign is attached to
     *
     * @param b The block which is attached
     * @return The block the sign is attached to
     */
    public static Block getAttached(@NotNull Block b) {
        if (b.getBlockData() instanceof Directional) {
            Directional directional = (Directional) b.getBlockData();
            return b.getRelative(directional.getFacing().getOppositeFace());

        } else {
            return null;
        }
    }
    /**
     * Return the Class name.
     *
     * @param c The class to get name
     * @return The class prefix
     */
    public static String getClassPrefix(@NotNull Class c) {
        String callClassName = Thread.currentThread().getStackTrace()[2].getClassName();
        String customClassName = c.getSimpleName();
        return "[" + callClassName + "-" + customClassName + "] ";
    }

    public static String getItemStackName(@NotNull ItemStack itemStack) {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            return itemStack.getItemMeta().getDisplayName();
        }
        return MsgUtil.getItemi18n(itemStack.getType().name());
    }

    /**
     * Get ItemStack's local name, return null if failed to get.
     *
     * @param itemStack Target ItemStack
     * @return LocalName or NULL
     */
    public static String getLocalizedName(@NotNull ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return null;
        }
        if (!itemMeta.hasLocalizedName()) {
            return null;
        }
        return itemMeta.getLocalizedName();
    }

    public static Class<?> getNMSClass(@Nullable String className) {
        if (className == null) {
            className = "MinecraftServer";
        }
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String version = name.substring(name.lastIndexOf('.') + 1);
        try {
            return Class.forName("net.minecraft.server." + version + "." + className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param iStack itemstack
     * @return potion data, readable
     */
    public static String getPotiondata(@NotNull ItemStack iStack) {
        if ((iStack.getType() != Material.POTION) && (iStack.getType() != Material.LINGERING_POTION) && (iStack
                .getType() != Material.SPLASH_POTION)) {
            return null;
        }
        List<String> pEffects = new ArrayList<>();
        PotionMeta pMeta = (PotionMeta) iStack.getItemMeta();
        //if (pMeta.getBasePotionData().getType() != null) {
        if (!(pMeta.getBasePotionData().isUpgraded())) {
            pEffects.add(ChatColor.BLUE + MsgUtil.getPotioni18n(pMeta.getBasePotionData().getType().getEffectType()));
        } else {
            pEffects.add(ChatColor.BLUE + MsgUtil.getPotioni18n(pMeta.getBasePotionData().getType().getEffectType()) + " II");
        }

        //}
        if (pMeta.hasCustomEffects()) {
            List<PotionEffect> cEffects = pMeta.getCustomEffects();
            for (PotionEffect potionEffect : cEffects) {
                pEffects.add(MsgUtil.getPotioni18n(potionEffect.getType()) + " " + RomanNumber
                        .toRoman(potionEffect.getAmplifier()));
            }
        }
        if (!pEffects.isEmpty()) {
            StringBuilder result = new StringBuilder();
            for (String effectString : pEffects) {
                result.append(effectString);
                result.append("\n");
            }
            return result.toString();
        } else {
            return null;
        }
    }

    /**
     * Return an entry with min and max prices, but null if there isn't a price restriction
     *
     * @param material mat
     * @return min, max
     */
    public static Entry<Double, Double> getPriceRestriction(@NotNull Material material) {
        return restrictedPrices.get(material);
    }

    /**
     * Returns the chest attached to the given chest. The given block must be a
     * chest.
     *
     * @param b The chest to check.
     * @return the block which is also a chest and connected to b.
     */
    public static Block getSecondHalf(@NotNull Block b) {
        if (!isDoubleChest(b)) {
            Util.debugLog("Target block not a DoubleChest, ignored.");
            return null;
        }
        Chest oneSideOfChest = (Chest) b.getState();
        InventoryHolder chestHolder = oneSideOfChest.getInventory().getHolder();
        if (chestHolder instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) chestHolder;
            InventoryHolder left = doubleChest.getLeftSide();
            InventoryHolder right = doubleChest.getRightSide();
            Chest leftC = (Chest) left;
            Chest rightC = (Chest) right;
            if (equalsBlockStateLocation(oneSideOfChest.getLocation(), rightC.getLocation())) {
                Util.debugLog("The left side of the chest was found.");
                return leftC.getBlock();
            }
            if (equalsBlockStateLocation(oneSideOfChest.getLocation(), leftC.getLocation())) {
                Util.debugLog("The right side of the chest was found.");
                return rightC.getBlock();
            }
            Util.debugLog("Bug detected, DoubleChest holder can't find the any one side chest.");
            return null;
        } else {
            Util.debugLog("ChestHolder not a DoubleChest holder.");
            return null;
        }
    }

    /**
     * Returns the chest attached to the given chest. The given block must be a
     * chest.
     *
     * @param b he chest to check.
     * @return the block which is also a chest and connected to b.
     * @deprecated
     */
    @Deprecated
    public static Block getSecondHalf_old(@NotNull Block b) {
        // if (b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST)
        //         //     return null;
        if (!isDoubleChest(b)) {
            return null;
        }
        Block[] blocks = new Block[4];
        blocks[0] = b.getRelative(1, 0, 0);
        blocks[1] = b.getRelative(-1, 0, 0);
        blocks[2] = b.getRelative(0, 0, 1);
        blocks[3] = b.getRelative(0, 0, -1);
        for (Block c : blocks) {
            if (c.getType() == b.getType()) {
                return c;
            }
        }
        return null;
    }

    /**
     * Get how many shop in the target world.
     *
     * @param worldName Target world.
     * @return The shops.
     */
    public static int getShopsInWorld(@NotNull String worldName) {
        int cost = 0;
        Iterator<Shop> iterator = plugin.getShopManager().getShopIterator();
        while (iterator.hasNext()) {
            Shop shop = iterator.next();
            if (shop.getLocation().getWorld().getName().equals(worldName)) {
                cost++;
            }
        }
        return cost;
    }

    /**
     * Gets the percentage (Without trailing %) damage on a tool.
     *
     * @param item The ItemStack of tools to check
     * @return The percentage 'health' the tool has. (Opposite of total damage)
     */
    public static String getToolPercentage(@NotNull ItemStack item) {
        if (!(item.getItemMeta() instanceof Damageable)) {
            Util.debugLog(item.getType().name() + " not Damageable.");
            return "Error: NaN";
        }
        double dura = ((Damageable) item.getItemMeta()).getDamage();
        double max = item.getType().getMaxDurability();
        DecimalFormat formatter = new DecimalFormat("0");
        return formatter.format((1 - dura / max) * 100.0);
    }

    /**
     * Use yaw to calc the BlockFace
     *
     * @param yaw Yaw (Player.getLocation().getYaw())
     * @return BlockFace blockFace
     * @deprecated Use Bukkit util not this one.
     */
    @Deprecated
    public static BlockFace getYawFace(float yaw) {
        if (yaw > 315 && yaw <= 45) {
            return BlockFace.NORTH;
        } else if (yaw > 45 && yaw <= 135) {
            return BlockFace.EAST;
        } else if (yaw > 135 && yaw <= 225) {
            return BlockFace.SOUTH;
        } else {
            return BlockFace.WEST;
        }
    }

    /**
     * Initialize the Util tools.
     */
    public static void initialize() {
        blacklist.clear();
        shoppables.clear();
        restrictedPrices.clear();
        worldBlacklist.clear();
        plugin = QuickShop.instance;
        devMode = plugin.getConfig().getBoolean("dev-mode");

        for (String s : plugin.getConfig().getStringList("shop-blocks")) {
            Material mat = Material.matchMaterial(s.toUpperCase());
            if (mat == null) {
                mat = Material.matchMaterial(s);
            }
            if (mat == null) {
                plugin.getLogger().warning("Invalid shop-block: " + s);
            } else {
                shoppables.add(mat);
            }
        }
        List<String> configBlacklist = plugin.getConfig().getStringList("blacklist");
        for (String s : configBlacklist) {
            Material mat = Material.getMaterial(s.toUpperCase());
            if (mat == null) {
                mat = Material.matchMaterial(s);
            }
            if (mat == null) {
                plugin.getLogger().warning(s + " is not a valid material.  Check your spelling or ID");
                continue;
            }
            blacklist.add(mat);
        }

        for (String s : plugin.getConfig().getStringList("price-restriction")) {
            String[] sp = s.split(";");
            if (sp.length == 3) {
                try {
                    Material mat = Material.matchMaterial(sp[0]);
                    if (mat == null) {
                        plugin.getLogger()
                                .warning("Material " + sp[0] + " in config.yml can't match with a valid Materials, check your config.yml!");
                        continue;
                    }
                    restrictedPrices.put(mat, new SimpleEntry<>(Double.valueOf(sp[1]), Double.valueOf(sp[2])));
                } catch (Exception e) {
                    plugin.getLogger().warning("Invalid price restricted material: " + s);
                }
            }
        }
        worldBlacklist = plugin.getConfig().getStringList("shop.blacklist-world");

    }

    /**
     * Read the InputStream to the byte array.
     *
     * @param filePath Target file
     * @return Byte array
     */
    public static byte[] inputStream2ByteArray(@NotNull String filePath) {
        try {
            InputStream in = new FileInputStream(filePath);
            byte[] data = toByteArray(in);
            in.close();
            return data;
        } catch (IOException e) {
            return null;
        }

    }

    /**
     * Read the InputStream to the byte array.
     *
     * @param inputStream Target stream
     * @return Byte array
     */
    public static byte[] inputStream2ByteArray(@NotNull InputStream inputStream) {
        try {
            byte[] data = toByteArray(inputStream);
            inputStream.close();
            return data;
        } catch (IOException e) {
            return null;
        }

    }

    /**
     * Call this to check items in inventory and remove it.
     *
     * @param inv inv
     */
    public static void inventoryCheck(@Nullable Inventory inv) {
        if (inv == null) {
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < inv.getSize(); i++) {
                        ItemStack itemStack = inv.getItem(i);
                        if (itemStack == null) {
                            continue;
                        }
                        if (DisplayItem.checkIsGuardItemStack(itemStack)) {
                            // Found Item and remove it.
                            Location location = inv.getLocation();
                            if (location == null) {
                                return; //Virtual GUI
                            }
                            plugin.getSyncTaskWatcher().getInventoryEditQueue()
                                    .offer(new InventoryEditContainer(inv, i, new ItemStack(Material.AIR, 0)));
                            Util.debugLog("Found a displayitem in an inventory, Scheduling to removeal...");
                            MsgUtil.sendGlobalAlert("[InventoryCheck] Found displayItem in inventory at " + location.toString() + ", Item is " + itemStack
                                    .getType().name());
                        }
                    }
                } catch (Throwable t) {
                    //Ignore
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public static boolean isAir(@NotNull Material mat) {
        if (mat == Material.AIR) {
            return true;
        }
        /* For 1.13 new AIR */
        try {
            if (mat == Material.CAVE_AIR) {
                return true;
            }
            if (mat == Material.VOID_AIR) {
                return true;
            }
        } catch (Throwable t) {
            //ignore
        }
        return false;
    }

    public static boolean isBlacklistWorld(@NotNull World world) {
        return worldBlacklist.contains(world.getName());
    }

    /**
     * @param stack The ItemStack to check if it is blacklisted
     * @return true if the ItemStack is black listed. False if not.
     */
    public static boolean isBlacklisted(@NotNull ItemStack stack) {
        if (blacklist.contains(stack.getType())){
            return true;
        }
        if(!stack.hasItemMeta()){
            return false;
        }
        if(!stack.getItemMeta().hasLore()){
            return  false;
        }
        for (String lore:stack.getItemMeta().getLore()) {
            if(plugin.getConfig().getStringList("shop.blacklist-lores").contains(lore)){
                return false;
            }
        }
        return true;
    }

    /**
     * Get this class available or not
     *
     * @param qualifiedName class qualifiedName
     * @return boolean Available
     */
    public static boolean isClassAvailable(@NotNull String qualifiedName) {
        try {
            Class.forName(qualifiedName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isDisplayAllowBlock(@NotNull Material mat) {
        if (isAir(mat)) {
            return true;
        }
        return isWallSign(mat);
    }

    public static boolean isDoubleChest(@Nullable Block b) {
        if (b == null) {
            Util.debugLog("The block is NULL.");
            return false;
        }
        if (!(b.getState() instanceof Container)) {
            Util.debugLog("Target block not a Container.");
            return false;
        }
        Container container = (Container) b.getState();
        return (container.getInventory() instanceof DoubleChestInventory);
    }

    /**
     * Returns true if the given location is loaded or not.
     *
     * @param loc The location
     * @return true if the given location is loaded or not.
     */
    public static boolean isLoaded(@NotNull Location loc) {
        // plugin.getLogger().log(Level.WARNING, "Checking isLoaded(Location loc)");
        if (loc.getWorld() == null) {
            // plugin.getLogger().log(Level.WARNING, "Is not loaded. (No world)");
            return false;
        }
        // Calculate the chunks coordinates. These are 1,2,3 for each chunk, NOT
        // location rounded to the nearest 16.
        int x = (int) Math.floor((loc.getBlockX()) / 16.0);
        int z = (int) Math.floor((loc.getBlockZ()) / 16.0);
        return (loc.getWorld().isChunkLoaded(x, z));
    }

    /**
     * Checks whether someone else's shop is within reach of a hopper being placed by a player.
     *
     * @param b The block being placed.
     * @param p The player performing the action.
     * @return true if a nearby shop was found, false otherwise.
     */
    public static boolean isOtherShopWithinHopperReach(@NotNull Block b, @NotNull Player p) {
        // Check 5 relative positions that can be affected by a hopper: behind, in front of, to the right,
        // to the left and underneath.
        Block[] blocks = new Block[5];
        blocks[0] = b.getRelative(0, 0, -1);
        blocks[1] = b.getRelative(0, 0, 1);
        blocks[2] = b.getRelative(1, 0, 0);
        blocks[3] = b.getRelative(-1, 0, 0);
        blocks[4] = b.getRelative(0, 1, 0);
        for (Block c : blocks) {
            Shop firstShop = plugin.getShopManager().getShop(c.getLocation());
            // If firstShop is null but is container, it can be used to drain contents from a shop created
            // on secondHalf.
            Block secondHalf = getSecondHalf(c);
            Shop secondShop = secondHalf == null ? null : plugin.getShopManager().getShop(secondHalf.getLocation());
            if (firstShop != null && !p.getUniqueId().equals(firstShop.getOwner())
                    || secondShop != null && !p.getUniqueId().equals(secondShop.getOwner())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check a material is possible become a shop
     *
     * @param material Mat
     * @return Can or not
     */
    public static boolean isShoppables(@NotNull Material material) {
        return shoppables.contains(material);
    }

    /**
     * @param mat The material to check
     * @return Returns true if the item is a tool (Has durability) or false if
     * it doesn't.
     */
    public static boolean isTool(@NotNull Material mat) {
        return !(mat.getMaxDurability() == 0);
    }

    /**
     * Check a string is or not a UUID string
     *
     * @param string Target string
     * @return is UUID
     */
    public static boolean isUUID(@NotNull String string) {
        if (string.length() != 36 && string.length() != 32) {
            return false;
        }
        Util.debugLog("Run an extra uuid check for " + string + ". Length: " + string.length());
        try {
            //noinspection ResultOfMethodCallIgnored
            UUID.fromString(string);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check a material is or not a WALL_SIGN
     *
     * @param material mat
     * @return is or not a wall_sign
     */
    public static boolean isWallSign(@Nullable Material material) {
        if (material == null) {
            return false;
        }
        try {
            return Tag.WALL_SIGNS.isTagged(material);
        } catch (NoSuchFieldError e) {
            return "WALL_SIGN".equals(material.name());
        }
    }

    /**
     * Convert strList to String.
     * E.g "Foo, Bar"
     *
     * @param strList Target list
     * @return str
     */
    public static String list2String(@NotNull List<String> strList) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strList.size(); i++) {
            builder.append(strList.get(i));
            if (i + 1 != strList.size()) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    /**
     * Returns loc with modified pitch/yaw angles so it faces lookat
     *
     * @param loc    The location a players head is
     * @param lookat The location they should be looking
     * @return The location the player should be facing to have their crosshairs on
     * the location lookAt Kudos to bergerkiller for most of this function
     */
    public static Location lookAt(Location loc, Location lookat) {
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
            loc.setYaw(loc.getYaw() - (float) Math.atan(dz / dx));
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

    /**
     * Match the both map1 and map2
     *
     * @param map1 Map1
     * @param map2 Map2
     * @return Map1 match Map2 and Map2 match Map1
     */
    public static boolean mapDuoMatches(@NotNull Map map1, @NotNull Map map2) {
        boolean result = mapMatches(map1, map2);
        if (!result) {
            return false;
        }
        return mapMatches(map2, map1);
    }

    /**
     * Match the map1 and map2
     *
     * @param map1 Map1
     * @param map2 Map2
     * @return Map1 match Map2
     */
    public static boolean mapMatches(@NotNull Map map1, @NotNull Map map2) {
        for (Object obj : map1.keySet()) {
            if (!map2.containsKey(obj)) {
                return false;
            }
            if (map1.get(obj) != map2.get(obj)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Match the list1 and list2
     *
     * @param list1 requireList
     * @param list2 givenList
     * @return Map1 match Map2
     */
    public static boolean listMatches(@NotNull List<?> list1, @NotNull List<?> list2) {
        for (Object obj : list1) {
            if (!list2.contains(obj)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Parse colors for the YamlConfiguration.
     *
     * @param config yaml config
     */
    public static void parseColours(@NotNull YamlConfiguration config) {
        Set<String> keys = config.getKeys(true);
        for (String key : keys) {
            String filtered = config.getString(key);
            if (filtered == null) {
                continue;
            }
            if (filtered.startsWith("MemorySection")) {
                continue;
            }
            filtered = ChatColor.translateAlternateColorCodes('&', filtered);
            config.set(key, filtered);
        }
    }

    /**
     * Parse colors for the Text.
     *
     * @param text the text
     * @return parsed text
     */
    public static String parseColours(@NotNull String text) {
        text = ChatColor.translateAlternateColorCodes('&', text);
        return text;
    }

    /**
     * Converts a name like IRON_INGOT into Iron Ingot to improve readability
     *
     * @param ugly The string such as IRON_INGOT
     * @return A nicer version, such as Iron Ingot
     */
    public static String prettifyText(@NotNull String ugly) {
        String[] nameParts = ugly.split("_");
        if (nameParts.length == 1) {
            return firstUppercase(ugly);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nameParts.length; i++) {
            sb.append(firstUppercase(nameParts[i]));
            if (i + 1 != nameParts.length) {
                sb.append(" ");
            }

        }
        return sb.toString();
    }

    /**
     * Read the file to the String
     *
     * @param fileName Target file.
     * @return Target file's content.
     */
    public static String readToString(@NotNull String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        long filelength = file.length();
        byte[] filecontent = new byte[(int) filelength];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return ("The OS does not support " + encoding);
        }
    }

    /**
     * Send warning message when some plugin calling deprecated method...
     * With the trace.
     */
    public static void sendDeprecatedMethodWarn() {
        QuickShop.instance.getLogger()
                .warning("Some plugin is calling a Deprecated method, Please contact the author to tell them to use the new api!");
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            QuickShop.instance.getLogger().warning("at " + stackTraceElement.getClassName() + "#" + stackTraceElement
                    .getMethodName() + " (" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")");
        }
    }

    /**
     * Covert ItemStack to YAML string.
     *
     * @param iStack target ItemStack
     * @return String serialized itemStack
     */
    public static String serialize(@NotNull ItemStack iStack) {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("item", iStack);
        return cfg.saveToString();
    }

    private static byte[] toByteArray(@NotNull InputStream in) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }

    /**
     * Return the Class name.
     *
     * @return The class prefix
     */
    public static String getClassPrefix() {

        String className = Thread.currentThread().getStackTrace()[2].getClassName();
        try {
            Class c = Class.forName(className);
            className = c.getSimpleName();
            if (!c.getSimpleName().isEmpty()) {
                className = c.getSimpleName();
            }
        } catch (ClassNotFoundException e) {
            //Ignore
        }
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        return "[" + className + "-" + methodName + "] ";
    }

    public static String getNMSVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    /**
     * Get the sign material using by plugin.
     * With compatiabily process.
     *
     * @return The material now using.
     */
    public static Material getSignMaterial() {

        Material signMaterial = Material.matchMaterial(plugin.getConfig().getString("shop.sign-material"));
        if (signMaterial != null) {
            return signMaterial;
        }
        signMaterial = Material.matchMaterial("OAK_WALL_SIGN"); //Fallback default sign in 1.14
        if (signMaterial != null) {
            return signMaterial;
        }
        signMaterial = Material.matchMaterial("WALL_SIGN"); //Fallback default sign in 1.13
        if (signMaterial != null) {
            return signMaterial;
        }
        //What the fuck!?
        plugin.getLogger().warning("QuickShop can't found any useable sign material, we will use default Sign Material.");
        try {
            return Material.OAK_WALL_SIGN;
        } catch (Throwable e) {
            return Material.matchMaterial("WALL_SIGN");
        }
    }

    /**
     * Get MinecraftServer's TPS
     *
     * @return TPS (e.g 19.92)
     */
    public static Double getTPS() {
        try {
            serverInstance = getNMSClass("MinecraftServer").getMethod("getServer").invoke(null);
            tpsField = serverInstance.getClass().getField("recentTps");
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        try {
            double[] tps = ((double[]) tpsField.get(serverInstance));
            return tps[0];
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }
    //
    // public static void shoppablesCheck(@NotNull Shop shop) {
    //     if (!Util.canBeShop(shop.getLocation().getBlock())) {
    //         Util.debugLog("This shopblock can't be a shop, deleting...");
    //         shop.onUnload();
    //         shop.delete();
    //     }
    // }

    /**
     * Check QuickShop is running on dev edition or not.
     *
     * @return DevEdition status
     */
    public static boolean isDevEdition() {
        String version = QuickShop.instance.getDescription().getVersion().toLowerCase();
        return (version.contains("dev") ||
                version.contains("develop") ||
                version.contains("alpha") ||
                version.contains("beta") ||
                version.contains("test") ||
                version.contains("snapshot") ||
                version.contains("preview"));
    }

    /**
     * Get the plugin is under dev-mode(debug mode)
     *
     * @return under dev-mode
     */
    public static boolean isDevMode() {
        return devMode;
    }

    /**
     * Get a material is a dye
     *
     * @param material The material
     * @return yes or not
     */
    public static boolean isDyes(@NotNull Material material) {
        return material.name().toUpperCase().endsWith("_DYE");
    }
}
