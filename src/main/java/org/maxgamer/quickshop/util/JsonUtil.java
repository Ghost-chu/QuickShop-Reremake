package org.maxgamer.quickshop.util;

import com.google.gson.Gson;

public class JsonUtil {

    private static final Gson gson = new Gson();

    private JsonUtil() {
    }

    public static Gson getGson() {
        return gson;
    }
}
