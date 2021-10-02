/*
 * This file is a part of project QuickShop, the name is JsonUtil.java
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

package org.maxgamer.quickshop.util;

import com.google.gson.*;
import me.lucko.helper.datatree.DataTree;
import me.lucko.helper.gson.typeadapters.BukkitSerializableAdapterFactory;
import me.lucko.helper.gson.typeadapters.GsonSerializableAdapterFactory;
import me.lucko.helper.gson.typeadapters.JsonElementTreeSerializer;
import me.lucko.helper.text3.serializer.gson.GsonComponentSerializer;

import javax.annotation.Nonnull;
import java.io.Reader;
import java.util.Objects;


/**
 * Utilities to prevent create Gson object in other place and reuse gson object in runtime
 *
 * @author Ghost_chu and sandtechnology, modified based on Lucko's Helper project
 */
public final class JsonUtil {
    private static final Gson STANDARD_GSON = GsonComponentSerializer.populate(new GsonBuilder())
            .registerTypeHierarchyAdapter(DataTree.class, JsonElementTreeSerializer.INSTANCE)
            .registerTypeAdapterFactory(GsonSerializableAdapterFactory.INSTANCE)
            .registerTypeAdapterFactory(BukkitSerializableAdapterFactory.INSTANCE)
            .enableComplexMapKeySerialization()
            .setExclusionStrategies(new HiddenAnnotationExclusionStrategy())
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    private static final Gson PRETTY_PRINT_GSON = GsonComponentSerializer.populate(new GsonBuilder())
            .registerTypeHierarchyAdapter(DataTree.class, JsonElementTreeSerializer.INSTANCE)
            .registerTypeAdapterFactory(GsonSerializableAdapterFactory.INSTANCE)
            .registerTypeAdapterFactory(BukkitSerializableAdapterFactory.INSTANCE)
            .enableComplexMapKeySerialization()
            .setExclusionStrategies(new HiddenAnnotationExclusionStrategy())
            .serializeNulls()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    private static final JsonParser PARSER = new JsonParser();

    @Nonnull
    public static Gson standard() {
        return STANDARD_GSON;
    }

    public static Gson getGson(){
        return STANDARD_GSON;
    }

    @Nonnull
    public static Gson prettyPrinting() {
        return PRETTY_PRINT_GSON;
    }

    @Nonnull
    public static JsonParser parser() {
        return PARSER;
    }

    @Nonnull
    public static JsonObject readObject(@Nonnull Reader reader) {
        return PARSER.parse(reader).getAsJsonObject();
    }

    @Nonnull
    public static JsonObject readObject(@Nonnull String s) {
        return PARSER.parse(s).getAsJsonObject();
    }

    public static void writeObject(@Nonnull Appendable writer, @Nonnull JsonObject object) {
        standard().toJson(object, writer);
    }

    public static void writeObjectPretty(@Nonnull Appendable writer, @Nonnull JsonObject object) {
        prettyPrinting().toJson(object, writer);
    }

    public static void writeElement(@Nonnull Appendable writer, @Nonnull JsonElement element) {
        standard().toJson(element, writer);
    }

    public static void writeElementPretty(@Nonnull Appendable writer, @Nonnull JsonElement element) {
        prettyPrinting().toJson(element, writer);
    }

    @Nonnull
    public static String toString(@Nonnull JsonElement element) {
        return Objects.requireNonNull(standard().toJson(element));
    }

    @Nonnull
    public static String toStringPretty(@Nonnull JsonElement element) {
        return Objects.requireNonNull(prettyPrinting().toJson(element));
    }


    @Nonnull
    @Deprecated
    public static Gson get() {
        return standard();
    }

    @Nonnull
    @Deprecated
    public static Gson getPrettyPrinting() {
        return prettyPrinting();
    }

    public @interface Hidden{

    }
    public static class HiddenAnnotationExclusionStrategy implements ExclusionStrategy
    {
        public boolean shouldSkipClass(Class<?> clazz) {
            return clazz.getDeclaredAnnotation(Hidden.class) != null;
        }

        public boolean shouldSkipField(FieldAttributes f) {
            return f.getAnnotation(Hidden.class) != null;
        }
    }

}
