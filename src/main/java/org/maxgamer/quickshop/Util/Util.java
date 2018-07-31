package org.maxgamer.quickshop.Util;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Sign;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;

@SuppressWarnings("deprecation")
public class Util {
	private static HashSet<Material> tools = new HashSet<Material>();
	private static HashSet<Material> blacklist = new HashSet<Material>();
	private static HashSet<Material> shoppables = new HashSet<Material>();
	private static Map<Material, Entry<Double,Double>> restrictedPrices = new HashMap<Material, Entry<Double,Double>>();
	private static QuickShop plugin;
	private static Method storageContents;

	public static void initialize() {
		tools.clear();
		blacklist.clear();
		shoppables.clear();
		restrictedPrices.clear();

		plugin = QuickShop.instance;
		for (String s : plugin.getConfig().getStringList("shop-blocks")) {
			Material mat = Material.getMaterial(s.toUpperCase());
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
		tools.add(Material.BOW);
		tools.add(Material.SHEARS);
		tools.add(Material.FISHING_ROD);
		tools.add(Material.FLINT_AND_STEEL);
		tools.add(Material.CHAINMAIL_BOOTS);
		tools.add(Material.CHAINMAIL_CHESTPLATE);
		tools.add(Material.CHAINMAIL_HELMET);
		tools.add(Material.CHAINMAIL_LEGGINGS);
		tools.add(Material.WOODEN_AXE);
		tools.add(Material.WOODEN_HOE);
		tools.add(Material.WOODEN_PICKAXE);
		tools.add(Material.WOODEN_SHOVEL);
		tools.add(Material.WOODEN_SWORD);
		tools.add(Material.LEATHER_BOOTS);
		tools.add(Material.LEATHER_CHESTPLATE);
		tools.add(Material.LEATHER_HELMET);
		tools.add(Material.LEATHER_LEGGINGS);
		tools.add(Material.DIAMOND_AXE);
		tools.add(Material.DIAMOND_HOE);
		tools.add(Material.DIAMOND_PICKAXE);
		tools.add(Material.DIAMOND_SHOVEL);
		tools.add(Material.DIAMOND_SWORD);
		tools.add(Material.DIAMOND_BOOTS);
		tools.add(Material.DIAMOND_CHESTPLATE);
		tools.add(Material.DIAMOND_HELMET);
		tools.add(Material.DIAMOND_LEGGINGS);
		tools.add(Material.STONE_AXE);
		tools.add(Material.STONE_HOE);
		tools.add(Material.STONE_PICKAXE);
		tools.add(Material.STONE_SHOVEL);
		tools.add(Material.STONE_SWORD);
		tools.add(Material.GOLDEN_AXE);
		tools.add(Material.GOLDEN_HOE);
		tools.add(Material.GOLDEN_PICKAXE);
		tools.add(Material.GOLDEN_SHOVEL);
		tools.add(Material.GOLDEN_SWORD);
		tools.add(Material.GOLDEN_BOOTS);
		tools.add(Material.GOLDEN_CHESTPLATE);
		tools.add(Material.GOLDEN_HELMET);
		tools.add(Material.GOLDEN_LEGGINGS);
		tools.add(Material.IRON_AXE);
		tools.add(Material.IRON_HOE);
		tools.add(Material.IRON_PICKAXE);
		tools.add(Material.IRON_SHOVEL);
		tools.add(Material.IRON_SWORD);
		tools.add(Material.IRON_BOOTS);
		tools.add(Material.IRON_CHESTPLATE);
		tools.add(Material.IRON_HELMET);
		tools.add(Material.IRON_LEGGINGS);
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

	public static boolean isTransparent(Material m) {
		boolean trans = m.isTransparent();
		return trans;
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

	/**
	 * Returns true if the given block could be used to make a shop out of.
	 * 
	 * @param b
	 *            The block to check. Possibly a chest, dispenser, etc.
	 * @return True if it can be made into a shop, otherwise false.
	 */
	public static boolean canBeShop(Block b) {
		BlockState bs = b.getState();
		if (bs instanceof InventoryHolder == false)
			return false;
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
		double dura = item.getDurability();
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
	public static Block getSecondHalf(Block b) {
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
	 * Converts a string into an item from the database.
	 * 
	 * @param itemString
	 *            The database string. Is the result of makeString(ItemStack
	 *            item).
	 * @return A new itemstack, with the properties given in the string
	 */
	public static ItemStack makeItem(String itemString) {
		String[] itemInfo = itemString.split(":");
		ItemStack item = new ItemStack(Material.getMaterial(itemInfo[0]));
		MaterialData data = new MaterialData(Material.matchMaterial(itemInfo[1]));
		item.setData(data);
		item.setDurability(Short.parseShort(itemInfo[2]));
		item.setAmount(Integer.parseInt(itemInfo[3]));
		for (int i = 4; i < itemInfo.length; i = i + 2) {
			int level = Integer.parseInt(itemInfo[i + 1]);
			Enchantment ench = Enchantment.getByName(itemInfo[i]);
			if (ench == null)
				continue; // Invalid
			if (ench.canEnchantItem(item)) {
				if (level <= 0)
					continue;
				level = Math.min(ench.getMaxLevel(), level);
				item.addEnchantment(ench, level);
			}
		}
		return item;
	}

	public static String serialize(ItemStack iStack) {
		YamlConfiguration cfg = new YamlConfiguration();
		cfg.set("item", iStack);
		return cfg.saveToString();
	}

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
		
		CustomItemNameAAA customItemName = QuickShop.instance.getCustomItemNames(itemStack);
		if (customItemName!=null) {
			return customItemName.getFullName();
		}
		
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

	// Let's make very long names shorter for our sign
	public static String getNameForSign(ItemStack itemStack) {
//		if (NMS.isPotion(itemStack.getType())) {
//			return CustomPotionsName.getSignName(itemStack);
//		}
		
		ItemStack is = itemStack.clone();
		is.setAmount(1);
		
		String name = MsgUtil.getItemi18n(itemStack.getType().name());

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
		return tools.contains(mat);
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
		if (stack1.getDurability() != stack2.getDurability())
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
	 * Formats the given number according to how vault would like it. E.g. $50
	 * or 5 dollars.
	 * 
	 * @return The formatted string.
	 */
	public static String format(double n) {
		try {
			return plugin.getEcon().format(n);
		} catch (NumberFormatException e) {
			return "$" + n;
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
			Sign sign = (Sign) b.getState().getData(); // Throws a NPE
			// sometimes??
			BlockFace attached = sign.getAttachedFace();
			if (attached == null)
				return null;
			return b.getRelative(attached);
		} catch (NullPointerException e) {
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
		return space;
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
	
	
	public static boolean isClassAvailable(String qualifiedName) {
		try {
			Class.forName(qualifiedName);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	public static void sendMessageToOps(String message) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.isOp()) {
				player.sendMessage(message);
			}
		}
	}
}
