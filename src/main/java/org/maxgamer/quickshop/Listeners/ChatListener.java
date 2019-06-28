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
            Util.debugLog("Ignored a chat event (Canceled by another plugin.)");
            return;
        }
        if (!plugin.getShopManager().getActions().containsKey(e.getPlayer().getUniqueId())) {
            return;
        }
        plugin.log(e.getPlayer().getName() + "(" + e.getPlayer().getUniqueId().toString() + ") was " + plugin.getShopManager()
                .getActions().get(e.getPlayer().getUniqueId()).getAction().name() + " the shop.");
        plugin.getShopManager().handleChat(e.getPlayer(), e.getMessage());
        e.setCancelled(true);
    }
}