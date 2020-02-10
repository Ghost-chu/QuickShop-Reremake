package org.maxgamer.quickshop;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.file.ConfigFile;

public final class QuickShop extends JavaPlugin {

    @Nullable
    private static QuickShop quickShop;

    @Nullable
    private static QuickShopLoader loader;

    @Override
    public void onLoad() {
        quickShop = this;
    }

    @Override
    public void onEnable() {
        final ConfigFile configFile = new ConfigFile();

        configFile.load();
        loader = new QuickShopLoader(
            this,
            configFile
        );

        getServer().getScheduler().runTask(this, () ->
            getServer().getScheduler().runTaskAsynchronously(this, () ->
                loader.reloadPlugin(true)
            )
        );
    }

    @Override
    public void onDisable() {
        if (loader != null) {
            loader.disablePlugin();
        }
    }

    @NotNull
    public static QuickShop getInstance() {
        if (quickShop == null) {
            throw new IllegalStateException("You cannot be used QuickShop plugin before its start!");
        }

        return quickShop;
    }

    @NotNull
    @SuppressWarnings("unused")
    public static QuickShopLoader getLoader() {
        if (loader == null) {
            throw new IllegalStateException("You cannot be used QuickShopLoader before its set!");
        }

        return loader;
    }

}
