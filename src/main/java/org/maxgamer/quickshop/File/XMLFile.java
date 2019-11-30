package org.maxgamer.quickshop.File;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public final class XMLFile extends FileEnvelope {

    public XMLFile(@NotNull Plugin plugin, @NotNull File file, @NotNull String resourcePath) {
        super(plugin, file, resourcePath);
    }

    @Override
    public void reload() {

    }

    @Override
    public void save() {

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
