/*
 * This file is a part of project QuickShop, the name is ConfigurationUpdater.java
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

package org.maxgamer.quickshop.util.config;

import de.leonhard.storage.Yaml;
import lombok.Getter;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.Util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class ConfigurationUpdater {
    private final QuickShop plugin;
    @Getter
    private final Yaml configuration;

    public ConfigurationUpdater(QuickShop plugin) {
        this.plugin = plugin;
        this.configuration = plugin.getConfiguration();
    }

    private void writeServerUniqueId() {
        String serverUUID = getConfiguration().getString("server-uuid");
        if (serverUUID == null || serverUUID.isEmpty()) {
            UUID uuid = UUID.randomUUID();
            serverUUID = uuid.toString();
            getConfiguration().set("server-uuid", serverUUID);
        }
        plugin.saveConfiguration();
    }

    public void update() {
        Util.debugLog("Starting configuration update...");
        writeServerUniqueId();

        for (Method updateScript : getUpdateScripts()) {
            try {
                ConfigUpdater configUpdater = updateScript.getAnnotation(ConfigUpdater.class);
                int current = getConfiguration().getInt("config-version");
                if (current >= configUpdater.version()) {
                    continue;
                }
                Util.debugLog("Executing " + updateScript.getName() + " for version " + configUpdater.version());
                if (updateScript.getParameterCount() == 0) {
                    updateScript.invoke(this);
                }
                if (updateScript.getParameterCount() == 1 && (updateScript.getParameterTypes()[0] == int.class || updateScript.getParameterTypes()[0] == Integer.class)) {
                    updateScript.invoke(this, current);
                }
                getConfiguration().set("config-version", configUpdater.version() + 1);
            } catch (Throwable throwable) {
                plugin.getLogger().log(Level.WARNING, "Failed execute update script " + updateScript.getName() + " for updating to version " + updateScript.getAnnotation(ConfigUpdater.class).version() + ", some configuration options may missing or outdated", throwable);
            }
        }
        saveConfig();
    }

    public List<Method> getUpdateScripts() {
        List<Method> methods = new ArrayList<>();
        for (Method declaredMethod : this.getClass().getDeclaredMethods()) {
            if (declaredMethod.getAnnotation(ConfigUpdater.class) == null) {
                continue;
            }
            methods.add(declaredMethod);
        }
        methods.sort(Comparator.comparingInt(o -> o.getAnnotation(ConfigUpdater.class).version()));
        return methods;
    }

    private void saveConfig() {
        plugin.saveConfiguration();
    }
//
//    @ConfigUpdater(version = 1)
//    private void addBStats() {
//        getConfiguration().set("disabled-metrics", false);
//    }
//
//    @ConfigUpdater(version = 2)
//    private void addProtects() {
//        getConfiguration().set("protect.minecart", true);
//        getConfiguration().set("protect.entity", true);
//        getConfiguration().set("protect.redstone", true);
//        getConfiguration().set("protect.structuregrow", true);
//        getConfiguration().set("protect.explode", true);
//        getConfiguration().set("protect.hopper", true);
//    }
//
//    @ConfigUpdater(version = 2)
//    private void addAlternateCurrencySymbol() {
//        getConfiguration().set("shop.alternate-currency-symbol", '$');
//    }
//
//    @ConfigUpdater(version = 3)
//    private void addUpdater() {
//        getConfiguration().set("updater", true);
//    }
//
//    @ConfigUpdater(version = 4)
//    private void addSneakControl() {
//        getConfiguration().set("shop.sneak-to-control", false);
//    }
//
//    @ConfigUpdater(version = 6)
//    private void addDatabaseTablePrefix() {
//        getConfiguration().set("database.prefix", "none");
//    }
//
//    @ConfigUpdater(version = 8)
//    private void addFastSearchAlgorithmAndPlib() {
//        getConfiguration().set("limits.old-algorithm", false);
//        getConfiguration().set("plugin.ProtocolLib", false);
//        getConfiguration().set("shop.ignore-unlimited", false);
//    }
//
//    @ConfigUpdater(version = 9)
//    private void addEnderChestShop() {
//        getConfiguration().set("shop.enable-enderchest", true);
//    }
//
//    @ConfigUpdater(version = 10)
//    private void removedOptionToPayUnlimtiedShopOwner() {
//        getConfiguration().set("shop.pay-player-from-unlimited-shop-owner", null);
//    }
//
//    @ConfigUpdater(version = 10)
//    private void addEnderChestShop_R() {
//        getConfiguration().set("shop.enable-enderchest", null); // Removed
//        getConfiguration().set("plugin.OpenInv", true);
//        List<String> shoppable = getConfiguration().getStringList("shop-blocks");
//        shoppable.add("ENDER_CHEST");
//        getConfiguration().set("shop-blocks", shoppable);
//    }
//    @ConfigUpdater(version = 10)
//    private void addPlib_() {
//
//    }
//
//    private void updateConfig(int selectedVersion) throws IOException {
//        if (selectedVersion == 11) {
//
//            selectedVersion = 12;
//        }
//        if (selectedVersion == 12) {
//            getConfiguration().set("plugin.ProtocolLib", null); // Removed
//            getConfiguration().set("plugin.BKCommonLib", null); // Removed
//            getConfiguration().set("database.use-varchar", null); // Removed
//            getConfiguration().set("database.reconnect", null); // Removed
//            getConfiguration().set("display-items-check-ticks", 1200);
//            getConfiguration().set("shop.bypass-owner-check", null); // Removed
//            getConfiguration().set("config-version", 13);
//            selectedVersion = 13;
//        }
//        if (selectedVersion == 13) {
//            getConfiguration().set("config-version", 14);
//            selectedVersion = 14;
//        }
//        if (selectedVersion == 14) {
//            getConfiguration().set("plugin.AreaShop", null);
//            getConfiguration().set("shop.special-region-only", null);
//            getConfiguration().set("config-version", 15);
//            selectedVersion = 15;
//        }
//        if (selectedVersion == 15) {
//            getConfiguration().set("ongoingfee", null);
//            getConfiguration().set("shop.display-item-show-name", false);
//            getConfiguration().set("shop.auto-fetch-shop-messages", true);
//            getConfiguration().set("config-version", 16);
//            selectedVersion = 16;
//        }
//        if (selectedVersion == 16) {
//            getConfiguration().set("config-version", 17);
//            selectedVersion = 17;
//        }
//        if (selectedVersion == 17) {
//            getConfiguration().set("ignore-cancel-chat-event", false);
//            getConfiguration().set("float", null);
//            getConfiguration().set("config-version", 18);
//            selectedVersion = 18;
//        }
//        if (selectedVersion == 18) {
//            getConfiguration().set("shop.disable-vault-format", false);
//            getConfiguration().set("config-version", 19);
//            selectedVersion = 19;
//        }
//        if (selectedVersion == 19) {
//            getConfiguration().set("shop.allow-shop-without-space-for-sign", true);
//            getConfiguration().set("config-version", 20);
//            selectedVersion = 20;
//        }
//        if (selectedVersion == 20) {
//            getConfiguration().set("shop.maximum-price", -1);
//            getConfiguration().set("config-version", 21);
//            selectedVersion = 21;
//        }
//        if (selectedVersion == 21) {
//            getConfiguration().set("shop.sign-material", "OAK_WALL_SIGN");
//            getConfiguration().set("config-version", 22);
//            selectedVersion = 22;
//        }
//        if (selectedVersion == 22) {
//            getConfiguration().set("include-offlineplayer-list", "false");
//            getConfiguration().set("config-version", 23);
//            selectedVersion = 23;
//        }
//        if (selectedVersion == 23) {
//            getConfiguration().set("lockette.enable", null);
//            getConfiguration().set("lockette.item", null);
//            getConfiguration().set("lockette.lore", null);
//            getConfiguration().set("lockette.displayname", null);
//            getConfiguration().set("float", null);
//            getConfiguration().set("lockette.enable", true);
//            getConfiguration().set("shop.blacklist-world", Lists.newArrayList("disabled_world_name"));
//            getConfiguration().set("config-version", 24);
//            selectedVersion = 24;
//        }
//        if (selectedVersion == 24) {
//            getConfiguration().set("config-version", 25);
//            selectedVersion = 25;
//        }
//        if (selectedVersion == 25) {
//            String language = getConfiguration().getString("language");
//            if (language == null || language.isEmpty() || "default".equals(language)) {
//                getConfiguration().set("language", "en");
//            }
//            getConfiguration().set("config-version", 26);
//            selectedVersion = 26;
//        }
//        if (selectedVersion == 26) {
//            getConfiguration().set("database.usessl", false);
//            getConfiguration().set("config-version", 27);
//            selectedVersion = 27;
//        }
//        if (selectedVersion == 27) {
//            getConfiguration().set("queue.enable", true);
//            getConfiguration().set("queue.shops-per-tick", 20);
//            getConfiguration().set("config-version", 28);
//            selectedVersion = 28;
//        }
//        if (selectedVersion == 28) {
//            getConfiguration().set("database.queue", true);
//            getConfiguration().set("config-version", 29);
//            selectedVersion = 29;
//        }
//        if (selectedVersion == 29) {
//            getConfiguration().set("plugin.Multiverse-Core", null);
//            getConfiguration().set("shop.protection-checking", true);
//            getConfiguration().set("config-version", 30);
//            selectedVersion = 30;
//        }
//        if (selectedVersion == 30) {
//            getConfiguration().set("auto-report-errors", true);
//            getConfiguration().set("config-version", 31);
//            selectedVersion = 31;
//        }
//        if (selectedVersion == 31) {
//            getConfiguration().set("shop.display-type", 0);
//            getConfiguration().set("config-version", 32);
//            selectedVersion = 32;
//        }
//        if (selectedVersion == 32) {
//            getConfiguration().set("effect.sound.ontabcomplete", true);
//            getConfiguration().set("effect.sound.oncommand", true);
//            getConfiguration().set("effect.sound.ononclick", true);
//            getConfiguration().set("config-version", 33);
//            selectedVersion = 33;
//        }
//        if (selectedVersion == 33) {
//            getConfiguration().set("matcher.item.damage", true);
//            getConfiguration().set("matcher.item.displayname", true);
//            getConfiguration().set("matcher.item.lores", true);
//            getConfiguration().set("matcher.item.enchs", true);
//            getConfiguration().set("matcher.item.potions", true);
//            getConfiguration().set("matcher.item.attributes", true);
//            getConfiguration().set("matcher.item.itemflags", true);
//            getConfiguration().set("matcher.item.custommodeldata", true);
//            getConfiguration().set("config-version", 34);
//            selectedVersion = 34;
//        }
//        if (selectedVersion == 34) {
//            if (getConfiguration().getInt("shop.display-items-check-ticks") == 1200) {
//                getConfiguration().set("shop.display-items-check-ticks", 6000);
//            }
//            getConfiguration().set("config-version", 35);
//            selectedVersion = 35;
//        }
//        if (selectedVersion == 35) {
//            getConfiguration().set("queue", null); // Close it for everyone
//            getConfiguration().set("config-version", 36);
//            selectedVersion = 36;
//        }
//        if (selectedVersion == 36) {
//            getConfiguration().set("economy-type", 0); // Close it for everyone
//            getConfiguration().set("config-version", 37);
//            selectedVersion = 37;
//        }
//        if (selectedVersion == 37) {
//            getConfiguration().set("shop.ignore-cancel-chat-event", true);
//            getConfiguration().set("config-version", 38);
//            selectedVersion = 38;
//        }
//        if (selectedVersion == 38) {
//            getConfiguration().set("protect.inventorymove", true);
//            getConfiguration().set("protect.spread", true);
//            getConfiguration().set("protect.fromto", true);
//            getConfiguration().set("protect.minecart", null);
//            getConfiguration().set("protect.hopper", null);
//            getConfiguration().set("config-version", 39);
//            selectedVersion = 39;
//        }
//        if (selectedVersion == 39) {
//            getConfiguration().set("update-sign-when-inventory-moving", true);
//            getConfiguration().set("config-version", 40);
//            selectedVersion = 40;
//        }
//        if (selectedVersion == 40) {
//            getConfiguration().set("allow-economy-loan", false);
//            getConfiguration().set("config-version", 41);
//            selectedVersion = 41;
//        }
//        if (selectedVersion == 41) {
//            getConfiguration().set("send-display-item-protection-alert", true);
//            getConfiguration().set("config-version", 42);
//            selectedVersion = 42;
//        }
//        if (selectedVersion == 42) {
//            getConfiguration().set("config-version", 43);
//            selectedVersion = 43;
//        }
//        if (selectedVersion == 43) {
//            getConfiguration().set("config-version", 44);
//            selectedVersion = 44;
//        }
//        if (selectedVersion == 44) {
//            getConfiguration().set("matcher.item.repaircost", false);
//            getConfiguration().set("config-version", 45);
//            selectedVersion = 45;
//        }
//        if (selectedVersion == 45) {
//            getConfiguration().set("shop.display-item-use-name", true);
//            getConfiguration().set("config-version", 46);
//            selectedVersion = 46;
//        }
//        if (selectedVersion == 46) {
//            getConfiguration().set("shop.max-shops-checks-in-once", 100);
//            getConfiguration().set("config-version", 47);
//            selectedVersion = 47;
//        }
//        if (selectedVersion == 47) {
//            getConfiguration().set("config-version", 48);
//            selectedVersion = 48;
//        }
//        if (selectedVersion == 48) {
//            getConfiguration().set("permission-type", null);
//            getConfiguration().set("shop.use-protection-checking-filter", null);
//            getConfiguration().set("shop.protection-checking-filter", null);
//            getConfiguration().set("config-version", 49);
//            selectedVersion = 49;
//        }
//        if (selectedVersion == 49 || selectedVersion == 50) {
//            getConfiguration().set("shop.enchance-display-protect", false);
//            getConfiguration().set("shop.enchance-shop-protect", false);
//            getConfiguration().set("protect", null);
//            getConfiguration().set("config-version", 51);
//            selectedVersion = 51;
//        }
//        if (selectedVersion < 60) { // Ahhh fuck versions
//            getConfiguration().set("config-version", 60);
//            selectedVersion = 60;
//        }
//        if (selectedVersion == 60) { // Ahhh fuck versions
//            getConfiguration().set("shop.strict-matches-check", null);
//            getConfiguration().set("shop.display-auto-despawn", true);
//            getConfiguration().set("shop.display-despawn-range", 10);
//            getConfiguration().set("shop.display-check-time", 10);
//            getConfiguration().set("config-version", 61);
//            selectedVersion = 61;
//        }
//        if (selectedVersion == 61) { // Ahhh fuck versions
//            getConfiguration().set("shop.word-for-sell-all-items", "all");
//            getConfiguration().set("plugin.PlaceHolderAPI", true);
//            getConfiguration().set("config-version", 62);
//            selectedVersion = 62;
//        }
//        if (selectedVersion == 62) { // Ahhh fuck versions
//            getConfiguration().set("shop.display-auto-despawn", false);
//            getConfiguration().set("shop.word-for-trade-all-items", getConfiguration().getString("shop.word-for-sell-all-items"));
//
//            getConfiguration().set("config-version", 63);
//            selectedVersion = 63;
//        }
//        if (selectedVersion == 63) { // Ahhh fuck versions
//            getConfiguration().set("shop.ongoing-fee.enable", false);
//            getConfiguration().set("shop.ongoing-fee.ticks", 42000);
//            getConfiguration().set("shop.ongoing-fee.cost-per-shop", 2);
//            getConfiguration().set("shop.ongoing-fee.ignore-unlimited", true);
//            getConfiguration().set("config-version", 64);
//            selectedVersion = 64;
//        }
//        if (selectedVersion == 64) {
//            getConfiguration().set("shop.allow-free-shop", false);
//            getConfiguration().set("config-version", 65);
//            selectedVersion = 65;
//        }
//        if (selectedVersion == 65) {
//            getConfiguration().set("shop.minimum-price", 0.01);
//            getConfiguration().set("config-version", 66);
//            selectedVersion = 66;
//        }
//        if (selectedVersion == 66) {
//            getConfiguration().set("use-decimal-format", false);
//            getConfiguration().set("decimal-format", "#,###.##");
//            getConfiguration().set("shop.show-owner-uuid-in-controlpanel-if-op", false);
//            getConfiguration().set("config-version", 67);
//            selectedVersion = 67;
//        }
//        if (selectedVersion == 67) {
//            getConfiguration().set("disable-debuglogger", false);
//            getConfiguration().set("matcher.use-bukkit-matcher", null);
//            getConfiguration().set("config-version", 68);
//            selectedVersion = 68;
//        }
//        if (selectedVersion == 68) {
//            getConfiguration().set("shop.blacklist-lores", Lists.newArrayList("SoulBound"));
//            getConfiguration().set("config-version", 69);
//            selectedVersion = 69;
//        }
//        if (selectedVersion == 69) {
//            getConfiguration().set("shop.display-item-use-name", false);
//            getConfiguration().set("config-version", 70);
//            selectedVersion = 70;
//        }
//        if (selectedVersion == 70) {
//            getConfiguration().set("cachingpool.enable", false);
//            getConfiguration().set("cachingpool.maxsize", 100000000);
//            getConfiguration().set("config-version", 71);
//            selectedVersion = 71;
//        }
//        if (selectedVersion == 71) {
//            if (Objects.equals(getConfiguration().getString("language"), "en")) {
//                getConfiguration().set("language", "en-US");
//            }
//            getConfiguration().set("server-platform", 0);
//            getConfiguration().set("config-version", 72);
//            selectedVersion = 72;
//        }
//        if (selectedVersion == 72) {
//            if (getConfiguration().getBoolean("use-deciaml-format")) {
//                getConfiguration().set("use-decimal-format", getConfiguration().getBoolean("use-deciaml-format"));
//            } else {
//                getConfiguration().set("use-decimal-format", false);
//            }
//            getConfiguration().set("use-deciaml-format", null);
//
//            getConfiguration().set("shop.force-load-downgrade-items.enable", false);
//            getConfiguration().set("shop.force-load-downgrade-items.method", 0);
//            getConfiguration().set("config-version", 73);
//            selectedVersion = 73;
//        }
//        if (selectedVersion == 73) {
//            getConfiguration().set("mixedeconomy.deposit", "eco give {0} {1}");
//            getConfiguration().set("mixedeconomy.withdraw", "eco take {0} {1}");
//            getConfiguration().set("config-version", 74);
//            selectedVersion = 74;
//        }
//        if (selectedVersion == 74) {
//            String langUtilsLanguage = getConfiguration().getString("langutils-language", "en_us");
//            getConfiguration().set("langutils-language", null);
//            if ("en_us".equals(langUtilsLanguage)) {
//                langUtilsLanguage = "default";
//            }
//            getConfiguration().set("game-language", langUtilsLanguage);
//            getConfiguration().set("maximum-digits-in-price", -1);
//            getConfiguration().set("config-version", 75);
//            selectedVersion = 75;
//        }
//        if (selectedVersion == 75) {
//            getConfiguration().set("langutils-language", null);
//            if (getConfiguration().getString("game-language") == null) {
//                getConfiguration().set("game-language", "default");
//            }
//            getConfiguration().set("config-version", 76);
//            selectedVersion = 76;
//        }
//        if (selectedVersion == 76) {
//            getConfiguration().set("database.auto-fix-encoding-issue-in-database", false);
//            getConfiguration().set("send-shop-protection-alert", false);
//            getConfiguration().set("send-display-item-protection-alert", false);
//            getConfiguration().set("shop.use-fast-shop-search-algorithm", false);
//            getConfiguration().set("config-version", 77);
//            selectedVersion = 77;
//        }
//        if (selectedVersion == 77) {
//            getConfiguration().set("integration.towny.enable", false);
//            getConfiguration().set("integration.towny.create", new String[]{"SHOPTYPE", "MODIFY"});
//            getConfiguration().set("integration.towny.trade", new String[]{});
//            getConfiguration().set("integration.worldguard.enable", false);
//            getConfiguration().set("integration.worldguard.create", new String[]{"FLAG", "CHEST_ACCESS"});
//            getConfiguration().set("integration.worldguard.trade", new String[]{});
//            getConfiguration().set("integration.plotsquared.enable", false);
//            getConfiguration().set("integration.plotsquared.enable", false);
//            getConfiguration().set("integration.plotsquared.enable", false);
//            getConfiguration().set("integration.residence.enable", false);
//            getConfiguration().set("integration.residence.create", new String[]{"FLAG", "interact", "use"});
//            getConfiguration().set("integration.residence.trade", new String[]{});
//
//            getConfiguration().set("integration.factions.enable", false);
//            getConfiguration().set("integration.factions.create.flag", new String[]{});
//            getConfiguration().set("integration.factions.trade.flag", new String[]{});
//            getConfiguration().set("integration.factions.create.require.open", false);
//            getConfiguration().set("integration.factions.create.require.normal", true);
//            getConfiguration().set("integration.factions.create.require.wilderness", false);
//            getConfiguration().set("integration.factions.create.require.peaceful", true);
//            getConfiguration().set("integration.factions.create.require.permanent", false);
//            getConfiguration().set("integration.factions.create.require.safezone", false);
//            getConfiguration().set("integration.factions.create.require.own", false);
//            getConfiguration().set("integration.factions.create.require.warzone", false);
//            getConfiguration().set("integration.factions.trade.require.open", false);
//            getConfiguration().set("integration.factions.trade.require.normal", true);
//            getConfiguration().set("integration.factions.trade.require.wilderness", false);
//            getConfiguration().set("integration.factions.trade.require.peaceful", false);
//            getConfiguration().set("integration.factions.trade.require.permanent", false);
//            getConfiguration().set("integration.factions.trade.require.safezone", false);
//            getConfiguration().set("integration.factions.trade.require.own", false);
//            getConfiguration().set("integration.factions.trade.require.warzone", false);
//            getConfiguration().set("anonymous-metrics", null);
//            getConfiguration().set("shop.ongoing-fee.async", true);
//            getConfiguration().set("config-version", 78);
//            selectedVersion = 78;
//        }
//        if (selectedVersion == 78) {
//            getConfiguration().set("shop.display-type-specifics", null);
//            getConfiguration().set("config-version", 79);
//            selectedVersion = 79;
//        }
//        if (selectedVersion == 79) {
//            getConfiguration().set("matcher.item.books", true);
//            getConfiguration().set("config-version", 80);
//            selectedVersion = 80;
//        }
//        if (selectedVersion == 80) {
//            getConfiguration().set("shop.use-fast-shop-search-algorithm", true);
//            getConfiguration().set("config-version", 81);
//            selectedVersion = 81;
//        }
//        if (selectedVersion == 81) {
//            getConfiguration().set("config-version", 82);
//            selectedVersion = 82;
//        }
//        if (selectedVersion == 82) {
//            getConfiguration().set("matcher.item.banner", true);
//            getConfiguration().set("config-version", 83);
//            selectedVersion = 83;
//        }
//        if (selectedVersion == 83) {
//            getConfiguration().set("matcher.item.banner", true);
//            getConfiguration().set("protect.explode", true);
//            getConfiguration().set("config-version", 84);
//            selectedVersion = 84;
//        }
//        if (selectedVersion == 84) {
//            getConfiguration().set("disable-debuglogger", null);
//            getConfiguration().set("config-version", 85);
//            selectedVersion = 85;
//        }
//        if (selectedVersion == 85) {
//            getConfiguration().set("config-version", 86);
//            selectedVersion = 86;
//        }
//        if (selectedVersion == 86) {
//            getConfiguration().set("shop.use-fast-shop-search-algorithm", true);
//            getConfiguration().set("config-version", 87);
//            selectedVersion = 87;
//        }
//        if (selectedVersion == 87) {
//            getConfiguration().set("plugin.BlockHub.enable", true);
//            getConfiguration().set("plugin.BlockHub.only", false);
//            if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
//                getConfiguration().set("shop.display-type", 2);
//            }
//            getConfiguration().set("config-version", 88);
//            selectedVersion = 88;
//        }
//        if (selectedVersion == 88) {
//            getConfiguration().set("respect-item-flag", true);
//            getConfiguration().set("config-version", 89);
//            selectedVersion = 89;
//        }
//        if (selectedVersion == 89) {
//            getConfiguration().set("use-caching", true);
//            getConfiguration().set("config-version", 90);
//            selectedVersion = 90;
//        }
//        if (selectedVersion == 90) {
//            getConfiguration().set("protect.hopper", true);
//            getConfiguration().set("config-version", 91);
//            selectedVersion = 91;
//        }
//        if (selectedVersion == 91) {
//            getConfiguration().set("database.queue-commit-interval", 2);
//            getConfiguration().set("config-version", 92);
//            selectedVersion = 92;
//        }
//        if (selectedVersion == 92) {
//            getConfiguration().set("send-display-item-protection-alert", false);
//            getConfiguration().set("send-shop-protection-alert", false);
//            getConfiguration().set("disable-creative-mode-trading", false);
//            getConfiguration().set("disable-super-tool", false);
//            getConfiguration().set("allow-owner-break-shop-sign", false);
//            getConfiguration().set("matcher.item.skull", true);
//            getConfiguration().set("matcher.item.firework", true);
//            getConfiguration().set("matcher.item.map", true);
//            getConfiguration().set("matcher.item.leatherArmor", true);
//            getConfiguration().set("matcher.item.fishBucket", true);
//            getConfiguration().set("matcher.item.suspiciousStew", true);
//            getConfiguration().set("matcher.item.shulkerBox", true);
//            getConfiguration().set("config-version", 93);
//            selectedVersion = 93;
//        }
//        if (selectedVersion == 93) {
//            getConfiguration().set("disable-creative-mode-trading", null);
//            getConfiguration().set("disable-super-tool", null);
//            getConfiguration().set("allow-owner-break-shop-sign", null);
//            getConfiguration().set("shop.disable-creative-mode-trading", true);
//            getConfiguration().set("shop.disable-super-tool", true);
//            getConfiguration().set("shop.allow-owner-break-shop-sign", false);
//            getConfiguration().set("config-version", 94);
//            selectedVersion = 94;
//        }
//        if (selectedVersion == 94) {
//            if (getConfiguration().isSet("price-restriction")) {
//                getConfiguration().set("shop.price-restriction", getConfiguration().getStringList("price-restriction"));
//                getConfiguration().set("price-restriction", null);
//            } else {
//                getConfiguration().set("shop.price-restriction", new ArrayList<>(0));
//            }
//            getConfiguration().set("enable-log4j", null);
//            getConfiguration().set("config-version", 95);
//            selectedVersion = 95;
//        }
//        if (selectedVersion == 95) {
//            getConfiguration().set("shop.allow-stacks", false);
//            getConfiguration().set("shop.display-allow-stacks", false);
//            getConfiguration().set("custom-item-stacksize", new ArrayList<>(0));
//            getConfiguration().set("config-version", 96);
//            selectedVersion = 96;
//        }
//        if (selectedVersion == 96) {
//            getConfiguration().set("shop.deny-non-shop-items-to-shop-container", false);
//            getConfiguration().set("config-version", 97);
//            selectedVersion = 97;
//        }
//        if (selectedVersion == 97) {
//            getConfiguration().set("shop.disable-quick-create", false);
//            getConfiguration().set("config-version", 98);
//            selectedVersion = 98;
//        }
//        if (selectedVersion == 98) {
//            getConfiguration().set("config-version", 99);
//            selectedVersion = 99;
//        }
//        if (selectedVersion == 99) {
//            getConfiguration().set("shop.currency-symbol-on-right", false);
//            getConfiguration().set("config-version", 100);
//            selectedVersion = 100;
//        }
//        if (selectedVersion == 100) {
//            getConfiguration().set("integration.towny.ignore-disabled-worlds", false);
//            getConfiguration().set("config-version", 101);
//            selectedVersion = 101;
//        }
//        if (selectedVersion == 101) {
//            getConfiguration().set("matcher.work-type", 1);
//            getConfiguration().set("work-type", null);
//            getConfiguration().set("plugin.LWC", true);
//            getConfiguration().set("config-version", 102);
//            selectedVersion = 102;
//        }
//        if (selectedVersion == 102) {
//            getConfiguration().set("protect.entity", true);
//            getConfiguration().set("config-version", 103);
//            selectedVersion = 103;
//        }
//        if (selectedVersion == 103) {
//            getConfiguration().set("integration.worldguard.whitelist-mode", false);
//            getConfiguration().set("integration.factions.whitelist-mode", true);
//            getConfiguration().set("integration.plotsquared.whitelist-mode", true);
//            getConfiguration().set("integration.residence.whitelist-mode", true);
//            getConfiguration().set("config-version", 104);
//            selectedVersion = 104;
//        }
//        if (selectedVersion == 104) {
//            getConfiguration().set("cachingpool", null);
//            getConfiguration().set("config-version", 105);
//            selectedVersion = 105;
//        }
//        if (selectedVersion == 105) {
//            getConfiguration().set("shop.interact.sneak-to-create", getConfiguration().getBoolean("shop.sneak-to-create"));
//            getConfiguration().set("shop.sneak-to-create", null);
//            getConfiguration().set("shop.interact.sneak-to-trade", getConfiguration().getBoolean("shop.sneak-to-trade"));
//            getConfiguration().set("shop.sneak-to-trade", null);
//            getConfiguration().set("shop.interact.sneak-to-control", getConfiguration().getBoolean("shop.sneak-to-control"));
//            getConfiguration().set("shop.sneak-to-control", null);
//            getConfiguration().set("config-version", 106);
//            selectedVersion = 106;
//        }
//        if (selectedVersion == 106) {
//            getConfiguration().set("shop.use-enchantment-for-enchanted-book", false);
//            getConfiguration().set("config-version", 107);
//            selectedVersion = 107;
//        }
//        if (selectedVersion == 107) {
//            getConfiguration().set("integration.lands.enable", false);
//            getConfiguration().set("integration.lands.whitelist-mode", false);
//            getConfiguration().set("integration.lands.ignore-disabled-worlds", true);
//            getConfiguration().set("config-version", 108);
//            selectedVersion = 108;
//        }
//        if (selectedVersion == 108) {
//            getConfiguration().set("debug.shop-deletion", false);
//            getConfiguration().set("config-version", 109);
//            selectedVersion = 109;
//        }
//        if (selectedVersion == 109) {
//            getConfiguration().set("shop.protection-checking-blacklist", Collections.singletonList("disabled_world"));
//            getConfiguration().set("config-version", 110);
//            selectedVersion = 110;
//        }
//        if (selectedVersion == 110) {
//            getConfiguration().set("integration.worldguard.any-owner", true);
//            getConfiguration().set("config-version", 111);
//            selectedVersion = 111;
//        }
//        if (selectedVersion == 111) {
//            getConfiguration().set("logging.enable", getConfiguration().getBoolean("log-actions"));
//            getConfiguration().set("logging.log-actions", getConfiguration().getBoolean("log-actions"));
//            getConfiguration().set("logging.log-balance", true);
//            getConfiguration().set("logging.file-size", 10);
//            getConfiguration().set("debug.disable-debuglogger", false);
//            getConfiguration().set("trying-fix-banlance-insuffient", false);
//            getConfiguration().set("log-actions", null);
//            getConfiguration().set("config-version", 112);
//            selectedVersion = 112;
//        }
//        if (selectedVersion == 112) {
//            getConfiguration().set("integration.lands.delete-on-lose-permission", false);
//            getConfiguration().set("config-version", 113);
//            selectedVersion = 113;
//        }
//        if (selectedVersion == 113) {
//            getConfiguration().set("config-damaged", false);
//            getConfiguration().set("config-version", 114);
//            selectedVersion = 114;
//        }
//        if (selectedVersion == 114) {
//            getConfiguration().set("shop.interact.interact-mode", getConfiguration().getBoolean("shop.interact.switch-mode") ? 0 : 1);
//            getConfiguration().set("shop.interact.switch-mode", null);
//            getConfiguration().set("config-version", 115);
//            selectedVersion = 115;
//        }
//        if (selectedVersion == 115) {
//            getConfiguration().set("integration.griefprevention.enable", false);
//            getConfiguration().set("integration.griefprevention.whitelist-mode", false);
//            getConfiguration().set("integration.griefprevention.create", Collections.emptyList());
//            getConfiguration().set("integration.griefprevention.trade", Collections.emptyList());
//            getConfiguration().set("config-version", 116);
//            selectedVersion = 116;
//        }
//        if (selectedVersion == 116) {
//            getConfiguration().set("shop.sending-stock-message-to-staffs", false);
//            getConfiguration().set("integration.towny.delete-shop-on-resident-leave", false);
//            getConfiguration().set("config-version", 117);
//            selectedVersion = 117;
//        }
//        if (selectedVersion == 117) {
//            getConfiguration().set("shop.finding.distance", getConfiguration().getInt("shop.find-distance"));
//            getConfiguration().set("shop.finding.limit", 10);
//            getConfiguration().set("shop.find-distance", null);
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 118) {
//            getConfiguration().set("shop.finding.oldLogic", false);
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 119) {
//            getConfiguration().set("debug.adventure", false);
//            getConfiguration().set("shop.finding.all", false);
//            getConfiguration().set("chat-type", 0);
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 120) {
//            getConfiguration().set("shop.finding.exclude-out-of-stock", false);
//            getConfiguration().set("chat-type", 0);
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 121) {
//            getConfiguration().set("shop.protection-checking-handler", 0);
//            getConfiguration().set("shop.protection-checking-listener-blacklist", Collections.singletonList("ignored_listener"));
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 122) {
//            getConfiguration().set("currency", "");
//            getConfiguration().set("shop.alternate-currency-symbol-list", Arrays.asList("CNY;¥", "USD;$"));
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 123) {
//            getConfiguration().set("integration.fabledskyblock.enable", false);
//            getConfiguration().set("integration.fabledskyblock.whitelist-mode", false);
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 124) {
//            getConfiguration().set("plugin.BKCommonLib", true);
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 125) {
//            getConfiguration().set("integration.superiorskyblock.enable", false);
//            getConfiguration().set("integration.superiorskyblock.owner-create-only", false);
//            getConfiguration().set("integration.superiorskyblock.delete-shop-on-member-leave", true);
//            getConfiguration().set("shop.interact.swap-click-behavior", false);
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 126) {
//            getConfiguration().set("debug.delete-corrupt-shops", false);
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//
//        if (selectedVersion == 127) {
//            getConfiguration().set("integration.plotsquared.delete-when-user-untrusted", true);
//            getConfiguration().set("integration.towny.delete-shop-on-plot-clear", true);
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 128) {
//            getConfiguration().set("shop.force-use-item-original-name", false);
//            getConfiguration().set("integration.griefprevention.delete-on-untrusted", false);
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//
//        if (selectedVersion == 129) {
//            getConfiguration().set("shop.use-global-virtual-item-queue", false);
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//
//        if (selectedVersion == 130) {
//            getConfiguration().set("plugin.WorldEdit", true);
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 131) {
//            getConfiguration().set("custom-commands", ImmutableList.of("shop", "chestshop", "cshop"));
//            getConfiguration().set("unlimited-shop-owner-change", false);
//            getConfiguration().set("unlimited-shop-owner-change-account", "quickshop");
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 132) {
//            getConfiguration().set("shop.sign-glowing", false);
//            getConfiguration().set("shop.sign-dye-color", "null");
//            getConfiguration().set("unlimited-shop-owner-change-account", "quickshop");
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 133) {
//            getConfiguration().set("integration.griefprevention.delete-on-unclaim", false);
//            getConfiguration().set("integration.griefprevention.delete-on-claim-expired", false);
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 134) {
//            getConfiguration().set("integration.griefprevention.delete-on-claim-resized", false);
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 135) {
//            getConfiguration().set("integration.advancedregionmarket.enable", true);
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 136) {
//            getConfiguration().set("shop.use-global-virtual-item-queue", null);
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 137) {
//            getConfiguration().set("integration.griefprevention.create", null);
//            getConfiguration().set("integration.griefprevention.create", "INVENTORY");
//
//            getConfiguration().set("integration.griefprevention.trade", null);
//            getConfiguration().set("integration.griefprevention.trade", Collections.emptyList());
//
//            boolean oldValueUntrusted = getConfiguration().getBoolean("integration.griefprevention.delete-on-untrusted", false);
//            getConfiguration().set("integration.griefprevention.delete-on-untrusted", null);
//            getConfiguration().set("integration.griefprevention.delete-on-claim-trust-changed", oldValueUntrusted);
//
//            boolean oldValueUnclaim = getConfiguration().getBoolean("integration.griefprevention.delete-on-unclaim", false);
//            getConfiguration().set("integration.griefprevention.delete-on-unclaim", null);
//            getConfiguration().set("integration.griefprevention.delete-on-claim-unclaimed", oldValueUnclaim);
//
//            getConfiguration().set("integration.griefprevention.delete-on-subclaim-created", false);
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 138) {
//            getConfiguration().set("integration.towny.whitelist-mode", true);
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//
//        if (selectedVersion == 139) {
//            getConfiguration().set("integration.iridiumskyblock.enable", false);
//            getConfiguration().set("integration.iridiumskyblock.owner-create-only", false);
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 140) {
//            getConfiguration().set("integration.towny.delete-shop-on-plot-destroy", true);
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 141) {
//            getConfiguration().set("language", null);
//            getConfiguration().set("disabled-languages", Collections.singletonList("disable_here"));
//            getConfiguration().set("mojangapi-mirror", 0);
//            getConfiguration().set("purge.enabled", false);
//            getConfiguration().set("purge.days", 60);
//            getConfiguration().set("purge.banned", true);
//            getConfiguration().set("purge.skip-op", true);
//            getConfiguration().set("purge.return-create-fee", true);
//            getConfiguration().set("shop.use-fast-shop-search-algorithm", null);
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 142) {
//            getConfiguration().set("disabled-languages", null);
//            getConfiguration().set("enabled-languages", Collections.singletonList("*"));
//            getConfiguration().set("config-version", ++selectedVersion);
//        }
//
//        if (getConfiguration().getInt("matcher.work-type") != 0 && GameVersion.get(ReflectFactory.getServerVersion()).name().contains("1_16")) {
//            getLogger().warning("You are not using QS Matcher, it may meeting item comparing issue mentioned there: https://hub.spigotmc.org/jira/browse/SPIGOT-5063");
//        }
//
//        try (InputStreamReader buildInConfigReader = new InputStreamReader(new BufferedInputStream(Objects.requireNonNull(getResource("config.yml"))))) {
//            if (new ConfigurationFixer(this, new File(getDataFolder(), "config.yml"), getConfiguration(), YamlConfiguration.loadConfiguration(buildInConfigReader)).fix()) {
//                reloadConfig();
//            }
//        }
//
//        saveConfig();
//        reloadConfig();
//
//        //Delete old example configuration files
//        new File(getDataFolder(), "xample.config.yml").delete();
//        new File(getDataFolder(), "example-configuration.txt").delete();
//
//        Path exampleConfigFile = new File(getDataFolder(), "example-configuration.yml").toPath();
//        try {
//            Files.copy(Objects.requireNonNull(getResource("config.yml")), exampleConfigFile, REPLACE_EXISTING);
//        } catch (IOException ioe) {
//            getLogger().warning("Error when creating the example config file: " + ioe.getMessage());
//        }
//    }


}
