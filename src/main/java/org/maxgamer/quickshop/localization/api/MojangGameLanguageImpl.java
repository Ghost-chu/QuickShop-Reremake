/*
 * This file is a part of project QuickShop, the name is MojangGameLanguageImpl.java
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

package org.maxgamer.quickshop.localization.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.localization.distributions.mojang.MojangDistribution;
import org.maxgamer.quickshop.localization.resources.BasicLocalizationResource;
import org.maxgamer.quickshop.localization.resources.MojangLocalizationResource;
import org.maxgamer.quickshop.localization.resources.OverrideLocalizationResource;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.holder.QuickShopInstanceHolder;
import org.maxgamer.quickshop.util.mojangapi.MojangApiBmclApiMirror;
import org.maxgamer.quickshop.util.mojangapi.MojangApiMcbbsApiMirror;
import org.maxgamer.quickshop.util.mojangapi.MojangApiMirror;
import org.maxgamer.quickshop.util.mojangapi.MojangApiOfficialMirror;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * MojangGameLanguageImpl - A simple GameLanguage impl
 *
 * @author Ghost_chu and sandtechnology
 */

public class MojangGameLanguageImpl extends QuickShopInstanceHolder {
    private final static Lock LOCK = new ReentrantLock();
    private static final Condition DOWNLOAD_CONDITION = LOCK.newCondition();
    private final QuickShop plugin;
    private final GameLanguage defaultGameLanguage;
    private MojangApiMirror mirror;
    private volatile Map<String, GameLanguage> lang = Collections.emptyMap();

    @SneakyThrows
    public MojangGameLanguageImpl(@NotNull QuickShop plugin) {
        super(plugin);
        this.plugin = plugin;
        defaultGameLanguage = new InternalGameLanguageImpl(plugin);
        switch (plugin.getConfig().getInt("mojangapi-mirror", 0)) {
            case 0:
                mirror = new MojangApiOfficialMirror();
                plugin.getLogger().info("Game assets server selected: Mojang API");
                break;
            case 1:
                mirror = new MojangApiBmclApiMirror();
                plugin.getLogger().info("Game assets server selected: BMCLAPI");
                plugin.getLogger().info("===Mirror description===");
                plugin.getLogger().info("BMCLAPI is a non-profit mirror service made by @bangbang93 to speed up download in China mainland region.");
                plugin.getLogger().info("Donate BMCLAPI or get details about BMCLAPI, check here: https://bmclapidoc.bangbang93.com");
                plugin.getLogger().info("You should only use this mirror if your server in China mainland or have connection trouble with Mojang server, otherwise use Mojang Official server");
                plugin.getLogger().warning("You're selected unofficial game assets server, use at your own risk.");
                break;
            case 2:
                mirror = new MojangApiMcbbsApiMirror();
                plugin.getLogger().info("Game assets server selected: BMCLAPI");
                plugin.getLogger().info("===Mirror description===");
                plugin.getLogger().info("MCBBSAPI is a special server of OpenBMCLAPI made by @bangbang93 but managed by MCBBS, same with BMCLAPI, MCBBSAPI is target speed up download in China mainland region.");
                plugin.getLogger().info("Donate BMCLAPI or get details about BMCLAPI (includes MCBBSAPI), check here: https://bmclapidoc.bangbang93.com");
                plugin.getLogger().info("You should only use this mirror if your server in China mainland or have connection trouble with Mojang server, otherwise use Mojang Official server");
                plugin.getLogger().warning("You're selected unofficial game assets server, use at your own risk.");
                break;
        }


        LOCK.lock();
        try {
            final GameLanguageLoadThread loadThread = new GameLanguageLoadThread(plugin, mirror, list -> {
                lang = new ConcurrentHashMap<>();
                list.parallelStream().forEach(resource -> lang.put(resource.getMinecraftLangCode(), new PerLocaleGameLanguage(resource)));
            });
            loadThread.start();
            boolean timeout = !DOWNLOAD_CONDITION.await(20, TimeUnit.SECONDS);
            if (timeout) {
                Util.debugLog("No longer waiting file downloading because it now timed out, now downloading in background.");
                plugin.getLogger().info("No longer waiting file downloading because it now timed out, now downloading in background, please reset itemi18n.yml, potioni18n.yml and enchi18n.yml after download completed.");
            }
        } finally {
            LOCK.unlock();
        }

    }

    public GameLanguage getLocaleLanguage(String locale) {
        String[] split;
        if (locale.equals("default")) {
            Locale locale1 = Locale.getDefault();
            split = new String[]{locale1.getLanguage(), locale1.getCountry()};
        } else {
            split = locale.replace("-", "_").toLowerCase().split("_", 2);
        }
        GameLanguage result = lang.get(locale);
        return result != null ? result : lang.entrySet().parallelStream().filter(e -> e.getKey().startsWith(split[0]) || e.getKey().endsWith(split[0])).map(Map.Entry::getValue).findFirst().orElse(defaultGameLanguage);
    }

    @Getter
    static class GameLanguageLoadThread extends Thread {
        private final QuickShop plugin;
        private final MojangDistribution mojangDistribution;
        private final Consumer<List<MojangLocalizationResource>> callback;

        public GameLanguageLoadThread(@NotNull QuickShop plugin, @NotNull MojangApiMirror mirror, Consumer<List<MojangLocalizationResource>> callback) {
            this.plugin = plugin;
            this.mojangDistribution = new MojangDistribution(plugin, mirror);
            this.callback = callback;
        }

        @Override
        public void run() {
            LOCK.lock();
            try {
                callback.accept(mojangDistribution.getAvailableLangResources());
                plugin.getLogger().log(Level.INFO, "Successfully to get mojang lang resources");
                DOWNLOAD_CONDITION.signalAll();
            } catch (Exception exception) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get mojang lang resources", exception);
            } finally {
                LOCK.unlock();
            }

        }
    }

    @AllArgsConstructor
    class PerLocaleGameLanguage implements GameLanguage {

        private MojangLocalizationResource localizationResource;
        private Map<Material, String> materialCache = new HashMap<>(0);
        private Map<PotionEffectType, String> effectCache = new HashMap<>(0);
        private Map<Enchantment, String> enchantmentCache = new HashMap<>(0);
        private Map<EntityType, String> entityTypeCache = new HashMap<>(0);

        public PerLocaleGameLanguage(MojangLocalizationResource localizationResource) {
            OverrideLocalizationResource resource = OverrideLocalizationResource.newResource(new File(plugin.getDataFolder(), "override" + File.separator + "mojang" + File.separator + localizationResource.getMinecraftLangCode() + ".json"));
            this.localizationResource = (MojangLocalizationResource) BasicLocalizationResource.LocalizationResourceProcessor.base(localizationResource).apply(resource).compile();
            resource.save();
        }

        @Override
        public @NotNull String getName() {
            return "Mojang API";
        }

        @Override
        public @NotNull Plugin getPlugin() {
            return plugin;
        }

        @Override
        public @NotNull String getItem(@NotNull ItemStack itemStack) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null && itemMeta.hasDisplayName()) {
                return itemMeta.getDisplayName();
            } else {
                return getItem(itemStack.getType());
            }
        }

        private <K, V> V getAndPutCache(Map<K, V> anyMap, K key, Function<K, V> generateFun) {
            V result = anyMap.get(key);
            if (result == null) {
                result = generateFun.apply(key);
                anyMap.put(key, result);
            }
            return result;
        }

        @Override
        public @NotNull String getItem(@NotNull Material material) {
            return getAndPutCache(materialCache, material, k -> localizationResource.getText("item.minecraft." + k.name().toLowerCase()));
        }

        @Override
        public @NotNull String getPotion(@NotNull PotionEffectType potionEffectType) {
            return getAndPutCache(effectCache, potionEffectType, k -> localizationResource.getText("effect.minecraft." + k.getName().toLowerCase()));
        }

        @Override
        public @NotNull String getEnchantment(@NotNull Enchantment enchantment) {
            return getAndPutCache(enchantmentCache, enchantment, k -> localizationResource.getText("enchantment.minecraft." + k.getKey().getKey().toLowerCase()));
        }

        @Override
        public @NotNull String getEntity(@NotNull EntityType entityType) {
            return getAndPutCache(entityTypeCache, entityType, k -> localizationResource.getText("entity.minecraft." + k.getKey().getKey().toLowerCase()));
        }
    }
}


