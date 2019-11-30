package org.maxgamer.quickshop.File;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class JSONFile extends FileEnvelope {

    public JSONFile(@NotNull Plugin plugin, @NotNull File file, @NotNull String resourcePath) {
        super(plugin, file, resourcePath);
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
