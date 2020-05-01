package org.maxgamer.quickshop.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtil {

    private static final Gson gson = new Gson();
    private static final Gson outputGson = new GsonBuilder().setPrettyPrinting().create();
    private static final Gson humanReadableGson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private JsonUtil() {
    }

    public static Gson getGson() {
        return gson;
    }

    public static Gson getOutputGson() { return outputGson;}

    public static Gson getHumanReadableGson() { return humanReadableGson; }


}
