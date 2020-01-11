/*
 * This file is a part of project QuickShop, the name is VersionJson.java
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


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Util.MsgUtil;

public class VersionJson {
    @NotNull String versionJson;
    final String pathTemplate = "minecraft/lang/{0}.json";
    public VersionJson(@NotNull String json){
        this.versionJson = json;
    }
    @Nullable
    public String getLanguageHash(@NotNull String languageCode){
        JsonObject json = (JsonObject) new JsonParser().parse(this.versionJson);
        if(json == null || json.isJsonNull()){
            return null;
        }
        JsonObject objs = json.get("objects").getAsJsonObject();
        if(objs == null || objs.isJsonNull()){
            return null;
        }
        JsonObject langObj = objs.getAsJsonObject(MsgUtil.fillArgs(pathTemplate,languageCode));
        if(langObj == null || langObj.isJsonNull()){
            return null;
        }
        return langObj.getAsString();
    }
}
