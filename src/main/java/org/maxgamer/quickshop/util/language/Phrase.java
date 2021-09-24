/*
 * This file is a part of project QuickShop, the name is Phrase.java
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

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An object allow fast and advanced control for phrase
 */
public class Phrase {
    private final Lang lang;
    private final String original;
    private String last;
    private boolean baked = false;
    private boolean ready = false;

    /**
     * Create a phrase
     *
     * @param lang Lang class object
     * @param path Phrase location
     */
    public Phrase(@NotNull Lang lang, @NotNull String path) {
        this.lang = lang;
        this.original = lang.getMap().getString(path);
        this.last = original;
    }

    /**
     * Create a phrase
     *
     * @param lang     Lang class object
     * @param path     Phrase location
     * @param defaults Default value
     */
    public Phrase(@NotNull Lang lang, @NotNull String path, @Nullable String defaults) {
        this.lang = lang;
        this.original = lang.getMap().getString(path, defaults);
        this.last = original;
    }

    /**
     * Apply args to original string
     *
     * @param args The args used for formatter
     */
    public void bake(@Nullable String... args) {
        this.bake(null, args);
    }

    /**
     * Apply args to original string
     *
     * @param args   The args used for formatter
     * @param sender The sender used for formatter
     */
    public void bake(@Nullable CommandSender sender, @Nullable String... args) {
        if (args != null) {
            this.last = this.lang.format(this.original, sender, args);
        } else {
            this.last = original;
        }
        this.baked = true;
        this.ready = true;
    }

    /**
     * Getting the baked string
     *
     * @param args The args used for formatter
     * @return Baked string
     */
    public String get(@Nullable String... args) {
        this.bake(args);
        this.ready = false;
        return this.last();
    }

    /**
     * Getting the baked string
     *
     * @param args   The args used for formatter
     * @param sender The sender used for formatter
     * @return Baked string
     */
    public String get(@NotNull CommandSender sender, @Nullable String... args) {
        this.bake(sender, args);
        this.ready = false;
        return this.last();
    }

    /**
     * Returns last processed string
     * It may or may not baked.
     *
     * @return Last processed string
     */
    public String last() {
        return this.last;
    }

    /**
     * Returns this phrase has been baked
     *
     * @return Does phrase baked
     */
    public boolean isBaked() {
        return this.baked;
    }

    /**
     * Returns Phrase ready for getting
     * Phrase will get ready after baking, but will set to no-ready after get once.
     *
     * @return Ready
     */
    public boolean isReady() {
        return this.ready;
    }
}
