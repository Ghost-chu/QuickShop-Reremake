/*
 * This file is a part of project QuickShop, the name is OneSkyAppPlatformRequestUtils.java
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

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.NonQuickShopStuffs.com.sk89q.worldedit.util.net.HttpRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

public class OneSkyAppPlatformRequestUtils {
    final static String PUBLIC_KEY = "Rd6lnQPyOk90ALq5CotRry2IQFpmp7s6";
    private static HashFunction hasher = Hashing.md5();
    public static HttpRequest doGet(@NotNull String url, @NotNull Map<String,String> args) throws MalformedURLException {
        long second = Instant.now().getEpochSecond();
        String encrypted = second + PUBLIC_KEY;
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(url);
        if(args.isEmpty()){
            urlBuilder.append("?api_key=").append(PUBLIC_KEY).append("&timestamp=").append(second).append("&dev_hash=").append(hasher.hashString(encrypted, StandardCharsets.UTF_8));
        }else{
            urlBuilder.append("?");
            boolean loaded = false;
            for (String keySet : args.keySet()){
                if(!loaded){
                    urlBuilder.append("&");
                    loaded = true;
                }
                urlBuilder.append(keySet).append("=").append(args.get(keySet));
            }
            urlBuilder.append("&api_key=").append(PUBLIC_KEY).append("&timestamp=").append(second).append("&dev_hash=").append(hasher.hashString(encrypted, StandardCharsets.UTF_8));

        }


        return HttpRequest.get(new URL(urlBuilder.toString())).header("content-type","application/json");
    }

    public static String formatByteArrayTOString(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        int temp;
        for (byte b : digest) {
            temp = b & 0xff;
            if (temp < 16) {
                sb.append(0);
            }
            sb.append(Integer.toHexString(temp));
        }
        return sb.toString();
    }
}
