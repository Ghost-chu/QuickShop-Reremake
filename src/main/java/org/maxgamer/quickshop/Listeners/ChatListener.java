package org.maxgamer.quickshop.Listeners;

import lombok.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

/**
 * @author Netherfoam
 */
@AllArgsConstructor
public class ChatListener implements Listener {
    private QuickShop plugin;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.isCancelled() && plugin.getConfig().getBoolean("shop.ignore-cancel-chat-event")) {
            Util.debugLog("Ignore chat event (cancel by other plugin.)");
            return;
        }
        if (!plugin.getShopManager().getActions().containsKey(e.getPlayer().getUniqueId())) {
            plugin.log(plugin.getShopManager().getActions().toString());
            return;
        }
        plugin.getShopManager().handleChat(e.getPlayer(), e.getMessage());
        e.setCancelled(true);
    }
}