package org.maxgamer.quickshop.File.BukkitFileAPI;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Map;
import java.util.logging.Level;

public final class JSONConfiguration extends FileConfiguration {

    private final Gson gson = new Gson();

    private final JsonObject jsonObject = new JsonObject();

    @NotNull
    @Override
    public String saveToString() {
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

    @Override
    public void loadFromString(@NotNull String contents) throws InvalidConfigurationException {
        final Map<?, ?> input;

        try {
            input = (Map<?, ?>) gson.fromJson(contents, Map.class);
        } catch (JsonSyntaxException e) {
            throw new InvalidConfigurationException(e);
        } catch (ClassCastException e) {
            throw new InvalidConfigurationException("Top level is not a Map.");
        }

        if (input != null) {
            convertMapsToSections(input, this);
        }
    }

    @NotNull
    @Override
    protected String buildHeader() {
        return "";
    }

    protected void convertMapsToSections(@NotNull Map<?, ?> input, @NotNull ConfigurationSection section) {
        for (Map.Entry<?, ?> entry : input.entrySet()) {
            final String key = entry.getKey().toString();
            final Object value = entry.getValue();

            if (value instanceof Map) {
                convertMapsToSections((Map<?, ?>) value, section.createSection(key));
            } else {
                section.set(key, value);
            }
        }
    }

    @NotNull
    @Override
    public JSONConfigurationOptions options() {
        if (options == null) {
            options = new JSONConfigurationOptions(this);
        }

        return (JSONConfigurationOptions) options;
    }

    @NotNull
    public static JSONConfiguration loadConfiguration(@NotNull File file) {
        final JSONConfiguration config = new JSONConfiguration();

        try {
            config.load(file);
        } catch (FileNotFoundException ignored) {
            // ignored...
        } catch (IOException | InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
        }

        return config;
    }

    @NotNull
    public static JSONConfiguration loadConfiguration(@NotNull Reader reader) {
        final JSONConfiguration config = new JSONConfiguration();

        try {
            config.load(reader);
        } catch (IOException | InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load configuration from stream", ex);
        }

        return config;
    }

}
