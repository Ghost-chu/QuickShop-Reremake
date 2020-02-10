package org.maxgamer.quickshop.utils;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public final class UpdateChecker {

    @NotNull
    private final Plugin plugin;

    private final int project;

    @NotNull
    private final String checkURL;

    @NotNull
    private String newVersion;

    public UpdateChecker(@NotNull Plugin plugin, int projectID) {
        this.plugin = plugin;
        this.newVersion = plugin.getDescription().getVersion();
        this.project = projectID;
        this.checkURL = "https://api.spigotmc.org/legacy/update.php?resource=" + projectID;
    }

    @NotNull
    public String getLatestVersion() {
        return newVersion;
    }

    @NotNull
    public String getResourceURL() {
        return "https://www.spigotmc.org/resources/" + project;
    }
 
    public boolean checkForUpdates() throws IOException {
        final URLConnection con = new URL(checkURL).openConnection();
        newVersion = new BufferedReader(
            new InputStreamReader(
                con.getInputStream()
            )
        ).readLine();

        return !plugin.getDescription().getVersion().equals(newVersion);
    }

}