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