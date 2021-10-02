package org.maxgamer.quickshop.util.language.game.distributions;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonElement;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.JsonUtil;
import org.maxgamer.quickshop.util.ReflectFactory;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.language.game.distributions.bean.GameManifest;
import org.maxgamer.quickshop.util.language.game.distributions.bean.VersionManifest;
import org.maxgamer.quickshop.util.mojangapi.MojangApiMirror;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class MojangDistribution {
    protected final Cache<String, String> requestCachePool = CacheBuilder.newBuilder()
            .initialCapacity(1)
            .expireAfterWrite(7, TimeUnit.DAYS)
            .recordStats()
            .build();
    private final QuickShop plugin;
    private final OkHttpClient client;
    private final MojangApiMirror mirror;

    public MojangDistribution(QuickShop plugin, MojangApiMirror mirror) {
        this.plugin = plugin;
        this.mirror = mirror;
        Util.getCacheFolder().mkdirs();
        this.client = new OkHttpClient.Builder()
                .build();

    }

    @Nullable
    public VersionManifest getVersionManifest() {
        String url = mirror.getLauncherMetaRoot()+"/mc/game/version_manifest.json";
        String data;
        if (requestCachePool.getIfPresent(url) != null)
            return JsonUtil.standard().fromJson(requestCachePool.getIfPresent(url),VersionManifest.class);
        if (!grabIntoCaches(url)) return null;
        return JsonUtil.standard().fromJson(requestCachePool.getIfPresent(url),VersionManifest.class);
    }


    @Nullable
    public GameManifest getGameManifest(VersionManifest versionManifest, String gameVersion){
        for (VersionManifest.VersionsDTO version : versionManifest.getVersions()) {
            if(version.getId().equals(gameVersion)){
                String url = version.getUrl();
                if (!grabIntoCaches(url)) return null;
                return JsonUtil.standard().fromJson(requestCachePool.getIfPresent(url),GameManifest.class);
            }
        }
        return null;
    }
    @NotNull
    public List<String> getAvailableLanguages(){
        List<String> languages = new ArrayList<>();
        VersionManifest versionManifest = getVersionManifest();
        if(versionManifest == null)
            return Collections.emptyList();
        GameManifest gameManifest = getGameManifest(versionManifest, ReflectFactory.getServerVersion());
        if(gameManifest == null)
            return Collections.emptyList();
        if(!grabIntoCaches(gameManifest.getAssetIndex().getUrl()))
            return Collections.emptyList();
        String versionMapping = requestCachePool.getIfPresent(gameManifest.getAssetIndex().getUrl());
        if(versionMapping == null)
            return Collections.emptyList();
        for (Map.Entry<String, JsonElement> objects : JsonUtil.parser().parse(versionMapping).getAsJsonObject().get("objects").getAsJsonObject().entrySet()) {
            if(objects.getKey().startsWith("minecraft/lang/")){
                languages.add(StringUtils.substringBetween("minecraft/lang/",".json"));
            }
        }
        return languages;
    }


    public boolean grabIntoCaches(String url) {
        String data;
        try (Response response = client.newCall(new Request.Builder().get().url(url).build()).execute()) {
            val body = response.body();
            if (body == null) return true;
            data = body.string();
            if (response.code() != 200) {
                plugin.getLogger().warning("Couldn't get manifest: " + response.code() + ", please report to QuickShop!");
                return false;
            }
            requestCachePool.put(url, data);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to download manifest.json, multi-language system won't work");
            return false;
        }
        return true;
    }
}
