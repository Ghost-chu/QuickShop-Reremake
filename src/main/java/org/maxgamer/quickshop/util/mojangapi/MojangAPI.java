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

package org.maxgamer.quickshop.util.mojangapi;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.nonquickshopstuff.com.sk89q.worldedit.util.net.HttpRequest;
import org.maxgamer.quickshop.util.Util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MojangAPI {
    final String versionManifestUrl = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    final String assetsUrl = "https://resources.download.minecraft.net/";

    final String pathTemplate = "minecraft/lang/{0}.json";

    final Gson gson = new Gson();

    @Nullable
    public String getAssetIndexJson(@NotNull String mcVer) throws IOException {
        String versionJson = getVersionJson(mcVer);
        if (versionJson == null) {
            return null;
        }
        JsonObject rootObj = new JsonParser().parse(versionJson).getAsJsonObject();
        JsonObject assetIndex = rootObj.getAsJsonObject("assetIndex");
        if (assetIndex == null) {
            Util.debugLog("Cannot get assetIndex obj.");
            return null;
        }
        JsonPrimitive urlObj = assetIndex.getAsJsonPrimitive("url");
        if (urlObj == null) {
            Util.debugLog("Cannot get asset url obj.");
            return null;
        }
        String url = urlObj.getAsString();
        if (url == null) {
            Util.debugLog("Cannot get asset url.");
            return null;
        }
        return HttpRequest.get(new URL(url))
            .execute()
            .expectResponseCode(200)
            .returnContent()
            .asString("UTF-8")
            .trim();
    }

    @Nullable
    public String getVersionJson(@NotNull String mcVer) throws IOException {
        VersionList list = gson.fromJson(this.getVersionManifest(), VersionList.class);
        for (VersionList.VersionsBean mcv : list.getVersions()) {
            if (mcv.getId().equals(mcVer)) {
                try {
                    QuickShop.instance.getLogger().info("Downloading version index...");
                    return HttpRequest.get(new URL(mcv.getUrl()))
                        .execute()
                        .expectResponseCode(200)
                        .returnContent()
                        .asString("UTF-8")
                        .trim();
                } catch (IOException e) {
                    Util.debugLog(e.getMessage());
                    return null;
                }
            }
        }
        return null;
    }

    @Nullable
    public String getVersionManifest() throws IOException {
        QuickShop.instance.getLogger().info("Downloading version manifest...");
        return HttpRequest.get(new URL(versionManifestUrl))
            .execute()
            .expectResponseCode(200)
            .returnContent()
            .asString("UTF-8")
            .trim();
    }

    @Nullable
    public String downloadTextFileFromMojang(@NotNull String hash) throws IOException {
        File cacheFile = new File(Util.getCacheFolder(), hash);
        if (cacheFile.exists()) {
            return Util.readToString(cacheFile);
        }
        String data;
        QuickShop.instance.getLogger().info("Downloading assets file...");
        data =
            HttpRequest.get(new URL(this.assetsUrl + hash.substring(0, 2) + "/" + hash))
                .execute()
                .expectResponseCode(200)
                .returnContent()
                .asString("UTF-8")
                .trim();
        Files.write(data,cacheFile, StandardCharsets.UTF_8);
        return data;
    }

}
