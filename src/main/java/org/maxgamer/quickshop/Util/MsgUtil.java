package org.maxgamer.quickshop.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.potion.PotionEffectType;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Database.DatabaseHelper;
import org.maxgamer.quickshop.Shop.Shop;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class MsgUtil {
	static QuickShop plugin = QuickShop.instance;
	private static YamlConfiguration messages;
	private static YamlConfiguration itemi18n;
	private static YamlConfiguration enchi18n;
	private static YamlConfiguration potioni18n;
	private static HashMap<UUID, LinkedList<String>> player_messages = new HashMap<UUID, LinkedList<String>>();
	/**
	 * Loads all the messages from messages.yml
	 */
	public static void loadCfgMessages() {
		// Load messages.yml
		File messageFile = new File(plugin.getDataFolder(), "messages.yml");
		if (!messageFile.exists()) {
			plugin.getLogger().info("Creating messages.yml");
			plugin.saveResource("messages.yml", true);
		}
		// Store it
		messages = YamlConfiguration.loadConfiguration(messageFile);
		messages.options().copyDefaults(true);
		// Load default messages
		YamlConfiguration defMessages = null;
		try {
			defMessages = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("messages.yml")));
		}catch (Exception e) {
		}
		
		messages.setDefaults(defMessages);
		// Parse colour codes
		Util.parseColours(messages);
	
		
		if(messages.getInt("language-version")==0) {
			messages.set("language-version", 1);
		}
		updateMessages(messages.getInt("language-version"));
		
		//Print language copyright infomation
		plugin.getLogger().info(messages.getString("language-author"));
		plugin.getLogger().info(messages.getString("language-contributors"));
		plugin.getLogger().info(messages.getString("language-country"));
		plugin.getLogger().info(messages.getString("language-version"));
	}

	public static void updateMessages(int selectedVersion) {
		if (selectedVersion == 1) {
			messages.set("shop-not-exist", "&cThere had no shop.");
			messages.set("controlpanel.infomation", "&aShop Control Panel:");
			messages.set("controlpanel.setowner", "&aOwner: &b{0} &e[&d&lChange&e]");
			messages.set("controlpanel.setowner-hover", "&eLooking you want changing shop and click to switch owner.");
			messages.set("controlpanel.unlimited", "&aUnlimited: {0} &e[&d&lSwitch&e]");
			messages.set("controlpanel.unlimited-hover", "&eLooking you want changing shop and click to switch enabled or disabled.");
			messages.set("controlpanel.mode-selling", "&aShop mode: &bSelling &e[&d&lSwitch&e]");
			messages.set("controlpanel.mode-selling-hover", "&eLooking you want changing shop and click to switch enabled or disabled.");
			messages.set("controlpanel.mode-buying", "&aShop mode: &bBuying &e[&d&lSwitch&e]");
			messages.set("controlpanel.mode-buying-hover", "&eLooking you want changing shop and click to switch enabled or disabled.");
			messages.set("controlpanel.price", "&aPrice: &b{0} &e[&d&lSet&e]");
			messages.set("controlpanel.price-hover", "&eLooking you want changing shop and click to set new price.");
			messages.set("controlpanel.refill", "&aRefill: Refill the shop items &e[&d&lOK&e]");
			messages.set("controlpanel.refill-hover", "&eLooking you want changing shop and click to refill.");
			messages.set("controlpanel.empty", "&aEmpty: Remove shop all items &e[&d&lOK&e]");
			messages.set("controlpanel.empty-hover", "&eLooking you want changing shop and click to clear.");
			messages.set("controlpanel.remove", "&c&l[Remove Shop]");
			messages.set("controlpanel.remove-hover", "&eClick to remove this shop.");
			messages.set("language-version", 2);
			saveMessages();
			Util.parseColours(messages);
			selectedVersion = 2;
		}
		if (selectedVersion == 2) {
			messages.set("command.no-target-given", "&cUsage: /qs export mysql|sqlite");
			messages.set("command.description.debug", "&ePrint debug infomation");
			messages.set("no-permission-remove-shop", "&cYou do not have permission to use that command. Try break the shop instead?");
			messages.set("language-version", 3);
			saveMessages();
			Util.parseColours(messages);
			selectedVersion = 3;
		}
		
	}
	public static void saveMessages() {
		try {
			messages.save(new File(plugin.getDataFolder(), "messages.yml"));
		} catch (IOException e) {
		}
	}
	
	public static void sendControlPanelInfo(CommandSender sender, Shop shop) {
		if (!sender.hasPermission("quickshop.use")) {
			return;
		}

		if (plugin.getConfig().getBoolean("sneak-to-control"))
			if (sender instanceof Player)
				if (!((Player) sender).isSneaking())
					return;
		sender.sendMessage("");
		sender.sendMessage("");
		sender.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
		sender.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("controlpanel.infomation"));
		// Owner
		if (!sender.hasPermission("quickshop.setowner")) {
			sender.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.owner", shop.ownerName()));
		} else {
			String Text = MsgUtil.getMessage("controlpanel.setowner",shop.ownerName());
			String hoverText = MsgUtil.getMessage("controlpanel.setowner-hover");
			String clickCommand = "/qs setowner [Player]";
			TextComponent message = new TextComponent(ChatColor.DARK_PURPLE + "| " + Text);
			message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand));
			message.setHoverEvent(
					new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
			sender.spigot().sendMessage(message);
		}
		// Unlimited
		if (sender.hasPermission("quickshop.unlimited")) {
			String Text = MsgUtil.getMessage("controlpanel.unlimited", bool2String(shop.isUnlimited()));
			String hoverText = MsgUtil.getMessage("controlpanel.unlimited-hover");
			String clickCommand = "/qs unlimited";
			MsgUtil.sendPanelMessage(sender, Text, hoverText, clickCommand);
		}
		// Buying/Selling Mode
		if (sender.hasPermission("quickshop.create.buy") && sender.hasPermission("quickshop.create.sell")) {
			if (shop.isSelling()) {
				String Text = MsgUtil.getMessage("controlpanel.mode-selling");
				String hoverText = MsgUtil.getMessage("controlpanel.mode-selling-hover");
				String clickCommand = "/qs buy";
				MsgUtil.sendPanelMessage(sender, Text, hoverText, clickCommand);
			} else if (shop.isBuying()) {
				String Text = MsgUtil.getMessage("controlpanel.mode-buying");
				String hoverText = MsgUtil.getMessage("controlpanel.mode-buying-hover");
				String clickCommand = "/qs sell";
				MsgUtil.sendPanelMessage(sender, Text, hoverText, clickCommand);
			}
		}
		// Set Price
		if (sender.hasPermission("quickshop.other.price")) {
			String Text = MsgUtil.getMessage("controlpanel.price", String.valueOf(shop.getPrice()));
			String hoverText = MsgUtil.getMessage("controlpanel.mode-buying-hover");
			String clickCommand = "/qs price [New Price]";
			TextComponent message = new TextComponent(ChatColor.DARK_PURPLE + "| " + Text);
			message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand));
			message.setHoverEvent(
					new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
			sender.spigot().sendMessage(message);
		}
		// Refill
		if (sender.hasPermission("quickshop.refill")) {
			String Text = MsgUtil.getMessage("controlpanel.refill", String.valueOf(shop.getPrice()));
			String hoverText = MsgUtil.getMessage("controlpanel.refill-hover");
			String clickCommand = "/qs refill [Amount]";
			TextComponent message = new TextComponent(ChatColor.DARK_PURPLE + "| " + Text);
			message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand));
			message.setHoverEvent(
					new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
			sender.spigot().sendMessage(message);
		}
		// Refill
		if (sender.hasPermission("quickshop.empty")) {
			String Text = MsgUtil.getMessage("controlpanel.empty", String.valueOf(shop.getPrice()));
			String hoverText = MsgUtil.getMessage("controlpanel.empty-hover");
			String clickCommand = "/qs empty";
			MsgUtil.sendPanelMessage(sender, Text, hoverText, clickCommand);
		}
		// Remove
		if (sender.hasPermission("quickshop.other.destroy") || shop.getOwner().equals(((Player)sender).getUniqueId())) {
			String Text = MsgUtil.getMessage("controlpanel.remove", String.valueOf(shop.getPrice()));
			String hoverText = MsgUtil.getMessage("controlpanel.remove-hover");
			String clickCommand = "/qs remove "+shop.getLocation().getBlockX()+" "+shop.getLocation().getBlockY()+" "+shop.getLocation().getBlockZ();
			MsgUtil.sendPanelMessage(sender, Text, hoverText, clickCommand);
		}

		sender.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
	}
	public static void sendPanelMessage(CommandSender sender, String Text,String hoverText, String clickCommand) {
		TextComponent message = new TextComponent(ChatColor.DARK_PURPLE + "| " + Text);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand));
        message.setHoverEvent(new HoverEvent (HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
        sender.spigot().sendMessage(message);
	}
	public static String bool2String(boolean bool) {
		if(bool) {
			return ChatColor.GREEN + "✔";
		}else {
			return ChatColor.RED + "✘";
		}
	}

	/**
	 * loads all player purchase messages from the database.
	 */
	public static void loadTransactionMessages() {
		player_messages.clear(); // Delete old messages
		try {
			ResultSet rs = DatabaseHelper.selectAllMessages(plugin.getDB());
			while (rs.next()) {
				UUID owner = UUID.fromString(rs.getString("owner"));
				String message = rs.getString("message");
				LinkedList<String> msgs = player_messages.get(owner);
				if (msgs == null) {
					msgs = new LinkedList<String>();
					player_messages.put(owner, msgs);
				}
			msgs.add(message);
				}
		} catch (SQLException e) {
			e.printStackTrace();
			plugin.getLogger().log(Level.WARNING, "Could not load transaction messages from database. Skipping.");
		}
	}
	public static void loadItemi18n() {
		File itemi18nFile = new File(plugin.getDataFolder(), "itemi18n.yml");
		if (!itemi18nFile.exists()) {
			plugin.getLogger().info("Creating itemi18n.yml");
			plugin.saveResource("itemi18n.yml", true);
		}
		// Store it
		itemi18n = YamlConfiguration.loadConfiguration(itemi18nFile);
		itemi18n.options().copyDefaults(true);
		YamlConfiguration itemi18nYAML = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("itemi18n.yml")));
		itemi18n.setDefaults(itemi18nYAML);
		Util.parseColours(itemi18n);
		Material[] itemsi18n = Material.values();
		for (Material material : itemsi18n) {
			String itemname = itemi18n.getString("itemi18n."+material.name());
			if(itemname==null || itemname.equals("")) {
				plugin.getLogger().info("Found new items/blocks ["+material.name()+"] ,add it in config...");
				itemi18n.set("itemi18n."+material.name(), material.name());
			}
		}
		try {
			itemi18n.save(itemi18nFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			plugin.getLogger().log(Level.WARNING, "Could not load/save transaction itemname from itemi18n.yml. Skipping.");
		}
		plugin.getLogger().info("Complete to load Itemname i18n.");
	}
	public static String getItemi18n(String ItemBukkitName) {
		ItemBukkitName = ItemBukkitName.trim().replaceAll(" ", "_").toUpperCase(Locale.ROOT);
		String Itemname_i18n = null;
		try {
		Itemname_i18n = itemi18n.getString("itemi18n."+ItemBukkitName).trim();
		}catch (Exception e) {
			//e.printStackTrace();
			Itemname_i18n = null;
		}
		if(ItemBukkitName==null) {
			return "";
		}
		if(Itemname_i18n==null) {
			String material = null;
			try {
			material =  Material.matchMaterial(ItemBukkitName).name();
			}catch (Exception e) {
				material = "ERROR";
			}
			return material;
		}else {
			return Itemname_i18n;
		}
	}

	@SuppressWarnings("deprecation")
	public static void loadEnchi18n() {
		plugin.getLogger().info("Starting loading Enchantment i18n...");
		File enchi18nFile = new File(plugin.getDataFolder(), "enchi18n.yml");
		if (!enchi18nFile.exists()) {
			plugin.getLogger().info("Creating enchi18n.yml");
			plugin.saveResource("enchi18n.yml", true);
		}
		// Store it
	    enchi18n = YamlConfiguration.loadConfiguration(enchi18nFile);
		enchi18n.options().copyDefaults(true);
		YamlConfiguration enchi18nYAML = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("enchi18n.yml")));
		enchi18n.setDefaults(enchi18nYAML);
		Util.parseColours(enchi18n);
		Enchantment[] enchsi18n = Enchantment.values();
		for (Enchantment ench : enchsi18n) {
			try{
			String enchname = enchi18n.getString("enchi18n."+ench.getKey().getKey().toString().trim());
			if(enchname==null || enchname.equals("")) {
				plugin.getLogger().info("Found new ench ["+ench.getKey().getKey().toString()+"] ,add it in config...");
				enchi18n.set("enchi18n."+ench.getKey().getKey().toString().trim(),ench.getKey().getKey().toString().trim());
			}
			//for old  minecraft version
			}catch(Throwable e){
				String enchname = enchi18n.getString("enchi18n."+ench.getName().trim());
				if(enchname==null || enchname.equals("")) {
					plugin.getLogger().info("Found new ench ["+ench.getName()+"] ,add it in config...");
					enchi18n.set("enchi18n."+ench.getName().trim(),ench.getName().trim());
				}
			}
		}
		try {
			enchi18n.save(enchi18nFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			plugin.getLogger().log(Level.WARNING, "Could not load/save transaction enchname from enchi18n.yml. Skipping.");
		}
		plugin.getLogger().info("Complete to load enchname i18n.");
	}
	@SuppressWarnings("deprecation")
	public static String getEnchi18n(Enchantment key) {
		if(key==null) {
			return "ERROR";
		}
		String EnchString = null;
		//support for legacy
		try {
			EnchString = key.getKey().getKey().toString().trim();
		}catch(Throwable e){
			EnchString = key.getName().trim();
		}
		String Ench_i18n = null;
		try {
			Ench_i18n = enchi18n.getString("enchi18n."+EnchString);
		}catch (Exception e) {
			e.printStackTrace();
			Ench_i18n = null;
		}
		if(Ench_i18n==null) {
			return EnchString;
		}else {
			return Ench_i18n;
		}
	}
	
	
	public static void loadPotioni18n() {
		plugin.getLogger().info("Starting loading Potion i18n...");
		File potioni18nFile = new File(plugin.getDataFolder(), "potioni18n.yml");
		if (!potioni18nFile.exists()) {
			plugin.getLogger().info("Creating potioni18n.yml");
			plugin.saveResource("potioni18n.yml", true);
		}
		// Store it
	    potioni18n = YamlConfiguration.loadConfiguration(potioni18nFile);
	    potioni18n.options().copyDefaults(true);
		YamlConfiguration potioni18nYAML = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("potioni18n.yml")));
		potioni18n.setDefaults(potioni18nYAML);
		Util.parseColours(potioni18n);
		PotionEffectType[] potionsi18n = PotionEffectType.values();
		for (PotionEffectType potion : potionsi18n) {
			if(potion!=null) {
				String potionname = potioni18n.getString("potioni18n."+potion.getName().trim());
				if(potionname == null || potionname.equals("")) {
					plugin.getLogger().info("Found new potion ["+potion.getName()+"] ,add it in config...");
					potioni18n.set("potioni18n."+potion.getName().trim(),potion.getName().trim());
				}
			}	
		}
		try {
			potioni18n.save(potioni18nFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			plugin.getLogger().log(Level.WARNING, "Could not load/save transaction potionname from potioni18n.yml. Skipping.");
		}
		plugin.getLogger().info("Complete to load potionname i18n.");
	}
	public static String getPotioni18n(PotionEffectType potion) {
		if(potion==null) {
			return "ERROR";
		}
		String PotionString = potion.getName().trim();
		String Potion_i18n = null;
		try {
			Potion_i18n = potioni18n.getString("potioni18n."+PotionString);
		}catch (Exception e) {
			e.printStackTrace();
			Potion_i18n = null;
		}
		if(Potion_i18n==null) {
			return PotionString;
		}else {
			return Potion_i18n;
		}
	}
	
	
	/**
	 * @param player
	 *            The name of the player to message
	 * @param message
	 *            The message to send them Sends the given player a message if
	 *            they're online. Else, if they're not online, queues it for
	 *            them in the database.
	 */
	public static void send(UUID player, String message) {	//TODO Converted to UUID
		OfflinePlayer p = Bukkit.getOfflinePlayer(player);
		if (p == null || !p.isOnline()) {
			LinkedList<String> msgs = player_messages.get(player);
			if (msgs == null) {
				msgs = new LinkedList<String>();
				player_messages.put(player, msgs);
			}
			msgs.add(message);
			String q = "INSERT INTO messages (owner, message, time) VALUES (?, ?, ?)";
			plugin.getDB().execute(q, player.toString(), message, System.currentTimeMillis());
		} else {
			p.getPlayer().sendMessage(message);
		}
	}

	/**
	 * Deletes any messages that are older than a week in the database, to save
	 * on space.
	 */
	public static void clean() {
		plugin.getLogger().log(Level.WARNING, "Cleaning purchase messages from database that are over a week old...");
		// 604800,000 msec = 1 week.
		long weekAgo = System.currentTimeMillis() - 604800000;
		plugin.getDB().execute("DELETE FROM messages WHERE time < ?", weekAgo);
	}

	/**
	 * Empties the queue of messages a player has and sends them to the player.
	 * 
	 * @param p
	 *            The player to message
	 * @return true if success, false if the player is offline or null
	 */
	public static boolean flush(OfflinePlayer p) {	//TODO Changed to UUID
		if (p != null && p.isOnline()) {
			UUID pName = p.getUniqueId();
			LinkedList<String> msgs = player_messages.get(pName);
			if (msgs != null) {
				for (String msg : msgs) {
					p.getPlayer().sendMessage(msg);
				}
				plugin.getDB().execute("DELETE FROM messages WHERE owner = ?", pName.toString());
				msgs.clear();
			}
			return true;
		}
		return false;
	}

	public static void sendShopInfo(Player p, Shop shop) {
		// Potentially faster with an array?
		ItemStack items = shop.getItem();
		p.sendMessage("");
		p.sendMessage("");
		p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
		p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.shop-information"));
		p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.owner", shop.ownerName()));
		//p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.item", MsgUtil.getItemi18n(shop.getDataName())));
		//Enabled
		Util.sendItemholochat(shop.getItem(),p,ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.item", MsgUtil.getDisplayName(shop.getItem())));
		
//		if (NMS.isPotion(items.getType())) {
//			String effects = CustomPotionsName.getEffects(items);
//			if (!effects.isEmpty()) {
//				p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.effects", effects));
//			}
//		}
		if (Util.isTool(items.getType())) {
			p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.damage-percent-remaining", Util.getToolPercentage(items)));
		}
		if (shop.isSelling()) {
			p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.stock", "" + shop.getRemainingStock()));
		} else {
			p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.space", "" + shop.getRemainingSpace()));
		}
		p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.price-per", MsgUtil.getItemi18n(shop.getDataName()),Util.format(shop.getPrice())));
		if (shop.isBuying()) {
			p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.this-shop-is-buying"));
		} else {
			p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.this-shop-is-selling"));
		}
		Map<Enchantment, Integer> enchs = items.getItemMeta().getEnchants();
		if (enchs != null && !enchs.isEmpty()) {
			p.sendMessage(ChatColor.DARK_PURPLE + "+--------------------" + MsgUtil.getMessage("menu.enchants") + "-----------------------+");
			for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
				p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + MsgUtil.getEnchi18n(entries.getKey()) + " " + entries.getValue());
			}
		}
		try {
			Class.forName("org.bukkit.inventory.meta.EnchantmentStorageMeta");
			if (items.getItemMeta() instanceof EnchantmentStorageMeta) {
				EnchantmentStorageMeta stor = (EnchantmentStorageMeta) items.getItemMeta();
				stor.getStoredEnchants();
				enchs = stor.getStoredEnchants();
				if (enchs != null && !enchs.isEmpty()) {
					p.sendMessage(ChatColor.DARK_PURPLE + "+-----------------" + MsgUtil.getMessage("menu.stored-enchants") + "--------------------+");
					for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
						p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + MsgUtil.getEnchi18n(entries.getKey()) + " " + entries.getValue());
					}
				}
			}
		} catch (ClassNotFoundException e) {
			// They don't have an up to date enough build of CB to do this.
			// TODO: Remove this when it becomes redundant
		}
		p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
	}

	public static void sendPurchaseSuccess(Player p, Shop shop, int amount) {
		p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
		p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.successful-purchase"));
		p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.item-name-and-price", "" + amount, MsgUtil.getDisplayName(shop.getItem()), Util.format((amount * shop.getPrice()))));
		Map<Enchantment, Integer> enchs = shop.getItem().getItemMeta().getEnchants();
		if (enchs != null && !enchs.isEmpty()) {
			p.sendMessage(ChatColor.DARK_PURPLE + "+--------------------" + MsgUtil.getMessage("menu.enchants") + "-----------------------+");
			for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
				p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW +MsgUtil.getEnchi18n(entries.getKey()));
			}
		}
		enchs = shop.getItem().getItemMeta().getEnchants();
		if (enchs != null && !enchs.isEmpty()) {
			p.sendMessage(ChatColor.DARK_PURPLE + "+-----------------" + MsgUtil.getMessage("menu.stored-enchants") + "--------------------+");
			for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
				p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + MsgUtil.getEnchi18n(entries.getKey()) + " " + entries.getValue());
			}
		}
		try {
			Class.forName("org.bukkit.inventory.meta.EnchantmentStorageMeta");
			if (shop.getItem().getItemMeta() instanceof EnchantmentStorageMeta) {
				EnchantmentStorageMeta stor = (EnchantmentStorageMeta) shop.getItem().getItemMeta();
				stor.getStoredEnchants();
				enchs = stor.getStoredEnchants();
				if (enchs != null && !enchs.isEmpty()) {
					p.sendMessage(ChatColor.DARK_PURPLE + "+-----------------" + MsgUtil.getMessage("menu.stored-enchants") + "--------------------+");
					for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
						p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW +MsgUtil.getEnchi18n(entries.getKey()));
					}
				}
			}
		} catch (ClassNotFoundException e) {
			// They don't have an up to date enough build of CB to do this.
			// TODO: Remove this when it becomes redundant
		}
		p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
	}

	public static void sendSellSuccess(Player p, Shop shop, int amount) {
		p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
		p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.successfully-sold"));
		p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.item-name-and-price", "" + amount, MsgUtil.getDisplayName(shop.getItem()), Util.format((amount * shop.getPrice()))));
		if (plugin.getConfig().getBoolean("show-tax")) {
			double tax = plugin.getConfig().getDouble("tax");
			double total = amount * shop.getPrice();
			if (tax != 0) {
				if (!p.getUniqueId().equals(shop.getOwner())) {
					p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.sell-tax", "" + Util.format((tax * total))));
				} else {
					p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.sell-tax-self"));
				}
			}
		}
		Map<Enchantment, Integer> enchs = shop.getItem().getItemMeta().getEnchants();
		if (enchs != null && !enchs.isEmpty()) {
			p.sendMessage(ChatColor.DARK_PURPLE + "+--------------------" + MsgUtil.getMessage("menu.enchants") + "-----------------------+");
			for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
				p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW +MsgUtil.getEnchi18n(entries.getKey()));
			}
		}
		try {
			Class.forName("org.bukkit.inventory.meta.EnchantmentStorageMeta");
			if (shop.getItem().getItemMeta() instanceof EnchantmentStorageMeta) {
				EnchantmentStorageMeta stor = (EnchantmentStorageMeta) shop.getItem().getItemMeta();
				stor.getStoredEnchants();
				enchs = stor.getStoredEnchants();
				if (enchs != null && !enchs.isEmpty()) {
					p.sendMessage(ChatColor.DARK_PURPLE + "+--------------------" + MsgUtil.getMessage("menu.stored-enchants") + "-----------------------+");
					for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
						p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW +MsgUtil.getEnchi18n(entries.getKey()));
					}
				}
			}
		} catch (ClassNotFoundException e) {
			// They don't have an up to date enough build of CB to do this.
			// TODO: Remove this when it becomes redundant
		}
		p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
	}
	public static String getDisplayName(ItemStack iStack){
		ItemStack is = iStack.clone();
		
		if(is.hasItemMeta()&&is.getItemMeta().hasDisplayName()) {
			return is.getItemMeta().getDisplayName();
		}else {
			return MsgUtil.getItemi18n(is.getType().name());
		}
		
	}
	public static String getMessage(String loc, String... args) {
		String raw = messages.getString(loc);
		if (raw == null) {
			return "Invalid message: " + loc+" Please update your messages.yml";
			
			
		}
		if (raw.isEmpty()) {
			return "";
		}
		if (args == null) {
			return raw;
		}
		for (int i = 0; i < args.length; i++) {
			raw = raw.replace("{" + i + "}", args[i]==null ? "null" : args[i]);
		}
		return raw;
	}
}
