/*
 * This file is a part of project QuickShop, the name is Lang.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.util.language;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.util.language.formatter.FilledFormatter;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class Lang {
    private final File file;
    private final Consumer<FileConfiguration> upgrading;
    private final ImmutableList<Formatter> formatters;
    @Getter
    private FileConfiguration map;

    /**
     * Creating Language utils from a file
     *
     * @param file       The Yaml language file
     * @param upgrading  The upgrading method, executing everytime and allows devs to pre-processing or upgrading their
     *                   language files.
     * @param formatters The formatters used to formatting texts passthroughs this utils..
     */
    public Lang(@NotNull File file, @NotNull Consumer<FileConfiguration> upgrading, @Nullable Formatter... formatters) {
        this.file = file;
        this.upgrading = upgrading;
        if (formatters != null) {
            this.formatters = ImmutableList.copyOf(formatters);
        } else {
            this.formatters = ImmutableList.of(new FilledFormatter());
        }
        this.reload();
    }

    /**
     * Clean up and reload the data from drive
     */
    public void reload() {
        if (!this.file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                this.file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.map = YamlConfiguration.loadConfiguration(this.file);
        this.upgrading.accept(map);
    }

    /**
     * Save the changes of the language file
     */
    public void save() {
        try {
            this.map.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the data
     *
     * @param path The data path
     * @param data The data
     */
    public void set(@NotNull String path, @Nullable Object data) {
        this.map.set(path, data);
    }

    /**
     * Formatting the phrase by formatters
     *
     * @param raw    Original text
     * @param sender (optional) The text sender
     * @param args   (optional) The args used for formatting
     * @return Formatted text
     */
    public String format(@Nullable String raw, @Nullable CommandSender sender, @Nullable String... args) {
        if (StringUtils.isNotEmpty(raw)) {
            for (Formatter formatter : formatters) {
                raw = formatter.format(raw, sender, args);
            }
        }
        return raw;
    }

    /**
     * Formatting the phrase by formatters
     *
     * @param raw  Original text
     * @param args (optional) The args used for formatting
     * @return Formatted text
     */
    public String format(@Nullable String raw, @Nullable String... args) {
        return format(raw, null, args);
    }

    /**
     * Creating a Phrase from location
     * Phrase object allows developer to advanced and faster controlling phrase
     *
     * @param path The text path
     * @return A Phrase
     */
    @NotNull
    public Phrase create(@NotNull String path) {
        return new Phrase(this, path);
    }

    /**
     * Getting text from language file
     *
     * @param path The phrase location
     * @param args (optional) The args used for formatting
     * @return The formatted text
     */
    @NotNull
    public String getString(@NotNull String path, @Nullable String... args) {
        return new Phrase(this, path).get(args);
    }

    /**
     * Getting text from language file
     *
     * @param path   The phrase location
     * @param sender (optional) The text sender
     * @param args   (optional) The args used for formatting
     * @return The formatted text
     */
    @NotNull
    public String getString(@NotNull String path, @NotNull CommandSender sender, @Nullable String... args) {
        return new Phrase(this, path).get(sender, args);
    }

}
