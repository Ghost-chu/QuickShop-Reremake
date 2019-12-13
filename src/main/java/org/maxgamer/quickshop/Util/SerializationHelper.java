/*
 * This file is a part of project QuickShop, the name is SerializationHelper.java
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

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.File.BukkitFileAPI.SerializableSet;
import org.yaml.snakeyaml.error.YAMLException;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jeremy Wood
 * @version 6/14/2017
 */
public class SerializationHelper {

    private static final Logger LOG = Logger.getLogger(SerializationHelper.class.getName());

    public static Object serialize(@NotNull Object value) {
        if (value instanceof Object[]) {
            value = new ArrayList<>(Arrays.asList((Object[]) value));
        }
        if (value instanceof Set && !(value instanceof SerializableSet)) {
            value = new SerializableSet((Set) value);
        }
        if (value instanceof ConfigurationSection) {
            return buildMap(((ConfigurationSection) value).getValues(false));
        } else if (value instanceof Map) {
            return buildMap((Map) value);
        } else if (value instanceof List) {
            return buildList((List) value);
        } else if (value instanceof ConfigurationSerializable) {
            ConfigurationSerializable serializable = (ConfigurationSerializable) value;
            Map<String, Object> values = new LinkedHashMap<>();
            values.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(serializable.getClass()));
            values.putAll(serializable.serialize());
            return buildMap(values);
        } else {
            return value;
        }
    }

    /**
     * Takes a Map and parses through the values, to ensure that, before saving, all objects are as appropriate as
     * possible for storage in most data formats.
     *
     * Specifically it does the following:
     *   for Map: calls this method recursively on the Map before putting it in the returned Map.
     *   for List: calls {@link #buildList(java.util.Collection)} which functions similar to this method.
     *   for ConfigurationSection: gets the values as a map and calls this method recursively on the Map before putting
     *       it in the returned Map.
     *   for ConfigurationSerializable: add the {@link ConfigurationSerialization#SERIALIZED_TYPE_KEY} to a new Map
     *       along with the Map given by {@link org.bukkit.configuration.serialization.ConfigurationSerializable#serialize()}
     *       and calls this method recursively on the new Map before putting it in the returned Map.
     *   for Everything else: stores it as is in the returned Map.
     */
    @NotNull
    private static Map<String, Object> buildMap(@NotNull final Map<?, ?> map) {
        final Map<String, Object> result = new LinkedHashMap<String, Object>(map.size());
        try {
            for (final Map.Entry<?, ?> entry : map.entrySet()) {
                result.put(entry.getKey().toString(), serialize(entry.getValue()));
            }
        } catch (final Exception e) {
            LOG.log(Level.WARNING, "Error while building configuration map.", e);
        }
        return result;
    }

    /**
     * Takes a Collection and parses through the values, to ensure that, before saving, all objects are as appropriate
     * as possible for storage in most data formats.
     *
     * Specifically it does the following:
     *   for Map: calls {@link #buildMap(java.util.Map)} on the Map before adding to the returned list.
     *   for List: calls this method recursively on the List.
     *   for ConfigurationSection: gets the values as a map and calls {@link #buildMap(java.util.Map)} on the Map
     *       before adding to the returned list.
     *   for ConfigurationSerializable: add the {@link ConfigurationSerialization#SERIALIZED_TYPE_KEY} to a new Map
     *       along with the Map given by {@link org.bukkit.configuration.serialization.ConfigurationSerializable#serialize()}
     *       and calls {@link #buildMap(java.util.Map)} on the new Map before adding to the returned list.
     *   for Everything else: stores it as is in the returned List.
     */
    private static List<Object> buildList(@NotNull final Collection<?> collection) {
        final List<Object> result = new ArrayList<Object>(collection.size());
        try {
            for (Object o : collection) {
                result.add(serialize(o));
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error while building configuration list.", e);
        }
        return result;
    }

    /**
     * Parses through the input map to deal with serialized objects a la {@link ConfigurationSerializable}.
     *
     * Called recursively first on Maps and Lists before passing the parsed input over to
     * {@link ConfigurationSerialization#deserializeObject(java.util.Map)}.  Basically this means it will deserialize
     * the most nested objects FIRST and the top level object LAST.
     */
    public static Object deserialize(@NotNull final Map<?, ?> input) {
        final Map<String, Object> output = new LinkedHashMap<String, Object>(input.size());
        for (final Map.Entry<?, ?> e : input.entrySet()) {
            if (e.getValue() instanceof Map) {
                output.put(e.getKey().toString(), deserialize((Map<?, ?>) e.getValue()));
            }  else if (e.getValue() instanceof List) {
                output.put(e.getKey().toString(), deserialize((List<?>) e.getValue()));
            } else {
                output.put(e.getKey().toString(), e.getValue());
            }
        }
        if (output.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
            try {
                return ConfigurationSerialization.deserializeObject(output);
            } catch (IllegalArgumentException ex) {
                throw new YAMLException("Could not deserialize object", ex);
            }
        }
        return output;
    }

    /**
     * Parses through the input list to deal with serialized objects a la {@link ConfigurationSerializable}.
     *
     * Functions similarly to {@link #deserialize(java.util.Map)} but only for detecting lists within
     * lists and maps within lists.
     */
    private static Object deserialize(@NotNull final List<?> input) {
        final List<Object> output = new ArrayList<Object>(input.size());
        for (final Object o : input) {
            if (o instanceof Map) {
                output.add(deserialize((Map<?, ?>) o));
            } else if (o instanceof List) {
                output.add(deserialize((List<?>) o));
            } else {
                output.add(o);
            }
        }
        return output;
    }
}
