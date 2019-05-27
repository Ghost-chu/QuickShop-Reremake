package org.maxgamer.quickshop.Util;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.maxgamer.quickshop.QuickShop;

public class Compatibility {
    private QuickShop plugin;
    private ArrayList<String> knownIncompatiablePlugin = new ArrayList<>();
    public Compatibility (QuickShop plugin){
        this.plugin = plugin;
        knownIncompatiablePlugin.add("OpenInv");
        knownIncompatiablePlugin.add("LWC");
    }
    private final ArrayList<RegisteredListener> disabledListeners = new ArrayList<>();
    /**
    Switch the compatibility mode on or off, set false to disable all we known incompatiable plugin listener,
     set true to enable back all disabled plugin liseners.
     */
    public void toggleInteractListeners(boolean status){
        if(status){
            for (RegisteredListener listener : PlayerInteractEvent.getHandlerList().getRegisteredListeners()) {
                for (String pluginName : knownIncompatiablePlugin){
                    Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
                    if(plugin != null){
                        if(listener.getPlugin() == plugin) {
                            Util.debugLog("Disabled plugin "+pluginName+"'s listener "+listener.getListener().getClass().getName());
                            PlayerInteractEvent.getHandlerList().unregister(plugin);
                            disabledListeners.add(listener);
                        }
                    }
                }
            }
            for (String pluginString : knownIncompatiablePlugin){
                Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginString);
                if(plugin!=null){
                    PlayerInteractEvent.getHandlerList().getRegisteredListeners();
                }
            }
        }else{
            PlayerInteractEvent.getHandlerList().registerAll(disabledListeners);
        }
    }
}
