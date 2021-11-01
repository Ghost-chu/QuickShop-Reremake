/*
 * This file is a part of project QuickShop, the name is CrowdinOTA.java
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

package org.maxgamer.quickshop.localization.text.distributions.crowdin;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import lombok.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.localization.text.distributions.Distribution;
import org.maxgamer.quickshop.localization.text.distributions.crowdin.bean.Manifest;
import org.maxgamer.quickshop.util.HttpUtil;
import org.maxgamer.quickshop.util.JsonUtil;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class CrowdinOTA implements Distribution {
    protected static final String CROWDIN_OTA_HOST = "https://distributions.crowdin.net/daf1a8db40f132ce157c457xrm4/";
    private final QuickShop plugin;

    public CrowdinOTA(QuickShop plugin) {
        this.plugin = plugin;
        Util.getCacheFolder().mkdirs();

    }

    /**
     * Getting the Crowdin distribution manifest
     *
     * @return The distribution manifest
     */

    @Nullable
    public Manifest getManifest() {
        return JsonUtil.regular().fromJson(getManifestJson(), Manifest.class);
    }

    /**
     * Getting the Crowdin distribution manifest json
     *
     * @return The distribution manifest json
     */
    @Nullable
    public String getManifestJson() {
        String url = CROWDIN_OTA_HOST + "manifest.json";
        return HttpUtil.createGet(url);
    }

    /**
     * Getting crowdin language mapping (crowdin code -> minecraft code)
     * Can be set on Crowdin platform
     *
     * @return The language mapping
     */
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

    /**
     * Getting all languages available on crowdin, so we can use that as the key to read language mapping.
     *
     * @return The languages available
     */
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
        // Validate
        if (manifest == null) {
            throw new IllegalStateException("Failed to get project manifest");
        }
        if (!manifest.getFiles().contains(fileCrowdinPath)) {
            throw new IllegalArgumentException("The file " + fileCrowdinPath + " not exists on Crowdin");
        }
//        if (manifest.getCustom_languages() != null && !manifest.getCustom_languages().contains(crowdinLocale)) {
//            throw new IllegalArgumentException("The locale " + crowdinLocale + " not exists on Crowdin");
//        }
        // Post path (replaced with locale code)
        String postProcessingPath = fileCrowdinPath.replace("%locale%", crowdinLocale);
        OTACacheControl otaCacheControl = new OTACacheControl();
        // Validating the manifest
        long manifestTimestamp = getManifest().getTimestamp();
        if (otaCacheControl.readManifestTimestamp() == getManifest().getTimestamp() && !forceFlush) {
            // Use cache
            try {
                // Check cache outdated
                if (!otaCacheControl.isCachedObjectOutdated(postProcessingPath, manifestTimestamp)) {
                    // Return the caches
                    Util.debugLog("Use local cache for " + postProcessingPath);
                    return new String(otaCacheControl.readObjectCache(postProcessingPath), StandardCharsets.UTF_8);
                } else {
                    Util.debugLog("Local cache outdated for " + postProcessingPath);
                    Util.debugLog("Excepted " + otaCacheControl.readCachedObjectTimestamp(postProcessingPath) + " actual: " + manifestTimestamp);
                }
            } catch (Exception exception) {
                MsgUtil.debugStackTrace(exception.getStackTrace());
            }
        } else {
            Util.debugLog("Manifest timestamp check failed " + postProcessingPath + " excepted:" + otaCacheControl.readManifestTimestamp() + " actual: " + getManifest().getTimestamp() + " forceUpdate: " + forceFlush);
        }
        // Out of the cache
        String url = CROWDIN_OTA_HOST + "content" + fileCrowdinPath.replace("%locale%", crowdinLocale);
        plugin.getLogger().info("Updating translation " + crowdinLocale + " from: " + url);
        String data = HttpUtil.createGet(url);
        if (data == null) {
            // Failed to grab data
            throw new OTAException();
        }
        // Successfully grab the data from the remote server
        otaCacheControl.writeObjectCache(postProcessingPath, data.getBytes(StandardCharsets.UTF_8), manifestTimestamp);
        otaCacheControl.writeManifestTimestamp(getManifest().getTimestamp());
        return data;

//        String pathHash = DigestUtils.sha1Hex(postProcessingPath);
//        // Reading metadata
//        File metadataFile = new File(Util.getCacheFolder(), "i18n.metadata");
//        YamlConfiguration cacheMetadata = YamlConfiguration.loadConfiguration(metadataFile);
//        // Reading cloud timestamp
//        long localeTimestamp = cacheMetadata.getLong(pathHash + ".timestamp");
//        // Reading locale cache
//        File cachedDataFile = new File(Util.getCacheFolder(), pathHash);
//        String data = null;
//        // Getting local cache
//        if (cachedDataFile.exists()) {
//            Util.debugLog("Reading data from local cache: " + cachedDataFile.getCanonicalPath());
//            data = Util.readToString(cachedDataFile);
//        }
//        // invalidate cache, flush it
//        // force flush required OR local cache not exists OR outdated
//        if (forceFlush || data == null || localeTimestamp != manifest.getTimestamp()) {
//            String url = CROWDIN_OTA_HOST + "content" + fileCrowdinPath.replace("%locale%", crowdinLocale);
//            //Util.debugLog("Reading data from remote server: " + url);
//            plugin.getLogger().info("Downloading translation " + crowdinLocale + " from: " + url);
//            try (Response response = HttpUtil.create().getClient().newCall(new Request.Builder().get().url(url).build()).execute()) {
//                val body = response.body();
//                if (body == null) {
//                    throw new OTAException(response.code(), ""); // Returns empty string (failed to getting content)
//                }
//                data = body.string();
//                if (response.code() != 200) {
//                    throw new OTAException(response.code(), data);
//                }
//                // save to local cache file
//                Files.write(cachedDataFile.toPath(), data.getBytes(StandardCharsets.UTF_8));
//            } catch (IOException e) {
//                plugin.getLogger().log(Level.WARNING, "Failed to download manifest.json, multi-language system may won't work");
//                e.printStackTrace();
//                return "";
//            }
//            // update cache index
//            cacheMetadata.set(pathHash + ".timestamp", manifest.getTimestamp());
//            cacheMetadata.save(metadataFile);
//            return data;
//        }
    }

    @EqualsAndHashCode(callSuper = true)
    @AllArgsConstructor
    @Builder
    @Data
    public static class OTAException extends Exception {
    }

    @AllArgsConstructor
    @Builder
    @Data
    public static class CrowdinGetFileRequest {
        private String fileCrowdinPath;
        private String crowdinLocale;
        private boolean forceFlush;
    }

    public static class OTACacheControl {
        private final File metadataFile = new File(Util.getCacheFolder(), "i18n.metadata");
        private final YamlConfiguration metadata;

        public OTACacheControl() {
            this.metadata = YamlConfiguration.loadConfiguration(this.metadataFile);
        }

        @SneakyThrows
        private void save() {
            this.metadata.save(this.metadataFile);
        }

        private String hash(String str) {
            return DigestUtils.sha1Hex(str);
        }

        public long readManifestTimestamp() {
            return this.metadata.getLong("manifest.timestamp", -1);
        }

        public void writeManifestTimestamp(long timestamp) {
            this.metadata.set("manifest.timestamp", timestamp);
            save();
        }

        public long readCachedObjectTimestamp(String path) {
            String cacheKey = hash(path);
            return this.metadata.getLong("objects." + cacheKey + ".time", -1);
        }

        public synchronized boolean isCachedObjectOutdated(String path, long manifestTimestamp) {
            return readCachedObjectTimestamp(path) != manifestTimestamp;
        }

        public byte[] readObjectCache(String path) throws IOException {
            String cacheKey = hash(path);
            return Files.readAllBytes(new File(Util.getCacheFolder(), cacheKey).toPath());
        }

        public synchronized void writeObjectCache(String path, byte[] data, long manifestTimestamp) throws IOException {
            String cacheKey = hash(path);
            Files.write(new File(Util.getCacheFolder(), cacheKey).toPath(), data);
            this.metadata.set("objects." + cacheKey + ".time", manifestTimestamp);
            save();
        }


    }
}