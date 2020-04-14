package org.maxgamer.quickshop.util;

import com.google.gson.Gson;

public class JsonUtil {

    private static final Gson gson = new Gson();

    public static Gson getGson() {
        return gson;
    }
}
