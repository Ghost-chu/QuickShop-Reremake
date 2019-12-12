package org.maxgamer.quickshop.File;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStreamReader;

public final class YAMLFile extends FileEnvelope {

    public YAMLFile(@NotNull final Plugin plugin, @NotNull final File file, @NotNull final String resourcePath,
                  boolean loadDefault) {
        super(
            plugin,
            file,
            resourcePath.endsWith(".yml")
                ? resourcePath
                : resourcePath + ".yml",
            loadDefault
        );
    }

    public YAMLFile(@NotNull final Plugin plugin, @NotNull final File file, @NotNull final String resourcePath) {
        super(
            plugin,
            file,
            resourcePath.endsWith(".yml")
                ? resourcePath
                : resourcePath + ".yml",
            true
        );
    }

    public YAMLFile(@NotNull final Plugin plugin, @NotNull final String resourcePath, @NotNull final String fileName) {
        this(
            plugin,
            new File(
                plugin.getDataFolder().getAbsolutePath() + (resourcePath.startsWith("/")
                    ? resourcePath
                    : "/" + resourcePath),
                fileName.endsWith(".yml") ? fileName : fileName + ".yml"
            ),
            resourcePath.isEmpty()
                ? fileName
                : resourcePath.endsWith("/")
                ? resourcePath + fileName
                : resourcePath + "/" + fileName
        );
    }

    public YAMLFile(@NotNull final Plugin plugin, @NotNull final String fileName) {
        this(plugin, "", fileName);
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

}
