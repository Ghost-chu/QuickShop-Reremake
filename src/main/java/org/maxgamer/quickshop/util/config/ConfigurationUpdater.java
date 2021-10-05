package org.maxgamer.quickshop.util.config;

import lombok.Getter;
import org.bukkit.configuration.Configuration;
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
    private final Configuration config;

    public ConfigurationUpdater(QuickShop plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    private void writeServerUniqueId() {
        String serverUUID = getConfig().getString("server-uuid");
        if (serverUUID == null || serverUUID.isEmpty()) {
            UUID uuid = UUID.randomUUID();
            serverUUID = uuid.toString();
            getConfig().set("server-uuid", serverUUID);
        }
        plugin.saveConfig();
    }

    public void update() {
        Util.debugLog("Starting configuration update...");
        writeServerUniqueId();

        for (Method updateScript : getUpdateScripts()) {
            try {
                ConfigUpdater configUpdater = updateScript.getAnnotation(ConfigUpdater.class);
                int current = getConfig().getInt("config-version");
                if (current >= configUpdater.version())
                    continue;
                Util.debugLog("Executing " + updateScript.getName() + " for version " + configUpdater.version());
                if (updateScript.getParameterCount() == 0)
                    updateScript.invoke(this);
                if (updateScript.getParameterCount() == 1 && (updateScript.getParameterTypes()[0] == int.class || updateScript.getParameterTypes()[0] == Integer.class))
                    updateScript.invoke(this, current);
                getConfig().set("config-version", configUpdater.version() + 1);
            } catch (Throwable throwable) {
                plugin.getLogger().log(Level.WARNING, "Failed execute update script " + updateScript.getName() + " for updating to version " + updateScript.getAnnotation(ConfigUpdater.class).version() + ", some configuration options may missing or outdated", throwable);
            }
        }
        saveConfig();
    }

    public List<Method> getUpdateScripts() {
        List<Method> methods = new ArrayList<>();
        for (Method declaredMethod : this.getClass().getDeclaredMethods()) {
            if (declaredMethod.getAnnotation(ConfigUpdater.class) == null)
                continue;
            methods.add(declaredMethod);
        }
        methods.sort(Comparator.comparingInt(o -> o.getAnnotation(ConfigUpdater.class).version()));
        return methods;
    }

    private void saveConfig() {
        plugin.saveConfig();
    }
//
//    @ConfigUpdater(version = 1)
//    private void addBStats() {
//        getConfig().set("disabled-metrics", false);
//    }
//
//    @ConfigUpdater(version = 2)
//    private void addProtects() {
//        getConfig().set("protect.minecart", true);
//        getConfig().set("protect.entity", true);
//        getConfig().set("protect.redstone", true);
//        getConfig().set("protect.structuregrow", true);
//        getConfig().set("protect.explode", true);
//        getConfig().set("protect.hopper", true);
//    }
//
//    @ConfigUpdater(version = 2)
//    private void addAlternateCurrencySymbol() {
//        getConfig().set("shop.alternate-currency-symbol", '$');
//    }
//
//    @ConfigUpdater(version = 3)
//    private void addUpdater() {
//        getConfig().set("updater", true);
//    }
//
//    @ConfigUpdater(version = 4)
//    private void addSneakControl() {
//        getConfig().set("shop.sneak-to-control", false);
//    }
//
//    @ConfigUpdater(version = 6)
//    private void addDatabaseTablePrefix() {
//        getConfig().set("database.prefix", "none");
//    }
//
//    @ConfigUpdater(version = 8)
//    private void addFastSearchAlgorithmAndPlib() {
//        getConfig().set("limits.old-algorithm", false);
//        getConfig().set("plugin.ProtocolLib", false);
//        getConfig().set("shop.ignore-unlimited", false);
//    }
//
//    @ConfigUpdater(version = 9)
//    private void addEnderChestShop() {
//        getConfig().set("shop.enable-enderchest", true);
//    }
//
//    @ConfigUpdater(version = 10)
//    private void removedOptionToPayUnlimtiedShopOwner() {
//        getConfig().set("shop.pay-player-from-unlimited-shop-owner", null);
//    }
//
//    @ConfigUpdater(version = 10)
//    private void addEnderChestShop_R() {
//        getConfig().set("shop.enable-enderchest", null); // Removed
//        getConfig().set("plugin.OpenInv", true);
//        List<String> shoppable = getConfig().getStringList("shop-blocks");
//        shoppable.add("ENDER_CHEST");
//        getConfig().set("shop-blocks", shoppable);
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
//            getConfig().set("plugin.ProtocolLib", null); // Removed
//            getConfig().set("plugin.BKCommonLib", null); // Removed
//            getConfig().set("database.use-varchar", null); // Removed
//            getConfig().set("database.reconnect", null); // Removed
//            getConfig().set("display-items-check-ticks", 1200);
//            getConfig().set("shop.bypass-owner-check", null); // Removed
//            getConfig().set("config-version", 13);
//            selectedVersion = 13;
//        }
//        if (selectedVersion == 13) {
//            getConfig().set("config-version", 14);
//            selectedVersion = 14;
//        }
//        if (selectedVersion == 14) {
//            getConfig().set("plugin.AreaShop", null);
//            getConfig().set("shop.special-region-only", null);
//            getConfig().set("config-version", 15);
//            selectedVersion = 15;
//        }
//        if (selectedVersion == 15) {
//            getConfig().set("ongoingfee", null);
//            getConfig().set("shop.display-item-show-name", false);
//            getConfig().set("shop.auto-fetch-shop-messages", true);
//            getConfig().set("config-version", 16);
//            selectedVersion = 16;
//        }
//        if (selectedVersion == 16) {
//            getConfig().set("config-version", 17);
//            selectedVersion = 17;
//        }
//        if (selectedVersion == 17) {
//            getConfig().set("ignore-cancel-chat-event", false);
//            getConfig().set("float", null);
//            getConfig().set("config-version", 18);
//            selectedVersion = 18;
//        }
//        if (selectedVersion == 18) {
//            getConfig().set("shop.disable-vault-format", false);
//            getConfig().set("config-version", 19);
//            selectedVersion = 19;
//        }
//        if (selectedVersion == 19) {
//            getConfig().set("shop.allow-shop-without-space-for-sign", true);
//            getConfig().set("config-version", 20);
//            selectedVersion = 20;
//        }
//        if (selectedVersion == 20) {
//            getConfig().set("shop.maximum-price", -1);
//            getConfig().set("config-version", 21);
//            selectedVersion = 21;
//        }
//        if (selectedVersion == 21) {
//            getConfig().set("shop.sign-material", "OAK_WALL_SIGN");
//            getConfig().set("config-version", 22);
//            selectedVersion = 22;
//        }
//        if (selectedVersion == 22) {
//            getConfig().set("include-offlineplayer-list", "false");
//            getConfig().set("config-version", 23);
//            selectedVersion = 23;
//        }
//        if (selectedVersion == 23) {
//            getConfig().set("lockette.enable", null);
//            getConfig().set("lockette.item", null);
//            getConfig().set("lockette.lore", null);
//            getConfig().set("lockette.displayname", null);
//            getConfig().set("float", null);
//            getConfig().set("lockette.enable", true);
//            getConfig().set("shop.blacklist-world", Lists.newArrayList("disabled_world_name"));
//            getConfig().set("config-version", 24);
//            selectedVersion = 24;
//        }
//        if (selectedVersion == 24) {
//            getConfig().set("config-version", 25);
//            selectedVersion = 25;
//        }
//        if (selectedVersion == 25) {
//            String language = getConfig().getString("language");
//            if (language == null || language.isEmpty() || "default".equals(language)) {
//                getConfig().set("language", "en");
//            }
//            getConfig().set("config-version", 26);
//            selectedVersion = 26;
//        }
//        if (selectedVersion == 26) {
//            getConfig().set("database.usessl", false);
//            getConfig().set("config-version", 27);
//            selectedVersion = 27;
//        }
//        if (selectedVersion == 27) {
//            getConfig().set("queue.enable", true);
//            getConfig().set("queue.shops-per-tick", 20);
//            getConfig().set("config-version", 28);
//            selectedVersion = 28;
//        }
//        if (selectedVersion == 28) {
//            getConfig().set("database.queue", true);
//            getConfig().set("config-version", 29);
//            selectedVersion = 29;
//        }
//        if (selectedVersion == 29) {
//            getConfig().set("plugin.Multiverse-Core", null);
//            getConfig().set("shop.protection-checking", true);
//            getConfig().set("config-version", 30);
//            selectedVersion = 30;
//        }
//        if (selectedVersion == 30) {
//            getConfig().set("auto-report-errors", true);
//            getConfig().set("config-version", 31);
//            selectedVersion = 31;
//        }
//        if (selectedVersion == 31) {
//            getConfig().set("shop.display-type", 0);
//            getConfig().set("config-version", 32);
//            selectedVersion = 32;
//        }
//        if (selectedVersion == 32) {
//            getConfig().set("effect.sound.ontabcomplete", true);
//            getConfig().set("effect.sound.oncommand", true);
//            getConfig().set("effect.sound.ononclick", true);
//            getConfig().set("config-version", 33);
//            selectedVersion = 33;
//        }
//        if (selectedVersion == 33) {
//            getConfig().set("matcher.item.damage", true);
//            getConfig().set("matcher.item.displayname", true);
//            getConfig().set("matcher.item.lores", true);
//            getConfig().set("matcher.item.enchs", true);
//            getConfig().set("matcher.item.potions", true);
//            getConfig().set("matcher.item.attributes", true);
//            getConfig().set("matcher.item.itemflags", true);
//            getConfig().set("matcher.item.custommodeldata", true);
//            getConfig().set("config-version", 34);
//            selectedVersion = 34;
//        }
//        if (selectedVersion == 34) {
//            if (getConfig().getInt("shop.display-items-check-ticks") == 1200) {
//                getConfig().set("shop.display-items-check-ticks", 6000);
//            }
//            getConfig().set("config-version", 35);
//            selectedVersion = 35;
//        }
//        if (selectedVersion == 35) {
//            getConfig().set("queue", null); // Close it for everyone
//            getConfig().set("config-version", 36);
//            selectedVersion = 36;
//        }
//        if (selectedVersion == 36) {
//            getConfig().set("economy-type", 0); // Close it for everyone
//            getConfig().set("config-version", 37);
//            selectedVersion = 37;
//        }
//        if (selectedVersion == 37) {
//            getConfig().set("shop.ignore-cancel-chat-event", true);
//            getConfig().set("config-version", 38);
//            selectedVersion = 38;
//        }
//        if (selectedVersion == 38) {
//            getConfig().set("protect.inventorymove", true);
//            getConfig().set("protect.spread", true);
//            getConfig().set("protect.fromto", true);
//            getConfig().set("protect.minecart", null);
//            getConfig().set("protect.hopper", null);
//            getConfig().set("config-version", 39);
//            selectedVersion = 39;
//        }
//        if (selectedVersion == 39) {
//            getConfig().set("update-sign-when-inventory-moving", true);
//            getConfig().set("config-version", 40);
//            selectedVersion = 40;
//        }
//        if (selectedVersion == 40) {
//            getConfig().set("allow-economy-loan", false);
//            getConfig().set("config-version", 41);
//            selectedVersion = 41;
//        }
//        if (selectedVersion == 41) {
//            getConfig().set("send-display-item-protection-alert", true);
//            getConfig().set("config-version", 42);
//            selectedVersion = 42;
//        }
//        if (selectedVersion == 42) {
//            getConfig().set("config-version", 43);
//            selectedVersion = 43;
//        }
//        if (selectedVersion == 43) {
//            getConfig().set("config-version", 44);
//            selectedVersion = 44;
//        }
//        if (selectedVersion == 44) {
//            getConfig().set("matcher.item.repaircost", false);
//            getConfig().set("config-version", 45);
//            selectedVersion = 45;
//        }
//        if (selectedVersion == 45) {
//            getConfig().set("shop.display-item-use-name", true);
//            getConfig().set("config-version", 46);
//            selectedVersion = 46;
//        }
//        if (selectedVersion == 46) {
//            getConfig().set("shop.max-shops-checks-in-once", 100);
//            getConfig().set("config-version", 47);
//            selectedVersion = 47;
//        }
//        if (selectedVersion == 47) {
//            getConfig().set("config-version", 48);
//            selectedVersion = 48;
//        }
//        if (selectedVersion == 48) {
//            getConfig().set("permission-type", null);
//            getConfig().set("shop.use-protection-checking-filter", null);
//            getConfig().set("shop.protection-checking-filter", null);
//            getConfig().set("config-version", 49);
//            selectedVersion = 49;
//        }
//        if (selectedVersion == 49 || selectedVersion == 50) {
//            getConfig().set("shop.enchance-display-protect", false);
//            getConfig().set("shop.enchance-shop-protect", false);
//            getConfig().set("protect", null);
//            getConfig().set("config-version", 51);
//            selectedVersion = 51;
//        }
//        if (selectedVersion < 60) { // Ahhh fuck versions
//            getConfig().set("config-version", 60);
//            selectedVersion = 60;
//        }
//        if (selectedVersion == 60) { // Ahhh fuck versions
//            getConfig().set("shop.strict-matches-check", null);
//            getConfig().set("shop.display-auto-despawn", true);
//            getConfig().set("shop.display-despawn-range", 10);
//            getConfig().set("shop.display-check-time", 10);
//            getConfig().set("config-version", 61);
//            selectedVersion = 61;
//        }
//        if (selectedVersion == 61) { // Ahhh fuck versions
//            getConfig().set("shop.word-for-sell-all-items", "all");
//            getConfig().set("plugin.PlaceHolderAPI", true);
//            getConfig().set("config-version", 62);
//            selectedVersion = 62;
//        }
//        if (selectedVersion == 62) { // Ahhh fuck versions
//            getConfig().set("shop.display-auto-despawn", false);
//            getConfig().set("shop.word-for-trade-all-items", getConfig().getString("shop.word-for-sell-all-items"));
//
//            getConfig().set("config-version", 63);
//            selectedVersion = 63;
//        }
//        if (selectedVersion == 63) { // Ahhh fuck versions
//            getConfig().set("shop.ongoing-fee.enable", false);
//            getConfig().set("shop.ongoing-fee.ticks", 42000);
//            getConfig().set("shop.ongoing-fee.cost-per-shop", 2);
//            getConfig().set("shop.ongoing-fee.ignore-unlimited", true);
//            getConfig().set("config-version", 64);
//            selectedVersion = 64;
//        }
//        if (selectedVersion == 64) {
//            getConfig().set("shop.allow-free-shop", false);
//            getConfig().set("config-version", 65);
//            selectedVersion = 65;
//        }
//        if (selectedVersion == 65) {
//            getConfig().set("shop.minimum-price", 0.01);
//            getConfig().set("config-version", 66);
//            selectedVersion = 66;
//        }
//        if (selectedVersion == 66) {
//            getConfig().set("use-decimal-format", false);
//            getConfig().set("decimal-format", "#,###.##");
//            getConfig().set("shop.show-owner-uuid-in-controlpanel-if-op", false);
//            getConfig().set("config-version", 67);
//            selectedVersion = 67;
//        }
//        if (selectedVersion == 67) {
//            getConfig().set("disable-debuglogger", false);
//            getConfig().set("matcher.use-bukkit-matcher", null);
//            getConfig().set("config-version", 68);
//            selectedVersion = 68;
//        }
//        if (selectedVersion == 68) {
//            getConfig().set("shop.blacklist-lores", Lists.newArrayList("SoulBound"));
//            getConfig().set("config-version", 69);
//            selectedVersion = 69;
//        }
//        if (selectedVersion == 69) {
//            getConfig().set("shop.display-item-use-name", false);
//            getConfig().set("config-version", 70);
//            selectedVersion = 70;
//        }
//        if (selectedVersion == 70) {
//            getConfig().set("cachingpool.enable", false);
//            getConfig().set("cachingpool.maxsize", 100000000);
//            getConfig().set("config-version", 71);
//            selectedVersion = 71;
//        }
//        if (selectedVersion == 71) {
//            if (Objects.equals(getConfig().getString("language"), "en")) {
//                getConfig().set("language", "en-US");
//            }
//            getConfig().set("server-platform", 0);
//            getConfig().set("config-version", 72);
//            selectedVersion = 72;
//        }
//        if (selectedVersion == 72) {
//            if (getConfig().getBoolean("use-deciaml-format")) {
//                getConfig().set("use-decimal-format", getConfig().getBoolean("use-deciaml-format"));
//            } else {
//                getConfig().set("use-decimal-format", false);
//            }
//            getConfig().set("use-deciaml-format", null);
//
//            getConfig().set("shop.force-load-downgrade-items.enable", false);
//            getConfig().set("shop.force-load-downgrade-items.method", 0);
//            getConfig().set("config-version", 73);
//            selectedVersion = 73;
//        }
//        if (selectedVersion == 73) {
//            getConfig().set("mixedeconomy.deposit", "eco give {0} {1}");
//            getConfig().set("mixedeconomy.withdraw", "eco take {0} {1}");
//            getConfig().set("config-version", 74);
//            selectedVersion = 74;
//        }
//        if (selectedVersion == 74) {
//            String langUtilsLanguage = getConfig().getString("langutils-language", "en_us");
//            getConfig().set("langutils-language", null);
//            if ("en_us".equals(langUtilsLanguage)) {
//                langUtilsLanguage = "default";
//            }
//            getConfig().set("game-language", langUtilsLanguage);
//            getConfig().set("maximum-digits-in-price", -1);
//            getConfig().set("config-version", 75);
//            selectedVersion = 75;
//        }
//        if (selectedVersion == 75) {
//            getConfig().set("langutils-language", null);
//            if (getConfig().getString("game-language") == null) {
//                getConfig().set("game-language", "default");
//            }
//            getConfig().set("config-version", 76);
//            selectedVersion = 76;
//        }
//        if (selectedVersion == 76) {
//            getConfig().set("database.auto-fix-encoding-issue-in-database", false);
//            getConfig().set("send-shop-protection-alert", false);
//            getConfig().set("send-display-item-protection-alert", false);
//            getConfig().set("shop.use-fast-shop-search-algorithm", false);
//            getConfig().set("config-version", 77);
//            selectedVersion = 77;
//        }
//        if (selectedVersion == 77) {
//            getConfig().set("integration.towny.enable", false);
//            getConfig().set("integration.towny.create", new String[]{"SHOPTYPE", "MODIFY"});
//            getConfig().set("integration.towny.trade", new String[]{});
//            getConfig().set("integration.worldguard.enable", false);
//            getConfig().set("integration.worldguard.create", new String[]{"FLAG", "CHEST_ACCESS"});
//            getConfig().set("integration.worldguard.trade", new String[]{});
//            getConfig().set("integration.plotsquared.enable", false);
//            getConfig().set("integration.plotsquared.enable", false);
//            getConfig().set("integration.plotsquared.enable", false);
//            getConfig().set("integration.residence.enable", false);
//            getConfig().set("integration.residence.create", new String[]{"FLAG", "interact", "use"});
//            getConfig().set("integration.residence.trade", new String[]{});
//
//            getConfig().set("integration.factions.enable", false);
//            getConfig().set("integration.factions.create.flag", new String[]{});
//            getConfig().set("integration.factions.trade.flag", new String[]{});
//            getConfig().set("integration.factions.create.require.open", false);
//            getConfig().set("integration.factions.create.require.normal", true);
//            getConfig().set("integration.factions.create.require.wilderness", false);
//            getConfig().set("integration.factions.create.require.peaceful", true);
//            getConfig().set("integration.factions.create.require.permanent", false);
//            getConfig().set("integration.factions.create.require.safezone", false);
//            getConfig().set("integration.factions.create.require.own", false);
//            getConfig().set("integration.factions.create.require.warzone", false);
//            getConfig().set("integration.factions.trade.require.open", false);
//            getConfig().set("integration.factions.trade.require.normal", true);
//            getConfig().set("integration.factions.trade.require.wilderness", false);
//            getConfig().set("integration.factions.trade.require.peaceful", false);
//            getConfig().set("integration.factions.trade.require.permanent", false);
//            getConfig().set("integration.factions.trade.require.safezone", false);
//            getConfig().set("integration.factions.trade.require.own", false);
//            getConfig().set("integration.factions.trade.require.warzone", false);
//            getConfig().set("anonymous-metrics", null);
//            getConfig().set("shop.ongoing-fee.async", true);
//            getConfig().set("config-version", 78);
//            selectedVersion = 78;
//        }
//        if (selectedVersion == 78) {
//            getConfig().set("shop.display-type-specifics", null);
//            getConfig().set("config-version", 79);
//            selectedVersion = 79;
//        }
//        if (selectedVersion == 79) {
//            getConfig().set("matcher.item.books", true);
//            getConfig().set("config-version", 80);
//            selectedVersion = 80;
//        }
//        if (selectedVersion == 80) {
//            getConfig().set("shop.use-fast-shop-search-algorithm", true);
//            getConfig().set("config-version", 81);
//            selectedVersion = 81;
//        }
//        if (selectedVersion == 81) {
//            getConfig().set("config-version", 82);
//            selectedVersion = 82;
//        }
//        if (selectedVersion == 82) {
//            getConfig().set("matcher.item.banner", true);
//            getConfig().set("config-version", 83);
//            selectedVersion = 83;
//        }
//        if (selectedVersion == 83) {
//            getConfig().set("matcher.item.banner", true);
//            getConfig().set("protect.explode", true);
//            getConfig().set("config-version", 84);
//            selectedVersion = 84;
//        }
//        if (selectedVersion == 84) {
//            getConfig().set("disable-debuglogger", null);
//            getConfig().set("config-version", 85);
//            selectedVersion = 85;
//        }
//        if (selectedVersion == 85) {
//            getConfig().set("config-version", 86);
//            selectedVersion = 86;
//        }
//        if (selectedVersion == 86) {
//            getConfig().set("shop.use-fast-shop-search-algorithm", true);
//            getConfig().set("config-version", 87);
//            selectedVersion = 87;
//        }
//        if (selectedVersion == 87) {
//            getConfig().set("plugin.BlockHub.enable", true);
//            getConfig().set("plugin.BlockHub.only", false);
//            if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
//                getConfig().set("shop.display-type", 2);
//            }
//            getConfig().set("config-version", 88);
//            selectedVersion = 88;
//        }
//        if (selectedVersion == 88) {
//            getConfig().set("respect-item-flag", true);
//            getConfig().set("config-version", 89);
//            selectedVersion = 89;
//        }
//        if (selectedVersion == 89) {
//            getConfig().set("use-caching", true);
//            getConfig().set("config-version", 90);
//            selectedVersion = 90;
//        }
//        if (selectedVersion == 90) {
//            getConfig().set("protect.hopper", true);
//            getConfig().set("config-version", 91);
//            selectedVersion = 91;
//        }
//        if (selectedVersion == 91) {
//            getConfig().set("database.queue-commit-interval", 2);
//            getConfig().set("config-version", 92);
//            selectedVersion = 92;
//        }
//        if (selectedVersion == 92) {
//            getConfig().set("send-display-item-protection-alert", false);
//            getConfig().set("send-shop-protection-alert", false);
//            getConfig().set("disable-creative-mode-trading", false);
//            getConfig().set("disable-super-tool", false);
//            getConfig().set("allow-owner-break-shop-sign", false);
//            getConfig().set("matcher.item.skull", true);
//            getConfig().set("matcher.item.firework", true);
//            getConfig().set("matcher.item.map", true);
//            getConfig().set("matcher.item.leatherArmor", true);
//            getConfig().set("matcher.item.fishBucket", true);
//            getConfig().set("matcher.item.suspiciousStew", true);
//            getConfig().set("matcher.item.shulkerBox", true);
//            getConfig().set("config-version", 93);
//            selectedVersion = 93;
//        }
//        if (selectedVersion == 93) {
//            getConfig().set("disable-creative-mode-trading", null);
//            getConfig().set("disable-super-tool", null);
//            getConfig().set("allow-owner-break-shop-sign", null);
//            getConfig().set("shop.disable-creative-mode-trading", true);
//            getConfig().set("shop.disable-super-tool", true);
//            getConfig().set("shop.allow-owner-break-shop-sign", false);
//            getConfig().set("config-version", 94);
//            selectedVersion = 94;
//        }
//        if (selectedVersion == 94) {
//            if (getConfig().isSet("price-restriction")) {
//                getConfig().set("shop.price-restriction", getConfig().getStringList("price-restriction"));
//                getConfig().set("price-restriction", null);
//            } else {
//                getConfig().set("shop.price-restriction", new ArrayList<>(0));
//            }
//            getConfig().set("enable-log4j", null);
//            getConfig().set("config-version", 95);
//            selectedVersion = 95;
//        }
//        if (selectedVersion == 95) {
//            getConfig().set("shop.allow-stacks", false);
//            getConfig().set("shop.display-allow-stacks", false);
//            getConfig().set("custom-item-stacksize", new ArrayList<>(0));
//            getConfig().set("config-version", 96);
//            selectedVersion = 96;
//        }
//        if (selectedVersion == 96) {
//            getConfig().set("shop.deny-non-shop-items-to-shop-container", false);
//            getConfig().set("config-version", 97);
//            selectedVersion = 97;
//        }
//        if (selectedVersion == 97) {
//            getConfig().set("shop.disable-quick-create", false);
//            getConfig().set("config-version", 98);
//            selectedVersion = 98;
//        }
//        if (selectedVersion == 98) {
//            getConfig().set("config-version", 99);
//            selectedVersion = 99;
//        }
//        if (selectedVersion == 99) {
//            getConfig().set("shop.currency-symbol-on-right", false);
//            getConfig().set("config-version", 100);
//            selectedVersion = 100;
//        }
//        if (selectedVersion == 100) {
//            getConfig().set("integration.towny.ignore-disabled-worlds", false);
//            getConfig().set("config-version", 101);
//            selectedVersion = 101;
//        }
//        if (selectedVersion == 101) {
//            getConfig().set("matcher.work-type", 1);
//            getConfig().set("work-type", null);
//            getConfig().set("plugin.LWC", true);
//            getConfig().set("config-version", 102);
//            selectedVersion = 102;
//        }
//        if (selectedVersion == 102) {
//            getConfig().set("protect.entity", true);
//            getConfig().set("config-version", 103);
//            selectedVersion = 103;
//        }
//        if (selectedVersion == 103) {
//            getConfig().set("integration.worldguard.whitelist-mode", false);
//            getConfig().set("integration.factions.whitelist-mode", true);
//            getConfig().set("integration.plotsquared.whitelist-mode", true);
//            getConfig().set("integration.residence.whitelist-mode", true);
//            getConfig().set("config-version", 104);
//            selectedVersion = 104;
//        }
//        if (selectedVersion == 104) {
//            getConfig().set("cachingpool", null);
//            getConfig().set("config-version", 105);
//            selectedVersion = 105;
//        }
//        if (selectedVersion == 105) {
//            getConfig().set("shop.interact.sneak-to-create", getConfig().getBoolean("shop.sneak-to-create"));
//            getConfig().set("shop.sneak-to-create", null);
//            getConfig().set("shop.interact.sneak-to-trade", getConfig().getBoolean("shop.sneak-to-trade"));
//            getConfig().set("shop.sneak-to-trade", null);
//            getConfig().set("shop.interact.sneak-to-control", getConfig().getBoolean("shop.sneak-to-control"));
//            getConfig().set("shop.sneak-to-control", null);
//            getConfig().set("config-version", 106);
//            selectedVersion = 106;
//        }
//        if (selectedVersion == 106) {
//            getConfig().set("shop.use-enchantment-for-enchanted-book", false);
//            getConfig().set("config-version", 107);
//            selectedVersion = 107;
//        }
//        if (selectedVersion == 107) {
//            getConfig().set("integration.lands.enable", false);
//            getConfig().set("integration.lands.whitelist-mode", false);
//            getConfig().set("integration.lands.ignore-disabled-worlds", true);
//            getConfig().set("config-version", 108);
//            selectedVersion = 108;
//        }
//        if (selectedVersion == 108) {
//            getConfig().set("debug.shop-deletion", false);
//            getConfig().set("config-version", 109);
//            selectedVersion = 109;
//        }
//        if (selectedVersion == 109) {
//            getConfig().set("shop.protection-checking-blacklist", Collections.singletonList("disabled_world"));
//            getConfig().set("config-version", 110);
//            selectedVersion = 110;
//        }
//        if (selectedVersion == 110) {
//            getConfig().set("integration.worldguard.any-owner", true);
//            getConfig().set("config-version", 111);
//            selectedVersion = 111;
//        }
//        if (selectedVersion == 111) {
//            getConfig().set("logging.enable", getConfig().getBoolean("log-actions"));
//            getConfig().set("logging.log-actions", getConfig().getBoolean("log-actions"));
//            getConfig().set("logging.log-balance", true);
//            getConfig().set("logging.file-size", 10);
//            getConfig().set("debug.disable-debuglogger", false);
//            getConfig().set("trying-fix-banlance-insuffient", false);
//            getConfig().set("log-actions", null);
//            getConfig().set("config-version", 112);
//            selectedVersion = 112;
//        }
//        if (selectedVersion == 112) {
//            getConfig().set("integration.lands.delete-on-lose-permission", false);
//            getConfig().set("config-version", 113);
//            selectedVersion = 113;
//        }
//        if (selectedVersion == 113) {
//            getConfig().set("config-damaged", false);
//            getConfig().set("config-version", 114);
//            selectedVersion = 114;
//        }
//        if (selectedVersion == 114) {
//            getConfig().set("shop.interact.interact-mode", getConfig().getBoolean("shop.interact.switch-mode") ? 0 : 1);
//            getConfig().set("shop.interact.switch-mode", null);
//            getConfig().set("config-version", 115);
//            selectedVersion = 115;
//        }
//        if (selectedVersion == 115) {
//            getConfig().set("integration.griefprevention.enable", false);
//            getConfig().set("integration.griefprevention.whitelist-mode", false);
//            getConfig().set("integration.griefprevention.create", Collections.emptyList());
//            getConfig().set("integration.griefprevention.trade", Collections.emptyList());
//            getConfig().set("config-version", 116);
//            selectedVersion = 116;
//        }
//        if (selectedVersion == 116) {
//            getConfig().set("shop.sending-stock-message-to-staffs", false);
//            getConfig().set("integration.towny.delete-shop-on-resident-leave", false);
//            getConfig().set("config-version", 117);
//            selectedVersion = 117;
//        }
//        if (selectedVersion == 117) {
//            getConfig().set("shop.finding.distance", getConfig().getInt("shop.find-distance"));
//            getConfig().set("shop.finding.limit", 10);
//            getConfig().set("shop.find-distance", null);
//            getConfig().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 118) {
//            getConfig().set("shop.finding.oldLogic", false);
//            getConfig().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 119) {
//            getConfig().set("debug.adventure", false);
//            getConfig().set("shop.finding.all", false);
//            getConfig().set("chat-type", 0);
//            getConfig().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 120) {
//            getConfig().set("shop.finding.exclude-out-of-stock", false);
//            getConfig().set("chat-type", 0);
//            getConfig().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 121) {
//            getConfig().set("shop.protection-checking-handler", 0);
//            getConfig().set("shop.protection-checking-listener-blacklist", Collections.singletonList("ignored_listener"));
//            getConfig().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 122) {
//            getConfig().set("currency", "");
//            getConfig().set("shop.alternate-currency-symbol-list", Arrays.asList("CNY;¥", "USD;$"));
//            getConfig().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 123) {
//            getConfig().set("integration.fabledskyblock.enable", false);
//            getConfig().set("integration.fabledskyblock.whitelist-mode", false);
//            getConfig().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 124) {
//            getConfig().set("plugin.BKCommonLib", true);
//            getConfig().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 125) {
//            getConfig().set("integration.superiorskyblock.enable", false);
//            getConfig().set("integration.superiorskyblock.owner-create-only", false);
//            getConfig().set("integration.superiorskyblock.delete-shop-on-member-leave", true);
//            getConfig().set("shop.interact.swap-click-behavior", false);
//            getConfig().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 126) {
//            getConfig().set("debug.delete-corrupt-shops", false);
//            getConfig().set("config-version", ++selectedVersion);
//        }
//
//        if (selectedVersion == 127) {
//            getConfig().set("integration.plotsquared.delete-when-user-untrusted", true);
//            getConfig().set("integration.towny.delete-shop-on-plot-clear", true);
//            getConfig().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 128) {
//            getConfig().set("shop.force-use-item-original-name", false);
//            getConfig().set("integration.griefprevention.delete-on-untrusted", false);
//            getConfig().set("config-version", ++selectedVersion);
//        }
//
//        if (selectedVersion == 129) {
//            getConfig().set("shop.use-global-virtual-item-queue", false);
//            getConfig().set("config-version", ++selectedVersion);
//        }
//
//        if (selectedVersion == 130) {
//            getConfig().set("plugin.WorldEdit", true);
//            getConfig().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 131) {
//            getConfig().set("custom-commands", ImmutableList.of("shop", "chestshop", "cshop"));
//            getConfig().set("unlimited-shop-owner-change", false);
//            getConfig().set("unlimited-shop-owner-change-account", "quickshop");
//            getConfig().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 132) {
//            getConfig().set("shop.sign-glowing", false);
//            getConfig().set("shop.sign-dye-color", "null");
//            getConfig().set("unlimited-shop-owner-change-account", "quickshop");
//            getConfig().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 133) {
//            getConfig().set("integration.griefprevention.delete-on-unclaim", false);
//            getConfig().set("integration.griefprevention.delete-on-claim-expired", false);
//            getConfig().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 134) {
//            getConfig().set("integration.griefprevention.delete-on-claim-resized", false);
//            getConfig().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 135) {
//            getConfig().set("integration.advancedregionmarket.enable", true);
//            getConfig().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 136) {
//            getConfig().set("shop.use-global-virtual-item-queue", null);
//            getConfig().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 137) {
//            getConfig().set("integration.griefprevention.create", null);
//            getConfig().set("integration.griefprevention.create", "INVENTORY");
//
//            getConfig().set("integration.griefprevention.trade", null);
//            getConfig().set("integration.griefprevention.trade", Collections.emptyList());
//
//            boolean oldValueUntrusted = getConfig().getBoolean("integration.griefprevention.delete-on-untrusted", false);
//            getConfig().set("integration.griefprevention.delete-on-untrusted", null);
//            getConfig().set("integration.griefprevention.delete-on-claim-trust-changed", oldValueUntrusted);
//
//            boolean oldValueUnclaim = getConfig().getBoolean("integration.griefprevention.delete-on-unclaim", false);
//            getConfig().set("integration.griefprevention.delete-on-unclaim", null);
//            getConfig().set("integration.griefprevention.delete-on-claim-unclaimed", oldValueUnclaim);
//
//            getConfig().set("integration.griefprevention.delete-on-subclaim-created", false);
//            getConfig().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 138) {
//            getConfig().set("integration.towny.whitelist-mode", true);
//            getConfig().set("config-version", ++selectedVersion);
//        }
//
//        if (selectedVersion == 139) {
//            getConfig().set("integration.iridiumskyblock.enable", false);
//            getConfig().set("integration.iridiumskyblock.owner-create-only", false);
//            getConfig().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 140) {
//            getConfig().set("integration.towny.delete-shop-on-plot-destroy", true);
//            getConfig().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 141) {
//            getConfig().set("language", null);
//            getConfig().set("disabled-languages", Collections.singletonList("disable_here"));
//            getConfig().set("mojangapi-mirror", 0);
//            getConfig().set("purge.enabled", false);
//            getConfig().set("purge.days", 60);
//            getConfig().set("purge.banned", true);
//            getConfig().set("purge.skip-op", true);
//            getConfig().set("purge.return-create-fee", true);
//            getConfig().set("shop.use-fast-shop-search-algorithm", null);
//            getConfig().set("config-version", ++selectedVersion);
//        }
//        if (selectedVersion == 142) {
//            getConfig().set("disabled-languages", null);
//            getConfig().set("enabled-languages", Collections.singletonList("*"));
//            getConfig().set("config-version", ++selectedVersion);
//        }
//
//        if (getConfig().getInt("matcher.work-type") != 0 && GameVersion.get(ReflectFactory.getServerVersion()).name().contains("1_16")) {
//            getLogger().warning("You are not using QS Matcher, it may meeting item comparing issue mentioned there: https://hub.spigotmc.org/jira/browse/SPIGOT-5063");
//        }
//
//        try (InputStreamReader buildInConfigReader = new InputStreamReader(new BufferedInputStream(Objects.requireNonNull(getResource("config.yml"))))) {
//            if (new ConfigurationFixer(this, new File(getDataFolder(), "config.yml"), getConfig(), YamlConfiguration.loadConfiguration(buildInConfigReader)).fix()) {
//                reloadConfig();
//            }
//        }
//
//        saveConfig();
//        reloadConfig();
//
//        //Delete old example configuration files
//        new File(getDataFolder(), "example.config.yml").delete();
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
