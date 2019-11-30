package org.maxgamer.quickshop.File;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class JSONFile extends FileEnvelope {

    public JSONFile(@NotNull Plugin plugin, @NotNull File file, @NotNull String resourcePath) {
        super(
            plugin,
            file,
            resourcePath.endsWith(".json")
                ? resourcePath
                : resourcePath + ".json"
        );
    }

    public JSONFile(@NotNull final Plugin plugin, @NotNull final String resourcePath, @NotNull final String fileName) {
        this(
            plugin,
            new File(
                plugin.getDataFolder().getAbsolutePath() + (resourcePath.startsWith("/")
                    ? resourcePath
                    : "/" + resourcePath),
                fileName.endsWith(".json") ? fileName : fileName + ".json"
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
        try {

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void save() {

    }

}
