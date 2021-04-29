/*
 * This file is a part of project QuickShop, the name is Util.java
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

package org.maxgamer.quickshop.util;

import io.papermc.lib.PaperLib;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.database.MySQLCore;
import org.maxgamer.quickshop.shop.DisplayItem;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.watcher.InventoryEditContainer;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

public class Util {
    private static final EnumSet<Material> blacklist = EnumSet.noneOf(Material.class);
    private static final EnumMap<Material, Entry<Double, Double>> restrictedPrices =
            new EnumMap<>(Material.class);
    private static final EnumMap<Material, Integer> customStackSize = new EnumMap<>(Material.class);
    private static final EnumSet<Material> shoppables = EnumSet.noneOf(Material.class);
    private static final List<BlockFace> verticalFacing = Collections.unmodifiableList(Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST));
    private static final List<String> debugLogs = new ArrayList<>();
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static int bypassedCustomStackSize = -1;
    private static Yaml yaml = null;
    private static boolean devMode = false;
    private static QuickShop plugin;
    private static Object serverInstance;
    private static Field tpsField;
    private static List<String> worldBlacklist = new ArrayList<>(5);
    @Getter
    private static boolean disableDebugLogger = false;
    private static boolean currencySymbolOnRight;
    private static String alternateCurrencySymbol;
    private static boolean disableVaultFormat;
    private static boolean useDecimalFormat;
    @Getter
    private static final Map<String, String> currency2Symbol = new HashMap<>();

    /**
     * Convert strArray to String. E.g "Foo, Bar"
     *
     * @param strArray Target array
     * @return str
     */
    @NotNull
    public static String array2String(@NotNull String[] strArray) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String str : strArray) {
            joiner.add(str);
        }
        return joiner.toString();
    }

    /**
     * Convert boolean to string status
     *
     * @param bool Boolean
     * @return Enabled or Disabled
     */
    @NotNull
    public static String boolean2Status(boolean bool) {
        if (bool) {
            return "Enabled";
        } else {
            return "Disabled";
        }
    }

    /**
     * Backup shops.db
     *
     * @return The result for backup
     */
    public static boolean backupDatabase() {
        if (plugin.getDatabaseManager().getDatabase() instanceof MySQLCore) {
            return true; // Backup and logs by MySQL
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
            Files.copy(sqlfile.toPath(), bksqlfile.toPath());
        } catch (Exception e1) {
            plugin.getLogger().log(Level.WARNING, "Failed to backup the database", e1);
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


        if (isBlacklistWorld(b.getWorld())) {
            return false;
        }

        // Specificed types by configuration
        if (!isShoppables(b.getType())) {
            return false;
        }
        final BlockState bs = PaperLib.getBlockState(b, false).getState();
        if (bs instanceof EnderChest) { // BlockState for Mod supporting
            return plugin.getOpenInvPlugin() != null;
        }

        return bs instanceof InventoryHolder;
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

    public static boolean isBlacklistWorld(@NotNull World world) {
        return worldBlacklist.contains(world.getName());
    }

    /**
     * Counts the number of items in the given inventory where Util.matches(inventory item, item) is
     * true.
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
        for (final ItemStack iStack : inv.getStorageContents()) {
            //noinspection ConstantConditions
            if (iStack == null || iStack.getType() == Material.AIR) {
                continue;
            }
            if (plugin.getItemMatcher().matches(item, iStack)) {
                items += iStack.getAmount();
            }
        }
        return items / item.getAmount();
    }

    /**
     * Returns the number of items that can be given to the inventory safely.
     *
     * @param inv  The inventory to count
     * @param item The item prototype. Material, durabiltiy and enchants must match for 'stackability'
     *             to occur.
     * @return The number of items that can be given to the inventory safely.
     */
    public static int countSpace(@Nullable Inventory inv, @NotNull ItemStack item) {
        if (inv == null) {
            return 0;
        }
        int space = 0;

        int itemMaxStackSize = getItemMaxStackSize(item.getType());

        ItemStack[] contents = inv.getStorageContents();
        for (final ItemStack iStack : contents) {
            if (iStack == null || iStack.getType() == Material.AIR) {
                space += itemMaxStackSize;
            } else if (plugin.getItemMatcher().matches(item, iStack)) {
                space += iStack.getAmount() >= itemMaxStackSize ? 0 : itemMaxStackSize - iStack.getAmount();
            }
        }
        return space / item.getAmount();
    }

    /**
     * Returns a material max stacksize
     *
     * @param material Material
     * @return Game StackSize or Custom
     */
    public static int getItemMaxStackSize(@NotNull Material material) {
        return customStackSize.getOrDefault(material, bypassedCustomStackSize == -1 ? material.getMaxStackSize() : bypassedCustomStackSize);
    }

    /**
     * Covert YAML string to ItemStack.
     *
     * @param config serialized ItemStack
     * @return ItemStack iStack
     * @throws InvalidConfigurationException when failed deserialize config
     */
    @Nullable
    public static ItemStack deserialize(@NotNull String config) throws InvalidConfigurationException {
        if (yaml == null) {
            DumperOptions yamlOptions = new DumperOptions();
            yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            yamlOptions.setIndent(2);
            yaml = new Yaml(yamlOptions); //Caching it!
        }
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        Map<Object, Object> root = yaml.load(config);
        //noinspection unchecked
        Map<String, Object> item = (Map<String, Object>) root.get("item");
        int itemDataVersion = Integer.parseInt(String.valueOf(item.getOrDefault("v", "0")));
        try {
            // Try load the itemDataVersion to do some checks.
            //noinspection deprecation
            if (itemDataVersion > Bukkit.getUnsafe().getDataVersion()) {
                Util.debugLog("WARNING: DataVersion not matched with ItemStack: " + config);
                // okay we need some things to do
                if (plugin.getConfig().getBoolean("shop.force-load-downgrade-items.enable")) {
                    // okay it enabled
                    Util.debugLog("QuickShop is trying force loading " + config);
                    if (plugin.getConfig().getInt("shop.force-load-downgrade-items.method") == 0) { // Mode 0
                        //noinspection deprecation
                        item.put("v", Bukkit.getUnsafe().getDataVersion() - 1);
                    } else { // Mode other
                        //noinspection deprecation
                        item.put("v", Bukkit.getUnsafe().getDataVersion());
                    }
                    // Okay we have hacked the dataVersion, now put it back
                    root.put("item", item);
                    config = yaml.dump(root);

                    Util.debugLog("Updated, we will try load as hacked ItemStack: " + config);
                } else {
                    plugin
                            .getLogger()
                            .warning(
                                    "Cannot load ItemStack "
                                            + config
                                            + " because it saved from higher Minecraft server version, the action will fail and you will receive a exception, PLELASE DON'T REPORT TO QUICKSHOP!");
                    plugin
                            .getLogger()
                            .warning(
                                    "You can try force load this ItemStack by our hacked ItemStack read util(shop.force-load-downgrade-items), but beware, the data may damaged if you load on this lower Minecraft server version, Please backup your world and database before enable!");
                }
            }
            yamlConfiguration.loadFromString(config);
            return yamlConfiguration.getItemStack("item");
        } catch (Exception e) {
            throw new InvalidConfigurationException("Exception in deserialize item", e);
        }
    }

    @NotNull
    public static List<String> getDebugLogs() {
        lock.readLock().lock();
        List<String> strings = new ArrayList<>(debugLogs);
        lock.readLock().unlock();
        return strings;
    }

    /**
     * Print debug log when plugin running on dev mode.
     *
     * @param logs logs
     */
    public static void debugLog(@NotNull String... logs) {
        if (disableDebugLogger) {
            return;
        }
        lock.writeLock().lock();
        if (debugLogs.size() >= 2000) {
            debugLogs.clear();
        }
        if (!devMode) {
            for (String log : logs) {
                debugLogs.add("[DEBUG] " + log);
            }
            lock.writeLock().unlock();
            return;
        }
        final StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        final String className = stackTraceElement.getClassName();
        final String methodName = stackTraceElement.getMethodName();
        final int codeLine = stackTraceElement.getLineNumber();
        for (String log : logs) {
            debugLogs.add("[DEBUG] [" + className + "] [" + methodName + "] (" + codeLine + ") " + log);
            QuickShop.getInstance().getLogger().info("[DEBUG] [" + className + "] [" + methodName + "] (" + codeLine + ") " + log);
        }
        lock.writeLock().unlock();
    }


    /**
     * Formats the given number according to how vault would like it. E.g. $50 or 5 dollars.
     *
     * @param n    price
     * @param shop shop
     * @return The formatted string.
     */
    @NotNull
    public static String format(double n, @Nullable Shop shop) {
        return format(n, disableVaultFormat, shop.getLocation().getWorld(), shop);
    }

    @NotNull
    public static String format(double n, boolean internalFormat, @NotNull World world, @Nullable Shop shop) {
        if (shop != null) {
            return format(n, internalFormat, world, shop.getCurrency());
        } else {
            return format(n, internalFormat, world, (Shop) null);
        }
    }

    @NotNull
    public static String format(double n, boolean internalFormat, @NotNull World world, @Nullable String currency) {
        if (internalFormat) {
            return getInternalFormat(n, currency);
        }

        if (plugin == null) {
            Util.debugLog("Called format before Plugin booted up, forcing fixing.");
            plugin = QuickShop.getInstance();
        }
        if (plugin.getEconomy() == null) {
            Util.debugLog("Called format before Economy booted up, using built-in formatter.");
            return getInternalFormat(n, currency);
        }
        try {
            String formatted = plugin.getEconomy().format(n, world, currency);
            if (StringUtils.isEmpty(formatted)) {
                Util.debugLog(
                        "Use alternate-currency-symbol to formatting, Cause economy plugin returned null");
                return getInternalFormat(n, currency);
            } else {
                return formatted;
            }
        } catch (NumberFormatException e) {
            Util.debugLog("format", e.getMessage());
            Util.debugLog(
                    "format", "Use alternate-currency-symbol to formatting, Cause NumberFormatException");
            return getInternalFormat(n, currency);
        }
    }

    private static String getInternalFormat(double amount, @Nullable String currency) {
        if (StringUtils.isEmpty(currency)) {
            Util.debugLog("Format: Currency is null");
            String formatted = useDecimalFormat ? MsgUtil.decimalFormat(amount) : Double.toString(amount);
            return currencySymbolOnRight ? formatted + alternateCurrencySymbol : alternateCurrencySymbol + formatted;
        } else {
            Util.debugLog("Format: Currency is: [" + currency + "]");
            String formatted = useDecimalFormat ? MsgUtil.decimalFormat(amount) : Double.toString(amount);
            String symbol = currency2Symbol.getOrDefault(currency, currency);
            return currencySymbolOnRight ? formatted + symbol : symbol + formatted;
        }
    }


    /**
     * return the right side for given blockFace
     *
     * @param blockFace given blockFace
     * @return the right side for given blockFace, UP and DOWN will return itself
     */
    @NotNull
    public static BlockFace getRightSide(@NonNull BlockFace blockFace) {
        switch (blockFace) {
            case EAST:
                return BlockFace.SOUTH;
            case NORTH:
                return BlockFace.EAST;
            case SOUTH:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.NORTH;
            default:
                return blockFace;
        }
    }


    /**
     * Get vertical BlockFace list
     *
     * @return vertical BlockFace list (unmodifiable)
     */
    @NotNull
    public static List<BlockFace> getVerticalFacing() {
        return verticalFacing;
    }

    /**
     * Fetches the block which the given sign is attached to
     *
     * @param b The block which is attached
     * @return The block the sign is attached to
     */
    @Nullable
    public static Block getAttached(@NotNull Block b) {
        final BlockData blockData = b.getBlockData();
        if (blockData instanceof Directional) {
            final Directional directional = (Directional) blockData;
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
    @NotNull
    public static String getClassPrefix(@NotNull Class<?> c) {
        String callClassName = Thread.currentThread().getStackTrace()[2].getClassName();
        String customClassName = c.getSimpleName();
        return "[" + callClassName + "-" + customClassName + "] ";
    }

    public static boolean useEnchantmentForEnchantedBook() {
        return plugin.getConfig().getBoolean("shop.use-enchantment-for-enchanted-book");
    }

    @NotNull
    public static String getItemStackName(@NotNull ItemStack itemStack) {
        if (useEnchantmentForEnchantedBook() && itemStack.getType() == Material.ENCHANTED_BOOK) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta instanceof EnchantmentStorageMeta && ((EnchantmentStorageMeta) meta).hasStoredEnchants()) {
                return getFirstEnchantmentName((EnchantmentStorageMeta) meta);
            }
        }
        if (itemStack.hasItemMeta()
                && Objects.requireNonNull(itemStack.getItemMeta()).hasDisplayName()) {
            return itemStack.getItemMeta().getDisplayName();
        }
        return MsgUtil.getItemi18n(itemStack.getType().name());
    }

    @NotNull
    public static String getFirstEnchantmentName(@NotNull EnchantmentStorageMeta meta) {
        if (!meta.hasStoredEnchants()) {
            throw new IllegalArgumentException("Item does not have an enchantment!");
        }
        Map.Entry<Enchantment, Integer> entry = meta.getStoredEnchants().entrySet().iterator().next();
        String name = MsgUtil.getEnchi18n(entry.getKey());
        if (entry.getValue() == 1 && entry.getKey().getMaxLevel() == 1) {
            return name;
        } else {
            return name + " " + RomanNumber.toRoman(entry.getValue());
        }
    }

    /**
     * Get ItemStack's local name, return null if failed to get.
     *
     * @param itemStack Target ItemStack
     * @return LocalName or NULL
     */
    @Nullable
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

    /**
     * Return an entry with min and max prices, but null if there isn't a price restriction
     *
     * @param material mat
     * @return min, max
     */
    @Nullable
    public static Entry<Double, Double> getPriceRestriction(@NotNull Material material) {
        return restrictedPrices.get(material);
    }


    public static boolean isDoubleChest(@Nullable BlockState state) {
        if (!(state instanceof Chest)) {
            return false;
        }
        String blockDataStr = state.getBlockData().getAsString();
        //Black magic for detect double chest
        //minecraft:chest[facing=north,type=right,waterlogged=false]
        //minecraft:chest[facing=north,type=left,waterlogged=false]
        //minecraft:chest[facing=north,type=single,waterlogged=false]
        return !blockDataStr.contains("type=single");
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
            if (Objects.requireNonNull(shop.getLocation().getWorld()).getName().equals(worldName)) {
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
    @NotNull
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
    @NotNull
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
        customStackSize.clear();
        currency2Symbol.clear();
        plugin = QuickShop.getInstance();
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

        for (String s : plugin.getConfig().getStringList("shop.price-restriction")) {
            String[] sp = s.split(";");
            if (sp.length == 3) {
                try {
                    Material mat = Material.matchMaterial(sp[0]);
                    if (mat == null) {
                        plugin
                                .getLogger()
                                .warning(
                                        "Material "
                                                + sp[0]
                                                + " in config.yml can't match with a valid Materials, check your config.yml!");
                        continue;
                    }
                    restrictedPrices.put(
                            mat, new SimpleEntry<>(Double.valueOf(sp[1]), Double.valueOf(sp[2])));
                } catch (Exception e) {
                    plugin.getLogger().warning("Invalid price restricted material: " + s);
                }
            }
        }
        for (String material : plugin.getConfig().getStringList("custom-item-stacksize")) {
            String[] data = material.split(":");
            if (data.length != 2) {
                continue;
            }

            if (data[0].equalsIgnoreCase("*")) {
                bypassedCustomStackSize = Integer.parseInt(data[1]);
            }
            Material mat = Material.matchMaterial(data[0]);
            if (mat == null || mat == Material.AIR) {
                plugin.getLogger().warning(material + " not a valid material type in custom-item-stacksize section.");
                continue;
            }
            customStackSize.put(mat, Integer.parseInt(data[1]));

        }
        worldBlacklist = plugin.getConfig().getStringList("shop.blacklist-world");
        disableDebugLogger = plugin.getConfig().getBoolean("debug.disable-debuglogger", false);

        currencySymbolOnRight = plugin.getConfig().getBoolean("shop.currency-symbol-on-right", false);
        alternateCurrencySymbol = plugin.getConfig().getString("shop.alternate-currency-symbol", "$");
        disableVaultFormat = plugin.getConfig().getBoolean("shop.disable-vault-format", false);
        useDecimalFormat = plugin.getConfig().getBoolean("use-decimal-format", false);

        List<String> symbols = plugin.getConfig().getStringList("shop.alternate-currency-symbol-list");


        symbols.forEach(entry -> {
            String[] splits = entry.split(";", 2);
            if (splits.length < 2) {
                plugin.getLogger().warning("Invalid entry in alternate-currency-symbol-list: " + entry);
            }
            currency2Symbol.put(splits[0], splits[1]);
        });

        InteractUtil.init(plugin.getConfig());
    }

    /**
     * Read the InputStream to the byte array.
     *
     * @param filePath Target file
     * @return Byte array
     */
    @Nullable
    public static byte[] inputStream2ByteArray(@NotNull String filePath) {
        try (InputStream in = new FileInputStream(filePath)) {
            return toByteArray(in);
        } catch (IOException e) {
            return null;
        }
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
     * Read the InputStream to the byte array.
     *
     * @param inputStream Target stream
     * @return Byte array
     */
    @Nullable
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
        if (inv.getHolder() == null) {
            Util.debugLog("Skipped plugin gui inventory check.");
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
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
                            return; // Virtual GUI
                        }
                        plugin
                                .getSyncTaskWatcher()
                                .getInventoryEditQueue()
                                .offer(new InventoryEditContainer(inv, i, itemStack, new ItemStack(Material.AIR)));
                        Util.debugLog("Found shop display item in an inventory, Scheduling to removal...");
                        MsgUtil.sendGlobalAlert(
                                "[InventoryCheck] Found displayItem in inventory at "
                                        + location
                                        + ", Item is "
                                        + itemStack.getType().name());
                    }
                }
            } catch (Exception t) {
                // Ignore
            }
        });
    }

    /**
     * @param stack The ItemStack to check if it is blacklisted
     * @return true if the ItemStack is black listed. False if not.
     */
    public static boolean isBlacklisted(@NotNull ItemStack stack) {
        if (blacklist.contains(stack.getType())) {
            return true;
        }
        if (!stack.hasItemMeta()) {
            return false;
        }
        if (!Objects.requireNonNull(stack.getItemMeta()).hasLore()) {
            return false;
        }
        for (String lore : Objects.requireNonNull(stack.getItemMeta().getLore())) {
            List<String> blacklistLores = plugin.getConfig().getStringList("shop.blacklist-lores");
            for (String blacklistLore : blacklistLores) {
                if (lore.contains(blacklistLore)) {
                    return true;
                }
            }
        }
        return false;
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
        } catch (Exception t) {
            // ignore
        }
        return false;
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
        Block bshop = Util.getAttached(b);
        if (bshop == null) {
            return false;
        }
        Shop shop = plugin.getShopManager().getShopIncludeAttached(bshop.getLocation());
        if (shop == null) {
            shop = plugin.getShopManager().getShopIncludeAttached(bshop.getLocation().clone().add(0, 1, 0));
        }
        return shop != null && !shop.getModerator().isModerator(p.getUniqueId());
    }

    /**
     * Returns the chest attached to the given chest. The given block must be a chest.
     *
     * @param state The chest to check.
     * @return the block which is also a chest and connected to b.
     */
    public static Block getSecondHalf(@NotNull BlockState state) {
        if (!(state instanceof Chest)) {
            return null;
        }
        Chest oneSideOfChest = (Chest) state;
        InventoryHolder chestHolder = oneSideOfChest.getInventory().getHolder();
        if (chestHolder instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) chestHolder;
            Chest leftC = (Chest) doubleChest.getLeftSide();
            Chest rightC = (Chest) doubleChest.getRightSide();
            if (equalsBlockStateLocation(oneSideOfChest.getLocation(), Objects.requireNonNull(rightC).getLocation())) {
                return leftC.getBlock();
            } else {
                return rightC.getBlock();
            }
        }
        return null;
    }

    /**
     * Check two location is or not equals for the BlockPosition on 2D
     *
     * @param b1 block 1
     * @param b2 block 2
     * @return Equals or not.
     */
    private static boolean equalsBlockStateLocation(@NotNull Location b1, @NotNull Location b2) {
        return (b1.getBlockX() == b2.getBlockX())
                && (b1.getBlockY() == b2.getBlockY())
                && (b1.getBlockZ() == b2.getBlockZ());
    }

    /**
     * @param mat The material to check
     * @return Returns true if the item is a tool (Has durability) or false if it doesn't.
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
        final int length = string.length();
        if (length != 36 && length != 32) {
            return false;
        }
        final String[] components = string.split("-");
        return components.length == 5;
    }

    /**
     * Convert strList to String. E.g "Foo, Bar"
     *
     * @param strList Target list
     * @return str
     */
    @NotNull
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
     * @return The location the player should be facing to have their crosshairs on the location
     * lookAt Kudos to bergerkiller for most of this function
     */
    public static @NotNull Location lookAt(@NotNull Location loc, @NotNull Location lookat) {
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

//    /**
//     * Match the both map1 and map2
//     *
//     * @param map1 Map1
//     * @param map2 Map2
//     * @return Map1 match Map2 and Map2 match Map1
//     */
//    @Deprecated
//    public static boolean mapDuoMatches(@NotNull Map<?, ?> map1, @NotNull Map<?, ?> map2) {
//        boolean result = mapMatches(map1, map2);
//        if (!result) {
//            return false;
//        }
//        return mapMatches(map2, map1);
//    }
//
//    /**
//     * Match the map1 and map2
//     *
//     * @param map1 Map1
//     * @param map2 Map2
//     * @return Map1 match Map2
//     */
//    @Deprecated
//    public static boolean mapMatches(@NotNull Map<?, ?> map1, @NotNull Map<?, ?> map2) {
//        Set<? extends Entry<?, ?>> objectSet = map2.entrySet();
//        for (Object obj : map1.keySet()) {
//            if (!objectSet.contains(obj)) {
//                return false;
//            }
//        }
//        return true;
//    }

    /**
     * Match the list1 and list2
     *
     * @param list1 requireList
     * @param list2 givenList
     * @return Map1 match Map2
     */
    @Deprecated
    public static boolean listMatches(@NotNull List<?> list1, @NotNull List<?> list2) {
        return list2.containsAll(list1);
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
            filtered = parseColours(filtered);
            config.set(key, filtered);
        }
    }

    /**
     * Parse colors for the Text.
     *
     * @param text the text
     * @return parsed text
     */
    @NotNull
    public static String parseColours(@Nullable String text) {
        if (StringUtils.isEmpty(text)) {
            return "";
        }
        text = ChatColor.translateAlternateColorCodes('&', text);
        return text;
    }

//    private static final ThreadLocalRandom random = ThreadLocalRandom.current();
    //     ONLY FOR FUN ^ ^
//    /**
//     * Replace &g to random HEX color
//     * @param text original message contains &g
//     * @return Processed message
//     */
//    public static String parseRandomHexText(@NotNull String text){
//        String newStr = text.replaceFirst("&g","&x"+randomHexGen());
//        while (!newStr.equals(text)){
//            text = newStr;
//            newStr = newStr.replaceFirst("&g","&x"+randomHexGen());
//        }
//        return newStr;
//    }
//
//    public static String randomHexGen(){
//        StringBuilder hex = new StringBuilder();
//        for (int i = 0; i < 6; i++) {
//            if(random.nextBoolean()) {
//                hex.append("&");
//                hex.append((char)random.nextInt('0', '9'));
//            }else{
//                hex.append("&");
//                hex.append((char)random.nextInt('a', 'f'));
//            }
//        }
//        return hex.toString();
//    }

    /**
     * Parse colors for the List.
     *
     * @param list the list
     * @return parsed list
     */
    @NotNull
    public static List<String> parseColours(@NotNull List<String> list) {
        final List<String> newList = new ArrayList<>();

        for (String s : list) {
            newList.add(parseColours(s));
        }

        return newList;
    }

    /**
     * Converts a name like IRON_INGOT into Iron Ingot to improve readability
     *
     * @param ugly The string such as IRON_INGOT
     * @return A nicer version, such as Iron Ingot
     */
    @NotNull
    public static String prettifyText(@NotNull String ugly) {
        String[] nameParts = ugly.split("_");
        if (nameParts.length == 1) {
            return firstUppercase(ugly);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nameParts.length; i++) {
            if (!nameParts[i].isEmpty()) {
                sb.append(Character.toUpperCase(nameParts[i].charAt(0))).append(nameParts[i].substring(1).toLowerCase());
            }
            if (i + 1 != nameParts.length) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    /**
     * First uppercase for every words the first char for a text.
     *
     * @param string text
     * @return Processed text.
     */
    @NotNull
    public static String firstUppercase(@NotNull String string) {
        if (string.length() > 1) {
            return Character.toUpperCase(string.charAt(0)) + string.substring(1).toLowerCase();
        } else {
            return string.toUpperCase();
        }
    }

    /**
     * Read the file to the String
     *
     * @param fileName Target file.
     * @return Target file's content.
     */
    @NotNull
    public static String readToString(@NotNull String fileName) {
        File file = new File(fileName);
        return readToString(file);
    }

    /**
     * Read the file to the String
     *
     * @param file Target file.
     * @return Target file's content.
     */
    @NotNull
    public static String readToString(@NotNull File file) {
        byte[] filecontent = new byte[(int) file.length()];
        try (FileInputStream in = new FileInputStream(file)) {
            in.read(filecontent);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to read file: " + file, e);
        }
        return new String(filecontent, StandardCharsets.UTF_8);
    }

//    /**
//     * Send warning message when some plugin calling deprecated method... With the trace.
//     */
//    @Deprecated
//    public static void sendDeprecatedMethodWarn() {
//        QuickShop.instance
//                .getLogger()
//                .warning(
//                        "Some plugin is calling a Deprecated method, Please contact the author to tell them to use the new api!");
//        MsgUtil.debugStackTrace(Thread.currentThread().getStackTrace());
//    }

    /**
     * Covert ItemStack to YAML string.
     *
     * @param iStack target ItemStack
     * @return String serialized itemStack
     */
    @NotNull
    public static String serialize(@NotNull ItemStack iStack) {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("item", iStack);
        return cfg.saveToString();
    }


    /**
     * Return the Class name.
     *
     * @return The class prefix
     */
    @NotNull
    public static String getClassPrefix() {

        String className = Thread.currentThread().getStackTrace()[2].getClassName();
        try {
            Class<?> c = Class.forName(className);
            className = c.getSimpleName();
            if (!c.getSimpleName().isEmpty()) {
                className = c.getSimpleName();
            }
        } catch (ClassNotFoundException e) {
            // Ignore
        }
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        return "[" + className + "-" + methodName + "] ";
    }

    @NotNull
    public static String getNMSVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    /**
     * Get the sign material using by plugin. With compatiabily process.
     *
     * @return The material now using.
     */
    @NotNull
    public static Material getSignMaterial() {

        Material signMaterial =
                Material.matchMaterial(
                        Objects.requireNonNull(plugin.getConfig().getString("shop.sign-material")));
        if (signMaterial != null) {
            return signMaterial;
        }
        signMaterial = Material.matchMaterial("OAK_WALL_SIGN"); // Fallback default sign in 1.14
        if (signMaterial != null) {
            return signMaterial;
        }
        signMaterial = Material.matchMaterial("WALL_SIGN"); // Fallback default sign in 1.13
        if (signMaterial != null) {
            return signMaterial;
        }
        // What the fuck!?
        plugin
                .getLogger()
                .warning(
                        "QuickShop can't found any usable sign material, we will use default Sign Material.");
        try {
            return Material.OAK_WALL_SIGN;
        } catch (Exception e) {
            return Material.matchMaterial("WALL_SIGN");
        }
    }

    /**
     * Get MinecraftServer's TPS
     *
     * @return TPS (e.g 19.92)
     */
    @NotNull
    public static Double getTPS() {
        if (serverInstance == null || tpsField == null) {
            try {
                serverInstance = getNMSClass("MinecraftServer").getMethod("getServer").invoke(null);
                tpsField = serverInstance.getClass().getField("recentTps");
            } catch (NoSuchFieldException
                    | SecurityException
                    | IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException
                    | NoSuchMethodException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to getting server TPS, please report to QuickShop.", e);
                serverInstance = null;
                tpsField = null;
                Util.debugLog("Failed to get TPS " + e.getMessage());
                return 20.0;
            }
        }
        try {
            double[] tps = ((double[]) tpsField.get(serverInstance));
            return tps[0];
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    //TODO: Need caching
    @NotNull
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
     * Check QuickShop is running on dev edition or not.
     *
     * @return DevEdition status
     */
    public static boolean isDevEdition() {
        return !QuickShop.getInstance().getBuildInfo().getGitBranch().equalsIgnoreCase("release");
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

    /**
     * Call a event and check it is cancelled.
     *
     * @param event The event implement the Cancellable interface.
     * @return The event is cancelled.
     */
    public static boolean fireCancellableEvent(@NotNull Cancellable event) {
        if (!(event instanceof Event)) {
            throw new IllegalArgumentException("Cancellable must is event implement");
        }
        Bukkit.getPluginManager().callEvent((Event) event);
        return event.isCancelled();
    }

    /**
     * Get QuickShop caching folder
     *
     * @return The caching folder
     */
    public static File getCacheFolder() {
        File cache = new File(QuickShop.getInstance().getDataFolder(), "cache");
        if (!cache.exists()) {
            cache.mkdirs();
        }
        return cache;
    }

    /**
     * Return the player names based on the configuration
     *
     * @return the player names
     */
    @NotNull
    public static List<String> getPlayerList() {
        List<String> tabList = new ArrayList<>();
        if (plugin.getConfig().getBoolean("include-offlineplayer-list")) {
            // Include
            for (OfflinePlayer offlinePlayer : plugin.getServer().getOfflinePlayers()) {
                tabList.add(offlinePlayer.getName());
            }
        } else {
            // Not Include
            for (OfflinePlayer offlinePlayer : plugin.getServer().getOnlinePlayers()) {
                tabList.add(offlinePlayer.getName());
            }
        }
        return tabList;
    }

    /**
     * Merge args array to a String object with space
     *
     * @param args Args
     * @return String object
     */
    @NotNull
    public static String mergeArgs(@NotNull String[] args) {
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg).append(" ");
        }
        return builder.toString().trim();
    }

    /**
     * Ensure this method is calling from specific thread
     *
     * @param async on async thread or main server thread.
     */
    public static void ensureThread(boolean async) {
        boolean isMainThread = Bukkit.isPrimaryThread();
        if (async) {
            if (isMainThread)
                throw new IllegalStateException("#[Illegal Access] This method require runs on async thread.");
        } else {
            if (!isMainThread)
                throw new IllegalStateException("#[Illegal Access] This method require runs on server main thread.");
        }
    }

    public static void mainThreadRun(@NotNull Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTask(QuickShop.getInstance(), runnable);
        }
    }

}
