package org.maxgamer.quickshop.localization.distributions.mojang;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.localization.distributions.mojang.bean.AssetIndex;
import org.maxgamer.quickshop.localization.distributions.mojang.bean.GameManifest;
import org.maxgamer.quickshop.localization.distributions.mojang.bean.VersionManifest;
import org.maxgamer.quickshop.localization.resources.MojangLocalizationResource;
import org.maxgamer.quickshop.util.JsonUtil;
import org.maxgamer.quickshop.util.ReflectFactory;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.mojangapi.MojangApiMirror;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class MojangDistribution {
    private final QuickShop plugin;
    private final OkHttpClient client;
    private final MojangApiMirror mirror;
    private final Gson gson = JsonUtil.standard();
    public MojangDistribution(QuickShop plugin, MojangApiMirror mirror) {
        this.plugin = plugin;
        this.mirror = mirror;
        Util.getCacheFolder().mkdirs();
        this.client = new OkHttpClient.Builder().
                cache(new okhttp3.Cache(new File(Util.getCacheFolder(), "mojang"), 10L * 1024L * 1024L))
                .build();
    }

    @Nullable
    private VersionManifest getVersionManifest() {
        String url = mirror.getLauncherMetaRoot() + "/mc/game/version_manifest.json";
        return getContent(url, VersionManifest.class);
    }


    @Nullable
    private GameManifest getGameManifest(VersionManifest versionManifest, String gameVersion) {
        for (VersionManifest.VersionsDTO version : versionManifest.getVersions()) {
            if (version.getId().equals(gameVersion)) {
                return getContent(version.getUrl(), GameManifest.class);
            }
        }
        return null;
    }

    @NotNull
    public List<MojangLocalizationResource> getAvailableLangResources() {
        VersionManifest versionManifest = getVersionManifest();
        if (versionManifest != null) {
            GameManifest gameManifest = getGameManifest(versionManifest, ReflectFactory.getServerVersion());
            if (gameManifest != null) {
                AssetIndex assetIndex = getContent(gameManifest.getAssetIndex().getUrl(), AssetIndex.class);
                if (assetIndex != null) {
                    return assetIndex.getObjects().entrySet().parallelStream().filter(objects -> objects.getKey().startsWith("minecraft/lang/"))
                            .map(objects ->
                            {
                                String langCode = StringUtils.substringBetween("/", ".json").toLowerCase();
                                String content = getRawContent(mirror.getResourcesDownloadRoot() + "/" + objects.getValue().getHash().substring(0, 2) + "/" + objects.getValue().getHash());
                                return MojangLocalizationResource.newResource(plugin, langCode, content);
                            }).collect(Collectors.toList());
                }
            }
        }
        return Collections.emptyList();
    }

    @Nullable
    public String getRawContent(String url) {
        try (Response response = client.newCall(new Request.Builder().url(url).build()).execute()) {
            if (!response.isSuccessful() || response.code() != 304) {
                plugin.getLogger().warning("Couldn't get manifest: " + response.code() + ", please report to QuickShop!");
                return null;
            }
            return response.body().string();
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to download manifest.json, multi-language system won't work");
            return null;
        }
    }

    @Nullable
    public <T> T getContent(String url, Class<T> targetClass) {
        String raw = getRawContent(url);
        if (raw != null) {
            return gson.fromJson(raw, targetClass);
        } else {
            return null;
        }
    }
}
