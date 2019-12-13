/*
 * This file is a part of project QuickShop, the name is CachingPool.java
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

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;

import java.util.HashMap;
import java.util.Map;

public class CachingPool {
    static Map<Object, ObjectArraySet<Object>> repoForCaching = new HashMap<>();

    public static boolean contains(@NotNull String indexName, @NotNull Object object){
        ObjectArraySet<Object> list = repoForCaching.get(indexName);
        if(list == null){
            return false;
        }
        return list.contains(object);
    }
    public static boolean add(@NotNull String indexName, @NotNull Object object){
        ObjectArraySet<Object> list = repoForCaching.get(indexName);
        if(list == null){
            list = new ObjectArraySet<>();
            list.add(object);
            repoForCaching.put(indexName,list);
            return true;
        }
        if(list.size() > QuickShop.instance.getConfig().getInt("cachingpool.maxsize")){
            list.clear();
            Util.debugLog("Caching pool is cleared!");
        }
        return list.add(object);
    }
    public static boolean remove(@NotNull String indexName, @NotNull Object object){
        ObjectArraySet<Object> list = repoForCaching.get(indexName);
        if(list == null){
            return false;
        }
        return list.remove(object);
    }
}