/*
 * This file is a part of project QuickShop, the name is Language.java
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

package org.maxgamer.quickshop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class Language {
    private final QuickShop plugin;
    // private List<String> languages = new ArrayList<>();

    Language(QuickShop plugin) {
        this.plugin = plugin;
    }

    /**
     * Save the target language's type file to the datafolder
     *
     * @param language Target language
     * @param type     Target type
     * @param fileName The filename you want write to the plugin datafolder.
     */
    public void saveFile(@NotNull String language, @NotNull String type, @NotNull String fileName) {
        File targetFile = new File(plugin.getDataFolder(), fileName);
        if (!targetFile.exists()) {
            try {
                targetFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to save translation files.", e);
            }
        }

        try (InputStream is = getFile(language, type)) {
            if (is == null) {
                plugin.getLogger().log(Level.WARNING, "Failed to save translation files (InputStream not exist).");
                return;
            }
            Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception err) {
            plugin.getLogger().log(Level.WARNING, "Failed to save translation files.", err);
        }
    }

    // Write file under plugin folder

    /**
     * Get target language's type file.
     *
     * @param language The target language
     * @param type     The file type for you want get. e.g. messages
     * @return The target file's InputStream.
     */
    @Nullable
    public InputStream getFile(@Nullable String language, @Nullable String type) {
        if (language == null) {
            language = "en-US";
            Util.debugLog("Using the default language (EN) cause language is null.");
        }
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty");
        }
        InputStream inputStream = plugin.getResource("lang/" + language + "/" + type + ".json");
        if (inputStream == null) {
            Util.debugLog("Using the default language because we can't get the InputStream.");
            inputStream = plugin.getResource("lang/en-US/" + type + ".json");
        }
        return inputStream;
        // File name should call    type-language.yml    ---> config-zh.yml
    }

}
