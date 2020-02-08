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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.MojangAPI.AssetJson;
import org.maxgamer.quickshop.Util.MojangAPI.MojangAPI;

public class GameLanguage extends Thread {
  private @Nullable JsonObject lang;
  private GameLanguageLoadThread loadThread;

  @SneakyThrows
  public GameLanguage(@NotNull String languageCode) {
    loadThread = new GameLanguageLoadThread();
    loadThread.setLanguageCode(languageCode);
    loadThread.setMainThreadWaiting(true); // Told thread we're waiting him
    loadThread.start();
    int count = 0;
    while (count < 20) {
      if (loadThread.isAlive()) {
        count++;
        Thread.sleep(1000);
        if (count >= 20) {
          Util.debugLog(
              "No longer waiting file downloading because it now timed out, now downloading in background.");
          QuickShop.instance
              .getLogger()
              .info(
                  "No longer waiting file downloading because it now timed out, now downloading in background, please reset itemi18n.yml, potioni18n.yml and enchi18n.yml after download completed.");
        }
      } else {
        break;
      }
    }
    this.lang = loadThread.getLang(); // Get the Lang whatever thread running or died.
    loadThread.setMainThreadWaiting(
        false); // Told thread it now move to background, thread should told user reset files.
  }

  /**
   * Get item and block translations, if not found, it will both call getBlock()
   *
   * @param material The material
   * @return The translations for material
   */
  @NotNull
  public String getItem(@NotNull Material material) {
    String name = Util.prettifyText(material.name());
    if (lang == null) {
      return name;
    }
    String jsonName;
    try {
      JsonElement element = lang.get("item.minecraft." + material.name().toLowerCase());
      if (element == null) {
        return getBlock(material);
      } else {
        return element.getAsString();
      }
    } catch (NullPointerException npe) {
      return name;
    }
  }

  /**
   * Get block only translations, if not found, it WON'T call getItem()
   *
   * @param material The material
   * @return The translations for material
   */
  @NotNull
  public String getBlock(@NotNull Material material) {
    String name = Util.prettifyText(material.name());
    if (lang == null) {
      return name;
    }
    try {
      return lang.get("block.minecraft." + material.name().toLowerCase()).getAsString();
    } catch (NullPointerException e) {
      return name;
    }
  }

  /**
   * Get entity translations.
   *
   * @param entity The entity name
   * @return The translations for entity
   */
  @NotNull
  public String getEntity(@NotNull Entity entity) {
    String name = Util.prettifyText(entity.getType().name());
    if (lang == null) {
      return name;
    }
    try {
      return lang.get("entity.minecraft." + entity.getType().name().toLowerCase()).getAsString();
    } catch (NullPointerException e) {
      return name;
    }
  }

  /**
   * Get potion/effect translations.
   *
   * @param effect The potion/effect name
   * @return The translations for effect/potions
   */
  @NotNull
  public String getPotion(@NotNull PotionEffectType effect) {
    String name = Util.prettifyText(effect.getName());
    if (lang == null) {
      return name;
    }
    try {
      return lang.get("effect.minecraft." + effect.getName().toLowerCase()).getAsString();
    } catch (NullPointerException e) {
      return name;
    }
  }

  /**
   * Get enchantment translations.
   *
   * @param enchantmentName The enchantment name
   * @return The translations for enchantment
   */
  @NotNull
  public String getEnchantment(@NotNull String enchantmentName) {
    String name = Util.prettifyText(enchantmentName);
    if (lang == null) {
      return name;
    }
    try {
      return lang.get("enchantment.minecraft." + enchantmentName.toLowerCase()).getAsString();
    } catch (NullPointerException e) {
      return name;
    }
  }

  /**
   * Get custom translations.
   *
   * @param node The target node path
   * @return The translations for you custom node path
   */
  @Nullable
  public String getCustom(@NotNull String node) {
    if (lang == null) {
      return null;
    }
    try {
      return lang.get(node).getAsString();
    } catch (NullPointerException e) {
      return null;
    }
  }
}

@Getter
@Setter
class GameLanguageLoadThread extends Thread {
  private JsonObject lang;
  private String languageCode;
  private boolean mainThreadWaiting;

  public void run() {
    try {
      File cacheFile = new File(Util.getCacheFolder(), "lang.cache"); // Load cache file
      if (!cacheFile.exists()) {
        cacheFile.createNewFile();
      }
      YamlConfiguration yamlConfiguration = new YamlConfiguration();
      yamlConfiguration.load(new File(Util.getCacheFolder(), "lang.cache"));
      boolean needUpdateCache = false;
      /* The cache data, if it all matches, we doesn't need connect to internet to download files again. */
      String cachingServerVersion = yamlConfiguration.getString("ver");
      String cachingLanguageHash = yamlConfiguration.getString("hash");
      String cachingLanguageName = yamlConfiguration.getString("lang");
      /* If language name is default, use computer language */
      if ("default".equals(languageCode)) {
        Locale locale = Locale.getDefault();
        languageCode = locale.getLanguage() + "_" + locale.getCountry();
      }
      if (!languageCode.equals(cachingLanguageName)) {
        cachingLanguageName = languageCode;
        needUpdateCache = true;
      }
      String languageCode1 = languageCode.toLowerCase();
      String serverVersion = ReflectFactory.getServerVersion();
      if (!serverVersion.equals(cachingServerVersion)) {
        cachingServerVersion = serverVersion;
        needUpdateCache = true;
      }
      if (cachingLanguageHash == null || cachingLanguageHash.isEmpty()) {
        needUpdateCache = true;
      }
      if (needUpdateCache) {
        MojangAPI mojangAPI = new MojangAPI();
        String assetJson = mojangAPI.getAssetIndexJson(cachingServerVersion);
        if (assetJson != null) {
          AssetJson versionJson = new AssetJson(assetJson);
          String hash = versionJson.getLanguageHash(languageCode1);
          if (hash != null) {
            cachingLanguageHash = hash;
            String langJson = mojangAPI.downloadTextFileFromMojang(hash);
            if (langJson != null) {
              new Copied(new File(Util.getCacheFolder(), hash))
                  .accept(new ByteArrayInputStream(langJson.getBytes(StandardCharsets.UTF_8)));
            } else {
              Util.debugLog("Cannot download file.");
              QuickShop.instance
                  .getLogger()
                  .warning(
                      "Cannot download require files, some items/blocks/potions/enchs language will use default English name.");
            }
          } else {
            Util.debugLog("Cannot get file hash for language " + languageCode1);
            QuickShop.instance
                .getLogger()
                .warning(
                    "Cannot download require files, some items/blocks/potions/enchs language will use default English name.");
          }
        } else {
          Util.debugLog("Cannot get version json.");
          QuickShop.instance
              .getLogger()
              .warning(
                  "Cannot download require files, some items/blocks/potions/enchs language will use default English name.");
        }
      }
      yamlConfiguration.set("ver", cachingServerVersion);
      yamlConfiguration.set("hash", cachingLanguageHash);
      yamlConfiguration.set("lang", cachingLanguageName);
      yamlConfiguration.save(cacheFile);
      String json = null;
      if (cachingLanguageHash != null) {
        json = Util.readToString(new File(Util.getCacheFolder(), cachingLanguageHash));
      } else {
        Util.debugLog("Caching LanguageHash is null");
      }
      if (json != null && !json.isEmpty()) {
        lang = new JsonParser().parse(json).getAsJsonObject();
      } else {
        Util.debugLog("json is null");
      }
    } catch (Exception e) {
      QuickShop.instance.getSentryErrorReporter().ignoreThrow();
      e.printStackTrace();
    }
    if (!this.mainThreadWaiting) {
      QuickShop.instance
          .getLogger()
          .info(
              "Download completed, please execute /qs reset lang to generate localized language files.");
    }
  }
}
