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

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent e) {
        if (ListenerHelper.isDisabled(e.getClass()))
            return;
        if (e.isCancelled() && plugin.getConfig().getBoolean("shop.ignore-cancel-chat-event")) {
            Util.debugLog("Ignored a chat event (Canceled by another plugin.)");
            return;
        }
        if (!plugin.getShopManager().getActions().containsKey(e.getPlayer().getUniqueId()))
            return;
        plugin.getShopManager().handleChat(e.getPlayer(), e.getMessage());
        e.setCancelled(true);
    }
}
