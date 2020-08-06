package org.maxgamer.quickshop.util.updater.impl;

import lombok.AllArgsConstructor;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.BuildInfo;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.nonquickshopstuff.com.sk89q.worldedit.util.net.HttpRequest;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.updater.QuickUpdater;
import org.maxgamer.quickshop.util.updater.VersionType;

import java.io.*;
import java.net.URL;

@AllArgsConstructor
public class JenkinsUpdater implements QuickUpdater {
    private final BuildInfo pluginBuildInfo;

    @Override
    public @NotNull VersionType getCurrentRunning() {
        return VersionType.STABLE; //TODO LTS
    }

    @Override
    public boolean isLatest(@NotNull VersionType versionType) throws IOException {
        InputStream inputStream = HttpRequest.get(new URL("https://ci.codemc.io/job/Ghost-chu/job/QuickShop-Reremake/lastSuccessfulBuild/artifact/target/BUILDINFO"))
                .header("User-Agent", "QuickShop-" + QuickShop.getFork() + " " + QuickShop.getVersion())
                .execute()
                .expectResponseCode(200)
                .getInputStream();
        BuildInfo buildInfo = new BuildInfo(inputStream);
        return buildInfo.getBuildId().equalsIgnoreCase(pluginBuildInfo.getBuildId()) || buildInfo.getGitCommit().equalsIgnoreCase(pluginBuildInfo.getGitCommit());
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

        OutputStream outputStream = new FileOutputStream(quickshop, false);
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();
    }
}
