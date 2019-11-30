package org.maxgamer.quickshop.File;

import com.fasterxml.jackson.databind.annotation.JsonTypeResolver;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public final class JSONFile extends FileEnvelope {

    private final Map<String, Object> cache = new HashMap<>();

    public JSONFile(@NotNull Plugin plugin, @NotNull File file, @NotNull String resourcePath) {
        super(
            plugin,
            file,
            resourcePath.endsWith(".json")
                ? resourcePath
                : resourcePath + ".json"
        );
    }

    public JSONFile(@NotNull final Plugin plugin, @NotNull final String resourcePath, @NotNull final String fileName) {
        this(
            plugin,
            new File(
                plugin.getDataFolder().getAbsolutePath() + (resourcePath.startsWith("/")
                    ? resourcePath
                    : "/" + resourcePath),
                fileName.endsWith(".json") ? fileName : fileName + ".json"
            ),
            resourcePath.isEmpty()
                ? fileName
                : resourcePath.endsWith("/")
                ? resourcePath + fileName
                : resourcePath + "/" + fileName
        );
    }

    public JSONFile(@NotNull final Plugin plugin, @NotNull final String fileName) {
        this(plugin, "", fileName);
    }

    @Override
    public void reload() {
        put(
            new Gson().<Map<String, Object>>fromJson(
                new InputStreamReader(getInputStream()),
                Map.class
            )
        );
    }

    @Override
    public void save() {

    }

    private void put(@NotNull Map<String, Object> map) {
        map.forEach((s, o) -> {
            if (o instanceof String) {
                cache.put(s, o);
                return;
            }

            if (o instanceof Map) {
                put(o);
            }

        });
    }

}
