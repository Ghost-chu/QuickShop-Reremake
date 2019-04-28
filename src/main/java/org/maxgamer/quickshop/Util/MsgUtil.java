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

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.Inventory;
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
//	private static YamlConfiguration messages;
	private static YamlConfiguration itemi18n;
	private static YamlConfiguration enchi18n;
	private static YamlConfiguration potioni18n;
	private static HashMap<UUID, LinkedList<String>> player_messages = new HashMap<UUID, LinkedList<String>>();
	private static boolean Inited;
	private static YamlConfiguration messagei18n;
	static File messageFile;
	public static void loadCfgMessages(String...reload ) {
		messageFile = new File(plugin.getDataFolder(), "messages.yml");
		if (!messageFile.exists()) {
			plugin.getLogger().info("Creating messages.yml");
			plugin.getLanguage().saveFile(plugin.getLanguage().getComputerLanguage(),"messages","messages.yml");
		}
		// Store it
		messagei18n = YamlConfiguration.loadConfiguration(messageFile);
		messagei18n.options().copyDefaults(true);
		
		YamlConfiguration messagei18nYAML = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getLanguage().getFile(plugin.getLanguage().getComputerLanguage(), "messages")));
		messagei18n.setDefaults(messagei18nYAML);
		
		if(messagei18n.getInt("language-version")==0) {
			messagei18n.set("language-version", 1);
		}
		if(reload.length==0)
			try {
				updateMessages(messagei18n.getInt("language-version"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		
		//Print language copyright infomation
		
		if(!Inited) {
			plugin.getLogger().info(getMessage("translation-author"));
			plugin.getLogger().info(getMessage("translation-contributors"));
			plugin.getLogger().info(getMessage("translation-country"));
			plugin.getLogger().info(getMessage("translation-version"));
			Inited=true;
		}
		try {
			messagei18n.save(messageFile);
		} catch (IOException e) {
			e.printStackTrace();
			plugin.getLogger().log(Level.WARNING, "Could not load/save transaction from messages.yml. Skipping.");
		}
		Util.parseColours(messagei18n);
	}
	public static void updateMessages(int selectedVersion) throws IOException {
		if (selectedVersion == 1) {
			messagei18n.set("shop-not-exist", "&cThere had no shop.");
			messagei18n.set("controlpanel.infomation", "&aShop Control Panel:");
			messagei18n.set("controlpanel.setowner", "&aOwner: &b{0} &e[&d&lChange&e]");
			messagei18n.set("controlpanel.setowner-hover", "&eLooking you want changing shop and click to switch owner.");
			messagei18n.set("controlpanel.unlimited", "&aUnlimited: {0} &e[&d&lSwitch&e]");
			messagei18n.set("controlpanel.unlimited-hover", "&eLooking you want changing shop and click to switch enabled or disabled.");
			messagei18n.set("controlpanel.mode-selling", "&aShop mode: &bSelling &e[&d&lSwitch&e]");
			messagei18n.set("controlpanel.mode-selling-hover", "&eLooking you want changing shop and click to switch enabled or disabled.");
			messagei18n.set("controlpanel.mode-buying", "&aShop mode: &bBuying &e[&d&lSwitch&e]");
			messagei18n.set("controlpanel.mode-buying-hover", "&eLooking you want changing shop and click to switch enabled or disabled.");
			messagei18n.set("controlpanel.price", "&aPrice: &b{0} &e[&d&lSet&e]");
			messagei18n.set("controlpanel.price-hover", "&eLooking you want changing shop and click to set new price.");
			messagei18n.set("controlpanel.refill", "&aRefill: Refill the shop items &e[&d&lOK&e]");
			messagei18n.set("controlpanel.refill-hover", "&eLooking you want changing shop and click to refill.");
			messagei18n.set("controlpanel.empty", "&aEmpty: Remove shop all items &e[&d&lOK&e]");
			messagei18n.set("controlpanel.empty-hover", "&eLooking you want changing shop and click to clear.");
			messagei18n.set("controlpanel.remove", "&c&l[Remove Shop]");
			messagei18n.set("controlpanel.remove-hover", "&eClick to remove this shop.");
			messagei18n.set("language-version", 2);
			selectedVersion = 2;
			messagei18n.save(messageFile);
		}
		if (selectedVersion == 2) {
			messagei18n.set("command.no-target-given", "&cUsage: /qs export mysql|sqlite");
			messagei18n.set("command.description.debug", "&ePrint debug infomation");
			messagei18n.set("no-permission-remove-shop", "&cYou do not have permission to use that command. Try break the shop instead?");
			messagei18n.set("language-version", 3);
			selectedVersion = 3;
			messagei18n.save(messageFile);
		}
		if (selectedVersion == 3) {
			messagei18n.set("signs.unlimited", "Unlimited");
			messagei18n.set("controlpanel.sign.owner.line1", "");
			messagei18n.set("controlpanel.sign.owner.line2", "Enter");
			messagei18n.set("controlpanel.sign.owner.line3", "new owner name");
			messagei18n.set("controlpanel.sign.owner.line4", "at first line");
			messagei18n.set("controlpanel.sign.price.line1", "");
			messagei18n.set("controlpanel.sign.price.line2", "Enter");
			messagei18n.set("controlpanel.sign.price.line3", "new shop price");
			messagei18n.set("controlpanel.sign.price.line4", "at first line");
			messagei18n.set("controlpanel.sign.refill.line1", "");
			messagei18n.set("controlpanel.sign.refill.line2", "Enter amount");
			messagei18n.set("controlpanel.sign.refill.line3", "you want fill");
			messagei18n.set("controlpanel.sign.refill.line4", "at first line");
			messagei18n.set("language-version", 4);
			selectedVersion = 4;
			messagei18n.save(messageFile);
		}
		if (selectedVersion == 4) {
			messagei18n.set("signs.unlimited", "Unlimited");
			messagei18n.set("controlpanel.sign",null);
			messagei18n.set("language-version", 5);
			selectedVersion = 5;
			messagei18n.save(messageFile);
		}
		if (selectedVersion == 5) {
			messagei18n.set("command.description.fetchmessage", "&eFetch unread shop message");
			messagei18n.set("nothing-to-flush", "&aYou had no new shop message.");
			messagei18n.set("language-version", 6);
			selectedVersion = 6;
			messagei18n.save(messageFile);
		}
		if (selectedVersion == 6) {
			messagei18n.set("command.description.info", "&eShow QuickShop Statistics");
			messagei18n.set("command.description.debug", "&eSwitch to developer mode");
			messagei18n.set("break-shop-use-supertool", "&eYou break the shop by use SuperTool.");
			messagei18n.set("no-creative-break", "&cYou cannot break other players shops in creative mode.  Use survival instead or use SuperTool ({0}).");
			messagei18n.set("command.now-debuging", "&aSuccessfully switch to developer mode, Reloading QuickShop...");
			messagei18n.set("command.now-nolonger-debuging", "&aSuccessfully switch to production mode, Reloading QuickShop...");
			messagei18n.set("language-version", 7);
			selectedVersion = 7;
			messagei18n.save(messageFile);
		}
		if (selectedVersion == 7) {
			messagei18n.set("failed-to-put-sign", "&cNo enough space around the shop to place infomation sign.");
			messagei18n.set("language-version", 8);
			selectedVersion = 8;
			messagei18n.save(messageFile);
		}
		if (selectedVersion == 8) {
			messagei18n.set("failed-to-paste", "&cFailed upload data to Pastebin, Check the internet and try again. (See console for details)");
			messagei18n.set("warn-to-paste", "&eCollecting data and upload to Pastebin, this may need a while. &c&lWarning&c, The data is keep public one week, it may leak your server configuration, make sure you only send it to your &ltrusted staff/developer.");
			messagei18n.set("command.description.paste", "&eAuto upload server data to Pastebin");
			messagei18n.set("language-version", 9);
			selectedVersion = 9;
			messagei18n.save(messageFile);
		}
		if (selectedVersion == 9) {
			messagei18n.set("controlpanel.commands.setowner", "/qs owner [Player]");
			messagei18n.set("controlpanel.commands.unlimited", "/qs slientunlimited {0} {1} {2} {3}");
			messagei18n.set("controlpanel.commands.buy", "/qs silentbuy {0} {1} {2} {3}");
			messagei18n.set("controlpanel.commands.sell", "/qs silentsell {0} {1} {2} {3}");
			messagei18n.set("controlpanel.commands.price", "/qs price [New Price]");
			messagei18n.set("controlpanel.commands.refill", "/qs refill [Amount]");
			messagei18n.set("controlpanel.commands.empty", "/qs silentempty {0} {1} {2} {3}");
			messagei18n.set("controlpanel.commands.remove", "/qs silentremove {0} {1} {2} {3}");
			messagei18n.set("tableformat.full_line", "+---------------------------------------------------+");
			messagei18n.set("tableformat.left_half_line", "+--------------------");
			messagei18n.set("tableformat.right_half_line", "--------------------+");
			messagei18n.set("tableformat.left_begin", "| ");
			messagei18n.set("booleanformat.success", "&a✔");
			messagei18n.set("booleanformat.failed", "&c✘");
			messagei18n.set("language-version", 10);
			selectedVersion = 10;
			messagei18n.save(messageFile);
		}
		if (selectedVersion == 10) {
			messagei18n.set("price-too-high", "&cShop price too high! You can't create price higher than {0} shop.");
			messagei18n.set("language-version", 11);
			selectedVersion = 11;
			messagei18n.save(messageFile);
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
		sender.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.full_line"));
		sender.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + MsgUtil.getMessage("controlpanel.infomation"));
		// Owner
		if (!sender.hasPermission("quickshop.setowner")) {
			sender.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + MsgUtil.getMessage("menu.owner", shop.ownerName()));
		} else {
			String Text = MsgUtil.getMessage("controlpanel.setowner",shop.ownerName());
			String hoverText = MsgUtil.getMessage("controlpanel.setowner-hover");
			//String clickCommand = "/qs setowner [Player]";
			String clickCommand = MsgUtil.getMessage("controlpanel.commands.setowner");
			TextComponent message = new TextComponent(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + Text);
			message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand));
			message.setHoverEvent(
					new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
			sender.spigot().sendMessage(message);
		}
		// Unlimited
		if (sender.hasPermission("quickshop.unlimited")) {
			String Text = MsgUtil.getMessage("controlpanel.unlimited", bool2String(shop.isUnlimited()));
			String hoverText = MsgUtil.getMessage("controlpanel.unlimited-hover");
			//String clickCommand = "/qs silentunlimited " + shop.getLocation().getWorld().getName() + " "
			//		+ shop.getLocation().getBlockX() + " " + shop.getLocation().getBlockY() + " "
			//		+ shop.getLocation().getBlockZ();
			String clickCommand = MsgUtil.getMessage("controlpanel.commands.unlimited",
					String.valueOf(shop.getLocation().getWorld().getName()),
					String.valueOf(shop.getLocation().getBlockX()),
					String.valueOf(shop.getLocation().getBlockY()),
					String.valueOf(shop.getLocation().getBlockZ()));
			MsgUtil.sendPanelMessage(sender, Text, hoverText, clickCommand);
		}
		// Buying/Selling Mode
		if (sender.hasPermission("quickshop.create.buy") && sender.hasPermission("quickshop.create.sell")) {
			if (shop.isSelling()) {
				String Text = MsgUtil.getMessage("controlpanel.mode-selling");
				String hoverText = MsgUtil.getMessage("controlpanel.mode-selling-hover");
				//String clickCommand = "/qs silentbuy " + shop.getLocation().getWorld().getName() + " "
				//		+ shop.getLocation().getBlockX() + " " + shop.getLocation().getBlockY() + " "
				//		+ shop.getLocation().getBlockZ();
				String clickCommand = MsgUtil.getMessage("controlpanel.commands.buy",
						String.valueOf(shop.getLocation().getWorld().getName()),
						String.valueOf(shop.getLocation().getBlockX()),
						String.valueOf(shop.getLocation().getBlockY()),
						String.valueOf(shop.getLocation().getBlockZ()));
				MsgUtil.sendPanelMessage(sender, Text, hoverText, clickCommand);
			} else if (shop.isBuying()) {
				String Text = MsgUtil.getMessage("controlpanel.mode-buying");
				String hoverText = MsgUtil.getMessage("controlpanel.mode-buying-hover");
				//String clickCommand = "/qs silentsell " + shop.getLocation().getWorld().getName() + " "
				//		+ shop.getLocation().getBlockX() + " " + shop.getLocation().getBlockY() + " "
				//		+ shop.getLocation().getBlockZ() ;
				String clickCommand = MsgUtil.getMessage("controlpanel.commands.sell",
						String.valueOf(shop.getLocation().getWorld().getName()),
						String.valueOf(shop.getLocation().getBlockX()),
						String.valueOf(shop.getLocation().getBlockY()),
						String.valueOf(shop.getLocation().getBlockZ()));
				MsgUtil.sendPanelMessage(sender, Text, hoverText, clickCommand);
			}
		}
		// Set Price
		if (sender.hasPermission("quickshop.other.price")||shop.getOwner().equals(((Player)sender).getUniqueId())) {
			String Text = MsgUtil.getMessage("controlpanel.price", String.valueOf(shop.getPrice()));
			String hoverText = MsgUtil.getMessage("controlpanel.price-hover");
			//String clickCommand = "/qs price [New Price]";
			String clickCommand = MsgUtil.getMessage("controlpanel.commands.price");
			TextComponent message = new TextComponent(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + Text);
			message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand));
			message.setHoverEvent(
					new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
			sender.spigot().sendMessage(message);
		}
		// Refill
		if (sender.hasPermission("quickshop.refill")) {
			String Text = MsgUtil.getMessage("controlpanel.refill", String.valueOf(shop.getPrice()));
			String hoverText = MsgUtil.getMessage("controlpanel.refill-hover");
			//String clickCommand = "/qs refill [Amount]";
			String clickCommand = MsgUtil.getMessage("controlpanel.commands.refill");
			TextComponent message = new TextComponent(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + Text);
			message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand));
			message.setHoverEvent(
					new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
			sender.spigot().sendMessage(message);
		}
		// Refill
		if (sender.hasPermission("quickshop.empty")) {
			String Text = MsgUtil.getMessage("controlpanel.empty", String.valueOf(shop.getPrice()));
			String hoverText = MsgUtil.getMessage("controlpanel.empty-hover");
			//String clickCommand = "/qs silentempty " + shop.getLocation().getWorld().getName() + " "
			//		+ shop.getLocation().getBlockX() + " " + shop.getLocation().getBlockY() + " "
			//		+ shop.getLocation().getBlockZ();
			String clickCommand = MsgUtil.getMessage("controlpanel.commands.empty",
					String.valueOf(shop.getLocation().getWorld().getName()),
					String.valueOf(shop.getLocation().getBlockX()),
					String.valueOf(shop.getLocation().getBlockY()),
					String.valueOf(shop.getLocation().getBlockZ()));
			MsgUtil.sendPanelMessage(sender, Text, hoverText, clickCommand);
		}
		// Remove
		if (sender.hasPermission("quickshop.other.destroy") || shop.getOwner().equals(((Player)sender).getUniqueId())) {
			String Text = MsgUtil.getMessage("controlpanel.remove", String.valueOf(shop.getPrice()));
			String hoverText = MsgUtil.getMessage("controlpanel.remove-hover");
			//String clickCommand = "/qs silentremove " + shop.getLocation().getWorld().getName() + " "
			//		+ shop.getLocation().getBlockX() + " " + shop.getLocation().getBlockY() + " "
			//		+ shop.getLocation().getBlockZ();
			String clickCommand = MsgUtil.getMessage("controlpanel.commands.remove",
					String.valueOf(shop.getLocation().getWorld().getName()),
					String.valueOf(shop.getLocation().getBlockX()),
					String.valueOf(shop.getLocation().getBlockY()),
					String.valueOf(shop.getLocation().getBlockZ()));
			MsgUtil.sendPanelMessage(sender, Text, hoverText, clickCommand);
		}

		sender.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.full_line"));
	}
	public static void sendPanelMessage(CommandSender sender, String Text,String hoverText, String clickCommand) {
		TextComponent message = new TextComponent(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + Text);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand));
        message.setHoverEvent(new HoverEvent (HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
        sender.spigot().sendMessage(message);
	}
	public static String bool2String(boolean bool) {
		if(bool) {
			return MsgUtil.getMessage("booleanformat.success");
		}else {
			return MsgUtil.getMessage("booleanformat.failed");
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
				plugin.getLogger().info("Found new items/blocks ["+Util.prettifyText(material.name()).trim()+"] ,add it in config...");
				itemi18n.set("itemi18n."+material.name(),Util.prettifyText(material.name()).trim());
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
	/**
	 * Get item's i18n name
	 * @param String ItemBukkitName(e.g. Material.STONE.name())
	 * @return String Item's i18n name.
	 */
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
			String enchname = enchi18n.getString("enchi18n."+ench.getKey().getKey().toString().trim());
			if(enchname==null || enchname.equals("")) {
				plugin.getLogger().info("Found new ench ["+ench.getKey().getKey().toString()+"] ,add it in config...");
				enchi18n.set("enchi18n."+ench.getKey().getKey().toString().trim(),ench.getKey().getKey().toString().trim());
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
	/**
	 * Get Enchantment's i18n name.
	 * @param Enchantment key
	 * @return String Enchantment's i18n name.
	 */
	public static String getEnchi18n(Enchantment key) {
		if(key==null) {
			return "ERROR";
		}
		String EnchString = null;
		//support for legacy
		EnchString = key.getKey().getKey().toString().trim();
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
	/**
	 * Get potion effect's i18n name.
	 * @param PotionEffectType potionType
	 * @return String Potion's i18n name.
	 */
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
	public static void send(UUID player, String message,boolean isUnlimited) {	//TODO Converted to UUID
		if(plugin.getConfig().getBoolean("shop.ignore-unlimited-shop-messages")&&isUnlimited)
			return; //Ignore unlimited shops messages.
		OfflinePlayer p = Bukkit.getOfflinePlayer(player);
		if (p == null || !p.isOnline()) {
			LinkedList<String> msgs = player_messages.get(player);
			if (msgs == null) {
				msgs = new LinkedList<String>();
				player_messages.put(player, msgs);
			}
			msgs.add(message);
			DatabaseHelper.sendMessage(player, message, System.currentTimeMillis());
		} else {
			p.getPlayer().sendMessage(message);
		}
	}

	/**
	 * Deletes any messages that are older than a week in the database, to save
	 * on space.
	 */
	public static void clean() {
		plugin.getLogger().info("Cleaning purchase messages from database that are over a week old...");
		// 604800,000 msec = 1 week.
		long weekAgo = System.currentTimeMillis() - 604800000;
		DatabaseHelper.cleanMessage(weekAgo);
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
				plugin.getDB().execute("DELETE FROM "+QuickShop.instance.getDbPrefix()+"messages WHERE owner = ?", pName.toString());
				msgs.clear();
			}else {
				p.getPlayer().sendMessage(getMessage("nothing-to-flush"));
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
		p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.full_line"));
		p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + MsgUtil.getMessage("menu.shop-information"));
		p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + MsgUtil.getMessage("menu.owner", shop.ownerName()));
		//Enabled
		Util.sendItemholochat(shop.getItem(),p,ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + MsgUtil.getMessage("menu.item", MsgUtil.getDisplayName(shop.getItem())));
		if (Util.isTool(items.getType())) {
			p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + MsgUtil.getMessage("menu.damage-percent-remaining", Util.getToolPercentage(items)));
		}
		if (shop.isSelling()) {
			if(shop.getRemainingStock()==-1) {
				p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + MsgUtil.getMessage("menu.stock", "" +  MsgUtil.getMessage("signs.unlimited")));
			}else {
				p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + MsgUtil.getMessage("menu.stock", "" + shop.getRemainingStock()));
			}
		} else {
			if(shop.getRemainingSpace()==-1) {
				p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + MsgUtil.getMessage("menu.space", "" + MsgUtil.getMessage("signs.unlimited")));
			}else {
			p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + MsgUtil.getMessage("menu.space", "" + shop.getRemainingSpace()));
		}}
		p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + MsgUtil.getMessage("menu.price-per", MsgUtil.getItemi18n(shop.getDataName()),Util.format(shop.getPrice())));
		if (shop.isBuying()) {
			p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + MsgUtil.getMessage("menu.this-shop-is-buying"));
		} else {
			p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + MsgUtil.getMessage("menu.this-shop-is-selling"));
		}
		Map<Enchantment, Integer> enchs = items.getItemMeta().getEnchants();
		if (enchs != null && !enchs.isEmpty()) {
			p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_half_line") + MsgUtil.getMessage("menu.enchants") + "-----------------------+");
			for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
				p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + ChatColor.YELLOW + MsgUtil.getEnchi18n(entries.getKey()) + " " + entries.getValue());
			}
		}
			if (items.getItemMeta() instanceof EnchantmentStorageMeta) {
				EnchantmentStorageMeta stor = (EnchantmentStorageMeta) items.getItemMeta();
				stor.getStoredEnchants();
				enchs = stor.getStoredEnchants();
				if (enchs != null && !enchs.isEmpty()) {
					p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_half_line") + MsgUtil.getMessage("menu.stored-enchants") + MsgUtil.getMessage("tableformat.right_half_line"));
					for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
						p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + ChatColor.YELLOW + MsgUtil.getEnchi18n(entries.getKey()) + " " + entries.getValue());
					}
				}
			}
		p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.full_line"));
	}

	public static void sendPurchaseSuccess(Player p, Shop shop, int amount) {
		p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.full_line"));
		p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + MsgUtil.getMessage("menu.successful-purchase"));
		p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + MsgUtil.getMessage("menu.item-name-and-price", "" + amount, MsgUtil.getDisplayName(shop.getItem()), Util.format((amount * shop.getPrice()))));
		Map<Enchantment, Integer> enchs = shop.getItem().getItemMeta().getEnchants();
		if (enchs != null && !enchs.isEmpty()) {
			p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_half_line") + MsgUtil.getMessage("menu.enchants") + "-----------------------+");
			for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
				p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + ChatColor.YELLOW +MsgUtil.getEnchi18n(entries.getKey()));
			}
		}
		enchs = shop.getItem().getItemMeta().getEnchants();
		if (enchs != null && !enchs.isEmpty()) {
			p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_half_line") + MsgUtil.getMessage("menu.stored-enchants") + MsgUtil.getMessage("tableformat.right_half_line"));
			for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
				p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + ChatColor.YELLOW + MsgUtil.getEnchi18n(entries.getKey()) + " " + entries.getValue());
			}
		}
			if (shop.getItem().getItemMeta() instanceof EnchantmentStorageMeta) {
				EnchantmentStorageMeta stor = (EnchantmentStorageMeta) shop.getItem().getItemMeta();
				stor.getStoredEnchants();
				enchs = stor.getStoredEnchants();
				if (enchs != null && !enchs.isEmpty()) {
					p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_half_line") + MsgUtil.getMessage("menu.stored-enchants") + MsgUtil.getMessage("tableformat.right_half_line"));
					for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
						p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + ChatColor.YELLOW +MsgUtil.getEnchi18n(entries.getKey()));
					}
				}
			}
		p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.full_line"));
	}

	public static void sendSellSuccess(Player p, Shop shop, int amount) {
		p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.full_line"));
		p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + MsgUtil.getMessage("menu.successfully-sold"));
		p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + MsgUtil.getMessage("menu.item-name-and-price", "" + amount, MsgUtil.getDisplayName(shop.getItem()), Util.format((amount * shop.getPrice()))));
		if (plugin.getConfig().getBoolean("show-tax")) {
			double tax = plugin.getConfig().getDouble("tax");
			double total = amount * shop.getPrice();
			if (tax != 0) {
				if (!p.getUniqueId().equals(shop.getOwner())) {
					p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + MsgUtil.getMessage("menu.sell-tax", "" + Util.format((tax * total))));
				} else {
					p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + MsgUtil.getMessage("menu.sell-tax-self"));
				}
			}
		}
		Map<Enchantment, Integer> enchs = shop.getItem().getItemMeta().getEnchants();
		if (enchs != null && !enchs.isEmpty()) {
			p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_half_line") + MsgUtil.getMessage("menu.enchants") + "-----------------------+");
			for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
				p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + ChatColor.YELLOW +MsgUtil.getEnchi18n(entries.getKey()));
			}
		}
			if (shop.getItem().getItemMeta() instanceof EnchantmentStorageMeta) {
				EnchantmentStorageMeta stor = (EnchantmentStorageMeta) shop.getItem().getItemMeta();
				stor.getStoredEnchants();
				enchs = stor.getStoredEnchants();
				if (enchs != null && !enchs.isEmpty()) {
					p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_half_line") + MsgUtil.getMessage("menu.stored-enchants") + "-----------------------+");
					for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
						p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin") + ChatColor.YELLOW +MsgUtil.getEnchi18n(entries.getKey()));
					}
				}
			}
		p.sendMessage(ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.full_line"));
	}
	/**
	 * Get item's displayname.
	 * @param ItemStack iStack
	 * @return String itemDisplayName
	 */
	public static String getDisplayName(ItemStack iStack){
		ItemStack is = iStack.clone();
		
		if(is.hasItemMeta()&&is.getItemMeta().hasDisplayName()) {
			return is.getItemMeta().getDisplayName();
		}else {
			return MsgUtil.getItemi18n(is.getType().name());
		}
		
	}
	/**
	 * getMessage in messages.yml
	 * @param String loc, String... args
	 * @return String message
	 */
	public static String getMessage(String loc, String... args) {
		String raw = messagei18n.getString(loc);
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
			raw = StringUtils.replace(raw, "{" + i + "}", args[i] == null ? "" : args[i]);
		}
		return raw;
	}
	public static void sendExploitAlert(Object objectDo, String action, Location location) {
		Util.sendMessageToOps(ChatColor.RED+"[QuickShop][ExploitAlert] A displayItem exploit was found!");
		if(objectDo instanceof Player) {
			Player player = (Player)objectDo;
			Util.sendMessageToOps(ChatColor.RED+"Exploiter: "+"Player="+player.getName());
		}
		if(objectDo instanceof Inventory) {
			Inventory inventory = (Inventory)objectDo;
			if(inventory.getHolder() instanceof LivingEntity) {
				LivingEntity livingEntity = (LivingEntity) inventory;
				if(livingEntity instanceof Tameable) {
					Tameable tamedEntity = (Tameable)livingEntity;
					AnimalTamer tamer = tamedEntity.getOwner();
					Util.sendMessageToOps(ChatColor.RED+"Exploiter: "+"LivingEntity=" + livingEntity.getType().name()+"; Tamer="+Bukkit.getOfflinePlayer(tamer.getUniqueId()));
				}
				Util.sendMessageToOps(ChatColor.RED+"Exploiter: "+"LivingEntity="+livingEntity.getType().name());
			}
			if(inventory.getHolder() instanceof Block) {
				Block block = (Block) inventory;
				Util.sendMessageToOps(ChatColor.RED+"Exploiter: "+"Block="+block.getType().name());
			}
			Util.sendMessageToOps(ChatColor.RED+"Exploiter: Unknown Inventory");
		}
		Util.sendMessageToOps(ChatColor.RED+"Action: "+action);
		Util.sendMessageToOps(ChatColor.RED+"Location: "+"World="+location.getWorld().getName()+" X="+location.getBlockX()+" Y="+location.getBlockY()+" Z="+location.getBlockZ());
	}
	
}
