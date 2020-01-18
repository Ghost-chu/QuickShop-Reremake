package org.maxgamer.quickshop.NonQuickShopStuffs.com.dumbtruckman.JsonConfiguration;

import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public final class JSONConfiguration extends FileConfiguration {

    protected static final String BLANK_CONFIG = "{}\n";

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

    @NotNull
    @Override
    public String saveToString() {
        final GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping();

        //if (!options().prettyPrint()) {
        gsonBuilder.setPrettyPrinting();
        //}
        final Gson gson = gsonBuilder.create();
        final Object value = SerializationHelper.serialize(getValues(false));
        final String dump = StringEscapeUtils.unescapeJava(gson.toJson(value));

        if (dump.equals(BLANK_CONFIG)) {
            return "";
        }

        return dump;
    }

    @Override
    public void loadFromString(@NotNull String contents) throws InvalidConfigurationException {
        if (contents.isEmpty()) {
            return;
        }

        final Map<?, ?> input;

        try {
            final Gson gson = new GsonBuilder()
                    .registerTypeAdapter(new TypeToken<Map<String, Object>>() {
                            }.getType(),
                            new MapDeserializerDoubleAsIntFix())
                    .create();
            input = gson.fromJson(contents, new TypeToken<Map<String, Object>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            throw new InvalidConfigurationException("Invalid JSON detected.", e);
        } catch (ClassCastException e) {
            throw new InvalidConfigurationException("Top level is not a Map.", e);
        }

        if (input != null) {
            convertMapsToSections(input, this);
        } else {
            throw new InvalidConfigurationException("An unknown error occurred while attempting to parse the json.");
        }
    }

    @NotNull
    @Override
    protected String buildHeader() {
        return "";
    }

    protected void convertMapsToSections(@NotNull Map<?, ?> input, @NotNull ConfigurationSection section) {
        final Object result = SerializationHelper.deserialize(input);

        if (result instanceof Map) {
            input = (Map<?, ?>) result;

            for (Map.Entry<?, ?> entry : input.entrySet()) {
                final String key = entry.getKey().toString();
                final Object value = entry.getValue();

                if (value instanceof Map) {
                    convertMapsToSections((Map<?, ?>) value, section.createSection(key));
                } else {
                    section.set(key, value);
                }
            }
        } else {
            section.set("", result);
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

    public static class MapDeserializerDoubleAsIntFix implements JsonDeserializer<Map<String, Object>> {

        @Override
        @SuppressWarnings("unchecked")
        public Map<String, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return (Map<String, Object>) read(json);
        }

        public Object read(JsonElement in) {

            if (in.isJsonArray()) {
                List<Object> list = new ArrayList<Object>();
                JsonArray arr = in.getAsJsonArray();
                for (JsonElement anArr : arr) {
                    list.add(read(anArr));
                }
                return list;
            } else if (in.isJsonObject()) {
                Map<String, Object> map = new LinkedTreeMap<String, Object>();
                JsonObject obj = in.getAsJsonObject();
                Set<Map.Entry<String, JsonElement>> entitySet = obj.entrySet();
                for (Map.Entry<String, JsonElement> entry : entitySet) {
                    map.put(entry.getKey(), read(entry.getValue()));
                }
                return map;
            } else if (in.isJsonPrimitive()) {
                JsonPrimitive prim = in.getAsJsonPrimitive();
                if (prim.isBoolean()) {
                    return prim.getAsBoolean();
                } else if (prim.isString()) {
                    return prim.getAsString();
                } else if (prim.isNumber()) {
                    Number num = prim.getAsNumber();
                    // here you can handle double int/long values
                    // and return any type you want
                    // this solution will transform 3.0 float to long values
                    if (Math.ceil(num.doubleValue()) == num.longValue())
                        return num.longValue();
                    else {
                        return num.doubleValue();
                    }
                }
            }
            return null;
        }
    }
}
