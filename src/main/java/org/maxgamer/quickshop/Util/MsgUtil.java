/*
 * This file is a part of project QuickShop, the name is MsgUtil.java
 * Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 * Copyright (C) Bukkit Commons Studio and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.maxgamer.quickshop.Util;

import lombok.SneakyThrows;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.File.IFile;
import org.maxgamer.quickshop.File.JSONFile;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;

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

@SuppressWarnings("WeakerAccess")
public class MsgUtil {
    private static YamlConfiguration builtInDefaultLanguage = YamlConfiguration.loadConfiguration(new InputStreamReader(QuickShop.instance.getLanguage()
            .getFile("en", "messages")));
    public static String invaildMsg = "Invaild message";
    private static YamlConfiguration enchi18n;
    private static boolean inited;
    private static YamlConfiguration itemi18n;
    private static IFile messagei18n;
    private static HashMap<UUID, LinkedList<String>> player_messages = new HashMap<>();
    private static QuickShop plugin = QuickShop.instance;
    private static YamlConfiguration potioni18n;
    private static DecimalFormat decimalFormat = new DecimalFormat(Objects.requireNonNull(plugin.getConfig().getString("decimal-format")));
    private static GameLanguage gameLanguage;

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

    /**
     * Deletes any messages that are older than a week in the database, to save
     * on space.
     */
    public static void clean() {
        plugin.getLogger().info("Cleaning purchase messages from the database that are over a week old...");
        // 604800,000 msec = 1 week.
        long weekAgo = System.currentTimeMillis() - 604800000;
        plugin.getDatabaseHelper().cleanMessage(weekAgo);
    }

    /**
     * Replace args in raw to args
     *
     * @param raw  text
     * @param args args
     * @return filled text
     */
    public static String fillArgs(@Nullable String raw, @Nullable String... args) {
        if (raw == null) {
            return "Invalid message: null";
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

    /**
     * Empties the queue of messages a player has and sends them to the player.
     *
     * @param p The player to message
     * @return True if success, False if the player is offline or null
     */
    public static boolean flush(@NotNull OfflinePlayer p) {
        if (p.isOnline()) {
            UUID pName = p.getUniqueId();
            LinkedList<String> msgs = player_messages.get(pName);
            if (msgs != null) {
                for (String msg : msgs) {
                    if (p.getPlayer() != null) {
                        Util.debugLog("Accepted the msg for player " + p.getName() + " : " + msg);
                        String[] msgData = msg.split("##########");
                        try {
                            sendItemholochat(p.getPlayer(), msgData[0], Util.deserialize(msgData[1]), msgData[2]);
                        } catch (InvalidConfigurationException e) {
                            p.getPlayer().sendMessage(msgData[0] + msgData[1] + msgData[2]);
                        } catch (ArrayIndexOutOfBoundsException e2) {
                            p.getPlayer().sendMessage(msg);
                        }
                        plugin.getDatabaseHelper().cleanMessageForPlayer(pName);
                        msgs.clear();
                        return true;
                    } else {
                        return false;
                    }
                }
                plugin.getDatabaseHelper().cleanMessageForPlayer(pName);
                msgs.clear();
            }
        }
        return false;
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
     * @param args   args
     * @param player The sender will send the message to
     * @return message
     */
    public static String getMessage(@NotNull String loc, @Nullable CommandSender player, @NotNull String... args) {
        Optional<String> raw = messagei18n.getString(loc);
        if (!raw.isPresent()) {
            Util.debugLog("ERR: MsgUtil cannot find the the phrase at "+loc+", printing the all readed datas: "+messagei18n);

            return invaildMsg + ": " + loc;
        }
        String filled = fillArgs(raw.get(), args);
        if (player instanceof OfflinePlayer) {
            if (plugin.getPlaceHolderAPI() != null && plugin.getPlaceHolderAPI().isEnabled()) {
                try {
                    filled = PlaceholderAPI.setPlaceholders((OfflinePlayer) player, filled);
                    Util.debugLog("Processed message " + filled + " by PlaceHolderAPI.");
                }catch (Exception ignored){
                    if(((OfflinePlayer) player).getPlayer() != null){
                        try {
                            filled = PlaceholderAPI.setPlaceholders(((OfflinePlayer) player).getPlayer(), filled);
                        }catch (Exception ignore){}
                    }
                }
            }
        }
        return filled;
    }

    /**
     * getMessage in messages.yml
     *
     * @param loc    location
     * @param player The sender will send the message to
     * @param args   args
     * @return message
     */
    public static String getMessageOfflinePlayer(@NotNull String loc, @Nullable OfflinePlayer player, @NotNull String... args) {
        Optional<String> raw = messagei18n.getString(loc);
        if (!raw.isPresent()) {
            return invaildMsg + ": " + loc;
        }
        String filled = fillArgs(raw.get(), args);
        if (player != null) {
            if (plugin.getPlaceHolderAPI() != null && plugin.getPlaceHolderAPI().isEnabled()) {
                filled = PlaceholderAPI.setPlaceholders(player, filled);
                Util.debugLog("Processed message " + filled + " by PlaceHolderAPI.");
            }
        }
        return filled;
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

//    private static void loadLangUtils(@NotNull String languageCode){
//        try {
//            //Load LangUtils support, before MsgUtil init.
//            //Cause we maybe will use them all.
//
//            Plugin langUtilsPlugin = Bukkit.getPluginManager().getPlugin("LangUtils");
//            if (langUtilsPlugin != null) {
//                List<String> langLanguages = langUtilsPlugin.getConfig().getStringList("LoadLanguage");
//                if (!langLanguages.contains(languageCode)) {
//                    langLanguages.add(languageCode);
//                    langUtilsPlugin.getConfig().set("LoadLanguage", langLanguages);
//                    langUtilsPlugin.saveConfig();
//                    langUtilsPlugin.reloadConfig();
//                    langUtilsPlugin.onDisable();
//                    langUtilsPlugin.onEnable();
//                }
//            }
//        } catch (Throwable throwable) {
//            plugin.getSentryErrorReporter().sendError(throwable,"LangUtils cannot load.");
//        }
//    }

    public static void loadGameLanguage(@NotNull String languageCode){
        gameLanguage = new GameLanguage(languageCode);
    }

    public static void loadCfgMessages() throws IOException, InvalidConfigurationException {
        /* Check & Load & Create default messages.yml */
        // Use try block to hook any possible exception, make sure not effect our cfgMessnages code.
        String languageCode = plugin.getConfig()
                .getString("language", "en");
        //noinspection ConstantConditions

        loadGameLanguage(plugin.getConfig().getString("game-language","default"));
        //Init nJson
        IFile nJson;
        if(plugin.getResource("messages/"+languageCode+".json") == null){
            nJson = new JSONFile(plugin, new File(plugin.getDataFolder(),"messages.json"),"messages/en.json",true);
        }else{
            nJson = new JSONFile(plugin, new File(plugin.getDataFolder(),"messages.json"),"messages/"+languageCode+".json",true);
        }

        nJson.create();


        File oldMsgFile = new File(plugin.getDataFolder(), "messages.yml");
        if(oldMsgFile.exists()){ //Old messages file convert.
            plugin.getLogger().info("Converting the old format message.yml to message.json...");
            plugin.getLanguage().saveFile(languageCode,"messages","messages.json");
            YamlConfiguration oldMsgI18n = YamlConfiguration.loadConfiguration(oldMsgFile);
            for (String key:oldMsgI18n.getKeys(true)){
                oldMsgI18n.get(key);
            }
            nJson.save();
            try {
                Files.move(oldMsgFile.toPath(), new File(plugin.getDataFolder(), "messages.yml.bak").toPath());
            }catch (IOException ignore){}
            if(oldMsgFile.exists()){
                oldMsgFile.delete();
            }
            plugin.getLogger().info("Successfully converted, Continue loading...");
        }else{
            Util.debugLog("Loading language file from exist file...");
           if(!new File(plugin.getDataFolder(),"messages.json").exists()){
               plugin.getLanguage().saveFile(languageCode,"messages","messages.json");
               nJson.loadFromString(Util.readToString(new File(plugin.getDataFolder(),"messages.json").getAbsolutePath()));
           }
        }
        messagei18n = nJson;
        /* Set default language vesion and update messages.yml */
        if (messagei18n.getInt("language-version") == 0) {
            messagei18n.set("language-version", 1);
        }
        updateMessages(messagei18n.getInt("language-version"));
        messagei18n.loadFromString(Util.parseColours(messagei18n.saveToString()));

        /* Print to console this language file's author, contributors, and region*/
        if (!inited) {
            plugin.getLogger().info(getMessage("translation-author", null));
            plugin.getLogger().info(getMessage("translation-contributors", null));
            plugin.getLogger().info(getMessage("translation-country", null));
            //plugin.getLogger().info(getMessage("translation-version"));
            inited = true;
        }
        /* Save the upgraded messages.yml */

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
        YamlConfiguration enchi18nYAML = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(plugin.
                getResource("enchi18n.yml"))));
        enchi18n.setDefaults(enchi18nYAML);
        Util.parseColours(enchi18n);
        Enchantment[] enchsi18n = Enchantment.values();
        for (Enchantment ench : enchsi18n) {
            String enchi18nString = enchi18n.getString("enchi18n." + ench.getKey().getKey().trim());
            if (enchi18nString != null && !enchi18nString.isEmpty()) {
                continue;
            }
            String enchName = gameLanguage.getEnchantment(ench.getKey().getKey());
//            if (Bukkit.getPluginManager().isPluginEnabled("LangUtils")) {
//                //noinspection ConstantConditions
//                enchName = LanguageHelper.getEnchantmentName(ench, plugin.getConfig().getString("langutils-language", "en_us"));
//            } else {
//                enchName = Util.prettifyText(ench.getKey().getKey());
//            }
            enchi18n.set("enchi18n." + ench.getKey().getKey(), enchName);
            plugin.getLogger().info("Found new ench [" + enchName + "] , adding it to the config...");
        }
        try {
            enchi18n.save(enchi18nFile);
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().log(Level.WARNING, "Could not load/save transaction enchname from enchi18n.yml. Skipping.");
        }
        plugin.getLogger().info("Complete to load enchname i18n.");
    }

    /**
     * Load Itemi18n fron file
     */
    public static void loadItemi18n() {
        File itemi18nFile = new File(plugin.getDataFolder(), "itemi18n.yml");
        if (!itemi18nFile.exists()) {
            plugin.getLogger().info("Creating itemi18n.yml");
            plugin.saveResource("itemi18n.yml", true);
        }
        // Store it
        itemi18n = YamlConfiguration.loadConfiguration(itemi18nFile);
        itemi18n.options().copyDefaults(true);
        YamlConfiguration itemi18nYAML = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(plugin
                .getResource("itemi18n.yml"))));
        itemi18n.setDefaults(itemi18nYAML);
        Util.parseColours(itemi18n);
        Material[] itemsi18n = Material.values();
        for (Material material : itemsi18n) {
            String itemi18nString = itemi18n.getString("itemi18n." + material.name());
            if (itemi18nString != null && !itemi18nString.isEmpty()) {
                continue;
            }
            String itemName = gameLanguage.getItem(material);
//            if (Bukkit.getPluginManager().isPluginEnabled("LangUtils")) {
//                //noinspection ConstantConditions
//                itemName = LanguageHelper.getItemName(new ItemStack(material), plugin.getConfig()
//                        .getString("langutils-language", "en_us"));
//            } else {
//                itemName = Util.prettifyText(material.name());
//            }
            itemi18n.set("itemi18n." + material.name(), itemName);
            plugin.getLogger().info("Found new items/blocks [" + itemName + "] , adding it to the config...");
        }
        try {
            itemi18n.save(itemi18nFile);
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().log(Level.WARNING, "Could not load/save transaction itemname from itemi18n.yml. Skipping.");
        }
        plugin.getLogger().info("Complete to load Itemname i18n.");
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
        YamlConfiguration potioni18nYAML = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(plugin
                .getResource("potioni18n.yml"))));
        potioni18n.setDefaults(potioni18nYAML);
        Util.parseColours(potioni18n);
        for (PotionEffectType potion : PotionEffectType.values()) {
            String potionI18n = potioni18n.getString("potioni18n." + potion.getName().trim());
            if (potionI18n != null && !potionI18n.isEmpty()) {
                continue;
            }
            String potionName = gameLanguage.getPotion(potion);
            plugin.getLogger().info("Found new potion [" +potionName + "] , adding it to the config...");
            potioni18n.set("potioni18n." + potion.getName(), potionName);
        }
        try {
            potioni18n.save(potioni18nFile);
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().log(Level.WARNING, "Could not load/save transaction potionname from potioni18n.yml. Skipping.");
        }
        plugin.getLogger().info("Complete to load potionname i18n.");
    }

    /**
     * loads all player purchase messages from the database.
     */
    public static void loadTransactionMessages() {
        player_messages.clear(); // Delete old messages
        try {
            ResultSet rs = plugin.getDatabaseHelper().selectAllMessages();
            while (rs.next()) {
                String owner = rs.getString("owner");
                UUID ownerUUID;
                if (Util.isUUID(owner)) {
                    ownerUUID = UUID.fromString(owner);
                } else {
                    ownerUUID = Bukkit.getOfflinePlayer(owner).getUniqueId();
                }
                String message = rs.getString("message");
                LinkedList<String> msgs = player_messages.computeIfAbsent(ownerUUID, k -> new LinkedList<>());
                msgs.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().log(Level.WARNING, "Could not load transaction messages from database. Skipping.");
        }
    }

    /**
     * @param player      The name of the player to message
     * @param message     The message to send them Sends the given player a message if
     *                    they're online. Else, if they're not online, queues it for
     *                    them in the database.
     * @param isUnlimited The shop is or unlimited
     */
    public static void send(@NotNull UUID player, @NotNull String message, boolean isUnlimited) {
        if (plugin.getConfig().getBoolean("shop.ignore-unlimited-shop-messages") && isUnlimited) {
            return; //Ignore unlimited shops messages.
        }
        Util.debugLog(message);
        String[] msgData = message.split("##########");
        OfflinePlayer p = Bukkit.getOfflinePlayer(player);
        if (!p.isOnline()) {
            LinkedList<String> msgs = player_messages.get(player);
            if (msgs == null) {
                msgs = new LinkedList<>();
            }
            player_messages.put(player, msgs);
            msgs.add(message);
            plugin.getDatabaseHelper().sendMessage(player, message, System.currentTimeMillis());
        } else {
            if (p.getPlayer() != null) {
                try {
                    sendItemholochat(p.getPlayer(), msgData[0], Util.deserialize(msgData[1]), msgData[2]);
                } catch (InvalidConfigurationException e) {
                    Util.debugLog("Unknown error, send by plain text.");
                    p.getPlayer().sendMessage(msgData[0] + msgData[1] + msgData[2]);
                } catch (ArrayIndexOutOfBoundsException e2) {
                    try {
                        sendItemholochat(p.getPlayer(), msgData[0], Util.deserialize(msgData[1]), "");
                    } catch (Exception any) {
                        //Normal msg
                        p.getPlayer().sendMessage(message);
                    }

                }
            }
        }
    }

    @SneakyThrows
    public static void sendItemholochat(@NotNull Player player, @NotNull String left, @NotNull ItemStack itemStack, @NotNull String right) {
        String json = ItemNMS.saveJsonfromNMS(itemStack);
        if (json == null) {
            return;
        }
        Util.debugLog(left);
        Util.debugLog(itemStack.toString());
        Util.debugLog(right);
        TextComponent centerItem = new TextComponent(left + Util.getItemStackName(itemStack) + right);
        ComponentBuilder cBuilder = new ComponentBuilder(json);
        centerItem.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, cBuilder.create()));
        player.spigot().sendMessage(centerItem);
    }

    public static @NotNull String getSubString(@NotNull String text, @NotNull String left, @NotNull String right) {
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
        if ((sender instanceof Player && !sender.isOp()) && !QuickShop.getPermissionManager().hasPermission(sender, "quickshop.use")) {
            return;
        }
        if (plugin.getConfig().getBoolean("sneak-to-control")) {
            if (sender instanceof Player) {
                if (!((Player) sender).isSneaking()) {//sneaking
                    return;
                }
            }
        }
        ChatSheetPrinter chatSheetPrinter = new ChatSheetPrinter(sender);
        chatSheetPrinter.printHeader();
        chatSheetPrinter.printLine(MsgUtil.getMessage("controlpanel.infomation", sender));
        // Owner
        if (!QuickShop.getPermissionManager().hasPermission(sender, "quickshop.setowner")) {
            chatSheetPrinter.printLine(MsgUtil.getMessage("menu.owner", sender, shop.ownerName()));
        } else {
            chatSheetPrinter.printSuggestableCmdLine(MsgUtil.getMessage("controlpanel.setowner", sender, shop.ownerName() + ((plugin.getConfig().getBoolean("shop.show-owner-uuid-in-controlpanel-if-op") && shop.isUnlimited()) ? (" (" + shop.getOwner() + ")") : "")), MsgUtil
                    .getMessage("controlpanel.setowner-hover", sender), MsgUtil.getMessage("controlpanel.commands.setowner", sender));
        }

        // Unlimited
        if (QuickShop.getPermissionManager().hasPermission(sender, "quickshop.unlimited")) {
            String text = MsgUtil.getMessage("controlpanel.unlimited", sender, bool2String(shop.isUnlimited()));
            String hoverText = MsgUtil.getMessage("controlpanel.unlimited-hover", sender);
            String clickCommand = MsgUtil.getMessage("controlpanel.commands.unlimited", sender,
                    Objects.requireNonNull(shop.getLocation().getWorld()).getName(),
                    String.valueOf(shop.getLocation().getBlockX()),
                    String.valueOf(shop.getLocation().getBlockY()),
                    String.valueOf(shop.getLocation().getBlockZ()));
            chatSheetPrinter.printExecuteableCmdLine(text, hoverText, clickCommand);
        }
        // Buying/Selling Mode
        if (QuickShop.getPermissionManager().hasPermission(sender, "quickshop.create.buy") && sender.hasPermission("quickshop.create.sell")) {
            if (shop.isSelling()) {
                String text = MsgUtil.getMessage("controlpanel.mode-selling", sender);
                String hoverText = MsgUtil.getMessage("controlpanel.mode-selling-hover", sender);
                String clickCommand = MsgUtil.getMessage("controlpanel.commands.buy", sender,
                        Objects.requireNonNull(shop.getLocation().getWorld()).getName(),
                        String.valueOf(shop.getLocation().getBlockX()),
                        String.valueOf(shop.getLocation().getBlockY()),
                        String.valueOf(shop.getLocation().getBlockZ()));
                chatSheetPrinter.printExecuteableCmdLine(text, hoverText, clickCommand);
            } else if (shop.isBuying()) {
                String text = MsgUtil.getMessage("controlpanel.mode-buying", sender);
                String hoverText = MsgUtil.getMessage("controlpanel.mode-buying-hover", sender);
                String clickCommand = MsgUtil.getMessage("controlpanel.commands.sell", sender,
                        Objects.requireNonNull(shop.getLocation().getWorld()).getName(),
                        String.valueOf(shop.getLocation().getBlockX()),
                        String.valueOf(shop.getLocation().getBlockY()),
                        String.valueOf(shop.getLocation().getBlockZ()));
                chatSheetPrinter.printExecuteableCmdLine(text, hoverText, clickCommand);
            }
        }
        // Set Price
        if (QuickShop.getPermissionManager().hasPermission(sender, "quickshop.other.price") || shop.getOwner().equals(((OfflinePlayer) sender).getUniqueId())) {
            String text = MsgUtil.getMessage("controlpanel.price", sender, (plugin.getConfig().getBoolean("use-decimal-format")) ? decimalFormat(shop.getPrice()) : "" + shop.getPrice());
            String hoverText = MsgUtil.getMessage("controlpanel.price-hover", sender);
            String clickCommand = MsgUtil.getMessage("controlpanel.commands.price", sender);
            chatSheetPrinter.printSuggestableCmdLine(text, hoverText, clickCommand);
        }
        // Refill
        if (QuickShop.getPermissionManager().hasPermission(sender, "quickshop.refill")) {
            String text = MsgUtil.getMessage("controlpanel.refill", sender, String.valueOf(shop.getPrice()));
            String hoverText = MsgUtil.getMessage("controlpanel.refill-hover", sender);
            String clickCommand = MsgUtil.getMessage("controlpanel.commands.refill", sender);
            chatSheetPrinter.printSuggestableCmdLine(text, hoverText, clickCommand);
        }
        // Refill
        if (QuickShop.getPermissionManager().hasPermission(sender, "quickshop.empty")) {
            String text = MsgUtil.getMessage("controlpanel.empty", sender, String.valueOf(shop.getPrice()));
            String hoverText = MsgUtil.getMessage("controlpanel.empty-hover", sender);
            String clickCommand = MsgUtil.getMessage("controlpanel.commands.empty", sender,
                    Objects.requireNonNull(shop.getLocation().getWorld()).getName(),
                    String.valueOf(shop.getLocation().getBlockX()),
                    String.valueOf(shop.getLocation().getBlockY()),
                    String.valueOf(shop.getLocation().getBlockZ()));
            chatSheetPrinter.printExecuteableCmdLine(text, hoverText, clickCommand);
        }
        // Remove
        if (QuickShop.getPermissionManager().hasPermission(sender, "quickshop.other.destroy") || shop.getOwner().equals(((OfflinePlayer) sender).getUniqueId())) {
            String text = MsgUtil.getMessage("controlpanel.remove", sender, String.valueOf(shop.getPrice()));
            String hoverText = MsgUtil.getMessage("controlpanel.remove-hover", sender);
            String clickCommand = MsgUtil.getMessage("controlpanel.commands.remove", sender,
                    Objects.requireNonNull(shop.getLocation().getWorld()).getName(),
                    String.valueOf(shop.getLocation().getBlockX()),
                    String.valueOf(shop.getLocation().getBlockY()),
                    String.valueOf(shop.getLocation().getBlockZ()));
            chatSheetPrinter.printExecuteableCmdLine(text, hoverText, clickCommand);
        }

        chatSheetPrinter.printFooter();
    }

    /**
     * Send globalAlert to ops, console, log file.
     *
     * @param content The content to send.
     */
    public static void sendGlobalAlert(@Nullable String content) {
        if (content == null) {
            Util.debugLog("Content is null");
            Throwable throwable = new Throwable("Known issue: Global Alert accepted null string, what the fuck");
            plugin.getSentryErrorReporter().sendError(throwable, "NullCheck");
            return;
        }
        sendMessageToOps(content);
        plugin.getLogger().warning(content);
        Objects.requireNonNull(plugin.getLogWatcher()).add(content);
        Util.debugLog(content);
    }

    /**
     * Send the ItemPreview chat msg by NMS.
     *
     * @param shop       Target shop
     * @param itemStack  Target ItemStack
     * @param player     Target player
     * @param normalText The text you will see
     */
    public static void sendItemholochat(@NotNull Shop shop, @NotNull ItemStack itemStack, @NotNull Player player, @NotNull String normalText) {
        try {
            String json = ItemNMS.saveJsonfromNMS(itemStack);
            if (json == null) {
                return;
            }
            TextComponent normalmessage = new TextComponent(normalText + "   " + MsgUtil.getMessage("menu.preview", player));
            ComponentBuilder cBuilder = new ComponentBuilder(json);
            if (QuickShop.getPermissionManager().hasPermission(player, "quickshop.preview")) {
                normalmessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, MsgUtil
                        .getMessage("menu.commands.preview", player, Objects.requireNonNull(shop.getLocation().getWorld()).getName(), String
                                .valueOf(shop.getLocation().getBlockX()), String.valueOf(shop.getLocation().getBlockY()), String
                                .valueOf(shop.getLocation().getBlockZ()))));
            }
            normalmessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, cBuilder.create()));
            player.spigot().sendMessage(normalmessage);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Send a message for all online Ops.
     *
     * @param message The message you want send
     */
    public static void sendMessageToOps(@NotNull String message) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.isOp() || QuickShop.getPermissionManager().hasPermission(player, "quickshop.alert")) {
                        player.sendMessage(message);
                    }
                }
            }
        }.runTaskAsynchronously(plugin);

    }

    /**
     * Send a purchaseSuccess message for a player.
     *
     * @param p      Target player
     * @param shop   Target shop
     * @param amount Trading item amounts.
     */
    public static void sendPurchaseSuccess(@NotNull Player p, @NotNull Shop shop, int amount) {
        ChatSheetPrinter chatSheetPrinter = new ChatSheetPrinter(p);
        chatSheetPrinter.printHeader();
        chatSheetPrinter.printLine(MsgUtil.getMessage("menu.successful-purchase", p));
        chatSheetPrinter.printLine(MsgUtil
                .getMessage("menu.item-name-and-price", p, "" + amount, Util.getItemStackName(shop.getItem()), Util
                        .format((amount * shop.getPrice()))));
        Map<Enchantment, Integer> enchs = new HashMap<>();
        if (shop.getItem().hasItemMeta() && Objects.requireNonNull(shop.getItem().getItemMeta()).hasEnchants()) {
            enchs = shop.getItem().getItemMeta().getEnchants();
        }
        if (!enchs.isEmpty()) {
            chatSheetPrinter.printCenterLine(MsgUtil.getMessage("menu.enchants", p));
            for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
                chatSheetPrinter.printLine(ChatColor.YELLOW + MsgUtil.getEnchi18n(entries.getKey()));
            }
        }
        if (shop.getItem().getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta stor = (EnchantmentStorageMeta) shop.getItem().getItemMeta();
            stor.getStoredEnchants();
            enchs = stor.getStoredEnchants();
            if (!enchs.isEmpty()) {
                chatSheetPrinter.printCenterLine(MsgUtil.getMessage("menu.stored-enchants", p));
                for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
                    chatSheetPrinter.printLine(ChatColor.YELLOW + MsgUtil.getEnchi18n(entries.getKey()));
                }
            }
        }
        chatSheetPrinter.printFooter();
    }

    /**
     * Send a sellSuccess message for a player.
     *
     * @param p      Target player
     * @param shop   Target shop
     * @param amount Trading item amounts.
     */
    public static void sendSellSuccess(@NotNull Player p, @NotNull Shop shop, int amount) {
        ChatSheetPrinter chatSheetPrinter = new ChatSheetPrinter(p);
        chatSheetPrinter.printHeader();
        chatSheetPrinter.printLine(MsgUtil.getMessage("menu.successfully-sold", p));
        chatSheetPrinter.printLine(MsgUtil
                .getMessage("menu.item-name-and-price", p, "" + amount, Util.getItemStackName(shop.getItem()), Util
                        .format((amount * shop.getPrice()))));
        if (plugin.getConfig().getBoolean("show-tax")) {
            double tax = plugin.getConfig().getDouble("tax");
            double total = amount * shop.getPrice();
            if (tax != 0) {
                if (!p.getUniqueId().equals(shop.getOwner())) {
                    chatSheetPrinter.printLine(MsgUtil.getMessage("menu.sell-tax", p, Util.format((tax * total))));
                } else {
                    chatSheetPrinter.printLine(MsgUtil.getMessage("menu.sell-tax-self", p));
                }
            }
        }
        Map<Enchantment, Integer> enchs = new HashMap<>();
        if (shop.getItem().hasItemMeta() && Objects.requireNonNull(shop.getItem().getItemMeta()).hasEnchants()) {
            enchs = shop.getItem().getItemMeta().getEnchants();
        }
        if (!enchs.isEmpty()) {
            chatSheetPrinter.printCenterLine(MsgUtil.getMessage("menu.enchants", p));
            for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
                chatSheetPrinter.printLine(ChatColor.YELLOW + MsgUtil.getEnchi18n(entries.getKey()));
            }
        }
        if (shop.getItem().getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta stor = (EnchantmentStorageMeta) shop.getItem().getItemMeta();
            stor.getStoredEnchants();
            enchs = stor.getStoredEnchants();
            if (!enchs.isEmpty()) {
                chatSheetPrinter.printCenterLine(MsgUtil.getMessage("menu.stored-enchants", p));
                for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
                    chatSheetPrinter.printLine(ChatColor.YELLOW + MsgUtil.getEnchi18n(entries.getKey()));
                }
            }
        }
        chatSheetPrinter.printFooter();
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
        //Enabled
        sendItemholochat(shop, items, p, ChatColor.DARK_PURPLE + MsgUtil
                .getMessage("tableformat.left_begin", p) + " " + MsgUtil
                .getMessage("menu.item", p, Util.getItemStackName(items)));
        if (Util.isTool(items.getType())) {
            chatSheetPrinter.printLine(MsgUtil.getMessage("menu.damage-percent-remaining", p, Util.getToolPercentage(items)));
        }
        if (shop.isSelling()) {
            if (shop.getRemainingStock() == -1) {
                chatSheetPrinter.printLine(MsgUtil.getMessage("menu.stock", p, "" + MsgUtil.getMessage("signs.unlimited", p)));
            } else {
                chatSheetPrinter.printLine(MsgUtil.getMessage("menu.stock", p, "" + shop.getRemainingStock()));
            }
        } else {
            if (shop.getRemainingSpace() == -1) {
                chatSheetPrinter.printLine(MsgUtil.getMessage("menu.space", p, "" + MsgUtil.getMessage("signs.unlimited", p)));
            } else {
                chatSheetPrinter.printLine(MsgUtil.getMessage("menu.space", p, "" + shop.getRemainingSpace()));
            }
        }
        chatSheetPrinter.printLine(MsgUtil
                .getMessage("menu.price-per", p, Util.getItemStackName(shop.getItem()), Util.format(shop.getPrice())));
        if (shop.isBuying()) {
            chatSheetPrinter.printLine(MsgUtil.getMessage("menu.this-shop-is-buying", p));
        } else {
            chatSheetPrinter.printLine(MsgUtil.getMessage("menu.this-shop-is-selling", p));
        }
        Map<Enchantment, Integer> enchs = new HashMap<>();
        if (items.hasItemMeta() && Objects.requireNonNull(items.getItemMeta()).hasEnchants()) {
            enchs = items.getItemMeta().getEnchants();
        }
        if (!enchs.isEmpty()) {
            chatSheetPrinter.printCenterLine(MsgUtil.getMessage("menu.enchants", p,""));
            for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
                chatSheetPrinter.printLine(ChatColor.YELLOW + MsgUtil.getEnchi18n(entries.getKey()) + " " + entries.getValue());
            }
        }
        if (items.getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta stor = (EnchantmentStorageMeta) items.getItemMeta();
            enchs = stor.getStoredEnchants();
            if (!enchs.isEmpty()) {
                chatSheetPrinter.printLine(MsgUtil.getMessage("menu.stored-enchants", p));
                for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
                    chatSheetPrinter.printLine(ChatColor.YELLOW + MsgUtil.getEnchi18n(entries.getKey()) + " " + entries
                            .getValue());
                }
            }
        }
        if(items.getItemMeta() instanceof PotionMeta){
            PotionMeta potionMeta = (PotionMeta)items.getItemMeta();
            PotionEffectType potionEffectType = potionMeta.getBasePotionData().getType().getEffectType();
            if(potionEffectType != null){
                chatSheetPrinter.printLine(MsgUtil.getMessage("menu.effects", p));
                chatSheetPrinter.printLine(ChatColor.YELLOW+MsgUtil.getPotioni18n(potionEffectType));
            }
            potionMeta.getCustomEffects().forEach((potionEffect -> chatSheetPrinter.printLine(ChatColor.YELLOW+MsgUtil.getPotioni18n(potionEffect.getType()))));
        }
        chatSheetPrinter.printFooter();
    }

    public static String decimalFormat(double value) {
        return decimalFormat.format(value);
    }

    @SuppressWarnings("UnusedAssignment")
    private static void updateMessages(int selectedVersion) throws IOException {

        if (selectedVersion == 1) {
            setAndUpdate("shop-not-exist", "&cThere had no shop.");
            setAndUpdate("controlpanel.infomation", "&aShop Control Panel:");
            setAndUpdate("controlpanel.setowner", "&aOwner: &b{0} &e[&d&lChange&e]");
            setAndUpdate("controlpanel.setowner-hover", "&eLooking you want changing shop and click to switch owner.");
            setAndUpdate("controlpanel.unlimited", "&aUnlimited: {0} &e[&d&lSwitch&e]");
            messagei18n
                    .set("controlpanel.unlimited-hover", "&eLooking you want changing shop and click to switch enabled or disabled.");
            setAndUpdate("controlpanel.mode-selling", "&aShop mode: &bSelling &e[&d&lSwitch&e]");
            messagei18n
                    .set("controlpanel.mode-selling-hover", "&eLooking you want changing shop and click to switch enabled or disabled.");
            setAndUpdate("controlpanel.mode-buying", "&aShop mode: &bBuying &e[&d&lSwitch&e]");
            messagei18n
                    .set("controlpanel.mode-buying-hover", "&eLooking you want changing shop and click to switch enabled or disabled.");
            setAndUpdate("controlpanel.price", "&aPrice: &b{0} &e[&d&lSet&e]");
            setAndUpdate("controlpanel.price-hover", "&eLooking you want changing shop and click to set new price.");
            setAndUpdate("controlpanel.refill", "&aRefill: Refill the shop items &e[&d&lOK&e]");
            setAndUpdate("controlpanel.refill-hover", "&eLooking you want changing shop and click to refill.");
            setAndUpdate("controlpanel.empty", "&aEmpty: Remove shop all items &e[&d&lOK&e]");
            setAndUpdate("controlpanel.empty-hover", "&eLooking you want changing shop and click to clear.");
            setAndUpdate("controlpanel.remove", "&c&l[Remove Shop]");
            setAndUpdate("controlpanel.remove-hover", "&eClick to remove this shop.");
            setAndUpdate("language-version", 2);
            selectedVersion = 2;

        }
        if (selectedVersion == 2) {
            setAndUpdate("command.no-target-given", "&cUsage: /qs export mysql|sqlite");
            setAndUpdate("command.description.debug", "&ePrint debug infomation");
            messagei18n
                    .set("no-permission-remove-shop", "&cYou do not have permission to use that command. Try break the shop instead?");
            setAndUpdate("language-version", 3);
            selectedVersion = 3;

        }
        if (selectedVersion == 3) {
            setAndUpdate("signs.unlimited", "Unlimited");
            setAndUpdate("controlpanel.sign.owner.line1", "");
            setAndUpdate("controlpanel.sign.owner.line2", "Enter");
            setAndUpdate("controlpanel.sign.owner.line3", "new owner name");
            setAndUpdate("controlpanel.sign.owner.line4", "at first line");
            setAndUpdate("controlpanel.sign.price.line1", "");
            setAndUpdate("controlpanel.sign.price.line2", "Enter");
            setAndUpdate("controlpanel.sign.price.line3", "new shop price");
            setAndUpdate("controlpanel.sign.price.line4", "at first line");
            setAndUpdate("controlpanel.sign.refill.line1", "");
            setAndUpdate("controlpanel.sign.refill.line2", "Enter amount");
            setAndUpdate("controlpanel.sign.refill.line3", "you want fill");
            setAndUpdate("controlpanel.sign.refill.line4", "at first line");
            setAndUpdate("language-version", 4);
            selectedVersion = 4;

        }
        if (selectedVersion == 4) {
            setAndUpdate("signs.unlimited", "Unlimited");
            setAndUpdate("controlpanel.sign", null);
            setAndUpdate("language-version", 5);
            selectedVersion = 5;

        }
        if (selectedVersion == 5) {
            setAndUpdate("command.description.fetchmessage", "&eFetch unread shop message");
            setAndUpdate("nothing-to-flush", "&aYou had no new shop message.");
            setAndUpdate("language-version", 6);
            selectedVersion = 6;

        }
        if (selectedVersion == 6) {
            setAndUpdate("command.description.info", "&eShow QuickShop Statistics");
            setAndUpdate("command.description.debug", "&eSwitch to developer mode");
            setAndUpdate("break-shop-use-supertool", "&eYou break the shop by use SuperTool.");
            messagei18n
                    .set("no-creative-break", "&cYou cannot break other players shops in creative mode.  Use survival instead or use SuperTool ({0}).");
            setAndUpdate("command.now-debuging", "&aSuccessfully switch to developer mode, Reloading QuickShop...");
            setAndUpdate("command.now-nolonger-debuging", "&aSuccessfully switch to production mode, Reloading QuickShop...");
            setAndUpdate("language-version", 7);
            selectedVersion = 7;

        }
        if (selectedVersion == 7) {
            setAndUpdate("failed-to-put-sign", "&cNo enough space around the shop to place infomation sign.");
            setAndUpdate("language-version", 8);
            selectedVersion = 8;

        }
        if (selectedVersion == 8) {
            messagei18n
                    .set("failed-to-paste", "&cFailed upload data to Pastebin, Check the internet and try again. (See console for details)");
            messagei18n
                    .set("warn-to-paste", "&eCollecting data and upload to Pastebin, this may need a while. &c&lWarning&c, The data is keep public one week, it may leak your server configuration, make sure you only send it to your &ltrusted staff/developer.");
            setAndUpdate("command.description.paste", "&eAuto upload server data to Pastebin");
            setAndUpdate("language-version", 9);
            selectedVersion = 9;

        }
        if (selectedVersion == 9) {
            setAndUpdate("controlpanel.commands.setowner", "/qs owner [Player]");
            setAndUpdate("controlpanel.commands.unlimited", "/qs slientunlimited {0} {1} {2} {3}");
            setAndUpdate("controlpanel.commands.buy", "/qs silentbuy {0} {1} {2} {3}");
            setAndUpdate("controlpanel.commands.sell", "/qs silentsell {0} {1} {2} {3}");
            setAndUpdate("controlpanel.commands.price", "/qs price [New Price]");
            setAndUpdate("controlpanel.commands.refill", "/qs refill [Amount]");
            setAndUpdate("controlpanel.commands.empty", "/qs silentempty {0} {1} {2} {3}");
            setAndUpdate("controlpanel.commands.remove", "/qs silentremove {0} {1} {2} {3}");
            setAndUpdate("tableformat.full_line", "+---------------------------------------------------+");
            setAndUpdate("tableformat.left_half_line", "+--------------------");
            setAndUpdate("tableformat.right_half_line", "--------------------+");
            setAndUpdate("tableformat.left_begin", "| ");
            setAndUpdate("booleanformat.success", "&a✔");
            setAndUpdate("booleanformat.failed", "&c✘");
            setAndUpdate("language-version", 10);
            selectedVersion = 10;

        }
        if (selectedVersion == 10) {
            setAndUpdate("price-too-high", "&cShop price too high! You can't create price higher than {0} shop.");
            setAndUpdate("language-version", 11);
            selectedVersion = 11;

        }
        if (selectedVersion == 11) {
            setAndUpdate("unknown-player", "&cTarget player not exist, please check username your typed.");
            setAndUpdate("shop-staff-cleared", "&aSuccessfully remove all staff for your shop.");
            setAndUpdate("shop-staff-added", "&aSuccessfully add {0} to your shop staffs.");
            setAndUpdate("shop-staff-deleted", "&aSuccessfully remove {0} to your shop staffs.");
            setAndUpdate("command.wrong-args", "&cParameters not matched, use /qs help to check help");
            setAndUpdate("command.description.staff", "&eManage your shop staffs.");
            setAndUpdate("unknown-player", "&cTarget player not exist, please check username your typed.");
            setAndUpdate("language-version", 12);
            selectedVersion = 12;

        }
        if (selectedVersion == 12) {
            setAndUpdate("menu.commands.preview", "/qs silentpreview {0} {1} {2} {3}");
            setAndUpdate("shop-staff-cleared", "&aSuccessfully remove all staff for your shop.");
            setAndUpdate("shop-staff-added", "&aSuccessfully add {0} to your shop staffs.");
            setAndUpdate("shop-staff-deleted", "&aSuccessfully remove {0} to your shop staffs.");
            setAndUpdate("command.wrong-args", "&cParameters not matched, use /qs help to check help");
            setAndUpdate("command.description.staff", "&eManage your shop staffs.");
            setAndUpdate("unknown-player", "&cTarget player not exist, please check username your typed.");
            setAndUpdate("language-version", 13);
            selectedVersion = 13;

        }
        if (selectedVersion == 13) {
            setAndUpdate("no-permission-build", "&cYou can't build shop here.");
            setAndUpdate("success-change-owner-to-server", "&aSuccessfully set shop owner to Server.");
            setAndUpdate("updatenotify.buttontitle", "[Update Now]");
            List<String> notifylist = new ArrayList<>();
            notifylist.add("{0} is released, You still using {1}!");
            notifylist.add("Boom! New update {0} incoming, Update!");
            notifylist.add("Surprise! {0} come out, you are on {1}");
            notifylist.add("Looks you need update, {0} is updated!");
            notifylist.add("Ooops! {0} new released, you are {1}!");
            notifylist.add("I promise, QS updated {0}, why not update?");
            notifylist.add("Fixing and re... Sorry {0} is updated!");
            notifylist.add("Err! Nope, not error, just {0} updated!");
            notifylist.add("OMG! {0} come out! Why you still use {1}?");
            notifylist.add("Today News: QuickShop updated {0}!");
            notifylist.add("Plugin K.I.A, You should update {0}!");
            notifylist.add("Fuze is fuzeing update {0}, save update!");
            notifylist.add("You are update commander, told u {0} come out!");
            notifylist.add("Look me style---{0} updated, you still {1}");
            notifylist.add("Ahhhhhhh! New update {0}! Update!");
            notifylist.add("What U thinking? {0} released! Update!");
            setAndUpdate("updatenotify.list", notifylist);
            setAndUpdate("language-version", 14);
            selectedVersion = 14;

        }
        if (selectedVersion == 14) {
            setAndUpdate("flush-finished", "&aSuccessfully flushed the messages.");
            setAndUpdate("language-version", 15);
            selectedVersion = 15;

        }
        if (selectedVersion == 15) {
            setAndUpdate("purchase-failed", "&cPurchase failed: Internal Error, please contact the server administrator..");
            setAndUpdate("language-version", 16);
            selectedVersion = 16;

        }
        if (selectedVersion == 16) {
            setAndUpdate("command.description.owner", "&eChanges who owns a shop");
            setAndUpdate("command.description.remove", "&eRemove your looking the shop");
            setAndUpdate("command.description.amount", "&eExecute for your actions with amount(For chat plugin issue)");
            setAndUpdate("command.description.about", "&eShow QuickShop abouts");
            setAndUpdate("command.description.help", "&eShow QuickShop helps");
            setAndUpdate("no-pending-action", "&cYou do not have any pending action");
            setAndUpdate("language-version", 17);
            selectedVersion = 17;

        }
        if (selectedVersion == 17) {
            setAndUpdate("updatenotify.onekeybuttontitle", "[OneKey Update]");
            setAndUpdate("language-version", 18);
            selectedVersion = 18;

        }
        if (selectedVersion == 18) {
            setAndUpdate("command.description.supercreate", "&eCreate a shop bypass all protection checks");
            setAndUpdate("language-version", 19);
            selectedVersion = 19;

        }
        if (selectedVersion == 19) {
            setAndUpdate("permission-denied-3rd-party", "&cPermission denied: 3rd party plugin [{0}].");
            setAndUpdate("updatenotify.remote-disable-warning", "&cThis version of QuickShop is marked disabled by remote server, that mean this version may have serious problem, get details from our SpigotMC page: {0}. This warning will appear and spam your console until you use other not disabled version to replace this one, doesn't effect your server running.");
            setAndUpdate("language-version", 20);
            selectedVersion = 20;
        }
        if (selectedVersion == 20) {
            setAndUpdate("how-many-buy", "&aEnter how many you wish to &bBUY&a in chat. Enter &ball&a to buy them all.");
            setAndUpdate("how-many-sell", "&aEnter how many you wish to &dSELL&a in chat. You have &e{0}&a available. Enter &ball&a to sell them all.");
            setAndUpdate("updatenotify.label.unstable", "[Unstable]");
            setAndUpdate("updatenotify.label.stable", "[Stable]");
            setAndUpdate("updatenotify.label.lts", "[LTS]");
            setAndUpdate("updatenotify.label.qualityverifyed", "[Quality]");
            setAndUpdate("updatenotify.label.github", "[Github]");
            setAndUpdate("updatenotify.label.spigotmc", "[SpigotMC]");
            setAndUpdate("updatenotify.label.bukkitdev", "[BukkitDev]");
            setAndUpdate("language-version", 21);
            selectedVersion = 21;
        }
        if (selectedVersion == 21) {
            setAndUpdate("shop-removed-cause-ongoing-fee", "&cYou shop at {0} was removed cause you had no enough money to keep it!");
            setAndUpdate("language-version", 22);
            selectedVersion = 22;
        }
        if (selectedVersion == 22) {
            setAndUpdate("not-a-number", "&cThere can only be number, but you input {0}");
            setAndUpdate("not-a-integer", "&cThere can only be integer, but you input {0}");
            setAndUpdate("language-version", 23);
            selectedVersion = 23;
        }
        if (selectedVersion == 23) {
            setAndUpdate("command.toggle-unlimited.unlimited", "&aShop is now unlimited}");
            setAndUpdate("command.toggle-unlimited.limited", "&aShop is now limited");
            setAndUpdate("language-version", 24);
            selectedVersion = 24;
        }
        if (selectedVersion == 24) {
            setAndUpdate("digits-reach-the-limit", "&cYou have reach the limit of the digits after the dot in price.");
            setAndUpdate("language-version", 25);
            selectedVersion = 25;
        }
        if (selectedVersion == 25) {
            setAndUpdate("complete", "&aComplete!");
            setAndUpdate("language-version", 26);
            selectedVersion = 26;
        }
        messagei18n.save();
    }

    public static void setAndUpdate(@NotNull String path, @Nullable Object object) {
        if (object == null) {
            messagei18n.set(path, null); //Removal
        }
        Object objFromBuiltIn = builtInDefaultLanguage.get(path); //Apply english default
        if (objFromBuiltIn == null) {
            objFromBuiltIn = object; //Apply hard-code default, maybe a language file i forgotten update??
        }
        messagei18n.set(path, objFromBuiltIn);
    }
    public static IFile getI18nFile() {
        return messagei18n;
    }

}
