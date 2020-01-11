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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.NonQuickShopStuffs.com.sk89q.worldedit.util.net.HttpRequest;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

import java.io.File;
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
    public String getVersionManifest() throws IOException {
            QuickShop.instance.getLogger().info("Downloading version manifest...");
            return HttpRequest.get(new URL(versionManifestUrl))
                    .execute()
                    .expectResponseCode(200)
                    .returnContent()
                    .asString("UTF-8").trim();
    }

    @Nullable
    public String getVersionJson(@NotNull String mcVer) throws IOException {
        VersionList list = gson.fromJson(this.getVersionManifest(), VersionList.class);
        for (VersionList.VersionsBean mcv :
                list.getVersions()) {
            if (mcv.getId().equals(mcVer)) {
                try {
                    QuickShop.instance.getLogger().info("Downloading version index...");
                    return HttpRequest.get(new URL(mcv.getUrl()))
                            .execute()
                            .expectResponseCode(200)
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
    public String getAssetIndexJson(@NotNull String mcVer) throws IOException{
        String versionJson = getVersionJson(mcVer);
        if(versionJson == null){
            return null;
        }
        PerVersionJson perVersionList = gson.fromJson(versionJson, PerVersionJson.class);
        String url = null;
        if(perVersionList == null){
            return null;
        }
        for (PerVersionJson.PatchesBean bean:
        perVersionList.getPatches()) {
            if(!"game".equals(bean.getId())){
                continue;
            }
            url = bean.getAssetIndex().getUrl();
            break;
        }
        if(url == null){
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
    public String downloadTextFileFromMojang(@NotNull String hash) throws IOException {
        File cacheFile = new File(Util.getCacheFolder(),hash);
        if(cacheFile.exists()){
            return Util.readToString(cacheFile);
        }
        String data;
        QuickShop.instance.getLogger().info("Downloading assets file...");
            data = HttpRequest.get(new URL(this.assetsUrl + hash.substring(0, 2)))
                    .execute()
                    .expectResponseCode(200)
                    .returnContent()
                    .asString("UTF-8").trim();

        return data;
    }
}
