package org.maxgamer.quickshop.Util;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
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
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.DisplayItem;
import org.maxgamer.quickshop.Shop.Shop;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author MACHENIKE
 *
 */
public class Util {
	private static final EnumSet<Material> blacklist = EnumSet.noneOf(Material.class);
	private static final EnumSet<Material> shoppables = EnumSet.noneOf(Material.class);
	private static final EnumMap<Material, Entry<Double,Double>> restrictedPrices = new EnumMap<Material, Entry<Double,Double>>(Material.class);
	private static QuickShop plugin;
	private static Method storageContents;
	static Map<UUID, Long> timerMap = new HashMap<UUID, Long>();
	protected static boolean devMode;
	public String boolean2String(boolean bool) {
		if (bool) {
			return "Enabled";
		} else {
			return "Disabled";
		}
	}
	public static void initialize() {
		blacklist.clear();
		shoppables.clear();
		restrictedPrices.clear();

		plugin = QuickShop.instance;
		devMode = plugin.getConfig().getBoolean("dev-mode");
		for (String s : plugin.getConfig().getStringList("shop-blocks")) {
			Material mat = Material.matchMaterial(s.toUpperCase());
			if (mat == null) {
				try {
					mat = Material.matchMaterial(s);
				} catch (NumberFormatException e) {
				}
			}
			if (mat == null) {
				plugin.getLogger().info("Invalid shop-block: " + s);
			} else {
				shoppables.add(mat);
			}
		}
		List<String> configBlacklist = plugin.getConfig().getStringList("blacklist");
		for (String s : configBlacklist) {
			Material mat = Material.getMaterial(s.toUpperCase());
			if (mat == null) {
				mat = Material.matchMaterial(s);
				if (mat == null) {
					plugin.getLogger().info(s + " is not a valid material.  Check your spelling or ID");
					continue;
				}
			}
			blacklist.add(mat);
		}		

		for (String s : plugin.getConfig().getStringList("price-restriction")) {
			String[] sp = s.split(";");
			if (sp.length==3) {
				try {
					Material mat = Material.matchMaterial(sp[0]);
					if (mat == null) {
						throw new Exception();
					}

					restrictedPrices.put(mat, new SimpleEntry<Double,Double>(Double.valueOf(sp[1]), Double.valueOf(sp[2])));
				} catch (Exception e) {
					plugin.getLogger().info("Invalid price restricted material: " + s);
				}
			}
		}
		
		try {
			storageContents = Inventory.class.getMethod("getStorageContents");
		} catch (Exception e) {
			try {
				storageContents = Inventory.class.getMethod("getContents");
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	/** Return an entry with min and max prices, but null if there isn't a price restriction */
	public static Entry<Double,Double> getPriceRestriction(Material material) {
		return restrictedPrices.get(material);
	}
	@SuppressWarnings("deprecation")
	public static boolean isTransparent(Material m) {
		boolean trans = m.isTransparent();
		return trans;
	}
	
	public static boolean isShoppables(Material material) {
		if(shoppables.contains(material)) {
			return true;
		}else {
			return false;
		}
			
		
	}

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
	public static String parseColours(String text) {
		text = ChatColor.translateAlternateColorCodes('&', text);
		return text;
	}
	/**
	 * Returns true if the given block could be used to make a shop out of.
	 * 
	 * @param b The block to check, Possibly a chest, dispenser, etc. player The player, can be null if onlyCheck=true, onlyCheck The option to disable check AreaShop use player.
	 * @return True if it can be made into a shop, otherwise false.
	 */
	public static boolean canBeShop(Block b,UUID player, boolean onlyCheck) {
		BlockState bs = b.getState();
		if ((bs instanceof InventoryHolder == false) && b.getState().getType() != Material.ENDER_CHEST) {
			return false;
		}
		if (b.getState().getType() == Material.ENDER_CHEST) {
			if (plugin.openInvPlugin == null) {
				Util.debugLog("OpenInv not loaded");
				return false;
			} else {
				return shoppables.contains(bs.getType());
			}
		}
		return shoppables.contains(bs.getType());

	}

	/**
	 * Gets the percentage (Without trailing %) damage on a tool.
	 * 
	 * @param item
	 *            The ItemStack of tools to check
	 * @return The percentage 'health' the tool has. (Opposite of total damage)
	 */
	public static String getToolPercentage(ItemStack item) {
		double dura = ((Damageable)item.getItemMeta()).getDamage();;
		double max = item.getType().getMaxDurability();
		DecimalFormat formatter = new DecimalFormat("0");
		return formatter.format((1 - dura / max) * 100.0);
	}

	/**
	 * Returns the chest attached to the given chest. The given block must be a
	 * chest.
	 * 
	 * @param b
	 *            The chest to check.
	 * @return the block which is also a chest and connected to b.
	 */
	/** @TODO 1.13+ compatibility */
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
	 * @param b
	 *            The chest to check.
	 * @return the block which is also a chest and connected to b.
	 */
	public static Block getSecondHalf(Block b) {
		Util.debugLog("Getting Second Half of DoubleChest at "+b.getLocation().toString());
		if(b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST){
			Util.debugLog("No matched type: "+b.getType().name());
			return null;
		}
		Chest oneSideOfChest = (Chest)b.getState();
		InventoryHolder chestHolder = oneSideOfChest.getInventory().getHolder();
		if(chestHolder instanceof DoubleChest){
			DoubleChest doubleChest = (DoubleChest)chestHolder;
			InventoryHolder left = doubleChest.getLeftSide();
			InventoryHolder right = doubleChest.getRightSide();

			Chest leftC = (Chest)left;
			Chest rightC = (Chest)right;
			Util.debugLog("Double chest match checker: ");
			Util.debugLog("Find args: "+b.getLocation().toString());
			Util.debugLog("Left: "+leftC.getLocation().toString());
			Util.debugLog("Right: "+rightC.getLocation().toString());
			if(equalsBlockStateLocation(oneSideOfChest, leftC)) {
				Util.debugLog("Clicked left chest");
				return rightC.getBlock();
			}
			if(equalsBlockStateLocation(oneSideOfChest, leftC)) {
				Util.debugLog("Clicked right chest");
				return leftC.getBlock();
			}
			Util.debugLog("No matched double chest check but pass the DoubleChest Inventory check");
			return null;
		}else{
			Util.debugLog("No matched DoubleChest: "+b.toString());
			return null;
		}
	}
	
	private static final boolean equalsBlockStateLocation(BlockState b1, BlockState b2) {
	    return b1.getX() == b2.getX() && b1.getY() == b2.getY() && b1.getZ() == b2.getZ();
	}
	
	public static boolean location3DEqual(Location loc1, Location loc2){
		if(loc1.getWorld().getName() != loc2.getWorld().getName())
			return false;
		if(loc1.getBlockX() != loc2.getBlockX())
			return false;
		if(loc1.getBlockY() != loc2.getBlockY())
			return false;
		if(loc1.getBlockZ() != loc2.getBlockZ())
			return false;
		return true;
	}
	/**
	 * Checks whether someone else's shop is within reach of a hopper being placed by a player.
	 * 
	 * @param b 
	 *            The block being placed.
	 * @param p
	 *            The player performing the action.
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
	 * @param itemStack
	 *            The itemstack to fetch the name of
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
	 * @param ugly
	 *            The string such as IRON_INGOT
	 * @return A nicer version, such as Iron Ingot
	 * 
	 */
	public static String prettifyText(String ugly) {
		String[] nameParts = ugly.split("_");
		if (nameParts.length==1) {
			return firstUppercase(ugly);
		}

		StringBuilder sb=new StringBuilder();
		for (String part : nameParts) {
			sb.append(firstUppercase(part)+" ");
		}

		return sb.toString();
	}
	/**
	 * Get item's sign name for display on the sign.
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
		
		if(is.hasItemMeta()) {
			if(is.getItemMeta().hasDisplayName()) {
				return is.getItemMeta().getDisplayName();
			}
		}
		
		String name = MsgUtil.getItemi18n(itemStack.getType().name()).trim();

		String[] nameParts = name.split("_");
		if (nameParts.length==1) {
			return firstUppercase(nameParts[0]);
		}

		for (int i=0; i<nameParts.length-1; i++) {
			int length = StringUtils.join(nameParts).length();
			if (length>16) {
				nameParts[i] = nameParts[i].substring(0, 1)+".";
			} else {
				nameParts[i] = firstUppercase(nameParts[i]);
			}
		}

		nameParts[nameParts.length-1] = firstUppercase(nameParts[nameParts.length-1]);

		return StringUtils.join(nameParts);
	}

	public static String firstUppercase(String string) {
		if (string.length()>1) {
			return Character.toUpperCase(string.charAt(0))+string.substring(1).toLowerCase();
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
	 * @param n
	 *            The number to convert
	 * @return The roman numeral representation of this number, or the number in
	 *         decimal form as a string if n <= 0 || n >= 40.
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
	 * @param mat
	 *            The material to check
	 * @return Returns true if the item is a tool (Has durability) or false if
	 *         it doesn't.
	 */
	public static boolean isTool(Material mat) {
		if(mat.getMaxDurability()==0){
			return false;
		}else{
			return true;
		}
	}

	/**
	 * Compares two items to each other. Returns true if they match.
	 * 
	 * @param stack1
	 *            The first item stack
	 * @param stack2
	 *            The second item stack
	 * @return true if the itemstacks match. (Material, durability, enchants, name)
	 */
	public static boolean matches(ItemStack stack1, ItemStack stack2) {
		if (stack1 == stack2)
			return true; // Referring to the same thing, or both are null.
		if (stack1 == null || stack2 == null)
			return false; // One of them is null (Can't be both, see above)
		if (stack1.getType() != stack2.getType())
			return false; // Not the same material
		if (((Damageable)stack1.getItemMeta()).getDamage() != ((Damageable)stack2.getItemMeta()).getDamage())
			return false; // Not the same durability
		if (!stack1.getEnchantments().equals(stack2.getEnchantments()))
			return false; // They have the same enchants
		if (stack1.getItemMeta().hasDisplayName() || stack2.getItemMeta().hasDisplayName()) {
			if (stack1.getItemMeta().hasDisplayName() && stack2.getItemMeta().hasDisplayName()) {
				if (!stack1.getItemMeta().getDisplayName().equals(stack2.getItemMeta().getDisplayName())) {
					return false; // items have different display name
				}
			} else {
				return false; // one of the item stacks have a display name
			}
		}
		try {
			Class.forName("org.bukkit.inventory.meta.EnchantmentStorageMeta");
			boolean book1 = stack1.getItemMeta() instanceof EnchantmentStorageMeta;
			boolean book2 = stack2.getItemMeta() instanceof EnchantmentStorageMeta;
			if (book1 != book2)
				return false;// One has enchantment meta, the other does not.
			if (book1 == true) { // They are the same here (both true or both
				// false). So if one is true, the other is
				// true.
				Map<Enchantment, Integer> ench1 = ((EnchantmentStorageMeta) stack1.getItemMeta()).getStoredEnchants();
				Map<Enchantment, Integer> ench2 = ((EnchantmentStorageMeta) stack2.getItemMeta()).getStoredEnchants();
				if (!ench1.equals(ench2))
					return false; // Enchants aren't the same.
			}
		} catch (ClassNotFoundException e) {
			// Nothing. They dont have a build high enough to support this.
		}
		return true;
	}

	/**
	 * Formats the given number according to how vault would like it. E.g. $50 or 5
	 * dollars.
	 * 
	 * @return The formatted string.
	 */
	public static String format(double n) {
		if(plugin.getConfig().getBoolean("shop.disable-vault-format")) {
			return plugin.getConfig().getString("shop.alternate-currency-symbol") + n;
		}
		try {
			String formated = plugin.getEcon().format(n);
			if (formated == null || formated.isEmpty()) {
				Util.debugLog("Use alternate-currency-symbol cause Economy Plugin returned null");
				return plugin.getConfig().getString("shop.alternate-currency-symbol") + n;
			} else {
				return formated;
			}
		} catch (NumberFormatException e) {
			Util.debugLog("Use alternate-currency-symbol cause NumberFormatException");
			return plugin.getConfig().getString("shop.alternate-currency-symbol") + n;
			// return "$" + n;
		}
	}

	/**
	 * @param m
	 *            The material to check if it is blacklisted
	 * @return true if the material is black listed. False if not.
	 */
	public static boolean isBlacklisted(Material m) {
		return blacklist.contains(m);
	}

	/**
	 * Fetches the block which the given sign is attached to
	 * 
	 * @param sign
	 *            The sign which is attached
	 * @return The block the sign is attached to
	 */
	public static Block getAttached(Block b) {
		try {	
			if(b.getBlockData() instanceof Directional) {
				Directional directional = (Directional) b.getBlockData();
				return b.getRelative(directional.getFacing().getOppositeFace());
			}else {
				debugLog("No Directionalable");
				return null;
			}
			// sometimes??
		} catch (NullPointerException|ClassCastException e) {
			debugLog("Known bug got trigged:");
			debugLog(e.getMessage());
			return null; // /Not sure what causes this.
		}
	}

	/**
	 * Counts the number of items in the given inventory where
	 * Util.matches(inventory item, item) is true.
	 * 
	 * @param inv
	 *            The inventory to search
	 * @param item
	 *            The ItemStack to search for
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
		Util.debugLog("Items: "+items);
		return items;
	}

	/**
	 * Returns the number of items that can be given to the inventory safely.
	 * 
	 * @param inv
	 *            The inventory to count
	 * @param item
	 *            The item prototype. Material, durabiltiy and enchants must
	 *            match for 'stackability' to occur.
	 * @return The number of items that can be given to the inventory safely.
	 */
	public static int countSpace(Inventory inv, ItemStack item) {
		int space = 0;
		
		try {
			ItemStack[] contents = (ItemStack[])storageContents.invoke(inv);
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
		Util.debugLog("Space:"+space);
		return space;
	}
	public static boolean isWallSign(Material material) {
		try {
		if(Tag.WALL_SIGNS.isTagged(material))
			return true;
		if(material.name().endsWith("WALL_SIGN"))
			return true;
		}catch (Throwable e) {
			if(material==Material.LEGACY_WALL_SIGN) //1.13 compatiable
				return true;
			if(material.name().equals("WALL_SIGN")) //1.13 compatiable
				return true;
			if(material.name().endsWith("WALL_SIGN"))
				return true;
		}
	}

	/**
	 * Returns true if the given location is loaded or not.
	 * 
	 * @param loc
	 *            The location
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
	public static void sendItemholochat(ItemStack itemStack, Player player, String normalText) {
	    try {
	        String json = ItemNMS.saveJsonfromNMS(itemStack);
	        if (json == null)
	            return;
	        TextComponent normalmessage = new TextComponent(normalText+"   "+MsgUtil.getMessage("menu.preview"));
	        ComponentBuilder cBuilder = new ComponentBuilder(json);
	        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_ITEM, cBuilder.create());
	        normalmessage.setHoverEvent(he);
	        player.spigot().sendMessage(normalmessage);
	    } catch (Throwable t) {
	        sendItemholochatAsNormaly(itemStack, player, normalText);
	    }
	}

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
		if(Itemname!=MsgUtil.getItemi18n(itemStack.getType().name())) {
			finalItemdata = Itemname+" "+ChatColor.GRAY+"("+MsgUtil.getItemi18n(itemStack.getType().name())+ChatColor.GRAY+")";
		}else {
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
		if(potionResult!=null) {
			finalItemdata += potionResult;
		}

		if (Itemlore != null) {
			for (String string : Itemlore) {
				finalItemdata += ChatColor.DARK_PURPLE +""+ ChatColor.ITALIC + string + "\n";
			}
		}
		TextComponent normalmessage = new TextComponent(normalText+"   "+MsgUtil.getMessage("menu.preview"));
		ComponentBuilder cBuilder = new ComponentBuilder(finalItemdata);
		HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, cBuilder.create());
		normalmessage.setHoverEvent(he);
		player.spigot().sendMessage(normalmessage);
		}catch (Exception e) {
			player.sendMessage(normalText);
			QuickShop.instance.getLogger().severe("QuickShop cannot send Advanced chat message, Are you using CraftBukkit? Please use Spigot or SpigotFork.");
		}
	}
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
		if((iStack.getType() != Material.POTION)==true && (iStack.getType() !=Material.LINGERING_POTION)==true && (iStack.getType() !=Material.SPLASH_POTION)==true){
			return null;
		}
		List<String> pEffects =  new ArrayList<String>();
		PotionMeta pMeta = (PotionMeta)iStack.getItemMeta();
		if(pMeta.getBasePotionData().getType()!=null) {
			if(!(pMeta.getBasePotionData().isUpgraded())){
				pEffects.add(ChatColor.BLUE+MsgUtil.getPotioni18n(pMeta.getBasePotionData().getType().getEffectType()));
			}else {
				pEffects.add(ChatColor.BLUE+MsgUtil.getPotioni18n(pMeta.getBasePotionData().getType().getEffectType())+" II");
			}
			
		}
		if(pMeta.hasCustomEffects()) {
			List<PotionEffect> cEffects = pMeta.getCustomEffects();
			for (PotionEffect potionEffect : cEffects) {
				pEffects.add(MsgUtil.getPotioni18n(potionEffect.getType())+" "+formatEnchLevel(potionEffect.getAmplifier()));
			}
		}
		if(pEffects != null && pEffects.isEmpty() == false) {
			String result = new String();
			for (String effectString : pEffects) {
				result+=effectString+"\n";
			}
			return result;
		}else {
		return null;
		} 
	}
	/**
	 * Send warning message when some plugin calling deprecated method... 
	 * @return
	 */
	public static void sendDeprecatedMethodWarn() {
		QuickShop.instance.getLogger().warning("Some plugin calling Deprecated method, Please contact author to use new api!");
	}
	/**
	 * Check QuickShop is running on dev mode or not.
	 * @return
	 */
	public static boolean isDevEdition() {
		if(QuickShop.instance.getDescription().getVersion().contains("dev")||QuickShop.instance.getDescription().getVersion().contains("alpha")||QuickShop.instance.getDescription().getVersion().contains("beta")||QuickShop.instance.getDescription().getVersion().contains("snapshot")) {
			return true;
		}else {
			return false;
		}
	}
	/**
	 * Call this to check items in inventory and remove it.
	 */
	public static void inventoryCheck(Inventory inv){
				try{
					for (int i =0; i < inv.getSize(); i++)
						if (DisplayItem.checkShopItem(inv.getItem(i))) {
							// Found Item and remove it.
							inv.setItem(i, new ItemStack(Material.AIR, 0));
							Util.debugLog("Something trying collect QuickShop displayItem, already cancelled. ("+inv.getLocation().toString()+")");
						}
				}catch (Throwable t){
				}

	}
	private static Object serverInstance;
    private static Field tpsField;
    /**
	 * Get MinecraftServer's TPS
	 * @return double TPS (e.g 19.92)
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
	 * @param String logs
	 */
	public static void debugLog(String logs)	{
		if(devMode) {
			plugin.getLogger().warning("[DEBUG] "+logs);
		}
		
	}
	/**
	 * Create a Timer and return this timer's UUID
	 * @return
	 */
	public static UUID setTimer() {
		UUID random = UUID.randomUUID();
		timerMap.put(random, System.currentTimeMillis());
		return random;
	}
	/**
	 * Return how long time running when timer set. THIS NOT WILL DESTORY AND STOP THE TIMER
	 * @param UUID timer's uuid
	 * @return long time
	 */
	public static long getTimer(UUID uuid) {
		return System.currentTimeMillis()-timerMap.get(uuid);
	}
	/**
	 * Return how long time running when timer set and destory the timer.
	 * @param String logs
	 * @return long time
	 */
	public static long endTimer(UUID uuid) {
		long time =System.currentTimeMillis()-timerMap.get(uuid);
		timerMap.remove(uuid);
		return time;
	}
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
	public static Material getSignMaterial() {

		Material signMaterial = Material.matchMaterial(plugin.getConfig().getString("shop.sign-material"));
		if(signMaterial!=null) {
			return signMaterial;
		}
		signMaterial = Material.matchMaterial("OAK_WALL_SIGN"); //Fallback default sign in 1.14
		if(signMaterial!=null) {
			return signMaterial;
		}
		signMaterial = Material.matchMaterial("WALL_SIGN"); //Fallback default sign in 1.13
		if(signMaterial!=null) {
			return signMaterial;
		}
		//What the fuck!?
		plugin.getLogger().warning("QuickShop can't found any useable sign material, report to author!");
		return null;
	}
}
