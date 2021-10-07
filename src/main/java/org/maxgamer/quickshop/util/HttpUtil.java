package org.maxgamer.quickshop.util;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class HttpUtil {
    private final OkHttpClient client = new OkHttpClient.Builder()
            .cache(new Cache(getCacheFolder(),50L * 1024L * 1024L)).build();

    public static HttpUtil create(){
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

    public static Response makeGet(@NotNull String url) throws IOException {
        return HttpUtil.create().getClient().newCall(new Request.Builder().get().url(url).build()).execute();
    }

    public static Response makePost(@NotNull String url, @NotNull RequestBody body) throws IOException {
        return HttpUtil.create().getClient().newCall(new Request.Builder().post(body).url(url).build()).execute();
    }
}
