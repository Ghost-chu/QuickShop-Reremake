package org.maxgamer.quickshop.Util;

import java.util.ArrayList;

import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.QuickShop;

/**
 * WIP
 */
public class Compatibility {
    private QuickShop plugin;
    final private ArrayList<String> knownIncompatiablePlugin = new ArrayList<>();

    public Compatibility(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        knownIncompatiablePlugin.add("OpenInv");
        knownIncompatiablePlugin.add("LWC");
    }

    private final ArrayList<RegisteredListener> disabledListeners = new ArrayList<>();

    /**
     * Switch the compatibility mode on or off, set false to disable all we known incompatiable plugin listener,
     * set true to enable back all disabled plugin liseners.
     * WIP
     *
     * @param status true=turn on closed listeners, false=turn off all turned on listeners.
     */
    public void toggleInteractListeners(@NotNull boolean status) {
        // if (status) {
        //     disabledListeners.clear();
        //     for (RegisteredListener listener : PlayerInteractEvent.getHandlerList().getRegisteredListeners()) {
        //         for (String pluginName : knownIncompatiablePlugin) {
        //             Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        //             if (plugin != null) {
        //                 if (listener.getPlugin() == plugin) {
        //                     Util.debugLog("Disabled plugin " + pluginName + "'s listener " + listener.getListener().getClass()
        //                             .getName());
        //                     //PlayerInteractEvent.getHandlerList().unregister(plugin);
        //                     PlayerInteractEvent.getHandlerList().unregister(listener);
        //                     disabledListeners.add(listener);
        //                 }
        //             }
        //         }
        //     }
        // } else {
        //    try{
        //        PlayerInteractEvent.getHandlerList().registerAll(disabledListeners);
        //        disabledListeners.clear();
        //    }catch (Throwable e){
        //        //Ignore
        //    }
        // }
    }
}
