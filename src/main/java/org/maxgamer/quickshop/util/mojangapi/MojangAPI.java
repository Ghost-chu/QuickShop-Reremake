/*
 * This file is a part of project QuickShop, the name is MojangAPI.java
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

package org.maxgamer.quickshop.util.mojangapi;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.util.HttpCacheLoader;
import org.maxgamer.quickshop.util.JsonUtil;

public class MojangAPI {

  public MojangAPI() {}

  @NotNull
  public AssetsAPI getAssetsAPI(@NotNull String serverVersion) {
    return new AssetsAPI(serverVersion);
  }

  @NotNull
  public GameInfoAPI getGameInfoAPI(@NotNull String gameVersionJson) {
    return new GameInfoAPI(gameVersionJson);
  }

  @NotNull
  public MetaAPI getMetaAPI(@NotNull String serverVersion) {
    return new MetaAPI(serverVersion);
  }

  public ResourcesAPI getResourcesAPI() { return new ResourcesAPI(); }

  @Data
  @AllArgsConstructor
  public static class AssetsFileData {
    private String content;
    private String sha1;
    private String id;
  }

  @Data
  @NoArgsConstructor
  public static class ResourcesAPI {
    private final LoadingCache<URL, Optional<String>> request =
        CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new HttpCacheLoader());

    public Optional<String> get(@NotNull String hash) {
      try {
        return request.get(new URL("https://resources.download.minecraft.net/" +
                                   hash.substring(0, 2) + "/" + hash));
      } catch (ExecutionException | MalformedURLException e) {
        return Optional.empty();
      }
    }
  }

  public static class AssetsAPI {
    private final LoadingCache<URL, Optional<String>> request =
        CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new HttpCacheLoader());
    private final MetaAPI metaAPI;

    AssetsAPI(@NotNull String version) { this.metaAPI = new MetaAPI(version); }

    public boolean isAvailable() { return this.metaAPI.get().isPresent(); }

    /**
     * Gets the GameAsset file content
     *
     * @return The file content
     */
    public Optional<AssetsFileData> getGameAssetsFile() {
      Optional<GameInfoAPI.DataBean> bean = getAssetsJson();
      if (!bean.isPresent())
        return Optional.empty();
      GameInfoAPI.DataBean.AssetIndexBean assetIndexBean =
          bean.get().getAssetIndex();
      if (assetIndexBean == null || assetIndexBean.getUrl() == null ||
          assetIndexBean.getId() == null)
        return Optional.empty();

      try {
        Optional<String> fileContent =
            request.get(new URL(assetIndexBean.getUrl()));
        return fileContent.map(s
                               -> new AssetsFileData(s,
                                                     assetIndexBean.getSha1(),
                                                     assetIndexBean.getId()));
      } catch (ExecutionException | MalformedURLException e) {
        return Optional.empty();
      }
    }

    private Optional<GameInfoAPI.DataBean> getAssetsJson() {
      if (!isAvailable())
        return Optional.empty();
      Optional<String> content = this.metaAPI.get();
      if (!content.isPresent())
        return Optional.empty();
      GameInfoAPI gameInfoAPI = new GameInfoAPI(content.get());
      return Optional.of(gameInfoAPI.get());
    }
  }

  @Data
  public static class GameInfoAPI {
    private final String json;
    private final Gson gson = JsonUtil.getGson();

    public GameInfoAPI(@NotNull String json) { this.json = json; }

    @NotNull
    public DataBean get() {
      return gson.fromJson(json, DataBean.class);
    }

    @Data
    static class DataBean {
      @Nullable private AssetIndexBean assetIndex;
      @Nullable private String assets;

      @Data
      public static class AssetIndexBean {
        /**
         * id : 1.16
         * sha1 : 3a5d110a6ab102c7083bae4296d2de4b8fcf92eb
         * size : 295421
         * totalSize : 330604420
         * url :
         * https://launchermeta.mojang.com/v1/packages/3a5d110a6ab102c7083bae4296d2de4b8fcf92eb/1.16.json
         */
        @Nullable private String id;
        @Nullable private String sha1;
        @Nullable private String url;
      }
    }
  }

  public static class MetaAPI {
    // Cache with URL and Content(String)
    private final LoadingCache<URL, Optional<String>> request =
        CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new HttpCacheLoader());
    private final URL metaEndpoint;
    private final String version;

    @SneakyThrows
    public MetaAPI(@NotNull String version) {
      this.version = version;
      this.metaEndpoint = new URL(
          "https://launchermeta.mojang.com/mc/game/version_manifest.json");
    }

    /**
     * Gets the available status and the Game Version Meta Json File content.
     *
     * @return The meta data
     */
    @SneakyThrows
    public Optional<String> get() {
      Optional<String> result = request.get(this.metaEndpoint);
      if (!result.isPresent())
        return Optional.empty();
      try {
        JsonElement index = new JsonParser().parse(result.get());
        if (!index.isJsonObject())
          return Optional.empty();
        JsonElement availableVersions = index.getAsJsonObject().get("versions");
        if (!availableVersions.isJsonArray())
          return Optional.empty();
        for (JsonElement gameVersionData : availableVersions.getAsJsonArray()) {
          if (gameVersionData.isJsonObject()) {
            JsonElement gameId = gameVersionData.getAsJsonObject().get("id");
            JsonElement gameIndexUrl =
                gameVersionData.getAsJsonObject().get("url");
            if (Objects.equals(gameId.getAsString(), version))
              return request.get(new URL(gameIndexUrl.getAsString()));
          }
        }
        return Optional.empty();
      } catch (RuntimeException exception) {
        return Optional.empty();
      }
    }
  }
}
