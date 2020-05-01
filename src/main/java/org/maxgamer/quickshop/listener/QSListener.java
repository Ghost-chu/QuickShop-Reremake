package org.maxgamer.quickshop.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.QuickShop;

public abstract class QSListener implements Listener {
    final QuickShop plugin;
    public QSListener (QuickShop plugin){
        this.plugin = plugin;
    }

    public void register(){
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }
}
