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
        try {
            config.load(configFile);
            InputStream defaultConfigStream = plugin.getResource("config.yml");
            if (defaultConfigStream != null) {
                config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream, StandardCharsets.UTF_8)));
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
