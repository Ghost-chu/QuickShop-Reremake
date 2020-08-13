/*
 * This file is a part of project QuickShop, the name is JSONFile.java
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

package org.maxgamer.quickshop.fileportlek.old;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.nonquickshopstuff.com.dumbtruckman.JsonConfiguration.JSONConfiguration;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class JSONFile extends FileEnvelope {

    public JSONFile(@NotNull final Plugin plugin, @NotNull final File file, @NotNull final String resourcePath, boolean loadDefault) {
        super(plugin, file, resourcePath.endsWith(".json") ? resourcePath : resourcePath + ".json", loadDefault);
    }

    public JSONFile(@NotNull final Plugin plugin, @NotNull final String fileName) {
        this(plugin, "", fileName);
    }

    public JSONFile(@NotNull final Plugin plugin, @NotNull final String resourcePath, @NotNull final String fileName) {
        this(plugin, new File(plugin.getDataFolder().getAbsolutePath() + (resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath), fileName.endsWith(".json") ? fileName : fileName + ".json"), resourcePath.isEmpty() ? fileName : resourcePath.endsWith("/") ? resourcePath + fileName : resourcePath + "/" + fileName);
    }

    public JSONFile(@NotNull final Plugin plugin, @NotNull final File file, @NotNull final String resourcePath) {
        super(plugin, file, resourcePath.endsWith(".json") ? resourcePath : resourcePath + ".json", true);
    }

    @Override
    public void reload() {
        fileConfiguration = JSONConfiguration.loadConfiguration(file);
        if (loadDefault) {
            fileConfiguration.setDefaults(JSONConfiguration.loadConfiguration(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8)));
        }
    }

}
