/*
 * This file is a part of project QuickShop, the name is GameLanguage.java
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

package org.maxgamer.quickshop.Util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Util.MojangAPI.MojangAPI;
import org.maxgamer.quickshop.Util.MojangAPI.VersionJson;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class GameLanguage {
    private Locale locale = Locale.getDefault();
    private  String languageCode;
    private MojangAPI mojangAPI = new MojangAPI();
    private @Nullable JsonObject lang = null;
    private YamlConfiguration yamlConfiguration = new YamlConfiguration();
    public GameLanguage(@NotNull String languageCode){

        try {
            yamlConfiguration.load(new File(Util.getCacheFolder(),"lang.cache"));
            boolean needUpdateCache = false;
            String cachingServerVersion = yamlConfiguration.getString("ver");
            String cachingLanguageHash = yamlConfiguration.getString("hash");
            String cachingLanguageName = yamlConfiguration.getString("lang");

            if ("default".equals(languageCode)) {
                languageCode = locale.getLanguage() + "_" + locale.getCountry();
            }
            if(!languageCode.equals(cachingLanguageName)){
                cachingLanguageName = languageCode;
                needUpdateCache = true;
            }
            this.languageCode = languageCode.toLowerCase();
            String serverVersion = ReflectFactory.getServerVersion();
            if(!serverVersion.equals(cachingServerVersion)){
                cachingServerVersion = serverVersion;
                needUpdateCache = true;
            }

            if(needUpdateCache){
                String versionJ = mojangAPI.getVersionJson(cachingServerVersion);
                if (versionJ != null) {
                    VersionJson versionJson = new VersionJson(versionJ);
                    String hash = versionJson.getLanguageHash(this.languageCode);
                    if (hash != null) {
                        cachingLanguageHash = hash;
                        String langJson = mojangAPI.downloadTextFileFromMojang(hash);
                        if(langJson != null){
                            new Copied(new File(Util.getCacheFolder(),hash)).accept(new ByteArrayInputStream(langJson.getBytes(StandardCharsets.UTF_8)));
                        }
                    }
                }
            }
            yamlConfiguration.set("ver",cachingServerVersion);
            yamlConfiguration.set("hash",cachingLanguageHash);
            yamlConfiguration.set("lang",cachingLanguageName);
            yamlConfiguration.save(new File(Util.getCacheFolder(),"lang.cache"));
            String json = null;
            if(cachingLanguageHash!=null){
               json = Util.readToString(new File(Util.getCacheFolder(),cachingLanguageHash));
            }
            if(json != null && !json.isEmpty()){
                lang = new JsonParser().parse(json).getAsJsonObject();
            }

        }catch (Exception e){
            Util.debugLog(e.getMessage());
        }
    }
    @NotNull
    public String getItem(@NotNull Material material){
        String name = Util.prettifyText(material.name());
        if(lang == null){
            return name;
        }
        JsonObject obj = lang.get("item").getAsJsonObject().get("minecraft").getAsJsonObject();
        String jsonName = obj.get(material.name().toLowerCase()).getAsString();
        if(jsonName == null || jsonName.isEmpty()){
            return name;
        }
        return jsonName;
    }
    @NotNull
    public String getBlock(@NotNull Material material){
        String name = Util.prettifyText(material.name());
        if(lang == null){
            return name;
        }
        JsonObject obj = lang.get("block").getAsJsonObject().get("minecraft").getAsJsonObject();
        String jsonName = obj.get(material.name().toLowerCase()).getAsString();
        if(jsonName == null || jsonName.isEmpty()){
            return name;
        }
        return jsonName;
    }
    @NotNull
    public String getEntity(@NotNull Material material){
        String name = Util.prettifyText(material.name());
        if(lang == null){
            return name;
        }
        JsonObject obj = lang.get("entity").getAsJsonObject().get("minecraft").getAsJsonObject();
        String jsonName = obj.get(material.name().toLowerCase()).getAsString();
        if(jsonName == null || jsonName.isEmpty()){
            return name;
        }
        return jsonName;
    }
    @NotNull
    public String getPotion(@NotNull PotionEffectType effect){
        String name = Util.prettifyText(effect.getName());
        if(lang == null){
            return name;
        }
        JsonObject obj = lang.get("effect").getAsJsonObject().get("minecraft").getAsJsonObject();
        String jsonName = obj.get(effect.getName().toLowerCase()).getAsString();
        if(jsonName == null || jsonName.isEmpty()){
            return name;
        }
        return jsonName;
    }
    @NotNull
    public String getEnchantment(@NotNull String enchantmentName){
        String name = Util.prettifyText(enchantmentName);
        if(lang == null){
            return name;
        }
        JsonObject obj = lang.get("enchantment").getAsJsonObject().get("minecraft").getAsJsonObject();
        String jsonName = obj.get(enchantmentName.toLowerCase()).getAsString();
        if(jsonName == null || jsonName.isEmpty()){
            return name;
        }
        return jsonName;
    }
    @Nullable
    public String getCustom(@NotNull String node){
        if(lang == null){
            return null;
        }
        node = node.toLowerCase();
        String[] nodes = node.split(".");
        JsonObject obj = null;
        for (String s : nodes) {
            if (obj == null) {
                obj = lang.get(s).getAsJsonObject();
            } else {
                obj = obj.get(s).getAsJsonObject();
            }
        }
        if(obj == null){
            return null;
        }
        return obj.getAsString();
    }
}
