/*
 * This file is a part of project QuickShop, the name is Updater.java
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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GitHub;
import org.maxgamer.quickshop.NonQuickShopStuffs.com.sk89q.worldedit.util.net.HttpRequest;
import org.maxgamer.quickshop.QuickShop;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.List;

public class Updater {
    /**
     * Check new update
     *
     * @return True=Have a new update; False=No new update or check update failed.
     */
    public static UpdateInfomation checkUpdate() {
        if (!QuickShop.instance.getConfig().getBoolean("updater")) {
            return new UpdateInfomation(false, null);
        }
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=62575")
                    .openConnection();
            int timed_out = 300000;
            connection.setConnectTimeout(timed_out);
            connection.setReadTimeout(timed_out);
            String localPluginVersion = QuickShop.instance.getDescription().getVersion();
            String spigotPluginVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
            if (spigotPluginVersion != null && !spigotPluginVersion.equals(localPluginVersion)) {
                connection.disconnect();
                return new UpdateInfomation(spigotPluginVersion.toLowerCase().contains("beta"), spigotPluginVersion);
            }
            connection.disconnect();
            return new UpdateInfomation(false, spigotPluginVersion);
        } catch (IOException e) {
            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.RED + "[QuickShop] Failed to check for an update on SpigotMC.org! It might be an internet issue or the SpigotMC host is down. If you want disable the update checker, you can disable in config.yml, but we still high-recommend check for updates on SpigotMC.org often.");
            return new UpdateInfomation(false, null);
        }
    }

    public static byte[] downloadUpdatedJar() throws IOException {
        org.kohsuke.github.GHRelease ghRelease = GitHub.connectAnonymously().getUser("Ghost-chu")
                .getRepository("QuickShop-Reremake").getLatestRelease();
        Util.debugLog("Pulling update with release: " + ghRelease.getTagName());
        List<GHAsset> assets = ghRelease.getAssets();
        String uurl = null;
        long uurlSize = 0;
        for (GHAsset asset : assets) {
            if (asset.getName().contains("original-")) {
                continue;
            }
            if (asset.getName().contains("-javadoc")) {
                continue;
            }
            if (asset.getName().contains("-sources")) {
                continue;
            }
            if (asset.getName().contains("-shaded")) {
                continue;
            }
            uurl = asset.getBrowserDownloadUrl();
            uurlSize = asset.getSize();
        }

        if (uurl == null) {
            throw new IOException("Failed read the URL, cause it is empty.");
        }
        Util.debugLog("Downloading from " + uurl);
        InputStream is = HttpRequest.get(new URL(uurl)).header("User-Agent","QuickShop-Reremake " + QuickShop.getVersion()).execute().getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int len;
        long downloaded = 0;
        int size = os.size();
        while ((len = is.read(buff)) != -1) {
            os.write(buff, 0, len);
            downloaded += len;
            Util.debugLog("File Downloader:  " + downloaded + "/" + size + " bytes.");
        }
        Util.debugLog("Downloaded: " + downloaded + " Server:" + uurlSize);
        if (downloaded != uurlSize) {
            Util.debugLog("Size not match, download may broken.");
        }
        Util.debugLog("Download complete.");
        return os.toByteArray();
    }

    public static void replaceTheJar(byte[] data) throws RuntimeException, IOException {
        File pluginFolder = new File("plugins");
        if (!pluginFolder.exists()) {
            throw new RuntimeException("Can't find the plugins folder.");
        }
        if (!pluginFolder.isDirectory()) {
            throw new RuntimeException("Plugins not a folder.");
        }
        File[] plugins = pluginFolder.listFiles();
        if (plugins == null) {
            throw new IOException("Can't get the files in plugins folder");
        }
        File quickshop = null;
        for (File plugin : plugins) {
            try {
                PluginDescriptionFile desc = QuickShop.instance.getPluginLoader().getPluginDescription(plugin);
                if (!desc.getName().equals(QuickShop.instance.getDescription().getName())) {
                    continue;
                }
                Util.debugLog("Selected: " + plugin.getPath());
                quickshop = plugin;
                break;
            } catch (InvalidDescriptionException e) { //Ignore }
            }

        }
        if (quickshop == null) {
            throw new RuntimeException("Failed to get QuickShop Jar File.");
        }
        OutputStream outputStream = new FileOutputStream(quickshop, false);
        outputStream.write(data);
        outputStream.flush();
        outputStream.close();
    }
}
