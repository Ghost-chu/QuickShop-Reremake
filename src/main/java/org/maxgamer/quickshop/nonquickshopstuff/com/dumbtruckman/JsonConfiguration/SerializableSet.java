/*
 * This file is a part of project QuickShop, the name is SerializableSet.java
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

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SerializableAs("set")
public class SerializableSet implements Set, ConfigurationSerializable {

    @NotNull
    private final Set backingSet;

    public SerializableSet(@NotNull Set backingSet) {
        this.backingSet = backingSet;
    }

    @SuppressWarnings("unchecked")
    public SerializableSet(@NotNull Map<String, Object> serializedForm) {
        Object o = serializedForm.get("contents");
        if (o instanceof List) {
            backingSet = new HashSet((List) o);
        } else {
            backingSet = Collections.emptySet();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> serializedForm = new HashMap<>(backingSet.size());
        List<Object> contents = new ArrayList(backingSet);
        serializedForm.put("contents", contents);
        return serializedForm;
    }

    @Override
    public int size() {
        return backingSet.size();
    }

    @Override
    public boolean isEmpty() {
        return backingSet.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return backingSet.contains(o);
    }

    @NotNull
    @Override
    public Iterator iterator() {
        return backingSet.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return backingSet.toArray();
    }

    @NotNull
    @Override
    public Object[] toArray(@NotNull Object[] a) {
        return backingSet.toArray(a);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean add(Object o) {
        return backingSet.add(o);
    }

    @Override
    public boolean remove(Object o) {
        return backingSet.remove(o);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsAll(@NotNull Collection c) {
        return backingSet.containsAll(c);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean addAll(@NotNull Collection c) {
        return backingSet.addAll(c);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean retainAll(@NotNull Collection c) {
        return backingSet.retainAll(c);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean removeAll(@NotNull Collection c) {
        return backingSet.removeAll(c);
    }

    @Override
    public void clear() {
        backingSet.clear();
    }

    @Override
    public Spliterator spliterator() {
        return backingSet.spliterator();
    }

    @Override
    public int hashCode() {
        return backingSet.hashCode();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return backingSet.equals(o);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean removeIf(Predicate filter) {
        return backingSet.removeIf(filter);
    }

    @Override
    public Stream stream() {
        return backingSet.stream();
    }

    @Override
    public Stream parallelStream() {
        return backingSet.parallelStream();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void forEach(Consumer action) {
        backingSet.forEach(action);
    }

}
