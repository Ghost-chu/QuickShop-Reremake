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

package org.maxgamer.quickshop.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.nonquickshopstuff.com.sk89q.worldedit.util.net.HttpRequest;
import org.maxgamer.quickshop.util.github.GithubAPI;
import org.maxgamer.quickshop.util.github.ReleaseJsonContainer;

import java.io.*;
import java.net.URL;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Updater {

    private static Pattern pattern = Pattern.compile("([0-9]*\\.)+[0-9]*");

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

            String localPluginVersion = QuickShop.instance.getDescription().getVersion();
            String spigotPluginVersion =
                HttpRequest.get(new URL("https://api.spigotmc.org/legacy/update.php?resource=62575"))
                    .execute()
                    .expectResponseCode(200)
                    .returnContent()
                    .asString("UTF-8")
                    .trim();
            if (!spigotPluginVersion.isEmpty() && !spigotPluginVersion.equals(localPluginVersion)) {
                Util.debugLog(spigotPluginVersion);
                return new UpdateInfomation(
                    spigotPluginVersion.toLowerCase().contains("beta"), spigotPluginVersion);
            }
            return new UpdateInfomation(false, spigotPluginVersion);
        } catch (IOException e) {
            MsgUtil.sendMessage(Bukkit.getConsoleSender(),
                    ChatColor.RED
                        + "[QuickShop] Failed to check for an update on SpigotMC.org! It might be an internet issue or the SpigotMC host is down. If you want disable the update checker, you can disable in config.yml, but we still high-recommend check for updates on SpigotMC.org often.");
            return new UpdateInfomation(false, null);
        }
    }

    public static byte[] downloadUpdatedJar() throws IOException {
        @Nullable String uurl;
        long uurlSize;
        try {
            ReleaseJsonContainer.AssetsBean bean =
                Objects.requireNonNull(new GithubAPI().getLatestRelease());
            uurl = bean.getBrowser_download_url();
            uurlSize = bean.getSize();
        } catch (Throwable ig) {
            throw new IOException(ig.getMessage());
        }

        if (uurl == null) {
            throw new IOException("Failed read the URL, cause it is empty.");
        }
        QuickShop.instance.getLogger().info("Downloading from " + uurl);
        InputStream is =
            HttpRequest.get(new URL(uurl))
                .header("User-Agent", "QuickShop-Reremake " + QuickShop.getVersion())
                .execute()
                .getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int len;
        long downloaded = 0;
        if (is == null) {
            throw new IOException("Failed downloading: Cannot open connection with remote server.");
        }
        while ((len = is.read(buff)) != -1) {
            os.write(buff, 0, len);
            downloaded += len;
            Util.debugLog("File Downloader:  " + downloaded + "/" + uurlSize + " bytes.");
        }
        Util.debugLog("Downloaded: " + downloaded + " Server:" + uurlSize);
        if (!(uurlSize < 1) && downloaded != uurlSize) {
            Util.debugLog("Size not match, download may broken.");
            QuickShop.instance.getLogger().info("Failed to download update: Size not match, downloaded: " + downloaded + " excepted: " + uurlSize);
            throw new IOException("Size not match, download mayb broken, aborting.");
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
                PluginDescriptionFile desc =
                    QuickShop.instance.getPluginLoader().getPluginDescription(plugin);
                if (!desc.getName().equals(QuickShop.instance.getDescription().getName())) {
                    continue;
                }
                Util.debugLog("Selected: " + plugin.getPath());
                quickshop = plugin;
                break;
            } catch (InvalidDescriptionException e) { // Ignore }
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

    public static boolean hasUpdate(String versionNow) {
        return hasUpdate(QuickShop.getVersion(), versionNow);
    }

    public static boolean hasUpdate(String versionPre, String versionNow) {
        if (versionNow == null) {
            return false;
        }
        if (!versionNow.equals(versionPre)) {
            Matcher matcher = pattern.matcher(versionNow);
            if (matcher.find()) {
                String result = matcher.group(0);
                if (result != null && !result.isEmpty()) {
                    String[] now = matcher.group(0).split("\\.");
                    String[] previous = versionPre.split("\\.");
                    for (int i = 0; i < now.length; i++) {
                        if (i < previous.length) {
                            int nowSub = Integer.parseInt(now[i]);
                            int preSub = Integer.parseInt(previous[i]);
                            if (nowSub == preSub) {
                                continue;
                            }
                            return nowSub > preSub;
                        }
                    }
                }
            }
        } else {
            //equals, so no update
            return false;
        }
        // no recognize, forced update
        return true;
    }
}
