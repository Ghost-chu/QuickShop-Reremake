/*
 * This file is a part of project QuickShop, the name is MsgUtil.java
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

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.SneakyThrows;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.ServiceInjector;
import org.maxgamer.quickshop.database.WarpedResultSet;
import org.maxgamer.quickshop.event.ShopControlPanelOpenEvent;
import org.maxgamer.quickshop.fileportlek.old.HumanReadableJsonConfiguration;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.util.language.game.GameLanguage;
import org.maxgamer.quickshop.util.language.game.MojangGameLanguageImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;


public class MsgUtil {
    private static final String invaildMsg = "Invaild message";

    private static final Map<UUID, LinkedList<String>> outGoingPlayerMessages = Maps.newConcurrentMap();
    private static final DecimalFormat decimalFormat = processFormat();
    public static GameLanguage gameLanguage;
    private static QuickShop plugin = QuickShop.getInstance();
    @Getter
    private static YamlConfiguration enchi18n;
    private static boolean inited;
    @Getter
    private static YamlConfiguration itemi18n;
    private static JsonConfiguration messagei18n;
    @Getter
    private static YamlConfiguration potioni18n;
    private static JsonConfiguration builtInLang;

    private static DecimalFormat processFormat() {
        try {
            return new DecimalFormat(Objects.requireNonNull(QuickShop.getInstance().getConfig().getString("decimal-format")));
        } catch (Exception e) {
            QuickShop.getInstance().getLogger().log(Level.WARNING, "Error when processing decimal format, using system default: " + e.getMessage());
            return new DecimalFormat();
        }
    }

    /**
     * Deletes any messages that are older than a week in the database, to save on space.
     */
    public static void clean() {
        plugin
                .getLogger()
                .info("Cleaning purchase messages from the database that are over a week old...");
        // 604800,000 msec = 1 week.
        plugin.getDatabaseHelper().cleanMessage(System.currentTimeMillis() - 604800000);
    }

    /**
     * Empties the queue of messages a player has and sends them to the player.
     *
     * @param p The player to message
     * @return True if success, False if the player is offline or null
     */
    public static boolean flush(@NotNull OfflinePlayer p) {
        Player player = p.getPlayer();
        if (player != null) {
            UUID pName = p.getUniqueId();
            LinkedList<String> msgs = outGoingPlayerMessages.get(pName);
            if (msgs != null) {
                for (String msg : msgs) {
                    if (p.getPlayer() != null) {
                        Util.debugLog("Accepted the msg for player " + p.getName() + " : " + msg);
                        String[] msgData = msg.split("##########");
                        if (msgData.length == 3) {
                            try {
                                ItemStack data = Util.deserialize(msgData[1]);
                                if (data == null) {
                                    MsgUtil.sendMessage(p.getPlayer(), msg);
                                } else {
                                    plugin.getQuickChat().sendItemHologramChat(player, msgData[0], data, msgData[2]);
                                }
                            } catch (InvalidConfigurationException e) {
                                MsgUtil.sendMessage(p.getPlayer(), msg);
                            }
                        } else {
                            MsgUtil.sendMessage(p.getPlayer(), msg);
                        }
                    }
                }
                plugin.getDatabaseHelper().cleanMessageForPlayer(pName);
                msgs.clear();
                return true;
            }
        }
        return false;
    }

    /**
     * Get item's i18n name, If you want get item name, use Util.getItemStackName
     *
     * @param itemBukkitName ItemBukkitName(e.g. Material.STONE.name())
     * @return String Item's i18n name.
     */
    public static String getItemi18n(@NotNull String itemBukkitName) {
        if (itemBukkitName.isEmpty()) {
            return "Item is empty";
        }
        String itemnameI18n = itemi18n.getString("itemi18n." + itemBukkitName);
        if (itemnameI18n != null && !itemnameI18n.isEmpty()) {
            return itemnameI18n;
        }
        Material material = Material.matchMaterial(itemBukkitName);
        if (material == null) {
            return "Material not exist";
        }
        return Util.prettifyText(material.name());
    }

    /**
     * getMessage in messages.yml
     *
     * @param loc    location
     * @param player The sender will send the message to
     * @param args   args
     * @return message
     */
    public static String getMessageOfflinePlayer(
            @NotNull String loc, @Nullable OfflinePlayer player, @NotNull String... args) {
        try {
            String raw = messagei18n.getString(loc);
            if (raw == null) {
                Util.debugLog("Missing language key: " + loc);
                return invaildMsg + ": " + loc;
            }
            String filled = fillArgs(raw, args);
            if (player != null) {
                if (plugin.getConfig().getBoolean("plugin.PlaceHolderAPI") && plugin.getPlaceHolderAPI() != null && plugin.getPlaceHolderAPI().isEnabled()) {
                    filled = PlaceholderAPI.setPlaceholders(player, filled);
                    Util.debugLog("Processed message " + filled + " by PlaceHolderAPI.");
                }
            }
            return filled;
        } catch (Exception th) {
            plugin.getSentryErrorReporter().ignoreThrow();
            plugin.getLogger().log(Level.WARNING, "Failed to process messages", th);
            return "Cannot load language key: " + loc + " because something not right, check the console for details.";
        }
    }

    /**
     * Replace args in raw to args
     *
     * @param raw  text
     * @param args args
     * @return filled text
     */
    public static String fillArgs(@Nullable String raw, @Nullable String... args) {
        if (StringUtils.isEmpty(raw)) {
            return "";
        }
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                raw = StringUtils.replace(raw, "{" + i + "}", args[i] == null ? "" : args[i]);
            }
        }
        return raw;
    }

    public static void loadGameLanguage(@NotNull String languageCode) {
        gameLanguage = ServiceInjector.getGameLanguage(new MojangGameLanguageImpl(plugin, languageCode));
    }

    public static void Loadi18nFile() throws InvalidConfigurationException {
        //Update instance
        plugin = QuickShop.getInstance();
        plugin.getLogger().info("Loading plugin translations files...");

        //Load game language i18n
        loadGameLanguage(plugin.getConfig().getString("game-language", "default"));

        /* Check & Load & Create default messages.yml */
        // Use try block to hook any possible exception, make sure not effect our cfgMessnages code.
        //en-US
        String languageCode = plugin.getConfig().getString("language", "en-US").replace("_", "-");

        // Init message file instance
        JsonConfiguration messageFile;
        File extractedMessageFile = new File(plugin.getDataFolder(), "messages.json");
        String buildInMessageFilePath = "lang/" + languageCode + "/messages.json";
        if (plugin.getResource(buildInMessageFilePath) == null) {
            //Use default
            buildInMessageFilePath = "lang-original/messages.json";
        }
        if (!extractedMessageFile.exists()) {
            try {
                Files.copy(Objects.requireNonNull(plugin.getResource(buildInMessageFilePath)), extractedMessageFile.toPath());
            } catch (IOException ioException) {
                plugin.getLogger().log(Level.WARNING, "Cannot extract the messages.json file", ioException);
            }
        }
        messageFile = HumanReadableJsonConfiguration.loadConfiguration(extractedMessageFile);
        //Handle old message file and load it up
        File oldMsgFile = new File(plugin.getDataFolder(), "messages.yml");
        if (oldMsgFile.exists()) {
            // Old messages file convert.
            plugin.getLogger().info("Converting the old format message.yml to message.json...");
            plugin.getLanguage().saveFile(languageCode, "messages", "messages.json");
            YamlConfiguration oldMsgI18n = YamlConfiguration.loadConfiguration(oldMsgFile);
            for (String key : oldMsgI18n.getKeys(true)) {
                messageFile.set(key, oldMsgI18n.get(key));
            }
            try {
                messageFile.save(extractedMessageFile);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            try {
                Files.move(
                        oldMsgFile.toPath(), new File(plugin.getDataFolder(), "messages.yml.bak").toPath());
            } catch (IOException ignore) {
            }
            if (oldMsgFile.exists()) {
                oldMsgFile.delete();
            }
            plugin.getLogger().info("Successfully converted, Continue loading...");
        } else {
            //Load the new files If not exists
            Util.debugLog("Loading language file from exist file...");
            if (!extractedMessageFile.exists()) {
                plugin.getLanguage().saveFile(languageCode, "messages", "messages.json");
                messageFile.loadFromString(Util.readToString(new File(plugin.getDataFolder(), "messages.json").getAbsolutePath()));
                messageFile.set("language-name", languageCode);
            }
        }

        //Init global instance
        File buildInLangFile = new File(Util.getCacheFolder(), "bulitin-messages.json");
        messagei18n = messageFile;
        try {
            if (buildInLangFile.exists()) {
                buildInLangFile.delete();
            }
            Files.copy(Objects.requireNonNull(plugin.getResource(buildInMessageFilePath)), buildInLangFile.toPath());
        } catch (IOException ioException) {
            Util.debugLog("Cannot load default built-in language file: " + ioException.getMessage());
        }
        builtInLang = HumanReadableJsonConfiguration.loadConfiguration(buildInLangFile);
        //Check the i18n language name and backup
        if (StringUtils.isEmpty(messagei18n.getString("language-name"))) {
            setAndUpdate("language-name");
        }
        String messageCodeInFile = messagei18n.getString("language-name");
        if (!Objects.equals(messageCodeInFile, languageCode)) {
            String backupFileName = "messages-bak-" + UUID.randomUUID().toString() + ".json";
            Util.debugLog("Language name " + messageCodeInFile + " not matched with " + languageCode + ", replacing with build-in files and renaming current file to " + backupFileName);
            plugin.getLogger().warning("Language name " + messageCodeInFile + " not matched with " + languageCode + ", replacing with build-in files and renaming current file to " + backupFileName);
            File pending = new File(plugin.getDataFolder(), "messages.json");
            try {
                Files.move(pending.toPath(), new File(plugin.getDataFolder(), backupFileName).toPath());
                plugin.getLanguage().saveFile(languageCode, "messages", "messages.json");
                messagei18n.loadFromString(Util.readToString(new File(plugin.getDataFolder(), "messages.json").getAbsolutePath()));
                messagei18n.set("language-name", languageCode);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to backup and save language file", e);
            }
        }


        /* Set default language vesion and update messages.yml */
        int ver = 0;
        String strVer = messagei18n.getString("language-version");
        if (StringUtils.isNumeric(strVer)) {
            try {
                ver = Integer.parseInt(strVer);
            } catch (NumberFormatException ignore) {
            }
        }
        if (ver == 0) {
            messagei18n.set("language-version", 1);
        } else {
            messagei18n.set("language-version", ver);
        }

        updateMessages(messagei18n.getInt("language-version"));

        //Update colors
        messagei18n.loadFromString(Util.parseColours(messagei18n.saveToString()));
        /* Print to console this language file's author, contributors, and region*/
        if (!inited) {
            plugin.getLogger().info(getMessage("translation-author", null));
            plugin.getLogger().info(getMessage("translation-contributors", null));
            plugin.getLogger().info(getMessage("translation-country", null));
            // plugin.getLogger().info(getMessage("translation-version"));
            inited = true;
        }

        /* Save the upgraded messages.yml */
        try {
            messagei18n.save(extractedMessageFile);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        plugin.getLogger().info("Completed to load plugin translations files.");
    }

    public static void loadEnchi18n() {
        plugin.getLogger().info("Loading enchantments translations...");
        File enchi18nFile = new File(plugin.getDataFolder(), "enchi18n.yml");
        if (!enchi18nFile.exists()) {
            plugin.getLogger().info("Creating enchi18n.yml");
            plugin.saveResource("enchi18n.yml", false);
        }
        // Store it
        enchi18n = YamlConfiguration.loadConfiguration(enchi18nFile);
        enchi18n.options().copyDefaults(false);
        YamlConfiguration enchi18nYAML =
                YamlConfiguration.loadConfiguration(
                        new InputStreamReader(Objects.requireNonNull(plugin.getResource("enchi18n.yml"))));
        enchi18n.setDefaults(enchi18nYAML);
        Util.parseColours(enchi18n);
        Enchantment[] enchsi18n = Enchantment.values();
        for (Enchantment ench : enchsi18n) {
            String enchi18nString = enchi18n.getString("enchi18n." + ench.getKey().getKey().trim());
            if (enchi18nString != null && !enchi18nString.isEmpty()) {
                continue;
            }
            String enchName = gameLanguage.getEnchantment(ench);
            enchi18n.set("enchi18n." + ench.getKey().getKey(), enchName);
            plugin.getLogger().info("Found new ench [" + enchName + "] , adding it to the config...");
        }
        try {
            enchi18n.save(enchi18nFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not load/save transaction enchname from enchi18n.yml. Skipping...", e);
        }
        plugin.getLogger().info("Complete to load enchantments translation.");
    }

    /**
     * Load Itemi18n fron file
     */
    public static void loadItemi18n() {
        plugin.getLogger().info("Loading items translations...");
        File itemi18nFile = new File(plugin.getDataFolder(), "itemi18n.yml");
        if (!itemi18nFile.exists()) {
            plugin.getLogger().info("Creating itemi18n.yml");
            plugin.saveResource("itemi18n.yml", false);
        }
        // Store it
        itemi18n = YamlConfiguration.loadConfiguration(itemi18nFile);
        itemi18n.options().copyDefaults(false);
        YamlConfiguration itemi18nYAML =
                YamlConfiguration.loadConfiguration(
                        new InputStreamReader(Objects.requireNonNull(plugin.getResource("itemi18n.yml"))));
        itemi18n.setDefaults(itemi18nYAML);
        Util.parseColours(itemi18n);
        Material[] itemsi18n = Material.values();
        for (Material material : itemsi18n) {
            String itemi18nString = itemi18n.getString("itemi18n." + material.name());
            if (itemi18nString != null && !itemi18nString.isEmpty()) {
                continue;
            }
            String itemName = gameLanguage.getItem(material);
            itemi18n.set("itemi18n." + material.name(), itemName);
            plugin
                    .getLogger()
                    .info("Found new items/blocks [" + itemName + "] , adding it to the config...");
        }
        try {
            itemi18n.save(itemi18nFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not load/save transaction itemname from itemi18n.yml. Skipping...", e);
        }
        plugin.getLogger().info("Complete to load items translation.");
    }

    public static void loadPotioni18n() {
        plugin.getLogger().info("Loading potions translations...");
        File potioni18nFile = new File(plugin.getDataFolder(), "potioni18n.yml");
        if (!potioni18nFile.exists()) {
            plugin.getLogger().info("Creating potioni18n.yml");
            plugin.saveResource("potioni18n.yml", false);
        }
        // Store it
        potioni18n = YamlConfiguration.loadConfiguration(potioni18nFile);
        potioni18n.options().copyDefaults(false);
        YamlConfiguration potioni18nYAML =
                YamlConfiguration.loadConfiguration(
                        new InputStreamReader(Objects.requireNonNull(plugin.getResource("potioni18n.yml"))));
        potioni18n.setDefaults(potioni18nYAML);
        Util.parseColours(potioni18n);
        for (PotionEffectType potion : PotionEffectType.values()) {
            if (potion == null) {
                continue;
            }
            String potionI18n = potioni18n.getString("potioni18n." + potion.getName());
            if (potionI18n != null && !potionI18n.isEmpty()) {
                continue;
            }
            String potionName = gameLanguage.getPotion(potion);
            plugin.getLogger().info("Found new potion [" + potionName + "] , adding it to the config...");
            potioni18n.set("potioni18n." + potion.getName(), potionName);
        }
        try {
            potioni18n.save(potioni18nFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not load/save transaction potionname from potioni18n.yml. Skipping...", e);
        }
        plugin.getLogger().info("Complete to load potions effect translation.");
    }

    /**
     * loads all player purchase messages from the database.
     */
    public static void loadTransactionMessages() {
        outGoingPlayerMessages.clear(); // Delete old messages
        try (WarpedResultSet warpRS = plugin.getDatabaseHelper().selectAllMessages()) {
            ResultSet rs = warpRS.getResultSet();
            while (rs.next()) {
                String owner = rs.getString("owner");
                UUID ownerUUID;
                if (Util.isUUID(owner)) {
                    ownerUUID = UUID.fromString(owner);
                } else {
                    //noinspection deprecation
                    ownerUUID = Bukkit.getOfflinePlayer(owner).getUniqueId();
                }
                String message = rs.getString("message");
                LinkedList<String> msgs = outGoingPlayerMessages.computeIfAbsent(ownerUUID, k -> new LinkedList<>());
                msgs.add(message);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Could not load transaction messages from database. Skipping.", e);
        }
    }

    /**
     * @param player      The name of the player to message
     * @param message     The message to send them Sends the given player a message if they're online.
     *                    Else, if they're not online, queues it for them in the database.
     * @param isUnlimited The shop is or unlimited
     *                    <p>
     *                    Deprecated for always use for bukkit deserialize method (costing ~145ms)
     */
    @Deprecated
    public static void send(@NotNull UUID player, @NotNull String message, boolean isUnlimited) {
        if (isUnlimited && plugin.getConfig().getBoolean("shop.ignore-unlimited-shop-messages")) {
            return; // Ignore unlimited shops messages.
        }
        Util.debugLog(message);
        String[] msgData = message.split("##########");
        OfflinePlayer p = Bukkit.getOfflinePlayer(player);
        if (!p.isOnline()) {
            LinkedList<String> msgs = outGoingPlayerMessages.getOrDefault(player, new LinkedList<>());
            msgs.add(message);
            outGoingPlayerMessages.put(player, msgs);
            plugin.getDatabaseHelper().sendMessage(player, message, System.currentTimeMillis());
        } else {
            if (p.getPlayer() != null) {
                if (msgData.length == 3) {
                    try {
                        plugin.getQuickChat().sendItemHologramChat(p.getPlayer(), msgData[0], Objects.requireNonNull(Util.deserialize(msgData[1])), msgData[2]);
                    } catch (Exception any) {
                        Util.debugLog("Unknown error, send by plain text.");
                        // Normal msg
                        MsgUtil.sendMessage(p.getPlayer(), message);
                    }
                } else {
                    // Normal msg
                    MsgUtil.sendMessage(p.getPlayer(), message);
                }
            }
        }
    }

    /**
     * @param shop    The shop purchased
     * @param player  The name of the player to message
     * @param message The message to send, if the given player are online it will be send immediately,
     *                Else, if they're not online, queues them in the database.
     */
    public static void send(@NotNull Shop shop, @NotNull UUID player, @NotNull String message) {
        if (shop.isUnlimited() && plugin.getConfig().getBoolean("shop.ignore-unlimited-shop-messages")) {
            return; // Ignore unlimited shops messages.
        }
        Util.debugLog(message);
        String[] msgData = message.split("##########");
        OfflinePlayer p = Bukkit.getOfflinePlayer(player);
        if (!p.isOnline()) {
            LinkedList<String> msgs = outGoingPlayerMessages.getOrDefault(player, new LinkedList<>());
            msgs.add(message);
            outGoingPlayerMessages.put(player, msgs);
            plugin.getDatabaseHelper().sendMessage(player, message, System.currentTimeMillis());
        } else {
            if (p.getPlayer() != null) {
                if (msgData.length == 3) {
                    try {
                        plugin.getQuickChat().sendItemHologramChat(p.getPlayer(), msgData[0], shop.getItem(), msgData[2]);
                    } catch (Exception any) {
                        Util.debugLog("Unknown error, send by plain text.");
                        // Normal msg
                        MsgUtil.sendMessage(p.getPlayer(), message);
                    }
                } else {
                    // Normal msg
                    MsgUtil.sendMessage(p.getPlayer(), message);
                }
            }
        }
    }

    public static @NotNull String getSubString(
            @NotNull String text, @NotNull String left, @NotNull String right) {
        String result;
        int zLen;
        if (left.isEmpty()) {
            zLen = 0;
        } else {
            zLen = text.indexOf(left);
            if (zLen > -1) {
                zLen += left.length();
            } else {
                zLen = 0;
            }
        }
        int yLen = text.indexOf(right, zLen);
        if (yLen < 0 || right.isEmpty()) {
            yLen = text.length();
        }
        result = text.substring(zLen, yLen);
        return result;
    }

    /**
     * Send controlPanel infomation to sender
     *
     * @param sender Target sender
     * @param shop   Target shop
     */
    public static void sendControlPanelInfo(@NotNull CommandSender sender, @NotNull Shop shop) {
        if ((sender instanceof Player)
                && !QuickShop.getPermissionManager().hasPermission(sender, "quickshop.use")
                && (shop.getOwner().equals(((Player) sender).getUniqueId()) || !QuickShop.getPermissionManager().hasPermission(sender, "quickshop.other.control"))
                && !InteractUtil.check(InteractUtil.Action.CONTROL, ((Player) sender).isSneaking())) {

            return;
        }
        if (Util.fireCancellableEvent(new ShopControlPanelOpenEvent(shop, sender))) {
            Util.debugLog("ControlPanel blocked by 3rd-party");
            return;
        }
        plugin.getShopManager().bakeShopRuntimeRandomUniqueIdCache(shop);
        ChatSheetPrinter chatSheetPrinter = new ChatSheetPrinter(sender);
        chatSheetPrinter.printHeader();
        chatSheetPrinter.printLine(MsgUtil.getMessage("controlpanel.infomation", sender));
        // Owner
        if (!QuickShop.getPermissionManager().hasPermission(sender, "quickshop.setowner")) {
            chatSheetPrinter.printLine(MsgUtil.getMessage("menu.owner", sender, shop.ownerName()));
        } else {
            chatSheetPrinter.printSuggestedCmdLine(
                    MsgUtil.getMessage(
                            "controlpanel.setowner",
                            sender,
                            shop.ownerName()
                                    + ((plugin.getConfig().getBoolean("shop.show-owner-uuid-in-controlpanel-if-op")
                                    && shop.isUnlimited())
                                    ? (" (" + shop.getOwner() + ")")
                                    : "")),
                    MsgUtil.getMessage("controlpanel.setowner-hover", sender),
                    "/qs setowner ");
        }


        // Unlimited
        if (QuickShop.getPermissionManager().hasPermission(sender, "quickshop.unlimited")) {
            String text =
                    MsgUtil.getMessage("controlpanel.unlimited", sender, bool2String(shop.isUnlimited()));
            String hoverText = MsgUtil.getMessage("controlpanel.unlimited-hover", sender);
            String clickCommand =
                    MsgUtil.fillArgs(
                            "/qs silentunlimited {0}",
                            shop.getRuntimeRandomUniqueId().toString());
            chatSheetPrinter.printExecutableCmdLine(text, hoverText, clickCommand);
        }
        // Buying/Selling Mode
        if (QuickShop.getPermissionManager().hasPermission(sender, "quickshop.create.buy")
                && QuickShop.getPermissionManager().hasPermission(sender, "quickshop.create.sell")) {
            if (shop.isSelling()) {
                String text = MsgUtil.getMessage("controlpanel.mode-selling", sender);
                String hoverText = MsgUtil.getMessage("controlpanel.mode-selling-hover", sender);
                String clickCommand =
                        MsgUtil.fillArgs(
                                "/qs silentbuy {0}",
                                shop.getRuntimeRandomUniqueId().toString());
                chatSheetPrinter.printExecutableCmdLine(text, hoverText, clickCommand);
            } else if (shop.isBuying()) {
                String text = MsgUtil.getMessage("controlpanel.mode-buying", sender);
                String hoverText = MsgUtil.getMessage("controlpanel.mode-buying-hover", sender);
                String clickCommand =
                        MsgUtil.fillArgs(
                                "/qs silentsell {0}",
                                shop.getRuntimeRandomUniqueId().toString());
                chatSheetPrinter.printExecutableCmdLine(text, hoverText, clickCommand);
            }
        }
        // Set Price
        if (QuickShop.getPermissionManager().hasPermission(sender, "quickshop.other.price")
                || shop.getOwner().equals(((OfflinePlayer) sender).getUniqueId())) {
            String text =
                    MsgUtil.fillArgs(
                            MsgUtil.getMessage("controlpanel.price", sender),
                            (plugin.getConfig().getBoolean("use-decimal-format"))
                                    ? decimalFormat(shop.getPrice())
                                    : Double.toString(shop.getPrice()));
            String hoverText = MsgUtil.getMessage("controlpanel.price-hover", sender);
            String clickCommand = "/qs price ";
            chatSheetPrinter.printSuggestedCmdLine(text, hoverText, clickCommand);
        }
        //Set amount per bulk
        if (QuickShop.getInstance().isAllowStack()) {
            if (QuickShop.getPermissionManager().hasPermission(sender, "quickshop.other.amount") || shop.getOwner().equals(((OfflinePlayer) sender).getUniqueId()) && QuickShop.getPermissionManager().hasPermission(sender, "quickshop.create.changeamount")) {
                String text = MsgUtil.getMessage(
                        "controlpanel.stack",
                        sender,
                        Integer.toString(shop.getItem().getAmount()));
                String hoverText = MsgUtil.getMessage("controlpanel.stack-hover", sender);
                String clickCommand = "/qs size ";
                chatSheetPrinter.printSuggestedCmdLine(text, hoverText, clickCommand);

            }
        }
        if (!shop.isUnlimited()) {
            // Refill
            if (QuickShop.getPermissionManager().hasPermission(sender, "quickshop.refill")) {
                String text =
                        MsgUtil.getMessage("controlpanel.refill", sender, String.valueOf(shop.getPrice()));
                String hoverText = MsgUtil.getMessage("controlpanel.refill-hover", sender);
                String clickCommand = "/qs refill ";
                chatSheetPrinter.printSuggestedCmdLine(text, hoverText, clickCommand);
            }
            // Empty
            if (QuickShop.getPermissionManager().hasPermission(sender, "quickshop.empty")) {
                String text =
                        MsgUtil.getMessage("controlpanel.empty", sender, String.valueOf(shop.getPrice()));
                String hoverText = MsgUtil.getMessage("controlpanel.empty-hover", sender);
                String clickCommand =
                        MsgUtil.fillArgs(
                                "/qs silentempty {0}",
                                shop.getRuntimeRandomUniqueId().toString());
                chatSheetPrinter.printExecutableCmdLine(text, hoverText, clickCommand);
            }
        }
        // Remove
        if (QuickShop.getPermissionManager().hasPermission(sender, "quickshop.other.destroy")
                || shop.getOwner().equals(((OfflinePlayer) sender).getUniqueId())) {
            String text = MsgUtil.getMessage("controlpanel.remove", sender, String.valueOf(shop.getPrice()));
            String hoverText = MsgUtil.getMessage("controlpanel.remove-hover", sender);
            String clickCommand = MsgUtil.fillArgs("/qs silentremove {0}", shop.getRuntimeRandomUniqueId().toString());
            chatSheetPrinter.printExecutableCmdLine(text, hoverText, clickCommand);
        }
        chatSheetPrinter.printFooter();
    }

    public static String getMessage(@NotNull String loc, @Nullable CommandSender player, @NotNull Object... args) {
        String[] strings = new String[args.length];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = Objects.toString(args[i]);
        }
        return getMessage(loc, player, strings);
    }

    public static String getMessage(@NotNull UUID uuid, @NotNull String loc, @NotNull String... args) {
        return getMessage(loc, Bukkit.getPlayer(uuid), args);
    }

    /**
     * getMessage in messages.yml
     *
     * @param loc    location
     * @param args   args
     * @param player The sender will send the message to
     * @return message
     */
    public static String getMessage(
            @NotNull String loc, @Nullable CommandSender player, @NotNull String... args) {
        try {
            final String raw = messagei18n.getString(loc);
            if (raw == null) {
                Util.debugLog("ERR: MsgUtil cannot find the the phrase at " + loc + ", printing the all readed datas: " + messagei18n);

                return invaildMsg + ": " + loc;
            }
            String filled = fillArgs(raw, args);
            if (player instanceof OfflinePlayer) {
                if (plugin.getPlaceHolderAPI() != null && plugin.getPlaceHolderAPI().isEnabled() && plugin.getConfig().getBoolean("plugin.PlaceHolderAPI")) {
                    try {
                        filled = PlaceholderAPI.setPlaceholders((OfflinePlayer) player, filled);
                    } catch (Exception ignored) {
                        if (((OfflinePlayer) player).getPlayer() != null) {
                            try {
                                filled = PlaceholderAPI.setPlaceholders(((OfflinePlayer) player).getPlayer(), filled);
                            } catch (Exception ignore) {
                            }
                        }
                    }
                }
            }
            return filled;
        } catch (Throwable th) {
            th.printStackTrace();
            plugin.getSentryErrorReporter().ignoreThrow();
            plugin.getLogger().log(Level.WARNING, "Failed to load language key", th);
            return "Cannot load language key: " + loc + " because something not right, check the console for details.";
        }
    }

    /**
     * Translate boolean value to String, the symbon is changeable by language file.
     *
     * @param bool The boolean value
     * @return The result of translate.
     */
    public static String bool2String(boolean bool) {
        if (bool) {
            return MsgUtil.getMessage("booleanformat.success", null);
        } else {
            return MsgUtil.getMessage("booleanformat.failed", null);
        }
    }

    public static String decimalFormat(double value) {
        return decimalFormat.format(value);
    }

    /**
     * Send globalAlert to ops, console, log file.
     *
     * @param content The content to send.
     */
    public static void sendGlobalAlert(@Nullable String content) {
        if (content == null) {
            Util.debugLog("Content is null");
            Throwable throwable =
                    new Throwable("Known issue: Global Alert accepted null string, what the fuck");
            plugin.getSentryErrorReporter().sendError(throwable, "NullCheck");
            return;
        }
        sendMessageToOps(content);
        plugin.getLogger().warning(content);
        Objects.requireNonNull(plugin.getLogWatcher()).add(content);
    }

    /**
     * Send a message for all online Ops.
     *
     * @param message The message you want send
     */
    public static void sendMessageToOps(@NotNull String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (QuickShop.getPermissionManager().hasPermission(player, "quickshop.alerts")) {
                MsgUtil.sendMessage(player, message);
            }
        }
    }

    /**
     * Send a purchaseSuccess message for a player.
     *
     * @param purchaser Target player
     * @param shop      Target shop
     * @param amount    Trading item amounts.
     */
    public static void sendPurchaseSuccess(@NotNull UUID purchaser, @NotNull Shop shop, int amount) {
        Player sender = Bukkit.getPlayer(purchaser);
        if (sender == null) {
            return;
        }
        ChatSheetPrinter chatSheetPrinter = new ChatSheetPrinter(sender);
        chatSheetPrinter.printHeader();
        chatSheetPrinter.printLine(MsgUtil.getMessage("menu.successful-purchase", sender));
        chatSheetPrinter.printLine(MsgUtil.getMessage("menu.item-name-and-price", sender, Integer.toString(amount * shop.getItem().getAmount()), Util.getItemStackName(shop.getItem()), Util.format(amount * shop.getPrice(), shop)));
        printEnchantment(sender, shop, chatSheetPrinter);
        chatSheetPrinter.printFooter();
    }

    /**
     * Get Enchantment's i18n name.
     *
     * @param key The Enchantment.
     * @return Enchantment's i18n name.
     */
    public static String getEnchi18n(@NotNull Enchantment key) {
        String enchString = key.getKey().getKey();
        if (enchString.isEmpty()) {
            return "Enchantment key is empty";
        }
        String enchI18n = enchi18n.getString("enchi18n." + enchString);
        if (enchI18n != null && !enchI18n.isEmpty()) {
            return enchI18n;
        }
        return Util.prettifyText(enchString);
    }

    /**
     * Send a sellSuccess message for a player.
     *
     * @param seller Target player
     * @param shop   Target shop
     * @param amount Trading item amounts.
     */
    public static void sendSellSuccess(@NotNull UUID seller, @NotNull Shop shop, int amount) {
        Player sender = Bukkit.getPlayer(seller);
        if (sender == null) {
            return;
        }
        ChatSheetPrinter chatSheetPrinter = new ChatSheetPrinter(sender);
        chatSheetPrinter.printHeader();
        chatSheetPrinter.printLine(MsgUtil.getMessage("menu.successfully-sold", sender));
        chatSheetPrinter.printLine(
                MsgUtil.getMessage(
                        "menu.item-name-and-price",
                        sender,
                        Integer.toString(amount),
                        Util.getItemStackName(shop.getItem()),
                        Util.format(amount * shop.getPrice(), shop)));
        if (plugin.getConfig().getBoolean("show-tax")) {
            double tax = plugin.getConfig().getDouble("tax");
            double total = amount * shop.getPrice();
            if (tax != 0) {
                if (!seller.equals(shop.getOwner())) {
                    chatSheetPrinter.printLine(
                            MsgUtil.getMessage("menu.sell-tax", sender, Util.format(tax * total, shop)));
                } else {
                    chatSheetPrinter.printLine(MsgUtil.getMessage("menu.sell-tax-self", sender));
                }
            }
        }
        printEnchantment(sender, shop, chatSheetPrinter);
        chatSheetPrinter.printFooter();
    }

    private static void printEnchantment(@NotNull Player p, @NotNull Shop shop, ChatSheetPrinter chatSheetPrinter) {
        if (shop.getItem().hasItemMeta() && shop.getItem().getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS) && plugin.getConfig().getBoolean("respect-item-flag")) {
            return;
        }
        Map<Enchantment, Integer> enchs = new HashMap<>();
        if (shop.getItem().hasItemMeta() && shop.getItem().getItemMeta().hasEnchants()) {
            enchs = shop.getItem().getItemMeta().getEnchants();
        }
        if (!enchs.isEmpty()) {
            chatSheetPrinter.printCenterLine(MsgUtil.getMessage("menu.enchants", p));
            printEnchantment(chatSheetPrinter, enchs);
        }
        if (shop.getItem().getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta stor = (EnchantmentStorageMeta) shop.getItem().getItemMeta();
            stor.getStoredEnchants();
            enchs = stor.getStoredEnchants();
            if (!enchs.isEmpty()) {
                chatSheetPrinter.printCenterLine(MsgUtil.getMessage("menu.stored-enchants", p));
                printEnchantment(chatSheetPrinter, enchs);
            }
        }
    }

    private static void printEnchantment(ChatSheetPrinter chatSheetPrinter, Map<Enchantment, Integer> enchs) {
        for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
            chatSheetPrinter.printLine(ChatColor.YELLOW + MsgUtil.getEnchi18n(entries.getKey()) + " " + RomanNumber.toRoman(entries.getValue()));
        }
    }

    /**
     * Send a shop infomation to a player.
     *
     * @param p    Target player
     * @param shop The shop
     */
    public static void sendShopInfo(@NotNull Player p, @NotNull Shop shop) {
        // Potentially faster with an array?
        ItemStack items = shop.getItem();
        ChatSheetPrinter chatSheetPrinter = new ChatSheetPrinter(p);
        chatSheetPrinter.printHeader();
        chatSheetPrinter.printLine(MsgUtil.getMessage("menu.shop-information", p));
        chatSheetPrinter.printLine(MsgUtil.getMessage("menu.owner", p, shop.ownerName()));
        // Enabled
        plugin.getQuickChat().send(p, plugin.getQuickChat().getItemHologramChat(shop, items, p, ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin", p) + MsgUtil.getMessage("menu.item", p, Util.getItemStackName(items)) + "  "));
        if (Util.isTool(items.getType())) {
            chatSheetPrinter.printLine(
                    MsgUtil.getMessage("menu.damage-percent-remaining", p, Util.getToolPercentage(items)));
        }
        if (shop.isSelling()) {
            if (shop.getRemainingStock() == -1) {
                chatSheetPrinter.printLine(
                        MsgUtil.getMessage("menu.stock", p, MsgUtil.getMessage("signs.unlimited", p)));
            } else {
                chatSheetPrinter.printLine(
                        MsgUtil.getMessage("menu.stock", p, Integer.toString(shop.getRemainingStock())));
            }
        } else {
            if (shop.getRemainingSpace() == -1) {
                chatSheetPrinter.printLine(
                        MsgUtil.getMessage("menu.space", p, MsgUtil.getMessage("signs.unlimited", p)));
            } else {
                chatSheetPrinter.printLine(
                        MsgUtil.getMessage("menu.space", p, Integer.toString(shop.getRemainingSpace())));
            }
        }
        if (shop.getItem().getAmount() == 1) {
            chatSheetPrinter.printLine(MsgUtil.getMessage("menu.price-per", p, Util.getItemStackName(shop.getItem()), Util.format(shop.getPrice(), shop)));
        } else {
            chatSheetPrinter.printLine(MsgUtil.getMessage("menu.price-per-stack", p, Util.getItemStackName(shop.getItem()), Util.format(shop.getPrice(), shop), Integer.toString(shop.getItem().getAmount())));
        }
        if (shop.isBuying()) {
            chatSheetPrinter.printLine(MsgUtil.getMessage("menu.this-shop-is-buying", p));
        } else {
            chatSheetPrinter.printLine(MsgUtil.getMessage("menu.this-shop-is-selling", p));
        }
        printEnchantment(p, shop, chatSheetPrinter);
        if (items.getItemMeta() instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) items.getItemMeta();
            PotionEffectType potionEffectType = potionMeta.getBasePotionData().getType().getEffectType();
            if (potionEffectType != null) {
                chatSheetPrinter.printLine(MsgUtil.getMessage("menu.effects", p));
                chatSheetPrinter.printLine(ChatColor.YELLOW + MsgUtil.getPotioni18n(potionEffectType));
            }
            for (PotionEffect potionEffect : potionMeta.getCustomEffects()) {
                chatSheetPrinter.printLine(ChatColor.YELLOW + MsgUtil.getPotioni18n(potionEffect.getType()));
            }
        }
        chatSheetPrinter.printFooter();
    }


    /**
     * Get potion effect's i18n name.
     *
     * @param potion potionType
     * @return Potion's i18n name.
     */
    public static String getPotioni18n(@NotNull PotionEffectType potion) {
        String potionString = potion.getName().trim();
        if (potionString.isEmpty()) {
            return "Potion name is empty.";
        }
        String potionI18n = potioni18n.getString("potioni18n." + potionString);
        if (potionI18n != null && !potionI18n.isEmpty()) {
            return potionI18n;
        }
        return Util.prettifyText(potionString);
    }

    public static JsonConfiguration getI18nFile() {
        return messagei18n;
    }

    public static void debugStackTrace(StackTraceElement[] traces) {
        if (Util.isDisableDebugLogger()) {
            return;
        }
        for (StackTraceElement stackTraceElement : traces) {
            final String className = stackTraceElement.getClassName();
            final String methodName = stackTraceElement.getMethodName();
            final int codeLine = stackTraceElement.getLineNumber();
            final String fileName = stackTraceElement.getFileName();
            Util.debugLog("[TRACE]  [" + className + "] [" + methodName + "] (" + fileName + ":" + codeLine + ") ");
        }
    }

    @SneakyThrows
    private static void updateMessages(int selectedVersion) {
        if (selectedVersion == 0) {
            selectedVersion = 1;
        }
        if (selectedVersion == 1) {
            setAndUpdate("shop-not-exist");
            setAndUpdate("controlpanel.infomation");
            setAndUpdate("controlpanel.setowner");
            setAndUpdate("controlpanel.setowner-hover");
            setAndUpdate("controlpanel.unlimited");
            setAndUpdate("controlpanel.unlimited-hover");
            setAndUpdate("controlpanel.mode-selling");
            setAndUpdate("controlpanel.mode-selling-hover");
            setAndUpdate("controlpanel.mode-buying");
            setAndUpdate("controlpanel.mode-buying-hover");
            setAndUpdate("controlpanel.price");
            setAndUpdate("controlpanel.price-hover");
            setAndUpdate("controlpanel.refill");
            setAndUpdate("controlpanel.refill-hover");
            setAndUpdate("controlpanel.empty");
            setAndUpdate("controlpanel.empty-hover");
            setAndUpdate("controlpanel.remove");
            setAndUpdate("controlpanel.remove-hover");
            setAndUpdate("language-version", 2);
            selectedVersion = 2;
        }
        if (selectedVersion == 2) {
            setAndUpdate("command.no-target-given");
            setAndUpdate("command.description.debug");
            setAndUpdate("no-permission-remove-shop");
            setAndUpdate("language-version", 3);
            selectedVersion = 3;
        }
        if (selectedVersion == 3) {
            setAndUpdate("signs.unlimited");
            setAndUpdate("controlpanel.sign.owner.line1");
            setAndUpdate("controlpanel.sign.owner.line2");
            setAndUpdate("controlpanel.sign.owner.line3");
            setAndUpdate("controlpanel.sign.owner.line4");
            setAndUpdate("controlpanel.sign.price.line1");
            setAndUpdate("controlpanel.sign.price.line2");
            setAndUpdate("controlpanel.sign.price.line3");
            setAndUpdate("controlpanel.sign.price.line4");
            setAndUpdate("controlpanel.sign.refill.line1");
            setAndUpdate("controlpanel.sign.refill.line2");
            setAndUpdate("controlpanel.sign.refill.line3");
            setAndUpdate("controlpanel.sign.refill.line4");
            setAndUpdate("language-version", 4);
            selectedVersion = 4;
        }
        if (selectedVersion == 4) {
            setAndUpdate("signs.unlimited");
            setAndUpdate("controlpanel.sign", null);
            setAndUpdate("language-version", 5);
            selectedVersion = 5;
        }
        if (selectedVersion == 5) {
            setAndUpdate("command.description.fetchmessage");
            setAndUpdate("nothing-to-flush");
            setAndUpdate("language-version", 6);
            selectedVersion = 6;
        }
        if (selectedVersion == 6) {
            setAndUpdate("command.description.info");
            setAndUpdate("command.description.debug");
            setAndUpdate("break-shop-use-supertool");
            setAndUpdate("no-creative-break");
            setAndUpdate("command.now-debuging");
            setAndUpdate("command.now-nolonger-debuging");
            setAndUpdate("language-version", 7);
            selectedVersion = 7;
        }
        if (selectedVersion == 7) {
            setAndUpdate("failed-to-put-sign");
            setAndUpdate("language-version", 8);
            selectedVersion = 8;
        }
        if (selectedVersion == 8) {
            setAndUpdate("failed-to-paste");
            setAndUpdate("warn-to-paste");
            setAndUpdate("command.description.paste");
            setAndUpdate("language-version", 9);
            selectedVersion = 9;
        }
        if (selectedVersion == 9) {
            setAndUpdate("controlpanel.commands.setowner");
            setAndUpdate("controlpanel.commands.unlimited");
            setAndUpdate("controlpanel.commands.buy");
            setAndUpdate("controlpanel.commands.sell");
            setAndUpdate("controlpanel.commands.price");
            setAndUpdate("controlpanel.commands.refill");
            setAndUpdate("controlpanel.commands.empty");
            setAndUpdate("controlpanel.commands.remove");
            setAndUpdate("tableformat.full_line");
            setAndUpdate("tableformat.left_half_line");
            setAndUpdate("tableformat.right_half_line");
            setAndUpdate("tableformat.left_begin");
            setAndUpdate("booleanformat.success");
            setAndUpdate("booleanformat.failed");
            setAndUpdate("language-version", 10);
            selectedVersion = 10;
        }
        if (selectedVersion == 10) {
            setAndUpdate("price-too-high");
            setAndUpdate("language-version", 11);
            selectedVersion = 11;
        }
        if (selectedVersion == 11) {
            setAndUpdate("unknown-player");
            setAndUpdate("shop-staff-cleared");
            setAndUpdate("shop-staff-added");
            setAndUpdate("shop-staff-deleted");
            setAndUpdate("command.wrong-args");
            setAndUpdate("command.description.staff");
            setAndUpdate("unknown-player");
            setAndUpdate("language-version", 12);
            selectedVersion = 12;
        }
        if (selectedVersion == 12) {
            setAndUpdate("menu.commands.preview");
            setAndUpdate("shop-staff-cleared");
            setAndUpdate("shop-staff-added");
            setAndUpdate("shop-staff-deleted");
            setAndUpdate("command.wrong-args");
            setAndUpdate("command.description.staff");
            setAndUpdate("unknown-player");
            setAndUpdate("language-version", 13);
            selectedVersion = 13;
        }
        if (selectedVersion == 13) {
            setAndUpdate("no-permission-build");
            setAndUpdate("success-change-owner-to-server");
            setAndUpdate("updatenotify.buttontitle");
            setAndUpdate("updatenotify.list");
            setAndUpdate("language-version", 14);
            selectedVersion = 14;
        }
        if (selectedVersion == 14) {
            setAndUpdate("flush-finished");
            setAndUpdate("language-version", 15);
            selectedVersion = 15;
        }
        if (selectedVersion == 15) {
            setAndUpdate("purchase-failed");
            setAndUpdate("language-version", 16);
            selectedVersion = 16;
        }
        if (selectedVersion == 16) {
            setAndUpdate("command.description.owner");
            setAndUpdate("command.description.remove");
            setAndUpdate("command.description.amount");
            setAndUpdate("command.description.about");
            setAndUpdate("command.description.help");
            setAndUpdate("no-pending-action");
            setAndUpdate("language-version", 17);
            selectedVersion = 17;
        }
        if (selectedVersion == 17) {
            setAndUpdate("updatenotify.onekeybuttontitle");
            setAndUpdate("language-version", 18);
            selectedVersion = 18;
        }
        if (selectedVersion == 18) {
            setAndUpdate("command.description.supercreate");
            setAndUpdate("language-version", 19);
            selectedVersion = 19;
        }
        if (selectedVersion == 19) {
            setAndUpdate("permission-denied-3rd-party");
            setAndUpdate("updatenotify.remote-disable-warning");
            setAndUpdate("language-version", 20);
            selectedVersion = 20;
        }
        if (selectedVersion == 20) {
            setAndUpdate("how-many-buy");
            setAndUpdate("how-many-sell");
            setAndUpdate("updatenotify.label.unstable");
            setAndUpdate("updatenotify.label.stable");
            setAndUpdate("updatenotify.label.lts");
            setAndUpdate("updatenotify.label.qualityverifyed");
            setAndUpdate("updatenotify.label.github");
            setAndUpdate("updatenotify.label.spigotmc");
            setAndUpdate("updatenotify.label.bukkitdev");
            setAndUpdate("language-version", 21);
            selectedVersion = 21;
        }
        if (selectedVersion == 21) {
            setAndUpdate("shop-removed-cause-ongoing-fee");
            setAndUpdate("language-version", 22);
            selectedVersion = 22;
        }
        if (selectedVersion == 22) {
            setAndUpdate("not-a-number");
            setAndUpdate("not-a-integer");
            setAndUpdate("language-version", 23);
            selectedVersion = 23;
        }
        if (selectedVersion == 23) {
            setAndUpdate("command.toggle-unlimited.unlimited");
            setAndUpdate("command.toggle-unlimited.limited");
            setAndUpdate("language-version", 24);
            selectedVersion = 24;
        }
        if (selectedVersion == 24) {
            setAndUpdate("digits-reach-the-limit");
            setAndUpdate("language-version", 25);
            selectedVersion = 25;
        }
        if (selectedVersion == 25) {
            setAndUpdate("complete");
            setAndUpdate("language-version", 26);
            selectedVersion = 26;
        }
        if (selectedVersion == 26) {
            setAndUpdate("updatenotify.label.master");
            setAndUpdate("language-version", 27);
            selectedVersion = 27;
        }
        if (selectedVersion == 27) {
            setAndUpdate("quickshop-gui-preview");
            setAndUpdate("shops-recovering");
            setAndUpdate("shops-backingup");
            setAndUpdate("saved-to-path");
            setAndUpdate("backup-failed");
            setAndUpdate("translate-not-completed-yet-click");
            setAndUpdate("translate-not-completed-yet-url");
            setAndUpdate("language-info-panel.name");
            setAndUpdate("language-info-panel.code");
            setAndUpdate("language-info-panel.progress");
            setAndUpdate("language-info-panel.help");
            setAndUpdate("language-info-panel.translate-on-crowdin");
            setAndUpdate("not-managed-shop");
            setAndUpdate("language-version", 28);
            selectedVersion = 28;
        }
        if (selectedVersion == 28) {
            setAndUpdate("quickshop-gui-preview");
            setAndUpdate("shops-recovering");
            setAndUpdate("shops-backingup");
            setAndUpdate("saved-to-path");
            setAndUpdate("backup-failed");
            setAndUpdate("translate-not-completed-yet-click");
            setAndUpdate("translate-not-completed-yet-url");
            setAndUpdate("language-info-panel.name");
            setAndUpdate("language-info-panel.code");
            setAndUpdate("language-info-panel.progress");
            setAndUpdate("language-info-panel.help");
            setAndUpdate("language-info-panel.translate-on-crowdin");
            setAndUpdate("not-managed-shop");
            setAndUpdate("language-version", 29);
            selectedVersion = 29;
        }
        if (selectedVersion == 29) {
            setAndUpdate("3rd-plugin-build-check-failed");
            setAndUpdate("language-version", 30);
            selectedVersion = 30;
        }
        if (selectedVersion == 30) {
            setAndUpdate("no-creative-break");
            setAndUpdate("trading-in-creative-mode-is-disabled");
            setAndUpdate("supertool-is-disabled");
            setAndUpdate("language-version", 31);
            selectedVersion = 31;
        }
        if (selectedVersion == 31) {
            setAndUpdate("menu.shop-stack");
            setAndUpdate("command.description.language");
            setAndUpdate("signs.stack-price");
            setAndUpdate("controlpanel.commands.stack");
            setAndUpdate("controlpanel.stack");
            setAndUpdate("controlpanel.stack-hover");
            setAndUpdate("shop-now-freezed");
            setAndUpdate("shop-nolonger-freezed");
            setAndUpdate("shop-freezed-at-location");
            setAndUpdate("shop-cannot-trade-when-freezing");
            setAndUpdate("denied-put-in-item");
            setAndUpdate("how-many-buy-stack");
            setAndUpdate("how-many-sell-stack");
            setAndUpdate("lang.name");
            setAndUpdate("lang.code");
            setAndUpdate("lang.translate-progress");
            setAndUpdate("lang.approval-progress");
            setAndUpdate("lang.qa-issues");
            setAndUpdate("lang.help-us");
            setAndUpdate("language-version", 32);
            selectedVersion = 32;
        }
        if (selectedVersion == 32) {
            setAndUpdate("signs.stack-selling");
            setAndUpdate("signs.stack-buying");
            setAndUpdate("menu.price-per-stack");
            setAndUpdate("menu.shop-stack");
            setAndUpdate("language-version", 33);
            selectedVersion = 33;
        }
        if (selectedVersion == 33) {
            setAndUpdate("integrations-check-failed-create");
            setAndUpdate("integrations-check-failed-trade");
            setAndUpdate("language-version", 34);
            selectedVersion = 34;
        }
        if (selectedVersion == 34) {
            setAndUpdate("how-many-buy-stack");
            setAndUpdate("how-many-sell-stack");
            setAndUpdate("language-version", 35);
            selectedVersion = 35;
        }
        if (selectedVersion == 35) {
            setAndUpdate("menu.price-per-stack");
            setAndUpdate("signs.stack-price");
            setAndUpdate("controlpanel.stack");
            setAndUpdate("controlpanel.stack-hover");
            setAndUpdate("controlpanel.commands.stack");
            setAndUpdate("controlpanel.item");
            setAndUpdate("controlpanel.item-hover");
            setAndUpdate("controlpanel.commands.item");
            setAndUpdate("how-much-to-trade-for");
            setAndUpdate("command.bulk-size-not-set");
            setAndUpdate("command.bulk-size-now");
            setAndUpdate("command.invalid-bulk-amount");
            setAndUpdate("command.description.size");
            setAndUpdate("command.no-trade-item");
            setAndUpdate("command.trade-item-now");
            setAndUpdate("command.description.item");
            setAndUpdate("item-holochat-error");
            setAndUpdate("shop-stack");
            setAndUpdate("language-version", 36);
            selectedVersion = 36;
        }
        if (selectedVersion == 36) {
            setAndUpdate("menu.price-per-stack");
            setAndUpdate("command.trade-item-now");
            setAndUpdate("command.bulk-size-now");
            setAndUpdate("how-much-to-trade-for");
            setAndUpdate("language-version", 37);
            selectedVersion = 37;
        }
        if (selectedVersion == 37) {
            setAndUpdate("signs.stack-price");
            setAndUpdate("command.some-shops-removed");
            setAndUpdate("command.description.removeall");
            setAndUpdate("command.no-owner-given");
            setAndUpdate("language-version", 38);
            selectedVersion = 38;
        }
        if (selectedVersion == 38) {
            setAndUpdate("integrations-check-failed-create");
            setAndUpdate("integrations-check-failed-trade");
            setAndUpdate("3rd-plugin-build-check-failed");
            setAndUpdate("language-version", 39);
            selectedVersion = 39;
        }
        if (selectedVersion == 39) {
            setAndUpdate("command.transfer-success");
            setAndUpdate("command.transfer-success-other");
            setAndUpdate("command.description.transfer");
            setAndUpdate("language-version", 40);
            selectedVersion = 40;
        }
        if (selectedVersion == 40) {
            setAndUpdate("controlpanel.commands", null);
            setAndUpdate("menu.commands", null);
            setAndUpdate("language-version", 41);
            selectedVersion = 41;
        }

        if (selectedVersion == 41) {
            setAndUpdate("shops-removed-in-world");
            setAndUpdate("command.description.removeworld");
            setAndUpdate("command.no-world-given");
            setAndUpdate("world-not-exists");
            setAndUpdate("language-version", 42);
            selectedVersion = 42;
        }
        if (selectedVersion == 42) {
            setAndUpdate("player-bought-from-your-store-tax");
            setAndUpdate("player-bought-from-your-store");
            setAndUpdate("language-version", 43);
            selectedVersion = 43;
        }
        if (selectedVersion == 43) {
            setAndUpdate("nearby-shop-this-way", null);
            setAndUpdate("nearby-shop-header");
            setAndUpdate("nearby-shop-entry");
            setAndUpdate("language-version", ++selectedVersion);
        }
        if (selectedVersion == 44) {
            setAndUpdate("nearby-shop-this-way");
            setAndUpdate("language-version", ++selectedVersion);
        }
        if (selectedVersion == 45) {
            setAndUpdate("exceeded-maximum");
            setAndUpdate("language-version", ++selectedVersion);
        }
        if (selectedVersion == 46) {
            setAndUpdate("currency-not-exists");
            setAndUpdate("currency-set");
            setAndUpdate("currency-unset");
            setAndUpdate("command.description.currency");
            setAndUpdate("controlpanel.currency");
            setAndUpdate("controlpanel.currency-hover");
            setAndUpdate("currency-not-support");
            setAndUpdate("language-version", ++selectedVersion);
        }
        if (selectedVersion == 47) {
            setAndUpdate("forbidden-vanilla-behavior");
            setAndUpdate("language-version", ++selectedVersion);
        }
        if (selectedVersion == 48) {
            setAndUpdate("tabcomplete.currency");
            setAndUpdate("tabcomplete.item");
            setAndUpdate("item-not-exist");
            setAndUpdate("language-version", ++selectedVersion);
        }
        if (selectedVersion == 49) {
            setAndUpdate("backup-success");
            setAndUpdate("console-only");
            setAndUpdate("console-only-danger");
            setAndUpdate("clean-warning");
            setAndUpdate("command.disabled");
            setAndUpdate("command.feature-not-enabled");
            setAndUpdate("language-version", ++selectedVersion);
        }
        if (selectedVersion == 50) {
            setAndUpdate("signs.header");
            setAndUpdate("signs.status-available");
            setAndUpdate("signs.status-unavailable");
            setAndUpdate("language-version", ++selectedVersion);
        }
        setAndUpdate("_comment", "Please edit this file after format with json formatter");
    }


    private static void setAndUpdate(@NotNull String path, @Nullable Object object) {
        if (object == null) {
            messagei18n.set(path, null); // Removal
            return;
        }
        Object alt = null;
        if (builtInLang != null) {
            alt = builtInLang.get(path, object);
        }
        if (alt == null) {
            messagei18n.set(path, object);
        } else {
            messagei18n.set(path, alt);
        }
    }

    private static void setAndUpdate(@NotNull String path) {
        Object alt = null;
        if (builtInLang != null) {
            alt = builtInLang.get(path);
        }
        if (alt == null) {
            messagei18n.set(path, "Missing no: " + path);
        } else {
            messagei18n.set(path, alt);
        }
    }

    public static void sendColoredMessage(@NotNull CommandSender sender, @NotNull ChatColor chatColor, @Nullable String... messages) {
        if (messages == null) {
            return;
        }
        for (String msg : messages) {
            try {
                if (StringUtils.isEmpty(msg)) {
                    continue;
                }
                plugin.getQuickChat().send(sender, chatColor + msg);
            } catch (Throwable throwable) {
                Util.debugLog("Failed to send formatted text.");
                sender.sendMessage(msg);
            }
        }

    }

    public static void sendMessage(@NotNull UUID sender, @Nullable String... messages) {
        sendMessage(Bukkit.getPlayer(sender), messages);
    }

    public static void sendMessage(@Nullable CommandSender sender, @Nullable String... messages) {
        if (messages == null) {
            Util.debugLog("INFO: null messages trying to be sent.");
            return;
        }
        if (sender == null) {
            Util.debugLog("INFO: Sending message to null sender.");
            return;
        }
        for (String msg : messages) {
            try {
                if (StringUtils.isEmpty(msg)) {
                    continue;
                }
                plugin.getQuickChat().send(sender, msg);
            } catch (Throwable throwable) {
                Util.debugLog("Failed to send formatted text.");
                if (!StringUtils.isEmpty(msg)) {
                    sender.sendMessage(msg);
                }
            }
        }
    }

}
