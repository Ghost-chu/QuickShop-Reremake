package org.maxgamer.quickshop.File;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.NonQuickShopStuffs.com.dumbtruckman.JsonConfiguration.JSONConfiguration;

public final class JSONFile extends FileEnvelope {

  public JSONFile(
      @NotNull final Plugin plugin,
      @NotNull final File file,
      @NotNull final String resourcePath,
      boolean loadDefault) {
    super(
        plugin,
        file,
        resourcePath.endsWith(".json") ? resourcePath : resourcePath + ".json",
        loadDefault);
  }

  public JSONFile(
      @NotNull final Plugin plugin, @NotNull final File file, @NotNull final String resourcePath) {
    super(
        plugin, file, resourcePath.endsWith(".json") ? resourcePath : resourcePath + ".json", true);
  }

  public JSONFile(
      @NotNull final Plugin plugin,
      @NotNull final String resourcePath,
      @NotNull final String fileName) {
    this(
        plugin,
        new File(
            plugin.getDataFolder().getAbsolutePath()
                + (resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath),
            fileName.endsWith(".json") ? fileName : fileName + ".json"),
        resourcePath.isEmpty()
            ? fileName
            : resourcePath.endsWith("/") ? resourcePath + fileName : resourcePath + "/" + fileName);
  }

  public JSONFile(@NotNull final Plugin plugin, @NotNull final String fileName) {
    this(plugin, "", fileName);
  }

  @Override
  public void reload() {
    fileConfiguration = JSONConfiguration.loadConfiguration(file);
    if (loadDefault) {
      fileConfiguration.setDefaults(
          JSONConfiguration.loadConfiguration(
              new InputStreamReader(getInputStream(), StandardCharsets.UTF_8)));
    }
  }
}
