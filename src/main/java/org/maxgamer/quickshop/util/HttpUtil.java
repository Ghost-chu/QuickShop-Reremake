package org.maxgamer.quickshop.util;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class HttpUtil {
    private final OkHttpClient client = new OkHttpClient.Builder()
            .cache(new Cache(getCacheFolder(),50L * 1024L * 1024L)).build();

    public static HttpUtil instance(){
        return new HttpUtil();
    }

    private File getCacheFolder(){
        try {
            File file = Files.createTempDirectory("quickshop_okhttp_tmp").toFile();
            file.mkdirs();
            return file;
        } catch (IOException e) {
            File file = new File(Util.getCacheFolder(),"okhttp_tmp");
            file.mkdirs();
            return file;
        }
    }

    public OkHttpClient getClient() {
        return client;
    }
}
