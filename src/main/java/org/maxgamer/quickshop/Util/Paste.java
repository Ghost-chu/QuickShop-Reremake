package org.maxgamer.quickshop.Util;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Economy.Economy;
import org.maxgamer.quickshop.Economy.EconomyCore;
import org.maxgamer.quickshop.Economy.Economy_Vault;
import org.maxgamer.quickshop.QuickShop;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * A util to generate a paste report and upload it to Ubuntu Paste
 */
@AllArgsConstructor
public class Paste {
    private QuickShop plugin;

    /**
     * Create a server infomation paste
     *
     * @return The paste result content.
     */
    public String genNewPaste() {
        StringBuilder finalReport = new StringBuilder();
        finalReport.append("###############################\n");
        finalReport.append("QuickShop-Reremake Paste Result\n");
        finalReport.append("###############################\n");
        finalReport.append("\n");
        finalReport.append("\n");
        finalReport.append("================================================\n");
        finalReport.append("QuickShop:\n");
        finalReport.append("\tVersion: ").append(QuickShop.getVersion()).append("\n");
        finalReport.append("\tFork: ").append(plugin.getFork()).append("\n");
        finalReport.append("\tServer ID: ").append(plugin.getServerUniqueID().toString()).append("\n");
        finalReport.append("\tOpenInv Hook: ").append(plugin.getOpenInvPlugin() == null ? "Disabled" : "Enabled").append("\n");
        finalReport.append("\tEconomy System: ");
        EconomyCore economyCore = plugin.getEconomy().getCore();
        if (economyCore != null) {
            switch (Economy.getNowUsing()) {
                case VAULT:
                    finalReport.append("Vault").append("%").append(((Economy_Vault) economyCore).getProviderName());
                    break;
                case RESERVE:
                    finalReport.append("Reserve").append("%").append("No details");
                    break;
                case UNKNOWN:
                    finalReport.append("Unknown").append("%").append("Unknown error");
                    break;
            }
            finalReport.append("\n");
        } else {
            finalReport.append("null");
        }
        finalReport.append("================================================\n");
        finalReport.append("System:\n");
        finalReport.append("\tOS: ").append(System.getProperty("os.name")).append("\n");
        finalReport.append("\tArch: ").append(System.getProperty("os.arch")).append("\n");
        finalReport.append("\tVersion: ").append(System.getProperty("os.version")).append("\n");
        finalReport.append("\tCores: ").append(Runtime.getRuntime().availableProcessors()).append("\n");
        finalReport.append("================================================\n");
        finalReport.append("Server:\n");
        finalReport.append("\tName: ").append(Bukkit.getName()).append("\n");
        finalReport.append("\tServer Name: ").append(Bukkit.getServer().getName()).append("\n");
        finalReport.append("\tBuild: ").append(Bukkit.getServer().getVersion()).append("\n");
        finalReport.append("\tNMSV: ").append(Util.getNMSVersion()).append("\n");
        finalReport.append("\tData Version: ").append(Bukkit.getUnsafe().getDataVersion()).append("\n");
        finalReport.append("\tJava: ").append(System.getProperty("java.version")).append("\n");
        finalReport.append("\tPlayers: ").append(Bukkit.getOnlinePlayers().size()).append("/").append(Bukkit.getMaxPlayers())
                .append("\n");
        finalReport.append("\tOnlineMode: ").append(Bukkit.getOnlineMode()).append("\n");
        finalReport.append("\tBukkitVersion: ").append(Bukkit.getVersion()).append("\n");
        finalReport.append("\tWorldContainer: ").append(Bukkit.getWorldContainer().toString()).append("\n");
        finalReport.append("================================================\n");
        finalReport.append("Worlds:\n");
        finalReport.append("\tTotal: ").append(Bukkit.getWorlds().size()).append("\n");
        for (World world : Bukkit.getWorlds()) {
            finalReport.append("\t*********************************\n");
            finalReport.append("\t\tName: ").append(world.getName()).append("\n");
            finalReport.append("\t\tEnvironment: ").append(world.getEnvironment().name()).append("\n");
            finalReport.append("\t\tLoaded Chunks: ").append(world.getLoadedChunks().length).append("\n");
            finalReport.append("\t\tPlayer In World: ").append(world.getPlayers().size()).append("\n");
            finalReport.append("\t\tShops In World: ").append(Util.getShopsInWorld(world.getName())).append("\n");
        }
        finalReport.append("\t*********************************\n");//Add a line after last world
        finalReport.append("================================================\n");
        finalReport.append("Plugins:\n");
        finalReport.append("\tTotal: ").append(Bukkit.getPluginManager().getPlugins().length).append("\n");
        for (Plugin bplugin : Bukkit.getPluginManager().getPlugins()) {
            finalReport.append("\t").append(bplugin.getName()).append("@").append(bplugin.isEnabled() ? "Enabled" : "Disabled")
                    .append("\n");
        }
        finalReport.append("================================================\n");
        finalReport.append("Configurations:\n");
        try {
            finalReport.append("\t*********************************\n");
            finalReport.append("\tconfig.yml:\n");
            finalReport.append("\t\t\n").append(new String(Util
                    .inputStream2ByteArray(plugin.getDataFolder().toString() + "/config.yml"))).append("\n");
            finalReport.append("\t*********************************\n");
            finalReport.append("\tmessages.yml:\n");
            finalReport.append("\t\t\n").append(new String(Util
                    .inputStream2ByteArray(plugin.getDataFolder().toString() + "/messages.yml"))).append("\n");
            finalReport.append("\t*********************************\n");
            finalReport.append("\t*********************************\n");
            finalReport.append("\titemi18n.yml:\n");
            finalReport.append("\t\t\n").append(new String(Util
                    .inputStream2ByteArray(new File(new File(".", "logs"), "latest.log").getPath()))).append("\n");
            finalReport.append("\t*********************************\n");
            finalReport.append("\t*********************************\n");
            finalReport.append("\tenchi18n.yml:\n");
            finalReport.append("\t\t\n").append(new String(Util
                    .inputStream2ByteArray(new File(new File(".", "logs"), "latest.log").getPath()))).append("\n");
            finalReport.append("\t*********************************\n");
            finalReport.append("\t*********************************\n");
            finalReport.append("\tpotioni18n.yml:\n");
            finalReport.append("\t\t\n").append(new String(Util
                    .inputStream2ByteArray(new File(new File(".", "logs"), "latest.log").getPath()))).append("\n");
            finalReport.append("\t*********************************\n");
            finalReport.append("\t*********************************\n");
            finalReport.append("\tInternal Debug Log:\n");
            finalReport.append("\t\t\n").append(Util.list2String(Util.getDebugLogs()).replaceAll(",", "\n")).append("\n");
            finalReport.append("\t*********************************\n");
            try {
                finalReport.append("\t*********************************\n");
                finalReport.append("\tlatest.log:\n");
                finalReport.append("\t\t\n").append(new String(Util
                        .inputStream2ByteArray(new File(new File(".", "logs"), "latest.log").getPath()))).append("\n");
                finalReport.append("\t*********************************\n");
            } catch (Throwable th) {
                finalReport.append("\t*********************************\n");
                finalReport.append("\tlatest.log:\n");
                finalReport.append("\t\t\n").append("Read failed.").append("\n");
                finalReport.append("\t*********************************\n");
            }
            try {
                finalReport.append("\t*********************************\n");
                finalReport.append("\tbukkit.yml:\n");
                finalReport.append("\t\t\n").append(new String(Util
                        .inputStream2ByteArray(new File(new File("."), "bukkit.yml").getPath()))).append("\n");
                finalReport.append("\t*********************************\n");
            } catch (Throwable th) {
                finalReport.append("\t*********************************\n");
                finalReport.append("\tbukkit.yml:\n");
                finalReport.append("\t\t\n").append("Read failed.").append("\n");
                finalReport.append("\t*********************************\n");
            }
            try {
                finalReport.append("\t*********************************\n");
                finalReport.append("\tspigot.yml:\n");
                finalReport.append("\t\t\n").append(new String(Util
                        .inputStream2ByteArray(new File(new File("."), "spigot.yml").getPath()))).append("\n");
                finalReport.append("\t*********************************\n");
            } catch (Throwable th) {
                finalReport.append("\t*********************************\n");
                finalReport.append("\tspigot.yml:\n");
                finalReport.append("\t\t\n").append("Read failed.").append("\n");
                finalReport.append("\t*********************************\n");
            }
            try {
                finalReport.append("\t*********************************\n");
                finalReport.append("\tpaper.yml:\n");
                finalReport.append("\t\t\n").append(new String(Util
                        .inputStream2ByteArray(new File(new File("."), "paper.yml").getPath()))).append("\n");
                finalReport.append("\t*********************************\n");
            } catch (Throwable th) {
                finalReport.append("\t*********************************\n");
                finalReport.append("\tpaper.yml:\n");
                finalReport.append("\t\t\n").append("Read failed.").append("\n");
                finalReport.append("\t*********************************\n");
            }

        } catch (Throwable th) {
            finalReport.append("\tFailed to get data\n");
        }
        finalReport.append("================================================\n");

        //Process the data to protect passwords.
        String report = finalReport.toString();
        try {
            ConfigurationSection configurationSection = plugin.getConfig().getConfigurationSection("database");
            report = report.replaceAll(configurationSection.getString("user"), "[PROTECTED]");
            report = report.replaceAll(configurationSection.getString("password"), "[PROTECTED]");
            report = report.replaceAll(configurationSection.getString("host"), "[PROTECTED]");
            report = report.replaceAll(configurationSection.getString("port"), "[PROTECTED]");
            report = report.replaceAll(configurationSection.getString("database"), "[PROTECTED]");
        } catch (Throwable tg) {
            //Ignore
        }
        return report;
    }

    /**
     * Paste a text to paste.ubuntu.com
     *
     * @param text The text you want paste.
     * @return Target paste URL.
     * @throws Exception the throws
     */
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
        Util.debugLog("Request Completed: " + conn.getURL().toString());
        String link = conn.getURL().toString();
        in.close();
        out.close();
        return link;
    }
}