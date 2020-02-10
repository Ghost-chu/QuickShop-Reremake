package org.maxgamer.quickshop;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.file.ConfigFile;

import java.util.Optional;

public final class QuickShop extends JavaPlugin {

    @NotNull
    private static Optional<QuickShop> quickShop = Optional.empty();

    @NotNull
    private static Optional<QuickShopLoader> loader = Optional.empty();

    @Override
    public void onLoad() {
        setInstance(Optional.of(this));
    }

    @Override
    public void onEnable() {
        final ConfigFile configFile = new ConfigFile();

        configFile.load();

        setLoader(Optional.of(new QuickShopLoader(this, configFile)));

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
        return quickShop.orElseThrow(() ->
            new IllegalStateException("You cannot be used QuickShop plugin before its start!"));
    }

    @NotNull
    public static QuickShopLoader getLoader() {
        return loader.orElseThrow(() ->
            new IllegalStateException("You cannot be used QuickShopLoader before its set!"));
    }

    private void setInstance(@NotNull Optional<QuickShop> quickShop) {
        QuickShop.quickShop = quickShop;
    }

    private void setLoader(@NotNull Optional<QuickShopLoader> loader) {
        QuickShop.loader = loader;
    }

}
