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

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.ConditionFailedException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.commands.QuickShopCommand;
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
        final BukkitCommandManager manager = new BukkitCommandManager(this);
        final ConfigFile configFile = new ConfigFile();

        configFile.load();
        setLoader(new QuickShopLoader(this, configFile));
        getServer().getScheduler().runTask(this, () ->
            getServer().getScheduler().runTaskAsynchronously(this, () ->
                loader.ifPresent(quickShopLoader -> quickShopLoader.reloadPlugin(true))
            )
        );
        loader.ifPresent(quickShopLoader -> {
            manager.getCommandConditions().addCondition(String[].class, "player", (c, exec, value) -> {
                if (value == null || value.length == 0) {
                    return;
                }

                final String playerName = value[c.getConfigValue("arg", 0)];

                if (c.hasConfig("arg") && Bukkit.getPlayer(playerName) == null) {
                    throw new ConditionFailedException(
                        quickShopLoader.getLanguageFile().error.player_not_found.build("%player_name%", () -> playerName)
                    );
                }
            });
            manager.registerCommand(
                new QuickShopCommand(quickShopLoader)
            );
        });
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
