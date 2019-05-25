package org.maxgamer.quickshop.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.maxgamer.quickshop.QuickShop;

public class Paste {
    private QuickShop plugin;
    public Paste(QuickShop plugin){
        this.plugin=plugin;
    }
    public String genNewPaste(){
        StringBuffer finalReport = new StringBuffer();
        finalReport.append("###############################\n");
        finalReport.append("QuickShop-Reremake Paste Result\n");
        finalReport.append("###############################\n");
        finalReport.append("\n");
        finalReport.append("\n");
        finalReport.append("================================================\n");
        finalReport.append("QuickShop:\n");
        finalReport.append("\tVersion: "+QuickShop.getVersion()+"\n");
        finalReport.append("\tFork: "+plugin.getFork()+"\n");
        finalReport.append("\tOpenInv Hook: "+(plugin.getOpenInvPlugin()==null ? "Disabled":"Enabled")+"\n");
        finalReport.append("\tMV Hook: "+(plugin.getMvPlugin()==null ? "Disabled":"Enabled")+"\n");
        //finalReport.append("Fork: "+plugin.+"\n");
        finalReport.append("================================================\n");
        finalReport.append("System:\n");
        JSONObject serverData = plugin.getMetrics().getServerData();
        finalReport.append("\tOS: "+serverData.get("osName")+"\n");
        finalReport.append("\tArch: "+serverData.get("osArch")+"\n");
        finalReport.append("\tVersion: "+serverData.get("osVersion")+"\n");
        finalReport.append("\tCores: "+serverData.get("coreCount")+"\n");
        finalReport.append("================================================\n");
        finalReport.append("Server:\n");
        finalReport.append("\tBuild: "+Bukkit.getServer().getVersion()+"\n");
        finalReport.append("\tJava: "+serverData.get("javaVersion")+"\n");
        finalReport.append("\tPlayers: "+serverData.get("playerAmount")+"/"+ Bukkit.getOfflinePlayers().length+"\n");
        finalReport.append("\tOnlineMode: "+serverData.get("onlineMode")+"\n");
        finalReport.append("\tBukkitVersion: "+serverData.get("bukkitVersion")+"\n");
        finalReport.append("\tWorldContainer: "+Bukkit.getWorldContainer().toString()+"\n");
        finalReport.append("================================================\n");
        finalReport.append("Worlds:\n");
        finalReport.append("\tTotal: "+Bukkit.getWorlds().size()+"\n");
        for (World world : Bukkit.getWorlds()){
            finalReport.append("\t*********************************\n");
            finalReport.append("\t\tName: "+world.getName()+"\n");
            finalReport.append("\t\tEnvironment: "+world.getEnvironment().name()+"\n");
            finalReport.append("\t\tLoaded Chunks: "+world.getLoadedChunks().length+"\n");
            finalReport.append("\t\tPlayer In World: "+world.getPlayers().size()+"\n");
            finalReport.append("\t\tShops In World: "+Util.getShopsInWorld(world.getName())+"\n");
        }
        finalReport.append("\t*********************************\n");//Add a line after last world
        finalReport.append("================================================\n");
        finalReport.append("Plugins:\n");
        finalReport.append("\tTotal: "+Bukkit.getPluginManager().getPlugins().length+"\n");
        for (Plugin bplugin : Bukkit.getPluginManager().getPlugins()){
            finalReport.append("\t"+bplugin.getName()+"@"+(bplugin.isEnabled() ? "Enabled":"Disabled")+"\n");
        }
        finalReport.append("================================================\n");
        finalReport.append("Configurations:\n");
        finalReport.append("\t*********************************\n");
        finalReport.append("\tconfig.yml:\n");
        finalReport.append("\t\t"+String.valueOf(Bukkit.getPluginManager().getPlugin(plugin.getName()).getDataFolder().toString()+"/config.yml").getBytes()+"\n");
        finalReport.append("\t*********************************\n");
        finalReport.append("\tmessages.yml:\n");
        finalReport.append("\t\t"+String.valueOf(Bukkit.getPluginManager().getPlugin(plugin.getName()).getDataFolder().toString()+"/messages.yml")+"\n");
        finalReport.append("\t*********************************\n");
        finalReport.append("\t*********************************\n");
        finalReport.append("\tlatest.log:\n");
        finalReport.append("\t\t"+String.valueOf(new File(new File(".","logs"),"latest.log").getPath())+"\n");
        finalReport.append("\t*********************************\n");
        finalReport.append("================================================\n");
        return finalReport.toString();
    }
    public String pasteTheText(String text) throws Exception{
        URL url = new URL("https://paste.ubuntu.com");
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        PrintWriter out = new PrintWriter(conn.getOutputStream());
        StringBuilder builder = new StringBuilder();
        builder.append("poster=");
        builder.append("QuickShop Paster");
        builder.append("&syntax=text");
        builder.append("&expiration=week");
        builder.append("&content=");
        builder.append(URLEncoder.encode(text,"UTF-8"));
        //poster=aaaaaaa&syntax=text&expiration=&content=%21%40
        out.print(builder.toString());
        out.flush();//Drop
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        Util.debugLog(this,"pasteTheText","Request Completed: "+conn.getURL().toString());
        String link = conn.getURL().toString();
        if(in!=null)
            in.close();
        if(out!=null)
            out.close();
        return link;
    }
}