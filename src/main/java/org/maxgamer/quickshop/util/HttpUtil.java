/*
 * This file is a part of project QuickShop, the name is HttpUtil.java
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

package org.maxgamer.quickshop.util;

import com.google.common.cache.CacheBuilder;
import lombok.val;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HttpUtil {
    private final OkHttpClient client = new OkHttpClient.Builder()
            .cache(new Cache(getCacheFolder(), 50L * 1024L * 1024L)).build();
    protected static final com.google.common.cache.Cache<String, String> requestCachePool = CacheBuilder.newBuilder()
            .expireAfterWrite(7, TimeUnit.DAYS)
            .build();

    public static HttpUtil create() {
        return new HttpUtil();
    }

    public static Response makeGet(@NotNull String url) throws IOException {
        return HttpUtil.create().getClient().newCall(new Request.Builder().get().url(url).build()).execute();
    }

    public static String createGet(@NotNull String url) {
        String cache = requestCachePool.getIfPresent(url);
        if (cache != null) {
            return cache;
        }
        try (Response response = HttpUtil.create().getClient().newCall(new Request.Builder().get().url(url).build()).execute()) {
            val body = response.body();
            if (body == null) {
                return null;
            }
            cache = body.string();
            if (response.code() != 200) {
                return null;
            }
            requestCachePool.put(url, cache);
            return cache;
        } catch (IOException e) {
            return null;
        }
    }

    public static Response makePost(@NotNull String url, @NotNull RequestBody body) throws IOException {
        return HttpUtil.create().getClient().newCall(new Request.Builder().post(body).url(url).build()).execute();
    }

    private File getCacheFolder() {
        File file = new File(Util.getCacheFolder(), "okhttp_tmp");
        file.mkdirs();
        return file;
    }

    public OkHttpClient getClient() {
        return client;
    }
}
