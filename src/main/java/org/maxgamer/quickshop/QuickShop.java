package org.maxgamer.quickshop;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.file.ConfigFile;

import java.util.Optional;

public final class QuickShop extends JavaPlugin {

    @NotNull
    private static Optional<QuickShop> instance = Optional.empty();

    @NotNull
    private static Optional<QuickShopLoader> loader = Optional.empty();

    @Override
    public void onLoad() {
        setInstance(this);
    }

    @Override
    public void onEnable() {
        final ConfigFile configFile = new ConfigFile();

        configFile.load();
        setLoader(new QuickShopLoader(this, configFile));
        getServer().getScheduler().runTask(this, () ->
            getServer().getScheduler().runTaskAsynchronously(this, () ->
                loader.ifPresent(quickShopLoader -> quickShopLoader.reloadPlugin(true))
            )
        );
    }

    @Override
    public void onDisable() {
        loader.ifPresent(QuickShopLoader::disablePlugin);
    }

    @NotNull
    public static QuickShop getInstance() {
        return instance.orElseThrow(() ->
            new IllegalStateException("You cannot be used QuickShop plugin before its start!"));
    }

    @NotNull
    public static QuickShopLoader getLoader() {
        return loader.orElseThrow(() ->
            new IllegalStateException("You cannot be used QuickShopLoader before its set!"));
    }

    private void setInstance(@NotNull QuickShop instance) {
        if (QuickShop.instance.isPresent()) {
            throw new IllegalStateException("You can't use #setInstance method twice!");
        }

        synchronized (this) {
            QuickShop.instance = Optional.of(instance);
        }
    }

    private void setLoader(@NotNull QuickShopLoader loader) {
        if (QuickShop.loader.isPresent()) {
            throw new IllegalStateException("You can't use #setLoader method twice!");
        }

        synchronized (this) {
            QuickShop.loader = Optional.of(loader);
        }
    }

}
