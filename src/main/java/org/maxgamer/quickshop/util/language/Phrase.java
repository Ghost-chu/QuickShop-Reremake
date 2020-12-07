/*
 * This file is a part of project QuickShop, the name is Phrase.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.util.language;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An object allow fast&advanced control for phrase
 */
public class Phrase {
    private final Lang lang;
    private final String original;
    private String last;

    /**
     * Create a phrase
     *
     * @param lang Lang class object
     * @param path Phrase location
     */
    public Phrase(@NotNull Lang lang, @NotNull String path) {
        this.lang = lang;
        this.original = lang.getString(path);
        this.last = original;
    }

    public void bake(@Nullable String... args) {
        this.bake(null, args);
    }

    public void bake(@Nullable CommandSender sender, @Nullable String... args) {
        if (args != null) {
            this.last = this.lang.format(this.original, sender, args);
        } else {
            this.last = original;
        }
    }

    public String get(@Nullable String... args) {
        this.bake(args);
        return this.last();
    }

    public String get(@NotNull CommandSender sender, @Nullable String... args) {
        this.bake(sender, args);
        return this.last();
    }

    public String last() {
        return this.last;
    }

}
