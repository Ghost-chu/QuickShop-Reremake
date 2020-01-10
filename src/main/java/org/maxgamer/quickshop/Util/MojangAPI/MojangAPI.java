/*
 * This file is a part of project QuickShop, the name is MojangAPI.java
 * Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 * Copyright (C) Bukkit Commons Studio and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.maxgamer.quickshop.Util.MojangAPI;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.NonQuickShopStuffs.com.sk89q.worldedit.util.net.HttpRequest;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

import java.io.IOException;
import java.net.URL;

public class MojangAPI {
    final String versionManifestUrl = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    final String assetsUrl = "https://resources.download.minecraft.net/";
    final String pathTemplate = "minecraft/lang/{0}.json";
    final Gson gson = new Gson();

    /**
     * Return Minecraft version manifest.
     *
     * @return Version Manifest, may be null when failed to get.
     */
    @Nullable
    public String getVersionManifest() {
        try {
            return HttpRequest.get(new URL(versionManifestUrl))
                    .expectResponseCode(200)
                    .execute()
                    .returnContent()
                    .asString("UTF-8").trim();
        } catch (Exception e) {
            Util.debugLog(e.getMessage());
            return null;
        }
    }

    @Nullable
    public String getVersionJson(@NotNull String versionManifest, @NotNull String mcVer) {
        VersionList list = gson.fromJson(versionManifest, VersionList.class);
        for (VersionList.VersionsBean mcv :
                list.getVersions()) {
            if (mcv.getId().equals(mcVer)) {
                try {
                    return HttpRequest.get(new URL(mcv.getUrl()))
                            .expectResponseCode(200)
                            .execute()
                            .returnContent()
                            .asString("UTF-8").trim();
                } catch (IOException e) {
                    Util.debugLog(e.getMessage());
                    return null;
                }
            }
        }
        return null;
    }

    @Nullable
    public String getLanguageFileFromAsset(@NotNull String versionJson, @NotNull String languageCode) {
        try {
            languageCode = languageCode.toLowerCase().trim();

            JsonObject object = new JsonParser().parse(versionJson).getAsJsonObject();
            JsonElement element = object.get(MsgUtil.fillArgs(pathTemplate, languageCode));
            String hash = element.getAsString();

            return HttpRequest.get(new URL(assetsUrl + hash.substring(0, 3)))
                    .execute()
                    .expectResponseCode(200)
                    .returnContent()
                    .asString("UTF-8").trim();
        } catch (Exception e) {
            Util.debugLog(e.getMessage());
            return null;
        }
    }
}
