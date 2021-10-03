package org.maxgamer.quickshop.util.language.text.distributions.crowdin;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import lombok.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.JsonUtil;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.language.text.distributions.Distribution;
import org.maxgamer.quickshop.util.language.text.distributions.crowdin.bean.Manifest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class CrowdinOTA implements Distribution {
    protected static final String CROWDIN_OTA_HOST = "https://distributions.crowdin.net/daf1a8db40f132ce157c457xrm4/";
    protected final Cache<String, String> requestCachePool = CacheBuilder.newBuilder()
            .initialCapacity(1)
            .expireAfterWrite(7, TimeUnit.DAYS)
            .recordStats()
            .build();
    private final QuickShop plugin;
    // private final File cacheFolder;
    // private final okhttp3.Cache cache;
    private final OkHttpClient client;
    //  private final OkHttpClient clientNoCache;

    public CrowdinOTA(QuickShop plugin) {
        this.plugin = plugin;
        Util.getCacheFolder().mkdirs();
        // this.cacheFolder = new File(Util.getCacheFolder(), "okhttp");
        //   this.cacheFolder.mkdirs();
        // this.cache = new okhttp3.Cache(cacheFolder, 100 * 1024 * 1024);
//        this.client = new OkHttpClient.Builder()
//                .cache(cache)
//                .build();
        this.client = new OkHttpClient.Builder()
                .build();

    }

//    private byte[] requestWithCache(@NotNull String url, @Nullable File saveTo) throws IOException {
//        byte[] data = requestCachePool.getIfPresent(url);
//        if (data == null) {
//            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//            HttpGet getRequest = new HttpGet(url);
//            getRequest.setConfig(RequestConfig.custom()
//                    .setSocketTimeout(30 * 1000)
//                    .setConnectTimeout(20 * 1000).build());
//            CloseableHttpResponse response = httpClient.execute(getRequest);
//            data = Util.inputStream2ByteArray(response.getEntity().getContent());
//            if (response.getStatusLine().getStatusCode() != 200)
//                throw new OTAException(response.getStatusLine().getStatusCode(), new String(data == null ? new byte[0] : Util.inputStream2ByteArray(response.getEntity().getContent()), StandardCharsets.UTF_8));
//            if (saveTo != null && data != null)
//                Files.write(saveTo.toPath(), data, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
//        }
//        return data;
//    }

    @Nullable
    public Manifest getManifest() {
        return JsonUtil.getGson().fromJson(getManifestJson(), Manifest.class);
    }

    @Nullable
    public String getManifestJson() {
        String url = CROWDIN_OTA_HOST + "manifest.json";
        String data;
        if (requestCachePool.getIfPresent(url) != null) {
            return requestCachePool.getIfPresent(url);
        }
        try (Response response = client.newCall(new Request.Builder().get().url(url).build()).execute()) {
            val body = response.body();
            if (body == null) {
                return null;
            }
            data = body.string();
            if (response.code() != 200) {
                plugin.getLogger().warning("Couldn't get manifest: " + response.code() + ", please report to QuickShop!");
                return null;
            }
            requestCachePool.put(url, data);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to download manifest.json, multi-language system won't work");
            return null;
        }
        return data;
    }

    public Map<String, String> genLanguageMapping() {
        if (getManifestJson() == null) {
            return new HashMap<>();
        }
        Map<String, String> mapping = new HashMap<>();
        JsonElement parser = new JsonParser().parse(getManifestJson());
        for (Map.Entry<String, JsonElement> set : parser.getAsJsonObject().getAsJsonObject("language_mapping").entrySet()) {
            if (!set.getValue().isJsonObject()) {
                continue;
            }
            JsonPrimitive object = set.getValue().getAsJsonObject().getAsJsonPrimitive("locale");
            if (object == null) {
                continue;
            }
            mapping.put(set.getKey(), object.getAsString());
        }
        return mapping;
    }

    @Override
    @NotNull
    public List<String> getAvailableLanguages() {
        Manifest manifest = getManifest();
        if (manifest == null) {
            return Collections.emptyList();
        }
        List<String> languages = new ArrayList<>();
        Map<String, String> mapping = genLanguageMapping();
        for (String language : manifest.getLanguages()) {
            languages.add(mapping.getOrDefault(language, language));
        }
        return languages;
    }

    @Override
    @NotNull
    public List<String> getAvailableFiles() {
        Manifest manifest = getManifest();
        if (manifest == null) {
            return Collections.emptyList();
        }
        return manifest.getFiles();
    }

    @Override
    public @NotNull String getFile(String fileCrowdinPath, String crowdinLocale) throws Exception {
        return getFile(fileCrowdinPath, crowdinLocale, false);
    }

    @Override
    @NotNull
    public String getFile(String fileCrowdinPath, String crowdinLocale, boolean forceFlush) throws Exception {
        Manifest manifest = getManifest();
        if (manifest == null) {
            throw new IllegalStateException("Failed to get project manifest");
        }
        if (!manifest.getFiles().contains(fileCrowdinPath)) {
            throw new IllegalArgumentException("The file " + fileCrowdinPath + " not exists on Crowdin");
        }
        if (manifest.getCustomLanguages() != null && !manifest.getCustomLanguages().contains(crowdinLocale)) {
            throw new IllegalArgumentException("The locale " + crowdinLocale + " not exists on Crowdin");
        }
        String postProcessingPath = fileCrowdinPath.replace("%locale%", crowdinLocale);
        String pathHash = DigestUtils.sha1Hex(postProcessingPath);
        File metadataFile = new File(Util.getCacheFolder(), "i18n.metadata");
        YamlConfiguration cacheMetadata = YamlConfiguration.loadConfiguration(metadataFile);
        long localeTimestamp = cacheMetadata.getLong(pathHash + ".timestamp");
        File cachedDataFile = new File(Util.getCacheFolder(), pathHash);
        String data = null;
        if (cachedDataFile.exists()) {
            Util.debugLog("Reading data from local cache: " + cachedDataFile.getCanonicalPath());
            data = Util.readToString(cachedDataFile);
        }
        // invalidate cache, flush it
        if (forceFlush || data == null || localeTimestamp != manifest.getTimestamp()) {
            String url = CROWDIN_OTA_HOST + "content" + fileCrowdinPath.replace("%locale%", crowdinLocale);
            Util.debugLog("Reading data from remote server: " + url);
            try (Response response = client.newCall(new Request.Builder().get().url(url).build()).execute()) {
                val body = response.body();
                if (body == null) {
                    throw new OTAException(response.code(), ""); // Returns empty string
                }
                data = body.string();
                if (response.code() != 200) {
                    throw new OTAException(response.code(), data);
                }
                Files.write(cachedDataFile.toPath(), data.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to download manifest.json, multi-language system may won't work");
                return "";
            }
            // update cache index
            cacheMetadata.set(pathHash + ".timestamp", manifest.getTimestamp());
            cacheMetadata.save(metadataFile);
            return data;
        }
        return data;
    }

    @EqualsAndHashCode(callSuper = true)
    @AllArgsConstructor
    @Builder
    @Data
    public static class OTAException extends Exception {
        private int httpCode;
        private String content;
    }

    @AllArgsConstructor
    @Builder
    @Data
    public static class CrowdinGetFileRequest {
        private String fileCrowdinPath;
        private String crowdinLocale;
        private boolean forceFlush;
    }
}