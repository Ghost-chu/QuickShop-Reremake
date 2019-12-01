package org.maxgamer.quickshop.Webhook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class WebhookHelper {
    private QuickShop plugin;
    private Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(60000)
            .setConnectTimeout(60000)
            .setConnectionRequestTimeout(60000)
            .build();

    public WebhookHelper(QuickShop plugin) {
        this.plugin = plugin;
    }

    public void noticeOnlyAsync(@NotNull Object obj) {
        new BukkitRunnable() {
            @Override
            public void run() {
                HandledConnection connection;
                try {
                    connection = handleConnection();
                } catch (IOException e) {
                    plugin.getSentryErrorReporter().ignoreThrow();
                    e.printStackTrace();
                    plugin.getLogger().warning("Failed request the webhook, check the stacktrace to fix the problem.");
                    return;
                }
                HttpPost httpPost = connection.getHttpPost();
                CloseableHttpClient httpClient = connection.getClient();
                Map<String, Object> gsonArgs = new HashMap<>();
                gsonArgs.put("type", RequestType.NOTICE_ONLY.name());
                gsonArgs.put("thread", ThreadType.ASYNC.name());
                gsonArgs.put("data", obj.toString());
                StringEntity entity;
                try {
                    entity = new StringEntity(gson.toJson(gsonArgs));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return;
                }
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
                CloseableHttpResponse response;
                try {
                    response = httpClient.execute(httpPost);
                } catch (IOException e) {
                    plugin.getSentryErrorReporter().ignoreThrow();
                    e.printStackTrace();
                    plugin.getLogger().warning("Failed request the webhook server.");
                    return;
                }
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    try {
                        plugin.getLogger().warning("Webhook may not success: HTTP_CODE=" + response.getStatusLine().getStatusCode() + " REASON=" + response.getStatusLine().getReasonPhrase() + " RESPONSE=" + new BufferedReader(new InputStreamReader(response.getEntity().getContent())).lines().collect(Collectors.joining()));
                    } catch (IOException ignored) {
                    }
                }
                try {
                    httpClient.close();
                } catch (IOException ignored) {
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void noticeOnlySync(@NotNull Object obj) {
        HandledConnection connection;
        try {
            connection = handleConnection();
        } catch (IOException e) {
            plugin.getSentryErrorReporter().ignoreThrow();
            e.printStackTrace();
            plugin.getLogger().warning("Failed request the webhook, check the stacktrace to fix the problem.");
            return;
        }
        HttpPost httpPost = connection.getHttpPost();
        CloseableHttpClient httpClient = connection.getClient();
        Map<String, Object> gsonArgs = new HashMap<>();
        gsonArgs.put("type", RequestType.NOTICE_ONLY.name());
        gsonArgs.put("thread", ThreadType.ASYNC.name());
        gsonArgs.put("data", obj.toString());
        StringEntity entity;
        try {
            entity = new StringEntity(gson.toJson(gsonArgs));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        CloseableHttpResponse response;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            plugin.getSentryErrorReporter().ignoreThrow();
            e.printStackTrace();
            plugin.getLogger().warning("Failed request the webhook server.");
            return;
        }
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            try {
                plugin.getLogger().warning("Webhook may not success: HTTP_CODE=" + response.getStatusLine().getStatusCode() + " REASON=" + response.getStatusLine().getReasonPhrase() + " RESPONSE=" + new BufferedReader(new InputStreamReader(response.getEntity().getContent())).lines().collect(Collectors.joining()));
            } catch (IOException ignored) {
            }
        }
        try {
            httpClient.close();
        } catch (IOException ignored) {
        }
    }

    public HandledConnection handleConnection() throws IOException, IllegalArgumentException {
        String urls = plugin.getConfig().getString("webhook.url");
        if (urls == null) {
            throw new IllegalArgumentException("Webhook URL is null, did you set the url in config.yml?");
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(urls);
        httpPost.setHeader("User-Agent", "QuickShop-Reremake " + QuickShop.getVersion());
        return new HandledConnection(httpClient, httpPost);
    }
}

enum RequestType {
    NOTICE_ONLY, NOTICE_AND_RESPONSE
}

enum ThreadType {
    SYNC, ASYNC
}

@AllArgsConstructor
@Data
class HandledConnection {
    private CloseableHttpClient client;
    private HttpPost httpPost;
}