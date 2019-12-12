package org.maxgamer.quickshop.Util.Paste;

import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Util.Util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class UbuntuPaster implements PasteInterface{
    /**
     * Paste a text to paste.ubuntu.com
     *
     * @param text The text you want paste.
     * @return Target paste URL.
     * @throws Exception the throws
     */
    @NotNull
    public String pasteTheText(@NotNull String text) throws Exception {
        URL url = new URL("https://paste.ubuntu.com");
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        PrintWriter out = new PrintWriter(conn.getOutputStream());
        //poster=aaaaaaa&syntax=text&expiration=&content=%21%40
        String builder = "poster=" +
                "QuickShop Paster" +
                "&syntax=text" +
                "&expiration=month" +
                "&content=" +
                URLEncoder.encode(text, "UTF-8");
        out.print(builder);
        out.flush();//Drop
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        Util.debugLog("Request Completed: " + conn.getURL());
        String link = conn.getURL().toString();
        in.close();
        out.close();
        return link;
    }
}
