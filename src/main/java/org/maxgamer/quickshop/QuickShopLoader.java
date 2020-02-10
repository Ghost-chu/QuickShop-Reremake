package org.maxgamer.quickshop;

import io.github.portlek.database.SQL;
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

    @NotNull
    public SQL sql;

    public QuickShopLoader(@NotNull QuickShop quickShop, @NotNull ConfigFile configFile) {
        this.quickShop = quickShop;
        this.configFile = configFile;
        this.languageFile = new LanguageFile(configFile);
        sql = configFile.createSQL();
    }

    public void reloadPlugin(boolean firstTime) {
        disablePlugin();
        languageFile.load();

        if (firstTime) {

            // TODO: Listeners should be here.
        } else {
            configFile.load();
            sql = configFile.createSQL();
        }

        if (configFile.saving.auto_save) {
            quickShop.getServer().getScheduler().runTaskTimer(
                quickShop,
                () -> {
                    // TODO Add codes for saving data as automatic
                },
                configFile.saving.auto_save_time * 20L,
                configFile.saving.auto_save_time * 20L
            );
        }

    }

    public void disablePlugin() {
        quickShop.getServer().getScheduler().cancelTasks(quickShop);


        if (configFile.saving.save_when_plugin_disable) {
            // TODO Add codes for saving data
        }

        sql.getDatabase().disconnect();
    }

}
