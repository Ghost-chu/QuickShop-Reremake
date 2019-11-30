package org.maxgamer.quickshop.File;

import com.google.gson.Gson;
import org.bukkit.plugin.Plugin;
import org.cactoos.io.InputStreamOf;
import org.cactoos.io.ReaderOf;
import org.cactoos.iterable.Mapped;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
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
                new ReaderOf(
                    new InputStreamOf(file)
                ),
                Map.class
            )
        );
    }

    @Override
    public void save() {

    }

    private void put(@NotNull Map<String, Object> map) {
        map.forEach((s, o) -> {
            System.out.println(s);
            System.out.println(o.getClass());
            if (o instanceof String || o instanceof List || o instanceof Number) {
                cache.put(s, o);
                return;
            }

            if (o instanceof Map) {
                put(
                    new MapOf<>(
                        new Mapped<>(
                            entry -> new MapEntry<>(
                                s + "." + entry.getKey(),
                                entry.getValue()
                            ),
                            ((Map<String, Object>) o).entrySet()
                        )
                    )
                );
            }
        });
    }

}
