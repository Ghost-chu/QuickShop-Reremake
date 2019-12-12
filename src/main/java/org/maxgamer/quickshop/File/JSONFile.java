package org.maxgamer.quickshop.File;

import org.bukkit.plugin.Plugin;
import org.cactoos.io.ReaderOf;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.File.BukkitFileAPI.JSONConfiguration;

import java.io.File;
import java.nio.charset.StandardCharsets;

public final class JSONFile extends FileEnvelope {

    public JSONFile(@NotNull final Plugin plugin, @NotNull final File file, @NotNull final String resourcePath,
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

    public JSONFile(@NotNull final Plugin plugin, @NotNull final File file, @NotNull final String resourcePath) {
        super(
            plugin,
            file,
            resourcePath.endsWith(".yml")
                ? resourcePath
                : resourcePath + ".yml",
            true
        );
    }

    public JSONFile(@NotNull final Plugin plugin, @NotNull final String resourcePath, @NotNull final String fileName) {
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

    public JSONFile(@NotNull final Plugin plugin, @NotNull final String fileName) {
        this(plugin, "", fileName);
    }

    @Override
    public void reload() {
        fileConfiguration = JSONConfiguration.loadConfiguration(file);
        if (loadDefault) {
            fileConfiguration.setDefaults(
                JSONConfiguration.loadConfiguration(
                    new ReaderOf(
                        getInputStream(),
                        StandardCharsets.UTF_8
                    )
                )
            );
        }
    }

}
