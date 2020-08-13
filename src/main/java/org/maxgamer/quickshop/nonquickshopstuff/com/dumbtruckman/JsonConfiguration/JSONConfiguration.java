/*
 * This file is a part of project QuickShop, the name is JSONConfiguration.java
 *  Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.nonquickshopstuff.com.dumbtruckman.JsonConfiguration;

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

public class JSONConfiguration extends FileConfiguration {

    protected static final String BLANK_CONFIG = "{}\n";
    private static final Gson outputGson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final Gson inputGson = new GsonBuilder().registerTypeAdapter(new TypeToken<Map<String, Object>>() {
    }.getType(), new MapDeserializerDoubleAsIntFix()).create();

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

        // if (!options().prettyPrint()) {
        // }
        final Object value = SerializationHelper.serialize(getValues(false));
        final String dump = StringEscapeUtils.unescapeJava(outputGson.toJson(value));

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
            input = inputGson.fromJson(contents, new TypeToken<Map<String, Object>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            throw new InvalidConfigurationException("Invalid JSON detected.", e);
        } catch (ClassCastException e) {
            throw new InvalidConfigurationException("Top level is not a Map.", e);
        }

        if (input != null) {
            convertMapsToSections(input, this);
        } else {
            throw new InvalidConfigurationException(
                    "An unknown error occurred while attempting to parse the json.");
        }
    }

    @NotNull
    @Override
    protected String buildHeader() {
        return "";
    }

    @NotNull
    @Override
    public JSONConfigurationOptions options() {
        if (options == null) {
            options = new JSONConfigurationOptions(this);
        }

        return (JSONConfigurationOptions) options;
    }

    protected void convertMapsToSections(
            @NotNull Map<?, ?> input, @NotNull ConfigurationSection section) {
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

    public static class MapDeserializerDoubleAsIntFix
            implements JsonDeserializer<Map<String, Object>> {

        @Override
        @SuppressWarnings("unchecked")
        public Map<String, Object> deserialize(
                JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return (Map<String, Object>) read(json);
        }

        public Object read(JsonElement in) {

            if (in.isJsonArray()) {
                List<Object> list = new ArrayList<>();
                JsonArray arr = in.getAsJsonArray();
                for (JsonElement anArr : arr) {
                    list.add(read(anArr));
                }
                return list;
            } else if (in.isJsonObject()) {
                Map<String, Object> map = new LinkedTreeMap<>();
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
                    if (Math.ceil(num.doubleValue()) == num.longValue()) {
                        return num.longValue();
                    } else {
                        return num.doubleValue();
                    }
                }
            }
            return null;
        }

    }

}
