/*
 * This file is a part of project QuickShop, the name is JenkinsUpdater.java
 *  Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.util.updater.impl;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.BuildInfo;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.nonquickshopstuff.com.sk89q.worldedit.util.net.HttpRequest;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.updater.QuickUpdater;
import org.maxgamer.quickshop.util.updater.VersionType;

import java.io.*;
import java.net.URL;
public class JenkinsUpdater implements QuickUpdater {
    private final BuildInfo pluginBuildInfo;

    private BuildInfo lastRemoteBuildInfo;

    public JenkinsUpdater(BuildInfo pluginBuildInfo) {
        this.pluginBuildInfo = pluginBuildInfo;
    }

    @Override
    public @NotNull VersionType getCurrentRunning() {
        return VersionType.STABLE; //TODO LTS
    }

    @Override
    public int getRemoteServerBuildId() {
        if (this.lastRemoteBuildInfo == null) {
            isLatest(getCurrentRunning());
        }
        if (this.lastRemoteBuildInfo == null) {
            return -1;
        }
        return this.lastRemoteBuildInfo.getBuildId();
    }

    @Override
    public @NotNull String getRemoteServerVersion() {
        if (this.lastRemoteBuildInfo == null) {
            isLatest(getCurrentRunning());
        }
        if (this.lastRemoteBuildInfo == null) {
            return "Error";
        }
        return this.lastRemoteBuildInfo.getBuildTag();
    }

    @Override
    public boolean isLatest(@NotNull VersionType versionType) {
        InputStream inputStream;
        try {
            inputStream = HttpRequest.get(new URL("https://ci.codemc.io/job/Ghost-chu/job/QuickShop-Reremake/lastSuccessfulBuild/artifact/target/BUILDINFO"))
                    .header("User-Agent", "QuickShop-" + QuickShop.getFork() + " " + QuickShop.getVersion())
                    .execute()
                    .expectResponseCode(200)
                    .getInputStream();
        } catch (IOException ioException) {
            MsgUtil.sendMessage(Bukkit.getConsoleSender(), ChatColor.RED + "[QuickShop] Failed to check for an update on SpigotMC.org! It might be an internet issue or the SpigotMC host is down. If you want disable the update checker, you can disable in config.yml, but we still high-recommend check for updates on SpigotMC.org often.");
            return true;
        }
        BuildInfo buildInfo = new BuildInfo(inputStream);
        this.lastRemoteBuildInfo = buildInfo;
        return buildInfo.getBuildId() >= pluginBuildInfo.getBuildId() || buildInfo.getGitCommit().equalsIgnoreCase(pluginBuildInfo.getGitCommit());
    }

    @Override
    public byte[] update(@NotNull VersionType versionType) throws IOException {
        InputStream is = HttpRequest.get(new URL("https://ci.codemc.io/job/Ghost-chu/job/QuickShop-Reremake/lastSuccessfulBuild/artifact/target/QuickShop.jar"))
                .header("User-Agent", "QuickShop-" + QuickShop.getFork() + " " + QuickShop.getVersion())
                .execute()
                .getInputStream();
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            byte[] buff = new byte[1024];
            int len;
            long downloaded = 0;
            if (is == null) {
                throw new IOException("Failed downloading: Cannot open connection with remote server.");
            }
            while ((len = is.read(buff)) != -1) {
                os.write(buff, 0, len);
                downloaded += len;
                Util.debugLog("File Downloader: " + downloaded + " bytes.");
            }
            is.close();
            byte[] file = os.toByteArray();
            os.close();
            return file;
        }
    }

    @Override
    public void install(byte[] bytes) throws IOException {
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
                PluginDescriptionFile desc = QuickShop.getInstance().getPluginLoader().getPluginDescription(plugin);
                if (!desc.getName().equals(QuickShop.getInstance().getDescription().getName())) {
                    continue;
                }
                Util.debugLog("Selected: " + plugin.getPath());
                quickshop = plugin;
                break;
            } catch (InvalidDescriptionException e) { // Ignore }
            }
        }
        if (quickshop == null) {
            throw new IOException("Failed to get QuickShop Jar File.");
        }

        try (OutputStream outputStream = new FileOutputStream(quickshop, false)) {
            outputStream.write(bytes);
            outputStream.flush();
        }
    }
}
