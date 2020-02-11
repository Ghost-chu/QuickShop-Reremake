/*
 * MIT License
 *
 * Copyright © 2020 Bukkit Commons Studio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.maxgamer.quickshop;

import io.github.portlek.database.SQL;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.file.ConfigFile;
import org.maxgamer.quickshop.file.LanguageFile;
import org.maxgamer.quickshop.file.Shops;
import org.maxgamer.quickshop.file.ShopsOptions;
import org.maxgamer.quickshop.handle.RegistryBasic;
import org.maxgamer.quickshop.handle.abs.Registry;
import org.maxgamer.quickshop.utils.ListenerBasic;
import org.maxgamer.quickshop.utils.UpdateChecker;

public final class QuickShopLoader {

    @Getter
    @NotNull
    private final QuickShop quickShop;

    @Getter
    @NotNull
    private final ConfigFile configFile;

    @Getter
    @NotNull
    private final LanguageFile languageFile;

    @Getter
    @NotNull
    private final Registry registry;

    @Getter
    @NotNull
    private SQL sql;

    @NotNull
    private final ShopsOptions shopsOptions;

    @Getter
    @NotNull
    private Shops shops;

    public QuickShopLoader(@NotNull QuickShop quickShop, @NotNull ConfigFile configFile) {
        this.quickShop = quickShop;
        this.configFile = configFile;
        this.languageFile = new LanguageFile(configFile);
        registry = new RegistryBasic(this);
        sql = configFile.createSQL();
        shopsOptions = new ShopsOptions(sql);
        shops = shopsOptions.value();
    }

    public void reloadPlugin(boolean firstTime) {
        disablePlugin();
        languageFile.load();

        if (firstTime) {

            // TODO: Listeners should be here.
            new ListenerBasic<>(
                PlayerJoinEvent.class,
                event -> event.getPlayer().hasPermission("quickshop.version"),
                event -> checkForUpdate(event.getPlayer())
            ).register(quickShop);
        } else {
            configFile.load();
            sql = configFile.createSQL();
            shops = shopsOptions.value();
        }

        if (configFile.saving.auto_save) {
            quickShop.getServer().getScheduler().runTaskTimerAsynchronously(
                quickShop,
                () -> {
                    // TODO Add codes for saving data as automatic
                },
                configFile.saving.auto_save_time * 20L,
                configFile.saving.auto_save_time * 20L
            );
        }

        checkForUpdate(quickShop.getServer().getConsoleSender());
    }

    public void disablePlugin() {
        quickShop.getServer().getScheduler().cancelTasks(quickShop);


        if (configFile.saving.save_when_plugin_disable) {
            // TODO Add codes for saving data
        }

        sql.getDatabase().disconnect();
    }

    public void checkForUpdate(@NotNull CommandSender sender) {
        if (!configFile.check_for_update) {
            return;
        }

        final UpdateChecker updater = new UpdateChecker(quickShop, 62575);

        try {
            if (updater.checkForUpdates()) {
                sender.sendMessage(
                    languageFile.general.new_version_found
                        .build("%version%", updater::getLatestVersion)
                );
            } else {
                sender.sendMessage(
                    languageFile.general.latest_version
                        .build("%version%", updater::getLatestVersion)
                );
            }
        } catch (Exception exception) {
            quickShop.getLogger().warning("Update checker failed, could not connect to the API.");
        }
    }

}
