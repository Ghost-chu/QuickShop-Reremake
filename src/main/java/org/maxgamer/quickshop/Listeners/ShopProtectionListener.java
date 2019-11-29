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
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;

import java.util.HashMap;

@SuppressWarnings("DuplicatedCode")
public class ShopProtectionListener implements Listener {

    @NotNull
    private final QuickShop plugin;
    private final boolean useEnhanceProtection;

    public ShopProtectionListener(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        useEnhanceProtection = plugin.getConfig().getBoolean("shop.enchance-shop-protect");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        if (ListenerHelper.isDisabled(e.getClass())) {
            return;
        }

        for (int i = 0; i < e.blockList().size(); i++) {
            final Block b = e.blockList().get(i);
            final Shop shop = plugin.getShopManager().getShopIncludeAttached(b.getLocation());

            if (shop != null) {
                if (plugin.getConfig().getBoolean("protect.explode")) {
                    e.setCancelled(true);
                } else {
                    shop.delete();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent e) {
        if (ListenerHelper.isDisabled(e.getClass()) ||
            !useEnhanceProtection) {
            return;
        }

        final Shop shop = plugin.getShopManager().getShopIncludeAttached(e.getToBlock().getLocation());

        if (shop == null) {
            return;
        }

        e.setCancelled(true);
    }

    //Protect Redstone active shop
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()) ||
            !useEnhanceProtection) {
            return;
        }

        final Shop shop = plugin.getShopManager().getShopIncludeAttached(event.getBlock().getLocation());

        if (shop == null) {
            return;
        }

        event.setNewCurrent(event.getOldCurrent());
        //plugin.getLogger().warning("[Exploit Alert] a Redstone tried to active of " + shop);
        //Util.debugLog(ChatColor.RED + "[QuickShop][Exploit alert] Redstone was activated on the following shop " + shop);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent e) {
        if (ListenerHelper.isDisabled(e.getClass()) ||
            !useEnhanceProtection) {
            return;
        }

        final Block newBlock = e.getNewState().getBlock();
        final Shop thisBlockShop = plugin.getShopManager().getShopIncludeAttached(newBlock.getLocation());
        final Shop underBlockShop = plugin.getShopManager().getShopIncludeAttached(newBlock.getRelative(BlockFace.DOWN).getLocation());

        if (thisBlockShop == null && underBlockShop == null) {
            return;
        }
        e.setCancelled(true);
    }

    /*
     * Handles shops breaking through explosions
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        if (ListenerHelper.isDisabled(e.getClass())) {
            return;
        }

        for (int i = 0; i < e.blockList().size(); i++) {
            final Block b = e.blockList().get(i);
            final Shop shop = plugin.getShopManager().getShopIncludeAttached(b.getLocation());

            if (shop != null) {
                if (plugin.getConfig().getBoolean("protect.explode")) {
                    e.setCancelled(true);
                } else {
                    shop.delete();
                }
            }
        }
    }

    //Protect Minecart steal shop
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }

        final Location loc = event.getSource().getLocation();

        if (loc == null) {
            return;
        }

        final HashMap<Location, Shop> shopsInChunk = plugin.getShopManager().getShops(loc.getChunk());

        if (shopsInChunk == null || shopsInChunk.isEmpty() || shopsInChunk.get(loc) == null) {
            return;
        }

        event.setCancelled(true);

        final Location location = event.getInitiator().getLocation();

        if (location == null) {
            return;
        }

        location.getBlock().breakNaturally();
        MsgUtil.sendGlobalAlert("[DisplayGuard] Breaked the block at " + location + " try steal the items for shop " + loc);
    }

    //Protect Entity pickup shop
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobChangeBlock(EntityChangeBlockEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()) ||
            !useEnhanceProtection) {
            return;
        }

        final Shop shop = plugin.getShopManager().getShopIncludeAttached(event.getBlock().getLocation());

        if (shop == null) {
            return;
        }

        if (plugin.getConfig().getBoolean("protect.entity")) {
            event.setCancelled(true);
            return;
        }

        shop.delete();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()) ||
            !useEnhanceProtection) {
            return;
        }

        for (BlockState blockstate : event.getBlocks()) {
            final Shop shop = plugin.getShopManager().getShopIncludeAttached(blockstate.getLocation());

            if (shop == null) {
                continue;
            }

            event.setCancelled(true);
            return;
            //plugin.getLogger().warning("[Exploit Alert] a StructureGrowing tried to break the shop of " + shop);
            //Util.sendMessageToOps(ChatColor.RED + "[QuickShop][Exploit alert] A StructureGrowing tried to break the shop of " + shop);
        }
    }
}
