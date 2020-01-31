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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.NonQuickShopStuffs.com.sk89q.worldedit.util.net.HttpRequest;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Github.GithubAPI;
import org.maxgamer.quickshop.Util.Github.ReleaseJsonContainer;
import com.google.common.collect.Lists;

public class Updater {
  public static List<String> getHistoryVersions() throws IOException {
    String hostUrl = "https://www.spigotmc.org/resources/62575/history";
    HttpURLConnection conn = (HttpURLConnection) new URL(hostUrl).openConnection();
    
    conn.setDoInput(true);
    conn.setRequestMethod("GET");
    conn.setRequestProperty("User-Agent", "Chrome/79.0.3945.130");
    
    BufferedReader bufIn = new BufferedReader(
        new InputStreamReader(conn.getInputStream()));
    
    List<String> list = Lists.newArrayList();
    String header = "<td class=\"version\">";
    String tailer = "</td>";
    String line = null;
    
    while ((line = bufIn.readLine()) != null) {
      if (line.startsWith(header) && line.endsWith(tailer))
        list.add(line.substring(header.length(), line.indexOf(tailer)));
    }
    
    return list;
  }

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
      List<String> versions = getHistoryVersions();
      int curIndex = versions.indexOf(QuickShop.getVersion());
      if (curIndex == -1 || curIndex == 0)
        return new UpdateInfomation(false, QuickShop.getVersion());
      
      String latest = versions.get(0);
      boolean beta = latest.toLowerCase().contains("beta");
      return new UpdateInfomation(beta, latest);
    } catch (IOException e) {
      Bukkit.getConsoleSender()
          .sendMessage(
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
    Util.debugLog("Downloading from " + uurl);
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
}
