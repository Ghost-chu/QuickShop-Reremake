/*
 * This file is a part of project QuickShop, the name is ChatListener.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;

/**
 * @author Netherfoam
 */
public class ChatListener extends AbstractQSListener {

    public ChatListener(QuickShop plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {

        if (e.isCancelled() && plugin.getConfig().getBoolean("shop.ignore-cancel-chat-event")) {
            Util.debugLog("Ignored a chat event (Canceled by another plugin.)");
            return;
        }

        if (!plugin.getShopManager().getActions().containsKey(e.getPlayer().getUniqueId())) {
            return;
        }
        // Fix stupid chat plugin will add a weird space before or after the number we want.
        plugin.getShopManager().handleChat(e.getPlayer(), e.getMessage().trim());
        e.setCancelled(true);
    }

    /**
     * Callback for reloading
     *
     * @return Reloading success
     */
    @Override
    public ReloadResult reloadModule() {
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }
}
