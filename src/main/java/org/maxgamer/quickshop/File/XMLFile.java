package org.maxgamer.quickshop.File;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class XMLFile extends FileEnvelope {

    public XMLFile(@NotNull final Plugin plugin, @NotNull final File file, @NotNull final String resourcePath,
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

    public XMLFile(@NotNull final Plugin plugin, @NotNull final File file, @NotNull final String resourcePath) {
        super(
            plugin,
            file,
            resourcePath.endsWith(".yml")
                ? resourcePath
                : resourcePath + ".yml",
            true
        );
    }

    public XMLFile(@NotNull final Plugin plugin, @NotNull final String resourcePath, @NotNull final String fileName) {
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

    public XMLFile(@NotNull final Plugin plugin, @NotNull final String fileName) {
        this(plugin, "", fileName);
    }

    @Override
    public void reload() {

    }

}
