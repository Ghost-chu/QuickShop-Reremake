package org.maxgamer.quickshop.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Mock.MckFileConfiguration;

import java.io.File;
import java.io.InputStreamReader;

public final class YAMLFile extends FileEnvelope {

    @NotNull
    private FileConfiguration fileConfiguration = new MckFileConfiguration();

    public YAMLFile(@NotNull Plugin plugin, @NotNull File file, @NotNull String resourcePath) {
        super(plugin, file, resourcePath);
    }

    @Override
    public void reload() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
        fileConfiguration.setDefaults(
            YamlConfiguration.loadConfiguration(
                new InputStreamReader(
                    getInputStream()
                )
            )
        );
    }

    @Override
    public void save() {
        try {
            if (fileConfiguration instanceof MckFileConfiguration) {
                reload();
            }

            fileConfiguration.save(file);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void set(@NotNull String path, @NotNull Object object) {

    }

    @Override
    public void setAndSave(@NotNull String path, @NotNull Object object) {

    }

    @Override
    public <T> @Nullable T get(@NotNull String path) {
        return null;
    }

    @Override
    public <T> @NotNull T get(@NotNull String path, @NotNull T fallback) {
        return null;
    }

    @Override
    public <T> @NotNull T getOrSet(@NotNull String path, @NotNull T fallback) {
        return null;
    }
}
