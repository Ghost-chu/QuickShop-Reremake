package org.maxgamer.quickshop.Util;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Comparator;
import java.util.Map.Entry;

import com.google.common.io.Files;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.maxgamer.quickshop.Database.MySQLCore;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.DisplayItem;
import org.maxgamer.quickshop.Shop.Shop;

/**
 * @author MACHENIKE
 */
public class Util {
    private static EnumSet<Material> blacklist = EnumSet.noneOf(Material.class);
    private static EnumSet<Material> shoppables = EnumSet.noneOf(Material.class);
    private static EnumMap<Material, Entry<Double, Double>> restrictedPrices = new EnumMap<Material, Entry<Double, Double>>(Material.class);
    private static List<String> worldBlacklist = new ArrayList<>();
    private static QuickShop plugin;
    private static Method storageContents;
    private static boolean devMode;
    static Map<UUID, Long> timerMap = new HashMap<UUID, Long>();

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
                                .warning("Material " + sp[0] + " in config.yml can't match with vaild Materials, check your config.yml!");
                        continue;
                    }
                    restrictedPrices.put(mat, new SimpleEntry<Double, Double>(Double.valueOf(sp[1]), Double.valueOf(sp[2])));
                } catch (Exception e) {
                    plugin.getLogger().warning("Invalid price restricted material: " + s);
                }
            }
        }

        // try {
        //     storageContents = Inventory.class.getMethod("getStorageContents");
        // } catch (Exception e) {
        //     try {
        //         storageContents = Inventory.class.getMethod("getContents");
        //     } catch (Exception e1) {
        //         throw new RuntimeException(e1);
        //     }
        // }
        worldBlacklist = plugin.getConfig().getStringList("shop.blacklist-world");

    }

    /** Return an entry with min and max prices, but null if there isn't a price restriction */
    public static Entry<Double, Double> getPriceRestriction(Material material) {
        return restrictedPrices.get(material);
    }

    @SuppressWarnings("deprecation")
    public static boolean isTransparent(Material m) {
        boolean trans = m.isTransparent();
        return trans;
    }

    public static boolean isShoppables(Material material) {
        return shoppables.contains(material);
    }

    /**
     * Parse colors for the YamlConfiguration.
     *
     * @param config
     */
    public static void parseColours(YamlConfiguration config) {
        Set<String> keys = config.getKeys(true);
        for (String key : keys) {
            String filtered = config.getString(key);
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
     * @param config
     */
    public static String parseColours(String text) {
        text = ChatColor.translateAlternateColorCodes('&', text);
        return text;
    }

    /**
     * Returns true if the given block could be used to make a shop out of.
     *
     * @param b The block to check, Possibly a chest, dispenser, etc.
     * @return True if it can be made into a shop, otherwise false.
     */
    public static boolean canBeShop(Block b) {
        BlockState bs;
        try {
            bs = b.getState();
        } catch (Throwable ex) {
            ex.printStackTrace();
            plugin.getLogger().warning("A known bug was trigged, i can't fix it because it looks a Spigot bug.");
            plugin.getLogger().warning("QuickShop just got that exception and catch it, if you see this message,");
            plugin.getLogger().warning("Please give author dump infomation under this line:");
            plugin.getLogger().warning("Null:" + ((b == null) ? "true" : "false"));
            if (b != null) {
                plugin.getLogger().warning("Location: " + b.getLocation().toString());
            }
            return false;
        }
        if ((bs instanceof InventoryHolder == false) && b.getType() != Material.ENDER_CHEST) {
            return false;
        }
        if (b.getType() == Material.ENDER_CHEST) {
            if (plugin.getOpenInvPlugin() == null) {
                return false;
            }
        }
        return (isShoppables(b.getType()) && isBlacklistWorld(b.getWorld()));

    }

    public static boolean isBlacklistWorld(World world) {
        return worldBlacklist.contains(world.getName());
    }

    /**
     * Gets the percentage (Without trailing %) damage on a tool.
     *
     * @param item The ItemStack of tools to check
     * @return The percentage 'health' the tool has. (Opposite of total damage)
     */
    public static String getToolPercentage(ItemStack item) {
        double dura = ((Damageable) item.getItemMeta()).getDamage();
        ;
        double max = item.getType().getMaxDurability();
        DecimalFormat formatter = new DecimalFormat("0");
        return formatter.format((1 - dura / max) * 100.0);
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
    public static Block getSecondHalf_old(Block b) {
        if (b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST)
            return null;
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
     * Returns the chest attached to the given chest. The given block must be a
     * chest.
     *
     * @param b The chest to check.
     * @return the block which is also a chest and connected to b.
     */
    public static Block getSecondHalf(Block b) {
        if ((b.getType() != Material.CHEST) && (b.getType() != Material.TRAPPED_CHEST)) {
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
                return leftC.getBlock();
            }
            if (equalsBlockStateLocation(oneSideOfChest.getLocation(), leftC.getLocation())) {
                return rightC.getBlock();
            }
            Util.debugLog("Bug detected, DoubleChest holder can't find the any one side chest.");
            return null;
        } else {
            return null;
        }
    }

    /**
     * Check two location is or not equals for the BlockPosition on 2D
     *
     * @param b1
     * @param b2
     * @return Equals or not.
     */
    private static final boolean equalsBlockStateLocation(Location b1, Location b2) {
        return (b1.getBlockX() == b2.getBlockX()) && (b1.getBlockY() == b2.getBlockY()) && (b1.getBlockZ() == b2
                .getBlockZ()) && (b1.getWorld().getName().equals(b2.getWorld().getName()));
    }

    /**
     * Check two location is or not equals for the BlockPosition on 2D
     *
     * @param b1
     * @param b2
     * @return Equals or not.
     */
    public static boolean location3DEqual(Location loc1, Location loc2) {
        if (loc1.getWorld().getName() != loc2.getWorld().getName())
            return false;
        if (loc1.getBlockX() != loc2.getBlockX())
            return false;
        if (loc1.getBlockY() != loc2.getBlockY())
            return false;
        if (loc1.getBlockZ() != loc2.getBlockZ())
            return false;
        return true;
    }

    /**
     * Checks whether someone else's shop is within reach of a hopper being placed by a player.
     *
     * @param b The block being placed.
     * @param p The player performing the action.
     * @return true if a nearby shop was found, false otherwise.
     */
    public static boolean isOtherShopWithinHopperReach(Block b, Player p) {
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
     * Covert ItemStack to YAML string.
     *
     * @param ItemStack iStack
     * @return String serialized itemStack
     */
    public static String serialize(ItemStack iStack) {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("item", iStack);
        return cfg.saveToString();
    }

    /**
     * Covert YAML string to ItemStack.
     *
     * @param String serialized itemStack
     * @return ItemStack iStack
     */
    public static ItemStack deserialize(String config) throws InvalidConfigurationException {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.loadFromString(config);
        cfg.getString("item");
        ItemStack stack = cfg.getItemStack("item");
        return stack;
    }

    /**
     * Fetches an ItemStack's name - For example, converting INK_SAC:11 to
     * Dandellion Yellow, or WOOL:14 to Red Wool
     *
     * @param itemStack The itemstack to fetch the name of
     * @return The human readable item name.
     */
    public static String getName(ItemStack itemStack) {
//		if (NMS.isPotion(itemStack.getType())) {
//			return CustomPotionsName.getFullName(itemStack);
//		}		
        String vanillaName = itemStack.getType().name();
        return prettifyText(vanillaName);
    }

    /**
     * Converts a name like IRON_INGOT into Iron Ingot to improve readability
     *
     * @param ugly The string such as IRON_INGOT
     * @return A nicer version, such as Iron Ingot
     */
    public static String prettifyText(String ugly) {
        String[] nameParts = ugly.split("_");
        if (nameParts.length == 1) {
            return firstUppercase(ugly);
        }

        StringBuilder sb = new StringBuilder();
        for (String part : nameParts) {
            sb.append(firstUppercase(part) + " ");
        }

        return sb.toString();
    }

    /**
     * Get item's sign name for display on the sign.
     *
     * @param ItemStack itemStack
     * @return String ItemOnSignName
     */
    // Let's make very long names shorter for our sign
    public static String getNameForSign(ItemStack itemStack) {
//		if (NMS.isPotion(itemStack.getType())) {
//			return CustomPotionsName.getSignName(itemStack);
//		}

        ItemStack is = itemStack.clone();
        is.setAmount(1);

        if (is.hasItemMeta()) {
            if (is.getItemMeta().hasDisplayName()) {
                return is.getItemMeta().getDisplayName();
            }
        }

        String name = MsgUtil.getItemi18n(itemStack.getType().name()).trim();

        String[] nameParts = name.split("_");
        if (nameParts.length == 1) {
            return firstUppercase(nameParts[0]);
        }

        for (int i = 0; i < nameParts.length - 1; i++) {
            int length = StringUtils.join(nameParts).length();
            if (length > 16) {
                nameParts[i] = nameParts[i].substring(0, 1) + ".";
            } else {
                nameParts[i] = firstUppercase(nameParts[i]);
            }
        }

        nameParts[nameParts.length - 1] = firstUppercase(nameParts[nameParts.length - 1]);

        return StringUtils.join(nameParts);
    }

    /**
     * First uppercase for every words the first char for a text.
     *
     * @param string
     * @return Processed text.
     */
    public static String firstUppercase(String string) {
        if (string.length() > 1) {
            return Character.toUpperCase(string.charAt(0)) + string.substring(1).toLowerCase();
        } else {
            return string.toUpperCase();
        }
    }

    public static String toRomain(Integer value) {
        return toRoman(value.intValue());
    }

    private static final String[] ROMAN = { "X", "IX", "V", "IV", "I" };
    private static final int[] DECIMAL = { 10, 9, 5, 4, 1 };

    /**
     * Converts the given number to roman numerals. If the number is >= 40 or <=
     * 0, it will just return the number as a string.
     *
     * @param n The number to convert
     * @return The roman numeral representation of this number, or the number in
     * decimal form as a string if n <= 0 || n >= 40.
     */
    public static String toRoman(int n) {
        if (n <= 0 || n >= 40)
            return "" + n;
        String roman = "";
        for (int i = 0; i < ROMAN.length; i++) {
            while (n >= DECIMAL[i]) {
                n -= DECIMAL[i];
                roman += ROMAN[i];
            }
        }
        return roman;
    }

    /**
     * @param mat The material to check
     * @return Returns true if the item is a tool (Has durability) or false if
     * it doesn't.
     */
    public static boolean isTool(Material mat) {
        if (mat.getMaxDurability() == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Compares two items to each other. Returns true if they match.
     *
     * @param stack1 The first item stack
     * @param stack2 The second item stack
     * @return true if the itemstacks match. (Material, durability, enchants, name)
     */
    public static boolean matches(ItemStack stack1, ItemStack stack2) {

        if (plugin.getConfig().getBoolean("shop.strict-matches-check"))
            if (stack1.hashCode() != stack2.hashCode())
                return false;

        if (stack1 == stack2)
            return true; // Referring to the same thing, or both are null.
        if (stack1 == null || stack2 == null)
            return false; // One of them is null (Can't be both, see above)
        if (stack1.getType() != stack2.getType())
            return false; // Not the same material
        if (stack1.hasItemMeta() != stack2.hasItemMeta())
            return false; //Not same

        if (stack1.hasItemMeta()) { //Both have ItemMeta, see above
            ItemMeta stack1Meta = stack1.getItemMeta();
            ItemMeta stack2Meta = stack2.getItemMeta();
            /** DisplayName check **/
            if (stack1Meta.hasDisplayName() != stack2Meta.hasDisplayName())
                return false; //Has displayName check
            if (stack1Meta.hasDisplayName()) {
                String stack1DisplayName = stack1Meta.getDisplayName();
                String stack2DisplayName = stack2Meta.getDisplayName();
                if (!stack1DisplayName.equals(stack2DisplayName))
                    return false; //DisplayName check;
            }
            /** Enchants check **/
            if (stack1Meta.hasEnchants() != stack2Meta.hasEnchants())
                return false;
            if (stack1Meta.hasEnchants()) {
                Map<Enchantment, Integer> stack1Ench = stack1Meta.getEnchants();
                Map<Enchantment, Integer> stack2Ench = stack2Meta.getEnchants();
                if (stack1Ench.hashCode() != stack2Ench.hashCode())
                    return false;
            }
            /** Damage check **/
            if (stack1Meta instanceof Damageable != stack2Meta instanceof Damageable)
                return false;
            if (stack1Meta instanceof Damageable) {
                Damageable stack1Damage = (Damageable) stack1Meta;
                Damageable stack2Damage = (Damageable) stack2Meta;
                if (stack1Damage.hashCode() != stack2Damage.hashCode())
                    return false;
            }
            /** Potion check **/
            if (stack1Meta instanceof PotionMeta != stack2Meta instanceof Damageable)
                return false;
            if (stack1Meta instanceof PotionMeta) {
                PotionMeta stack1Potion = (PotionMeta) stack1Meta;
                PotionMeta stack2Potion = (PotionMeta) stack2Meta;
                if (stack1Potion.hashCode() != stack2Potion.hashCode())
                    return false;

            }
        }
        return true;
    }

    public static Map sortHashMap(Map map) {
        List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
            //升序排序
            public int compare(Entry<String, String> o1,
                               Entry<String, String> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }

        });
        return map;
    }

    /**
     * Formats the given number according to how vault would like it. E.g. $50 or 5
     * dollars.
     *
     * @return The formatted string.
     */
    public static String format(double n) {
        if (plugin.getConfig().getBoolean("shop.disable-vault-format")) {
            return plugin.getConfig().getString("shop.alternate-currency-symbol") + n;
        }
        try {
            String formated = plugin.getEcon().format(n);
            if (formated == null || formated.isEmpty()) {
                Util.debugLog("Use alternate-currency-symbol to formatting, Cause economy plugin returned null");
                return plugin.getConfig().getString("shop.alternate-currency-symbol") + n;
            } else {
                return formated;
            }
        } catch (NumberFormatException e) {
            Util.debugLog("format", e.getMessage());
            Util.debugLog("format", "Use alternate-currency-symbol to formatting, Cause NumberFormatException");
            return plugin.getConfig().getString("shop.alternate-currency-symbol") + n;
            // return "$" + n;
        }
    }

    /**
     * @param m The material to check if it is blacklisted
     * @return true if the material is black listed. False if not.
     */
    public static boolean isBlacklisted(Material m) {
        return blacklist.contains(m);
    }

    /**
     * Fetches the block which the given sign is attached to
     *
     * @param sign The sign which is attached
     * @return The block the sign is attached to
     */
    public static Block getAttached(Block b) {
        try {
            if (b.getBlockData() instanceof Directional) {
                Directional directional = (Directional) b.getBlockData();
                return b.getRelative(directional.getFacing().getOppositeFace());
            } else {
                return null;
            }
            // sometimes??
        } catch (NullPointerException | ClassCastException e) {
            Util.debugLog("Exception caughted: " + e.getMessage());
            return null; // /Not sure what causes this.
        }
    }

    /**
     * Counts the number of items in the given inventory where
     * Util.matches(inventory item, item) is true.
     *
     * @param inv  The inventory to search
     * @param item The ItemStack to search for
     * @return The number of items that match in this inventory.
     */
    public static int countItems(Inventory inv, ItemStack item) {
        int items = 0;
        for (ItemStack iStack : inv.getContents()) {
            if (iStack == null)
                continue;
            if (Util.matches(item, iStack)) {
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
    public static int countSpace(Inventory inv, ItemStack item) {
        int space = 0;

        try {
            ItemStack[] contents = (ItemStack[]) storageContents.invoke(inv);
            for (ItemStack iStack : contents) {
                if (iStack == null || iStack.getType() == Material.AIR) {
                    space += item.getMaxStackSize();
                } else if (matches(item, iStack)) {
                    space += item.getMaxStackSize() - iStack.getAmount();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return space;
    }

    /**
     * Check a material is or not a WALL_SIGN
     *
     * @param material
     * @return
     */
    public static boolean isWallSign(Material material) {
        try {
            if (Tag.WALL_SIGNS.isTagged(material))
                return true;
        } catch (Throwable e) {
            if (material.name().endsWith("WALL_SIGN") || material.name().equals("WALL_SIGN"))
                return true;
        }
        //if (material.name().endsWith("WALL_SIGN") || material.name().equals("WALL_SIGN"))
        //    return true;
        return false;
    }

    /**
     * Returns true if the given location is loaded or not.
     *
     * @param loc The location
     * @return true if the given location is loaded or not.
     */
    public static boolean isLoaded(Location loc) {
        // plugin.getLogger().log(Level.WARNING, "Checking isLoaded(Location loc)");
        if (loc.getWorld() == null) {
            // plugin.getLogger().log(Level.WARNING, "Is not loaded. (No world)");
            return false;
        }
        // Calculate the chunks coordinates. These are 1,2,3 for each chunk, NOT
        // location rounded to the nearest 16.
        int x = (int) Math.floor((loc.getBlockX()) / 16.0);
        int z = (int) Math.floor((loc.getBlockZ()) / 16.0);
        if (loc.getWorld().isChunkLoaded(x, z)) {
            // plugin.getLogger().log(Level.WARNING, "Chunk is loaded " + x + ", " + z);
            return true;
        } else {
            // plugin.getLogger().log(Level.WARNING, "Chunk is NOT loaded " + x + ", " + z);
            return false;
        }
    }

    /**
     * Use yaw to calc the BlockFace
     *
     * @param float yaw
     * @return BlockFace blockFace
     */
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
     * Get this class available or not
     *
     * @param String qualifiedName
     * @return boolean Available
     */
    public static boolean isClassAvailable(String qualifiedName) {
        try {
            Class.forName(qualifiedName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Send a message for all online Ops.
     *
     * @param String message
     * @return
     */
    public static void sendMessageToOps(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp() || player.hasPermission("quickshop.alert")) {
                player.sendMessage(message);
            }
        }
    }

    //Use NMS

    /**
     * Send the ItemPreview chat msg by NMS.
     *
     * @param shop
     * @param itemStack
     * @param player
     * @param normalText The text you will see
     */
    public static void sendItemholochat(Shop shop, ItemStack itemStack, Player player, String normalText) {
        try {
            String json = ItemNMS.saveJsonfromNMS(itemStack);
            if (json == null)
                return;
            TextComponent normalmessage = new TextComponent(normalText + "   " + MsgUtil.getMessage("menu.preview"));
            ComponentBuilder cBuilder = new ComponentBuilder(json);
            if (player.hasPermission("quickshop.preview"))
                normalmessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, MsgUtil
                        .getMessage("menu.commands.preview", shop.getLocation().getWorld().getName(), String
                                .valueOf(shop.getLocation().getBlockX()), String.valueOf(shop.getLocation().getBlockY()), String
                                .valueOf(shop.getLocation().getBlockZ()))));
            normalmessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, cBuilder.create()));
            player.spigot().sendMessage(normalmessage);
        } catch (Throwable t) {
            sendItemholochatAsNormaly(itemStack, player, normalText);
        }
    }

    /**
     * Send the ItemPreview chat msg by Bukkit API.
     * More worst than NMS mode.
     * *STILL WIP*
     *
     * @param shop
     * @param itemStack
     * @param player
     * @param normalText The text you will see
     */
    // Without NMS
    public static void sendItemholochatAsNormaly(ItemStack itemStack, Player player, String normalText) {
        try {
            String Itemname = null;
            List<String> Itemlore = new ArrayList<>();
            String finalItemdata = null;
            Map<Enchantment, Integer> enchs = new HashMap<Enchantment, Integer>();
            Map<String, Integer> Itemenchs = new HashMap<String, Integer>();
            if (itemStack.hasItemMeta()) {
                ItemMeta iMeta = itemStack.getItemMeta();
                if (iMeta.hasDisplayName()) {
                    Itemname = iMeta.getDisplayName();
                } else {
                    Itemname = MsgUtil.getItemi18n(itemStack.getType().name());
                }
                if (iMeta.hasLore()) {
                    Itemlore = iMeta.getLore();
                } else {
                    Itemlore = new ArrayList<String>();
                }
                if (iMeta.hasEnchants()) {
                    enchs = iMeta.getEnchants();
                    for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
                        String i18n = MsgUtil.getEnchi18n(entries.getKey());
                        if (i18n != null) {
                            Itemenchs.put(i18n, entries.getValue());
                        } else {
                            Itemenchs = null;
                        }
                    }
                }
            } else {
                Itemname = MsgUtil.getDisplayName(itemStack);
                Itemlore = null;
                Itemenchs = null;
            }
            if (Itemname != MsgUtil.getItemi18n(itemStack.getType().name())) {
                finalItemdata = Itemname + " " + ChatColor.GRAY + "(" + MsgUtil.getItemi18n(itemStack.getType()
                        .name()) + ChatColor.GRAY + ")";
            } else {
                finalItemdata = Itemname;
            }

            finalItemdata += "\n";
            List<String> a = new ArrayList<>();
            List<Integer> b = new ArrayList<>();
            a.addAll(Itemenchs.keySet());
            b.addAll(Itemenchs.values());
            for (int i = 0; i < a.size(); i++) {
                finalItemdata += ChatColor.GRAY + a.get(i) + " " + Util.formatEnchLevel(b.get(i)) + "\n";
            }

            String potionResult = getPotiondata(itemStack);
            if (potionResult != null) {
                finalItemdata += potionResult;
            }

            if (Itemlore != null) {
                for (String string : Itemlore) {
                    finalItemdata += ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + string + "\n";
                }
            }
            TextComponent normalmessage = new TextComponent(normalText + "   " + MsgUtil.getMessage("menu.preview"));
            ComponentBuilder cBuilder = new ComponentBuilder(finalItemdata);
            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, cBuilder.create());
            normalmessage.setHoverEvent(he);
            player.spigot().sendMessage(normalmessage);
        } catch (Exception e) {
            player.sendMessage(normalText);
            QuickShop.instance.getLogger()
                    .severe("QuickShop cannot send Advanced chat message, Are you using CraftBukkit? Please use Spigot or SpigotFork.");
        }
    }

    /**
     * Format ench level.
     *
     * @param level
     * @return
     */
    private static String formatEnchLevel(Integer level) {
        switch (level) {
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
            default:
                return String.valueOf(level);

        }
    }

    /**
     * @param iStack
     */
    public static String getPotiondata(ItemStack iStack) {
        if ((iStack.getType() != Material.POTION) == true && (iStack.getType() != Material.LINGERING_POTION) == true && (iStack
                .getType() != Material.SPLASH_POTION) == true) {
            return null;
        }
        List<String> pEffects = new ArrayList<String>();
        PotionMeta pMeta = (PotionMeta) iStack.getItemMeta();
        if (pMeta.getBasePotionData().getType() != null) {
            if (!(pMeta.getBasePotionData().isUpgraded())) {
                pEffects.add(ChatColor.BLUE + MsgUtil.getPotioni18n(pMeta.getBasePotionData().getType().getEffectType()));
            } else {
                pEffects.add(ChatColor.BLUE + MsgUtil.getPotioni18n(pMeta.getBasePotionData().getType().getEffectType()) + " II");
            }

        }
        if (pMeta.hasCustomEffects()) {
            List<PotionEffect> cEffects = pMeta.getCustomEffects();
            for (PotionEffect potionEffect : cEffects) {
                pEffects.add(MsgUtil.getPotioni18n(potionEffect.getType()) + " " + formatEnchLevel(potionEffect.getAmplifier()));
            }
        }
        if (pEffects != null && pEffects.isEmpty() == false) {
            String result = new String();
            for (String effectString : pEffects) {
                result += effectString + "\n";
            }
            return result;
        } else {
            return null;
        }
    }

    /**
     * Send warning message when some plugin calling deprecated method...
     * With the trace.
     */
    public static void sendDeprecatedMethodWarn() {
        QuickShop.instance.getLogger().warning("Some plugin calling Deprecated method, Please contact author to use new api!");
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            QuickShop.instance.getLogger().warning("at " + stackTraceElement.getClassName() + "#" + stackTraceElement
                    .getMethodName() + " (" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")");
        }
    }

    /**
     * Check QuickShop is running on dev mode or not.
     *
     * @return DevMode status
     */
    public static boolean isDevEdition() {
        String version = QuickShop.instance.getDescription().getVersion().toLowerCase();
        if (version.contains("dev") | version.contains("develop") | version.contains("alpha") | version.contains("beta") | version
                .contains("test") | version.contains("snapshot") | version.contains("preview")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Call this to check items in inventory and remove it.
     */
    public static void inventoryCheck(Inventory inv) {
        try {
            for (int i = 0; i < inv.getSize(); i++)
                if (DisplayItem.checkShopItem(inv.getItem(i))) {
                    // Found Item and remove it.
                    inv.setItem(i, new ItemStack(Material.AIR, 0));
                    Util.debugLog("Found displayitem in inventory, removed.");
                }
        } catch (Throwable t) {
        }

    }

    private static Object serverInstance;
    private static Field tpsField;

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

    private static Class<?> getNMSClass(String className) {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String version = name.substring(name.lastIndexOf('.') + 1);
        try {
            return Class.forName("net.minecraft.server." + version + "." + className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Print debug log when plugin running on dev mode.
     *
     * @param String logs
     */
    public static void debugLog(String... logs) {
        if (!devMode)
            return;
        String className = Thread.currentThread().getStackTrace()[2].getClassName();
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        int codeLine = Thread.currentThread().getStackTrace()[2].getLineNumber();
        for (String log : logs) {
            plugin.getLogger().info("[DEBUG] [" + className + "]" + " [" + methodName + "] (" + codeLine + ") " + log);
        }
    }

    /**
     * Create a Timer and return this timer's UUID
     * Time Unit: ms
     *
     * @return The timer UUID.
     */
    public static UUID setTimer() {
        UUID random = UUID.randomUUID();
        timerMap.put(random, System.currentTimeMillis());
        return random;
    }

    /**
     * Return how long time running when timer set. THIS NOT WILL DESTORY AND STOP THE TIMER
     *
     * @param UUID timer's uuid
     * @return long time
     */
    public static long getTimer(UUID uuid) {
        return System.currentTimeMillis() - timerMap.get(uuid);
    }

    /**
     * Return how long time running when timer set and destory the timer.
     *
     * @param String logs
     * @return long time
     */
    public static long endTimer(UUID uuid) {
        long time = System.currentTimeMillis() - timerMap.get(uuid);
        timerMap.remove(uuid);
        return time;
    }

    /**
     * Get how many shop in the target world.
     *
     * @param worldName Target world.
     * @return The shops.
     */
    public static int getShopsInWorld(String worldName) {
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
     * Read the file to the String
     *
     * @param fileName Target file.
     * @return Target file's content.
     */
    public static String readToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
        plugin.getLogger().warning("QuickShop can't found any useable sign material, report to author!");
        return null;
    }

    /**
     * Read the InputStream to the byte array.
     *
     * @param filePath Target file
     * @return Byte array
     */
    public static byte[] inputStream2ByteArray(String filePath) {
        try {
            InputStream in = new FileInputStream(filePath);
            byte[] data = toByteArray(in);
            in.close();
            return data;
        } catch (IOException e) {
            return null;
        }

    }

    private static byte[] toByteArray(InputStream in) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n = 0;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }

    /**
     * Backup shops.db
     *
     * @return The result for backup
     */
    public static boolean backupDatabase() {
        if (plugin.getDatabase().getCore() instanceof MySQLCore)
            return true; //Backup and logs by MySQL
        File sqlfile = new File(Bukkit.getPluginManager().getPlugin("QuickShop").getDataFolder(), "shop.db");
        if (!sqlfile.exists()) {
            plugin.getLogger().warning("Failed to backup! (File not found)");
            return false;
        }
        String uuid = UUID.randomUUID().toString().replaceAll("_", "");
        File bksqlfile = new File(Bukkit.getPluginManager().getPlugin("QuickShop").getDataFolder()
                .getAbsolutePath().toString() + "/shop_backup_" + uuid + ".db");
        try {
            Files.copy(sqlfile, bksqlfile);
        } catch (Exception e1) {
            e1.printStackTrace();
            plugin.getLogger().warning("Failed to backup database.");
            return false;
        }
        return true;
    }

    public static boolean isUUID(String string) {
        if (string.length() != 36 && string.length() != 32)
            return false;
        Util.debugLog("Run extra uuid checking for " + string + ". Length: " + string.length());
        try {
            UUID.fromString(string);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static void shoppablesCheck(Shop shop) {
        if (!Util.canBeShop(shop.getLocation().getBlock())) {
            Util.debugLog("This shopblock can't be shop, deleteing...");
            shop.onUnload();
            shop.delete();
            return;
        }
    }

    public static boolean isDevMode() {
        return devMode;
    }
}
