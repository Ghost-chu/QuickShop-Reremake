package org.maxgamer.quickshop.Listeners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;

public class ShopProtectionListener implements Listener {
    private QuickShop plugin;

    public ShopProtectionListener(QuickShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        if (ListenerHelper.isDisabled(e.getClass()))
            return;
        for (int i = 0; i < e.blockList().size(); i++) {
            Block b = e.blockList().get(i);
            Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
                if (plugin.getConfig().getBoolean("protect.explode"))
                    e.setCancelled(true);
                else
                    shop.delete();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent e) {
        if (ListenerHelper.isDisabled(e.getClass()))
            return;
        if (!plugin.getConfig().getBoolean("protect.fromto"))
            return;
        Shop shop = plugin.getShopManager().getShop(e.getToBlock().getLocation());
        if (shop == null)
            return;
        e.setCancelled(true);
    }

    //Protect Redstone active shop
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        if (!plugin.getConfig().getBoolean("protect.redstone"))
            return;
        Shop shop = plugin.getShopManager().getShop(event.getBlock().getLocation());
        if (shop == null)
            return;
        event.setNewCurrent(event.getOldCurrent());
        //plugin.getLogger().warning("[Exploit Alert] a Redstone tried to active of " + shop);
        //Util.debugLog(ChatColor.RED + "[QuickShop][Exploit alert] Redstone was activated on the following shop " + shop);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent e) {
        if (ListenerHelper.isDisabled(e.getClass()))
            return;
        Block newBlock = e.getNewState().getBlock();
        Shop thisBlockShop = plugin.getShopManager().getShop(newBlock.getLocation());
        Shop underBlockShop = plugin.getShopManager().getShop(newBlock.getRelative(BlockFace.DOWN).getLocation());
        if (thisBlockShop == null && underBlockShop == null)
            return;
        if (plugin.getConfig().getBoolean("protect.spread"))
            e.setCancelled(true);
    }

    /*
     * Handles shops breaking through explosions
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        if (ListenerHelper.isDisabled(e.getClass()))
            return;
        for (int i = 0; i < e.blockList().size(); i++) {
            Block b = e.blockList().get(i);
            Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
                if (plugin.getConfig().getBoolean("protect.explode"))
                    e.setCancelled(true);
                else
                    shop.delete();
            }
        }
    }

    //Protect Minecart steal shop
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        if (!plugin.getConfig().getBoolean("protect.inventorymove"))
            return;
        Location loc = event.getSource().getLocation();
        if (loc == null)
            return;
        Shop shop = plugin.getShopManager().getShop(loc);
        if (shop == null)
            return;
        event.setCancelled(true);
    }

    //Protect Entity pickup shop
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobChangeBlock(EntityChangeBlockEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        Shop shop = plugin.getShopManager().getShop(event.getBlock().getLocation());
        if (shop == null)
            return;
        if (plugin.getConfig().getBoolean("protect.entity"))
            event.setCancelled(true);
        else
            shop.delete();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        if (!plugin.getConfig().getBoolean("protect.structuregrow"))
            return;
        for (BlockState blockstate : event.getBlocks()) {
            Shop shop = plugin.getShopManager().getShop(blockstate.getLocation());
            if (shop == null)
                continue;
            event.setCancelled(true);
            return;
            //plugin.getLogger().warning("[Exploit Alert] a StructureGrowing tried to break the shop of " + shop);
            //Util.sendMessageToOps(ChatColor.RED + "[QuickShop][Exploit alert] A StructureGrowing tried to break the shop of " + shop);
        }
    }
}
