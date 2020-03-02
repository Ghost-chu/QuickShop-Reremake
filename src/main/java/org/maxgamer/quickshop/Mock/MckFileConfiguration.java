package org.maxgamer.quickshop.Mock;

import lombok.ToString;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

@ToString
public final class MckFileConfiguration extends FileConfiguration {
    @NotNull
    @Override
    public String saveToString() {
        return "";
    }

    @Override
    public void loadFromString(@NotNull String s) {
    }

    @NotNull
    @Override
    protected String buildHeader() {
        return "";
    }

}
