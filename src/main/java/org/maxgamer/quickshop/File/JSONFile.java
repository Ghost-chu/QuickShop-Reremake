package org.maxgamer.quickshop.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.cactoos.io.ReaderOf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.File.BukkitFileAPI.JSONConfiguration;
import org.maxgamer.quickshop.Mock.MckFileConfiguration;

import java.io.File;
import java.nio.charset.StandardCharsets;

public final class JSONFile extends FileEnvelope {

    @NotNull
    private FileConfiguration fileConfiguration = new JSONConfiguration();

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
        fileConfiguration = JSONConfiguration.loadConfiguration(file);
        fileConfiguration.setDefaults(
            JSONConfiguration.loadConfiguration(
                new ReaderOf(
                    getInputStream(),
                    StandardCharsets.UTF_8
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

    @Nullable
    @Override
    public Object get(@NotNull String path) {
        return fileConfiguration.get(path);
    }

    @Override
    public void set(@NotNull String path, @NotNull Object object) {
        fileConfiguration.set(path, object);
    }

}
