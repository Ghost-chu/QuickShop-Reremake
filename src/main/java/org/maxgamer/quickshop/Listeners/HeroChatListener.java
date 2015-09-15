package org.maxgamer.quickshop.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.QuickShop;

import com.dthielke.herochat.ChannelChatEvent;
import com.dthielke.herochat.Chatter.Result;

/**
 * 
 * @author Netherfoam
 * 
 */
public class HeroChatListener implements Listener {
	QuickShop plugin;

	public HeroChatListener(QuickShop plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onHeroChat(ChannelChatEvent e) {
		if (!plugin.getShopManager().getActions().containsKey(e.getSender().getPlayer().getUniqueId()))
			return;
		plugin.getShopManager().handleChat(e.getSender().getPlayer(), e.getMessage());
		e.setResult(Result.FAIL);
	}
}
