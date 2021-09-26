package org.maxgamer.quickshop.util.language.text.distributions.crowdin;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.nonquickshopstuff.com.sk89q.worldedit.util.net.HttpRequest;
import org.maxgamer.quickshop.util.JsonUtil;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.language.text.distributions.Distribution;
import org.maxgamer.quickshop.util.language.text.distributions.crowdin.bean.Manifest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CrowdinOTA implements Distribution {
    protected static final String CROWDIN_OTA_HOST = "https://distributions.crowdin.net/daf1a8db40f132ce157c457xrm4/";
    protected final Cache<String, String> requestCachePool = CacheBuilder.newBuilder()
            .expireAfterWrite(7, TimeUnit.DAYS)
            .recordStats()
            .build();
    private QuickShop plugin;

    public CrowdinOTA(QuickShop plugin) {
        this.plugin = plugin;
        Util.getCacheFolder().mkdirs();
    }

    private @Nullable String requestWithCache(String url) {
        String data = requestCachePool.getIfPresent(url);
        if (data == null) {
            try {
                data = HttpRequest.get(new URL(url))
                        .execute()
                        .expectResponseCode(200)
                        .returnContent()
                        .asString("UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            if (data != null)
                requestCachePool.put(url, data);
        }
        return data;
    }

    @Nullable
    public Manifest getManifest() {
        String url = CROWDIN_OTA_HOST + "manifest.json";
        String data = requestWithCache(url);
        if (data == null)
            return null;
        return JsonUtil.getGson().fromJson(data, Manifest.class);
    }

    @NotNull
    public List<String> getAvailableLanguages() {
        Manifest manifest = getManifest();
        if (manifest == null)
            return Collections.emptyList();
        return manifest.getLanguages();
    }

    @NotNull
    public List<String> getAvailableFiles() {
        Manifest manifest = getManifest();
        if (manifest == null)
            return Collections.emptyList();
        return manifest.getFiles();
    }

    @Override
    public @NotNull String getFile(String fileCrowdinPath, String crowdinLocale) throws Exception {
        return getFile(fileCrowdinPath,crowdinLocale,false);
    }

    @NotNull
    public String getFile(String fileCrowdinPath, String crowdinLocale, boolean forceFlush)throws Exception  {
        Manifest manifest = getManifest();
        if (manifest == null)
            throw new IllegalStateException("Failed to get project manifest");
        if (!manifest.getFiles().contains(fileCrowdinPath))
            throw new IllegalArgumentException("The file " + fileCrowdinPath + " not exists on Crowdin");
        if (manifest.getCustomLanguages() != null && !manifest.getCustomLanguages().contains(crowdinLocale))
            throw new IllegalArgumentException("The locale " + crowdinLocale + " not exists on Crowdin");
        String postProcessingPath = fileCrowdinPath.replace("%locale%", crowdinLocale);
        String pathHash = DigestUtils.sha1Hex(postProcessingPath);
        File metadataFile = new File(Util.getCacheFolder(), "i18n.metadata");
        YamlConfiguration cacheMetadata = YamlConfiguration.loadConfiguration(metadataFile);
        long localeTimestamp = cacheMetadata.getLong(pathHash + ".timestamp");
        File cachedDataFile = new File(Util.getCacheFolder(), pathHash);
        String data = null;
        if(cachedDataFile.exists()){
            data = Util.readToString(cachedDataFile);
        }
        // invalidate cache, flush it
        if (forceFlush || data == null ||localeTimestamp != manifest.getTimestamp()) {
            String url = CROWDIN_OTA_HOST + "content/" + fileCrowdinPath.replace("%locale%", postProcessingPath);
            data = requestWithCache(url);
            if (data == null)
                throw new IOException("Couldn't download translation from remote server");
            // update cache index
            cacheMetadata.set(pathHash + ".timestamp", manifest.getTimestamp());
            cacheMetadata.save(metadataFile);
        }
//        if (data == null) {
//            cacheMetadata.set(pathHash, null);
//            cacheMetadata.save(metadataFile);
//            throw new IOException("Couldn't read translation from local cache, please try again");
//        }
        return data;
    }
}