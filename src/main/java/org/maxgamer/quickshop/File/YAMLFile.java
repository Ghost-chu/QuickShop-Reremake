package org.maxgamer.quickshop.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Mock.MckFileConfiguration;

import java.nio.charset.StandardCharsets;

public final class YAMLFile extends FileEnvelope {

    @NotNull
    private FileConfiguration fileConfiguration = new MckFileConfiguration();

    @NotNull
    @Override
    public String getMessage(@NotNull String path, @NotNull String fallback) {
        return "null";
    }

    @Override
    public void reload() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
        fileConfiguration.setDefaults(
            YamlConfiguration.loadConfiguration(
                new ReaderOf(
                    plugin.getResource(
                        resourcePath
                    ),
                    StandardCharsets.UTF_8
                )
            )
        );
    }

    @Override
    public void save() {

    }

}
