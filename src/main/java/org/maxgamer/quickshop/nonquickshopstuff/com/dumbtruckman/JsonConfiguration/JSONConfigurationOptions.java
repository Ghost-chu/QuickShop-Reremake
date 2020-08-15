/*
 * This file is a part of project QuickShop, the name is JSONConfigurationOptions.java
 *  Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
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

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.maxgamer.quickshop.nonquickshopstuff.com.dumbtruckman.JsonConfiguration;

import org.bukkit.configuration.file.FileConfigurationOptions;
import org.jetbrains.annotations.NotNull;

public class JSONConfigurationOptions extends FileConfigurationOptions {

    private boolean enablePrettyPrint = true;

    protected JSONConfigurationOptions(@NotNull final JSONConfiguration configuration) {
        super(configuration);
    }

    @Override
    public @NotNull JSONConfiguration configuration() {
        return (JSONConfiguration) super.configuration();
    }

    @Override
    public @NotNull JSONConfigurationOptions copyDefaults(final boolean value) {
        super.copyDefaults(value);
        return this;
    }

    @Override
    public @NotNull JSONConfigurationOptions pathSeparator(final char value) {
        super.pathSeparator(value);
        return this;
    }

    @Override
    public @NotNull JSONConfigurationOptions header(final String value) {
        super.header(value);
        return this;
    }

    @Override
    public @NotNull JSONConfigurationOptions copyHeader(final boolean value) {
        super.copyHeader(value);
        return this;
    }

    /**
     * Sets whether or not to pretty print the json output of the configuration.
     *
     * @param enable Whether or not pretty printing should be enabled.
     * @return This object, for chaining.
     */
    public JSONConfigurationOptions prettyPrint(final boolean enable) {
        enablePrettyPrint = enable;
        return this;
    }

    /**
     * Gets whether or not to pretty print the json output of the configuration.
     *
     * @return Whether or not to pretty print the json.
     */
    public boolean prettyPrint() {
        return enablePrettyPrint;
    }

}
