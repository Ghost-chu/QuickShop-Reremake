/*
 * This file is a part of project QuickShop, the name is LanguageOTA.java
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

package org.maxgamer.quickshop.Util.OneSkyAppPlatform;

import com.google.gson.Gson;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.NonQuickShopStuffs.com.sk89q.worldedit.util.net.HttpRequest;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class LanguageOTA {
    List<LanguageDetailsContainer> avaliableLanguages;
    boolean ready;
    Gson gson = new Gson();
    public LanguageOTA(@NotNull QuickShop plugin){
        Thread thread = new Thread(() -> {
            Util.debugLog("Asynchronous get languages data from OneSkyApp platform OTA api...");
            avaliableLanguages = requestTheAvaliableLanguages();
            ready = true;
            Util.debugLog("Finished");
        });
        thread.setName("Async OTA pulling task thread");
        thread.start();
    }
    @Nullable
    public  String downloadTranslations(@NotNull String code)  {
        try {
            Map<String,String> args = new HashMap<>();
            args.put("locale",code);
            args.put("source_file_name","messages-en.json");

            return OneSkyAppPlatformRequestUtils.doGet("https://platform.api.onesky.io/1/projects/166413/translations",args)
                    .execute()
                    .expectResponseCode(200)
                    .returnContent()
                    .asString("UTF-8").trim();
        }catch (Exception e){
            return null;
        }

    }
    @NotNull
    public List<LanguageDetailsContainer> requestTheAvaliableLanguages(){
        List<LanguageDetailsContainer> list = new ArrayList<>();
        try {
            Map<String,String> args = new HashMap<>();
            HttpRequest httpRequest = OneSkyAppPlatformRequestUtils.doGet("https://platform.api.onesky.io/1/projects/166413/languages",args);
            String resultJson = httpRequest
                    .execute()
                    .expectResponseCode(200)
                    .returnContent()
                    .asString("UTF-8").trim();
            ListOfLanguagesForOTA container = gson.fromJson(resultJson,ListOfLanguagesForOTA.class);
            container.getData().forEach((dataBean -> {
                String displayName = dataBean.getEnglish_name()+"["+dataBean.getLocal_name()+"%"+"{"+dataBean.getCode()+"}"+"]";
                Util.debugLog("OTA have required: "+displayName);
                list.add(new LanguageDetailsContainer(dataBean.getCode(),displayName,dataBean.getLocale(),dataBean.getRegion()));
            }));
            return list;
        }catch (Exception e){
            Util.debugLog(e.getClass().getName()+":"+e.getMessage());
            return new ArrayList<>();
        }
    }




}
