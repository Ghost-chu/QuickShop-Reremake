/*
 * This file is a part of project QuickShop, the name is ConfigProvider.java
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

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.holder.QuickShopInstanceHolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ConfigProvider extends QuickShopInstanceHolder {


    private final File configFile = new File(plugin.getDataFolder(), "config.yml");
    private final Logger logger = plugin.getLogger();
    private FileConfiguration config = null;

    public ConfigProvider(QuickShop plugin) {
        super(plugin);
    }

    public @NotNull FileConfiguration get() {
        if (config == null) {
            reload();
        }
        return config;
    }

    public void save() {
        try {
            get().save(configFile);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save configuration!", e);
        }
    }

    public void reload() {
        reload(false);
    }

    /**
     * Reloads QuickShops config
     *
     * @param defaults is using default configuration
     */
    public void reload(boolean defaults) {
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }
        if (config == null) {
            config = new YamlConfiguration();
        }
        try (InputStream defaultConfigStream = plugin.getResource("config.yml")) {
            config.load(configFile);
            if (defaultConfigStream != null) {
                try (InputStreamReader reader = new InputStreamReader(defaultConfigStream, StandardCharsets.UTF_8)) {
                    config.setDefaults(YamlConfiguration.loadConfiguration(reader));
                }
            }
        } catch (IOException | InvalidConfigurationException exception) {
            if (!defaults) {
                logger.log(Level.SEVERE, "Cannot reading the configuration, doing backup configuration and use default", exception);
                try {
                    Files.copy(configFile.toPath(), plugin.getDataFolder().toPath().resolve("config-broken-" + UUID.randomUUID() + ".yml"), REPLACE_EXISTING);
                } catch (IOException fatalException) {
                    logger.log(Level.SEVERE, "Failed to backup plugin config! Disabling plugin....", fatalException);
                    throw new IllegalStateException("Failed to backup plugin config!", fatalException);
                }
                plugin.saveResource("config.yml", true);
                reload(true);
            } else {
                logger.log(Level.SEVERE, "Failed to load default configuration! Disabling plugin....", exception);
                throw new IllegalStateException("Failed to load default configuration!", exception);
            }
        }

    }
}
