package org.maxgamer.quickshop.File;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import org.bukkit.plugin.Plugin;
import org.cactoos.io.InputStreamOf;
import org.cactoos.io.OutputTo;
import org.cactoos.io.ReaderOf;
import org.cactoos.io.WriterTo;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public final class JSONFile extends FileEnvelope {

    private final Gson gson = new Gson();

    private JsonObject jsonObject = new JsonObject();

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
        jsonObject = gson.fromJson(
            new ReaderOf(
                new InputStreamOf(file)
            ),
            JsonObject.class
        );
    }

    @Override
    public void save() {
        if (jsonObject.size() == 0) {
            reload();
        }

        try(final Writer writer = new WriterTo(
            new OutputTo(file),
            StandardCharsets.UTF_8)
        ) {
            writer.write(toString());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @NotNull
    public String toString() {
        try {
            final Writer stringWriter = new StringWriter();
            final JsonWriter jsonWriter = new JsonWriter(stringWriter);

            jsonWriter.setLenient(true);
            jsonWriter.setIndent("  ");
            Streams.write(jsonObject, jsonWriter);

            return stringWriter.toString();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

}
