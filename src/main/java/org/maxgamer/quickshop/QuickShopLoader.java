package org.maxgamer.quickshop;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.file.ConfigFile;
import org.maxgamer.quickshop.file.LanguageFile;

public final class QuickShopLoader {

    @NotNull
    private final QuickShop quickShop;

    @NotNull
    public final ConfigFile configFile;

    @NotNull
    public final LanguageFile languageFile;

    public QuickShopLoader(@NotNull QuickShop quickShop, @NotNull ConfigFile configFile) {
        this.quickShop = quickShop;
        this.configFile = configFile;
        this.languageFile = new LanguageFile(configFile);
    }

    public void reloadPlugin(boolean firstTime) {
        disablePlugin();
        languageFile.load();

        if (firstTime) {

            // TODO: Listeners should be here.
        } else {
            configFile.load();

        }


    }

    public void disablePlugin() {
        quickShop.getServer().getScheduler().cancelTasks(quickShop);
    }

}
