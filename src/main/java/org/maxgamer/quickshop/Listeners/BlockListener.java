package org.maxgamer.quickshop.Listeners;

import lombok.AllArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Info;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopAction;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

@AllArgsConstructor
public class BlockListener implements Listener {
    private QuickShop plugin;

    /**
     * Gets the shop a sign is attached to
     *
     * @param loc The location of the sign
     * @return The shop
     */
    private Shop getShopNextTo(Location loc) {
        Block b = Util.getAttached(loc.getBlock());
        // Util.getAttached(b)
        if (b == null) {
            return null;
        }
        return plugin.getShopManager().getShop(b.getLocation());
    }

    /*
     * Removes chests when they're destroyed.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onBreak(BlockBreakEvent e) {
        if (ListenerHelper.isDisabled(e.getClass())) {
            return;
        }
        Block b = e.getBlock();
        if (b.getState() instanceof Sign) {
            Sign sign = (Sign) b.getState();
            if (plugin.getConfig().getBoolean("lockette.enable")) {
                if (sign.getLine(0).equals(plugin.getConfig().getString("lockette.private")) || sign.getLine(0).equals(plugin
                        .getConfig().getString("lockette.more_users"))) {
                    //Ignore break lockette sign
                    plugin.getLogger().info("Skipped a dead-lock shop sign.(Lockette or other sign-lock plugin)");
                    return;
                }
            }
        }
        Player p = e.getPlayer();
        // If the shop was a chest
        if (Util.canBeShop(b)) {
            Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop == null) {
                return;
            }
            // If they're either survival or the owner, they can break it
            if (p.getGameMode() == GameMode.CREATIVE && !p.getUniqueId().equals(shop.getOwner())) {
                //Check SuperTool
                if (p.getInventory().getItemInMainHand().getType() == Material.GOLDEN_AXE) {
                    p.sendMessage(MsgUtil.getMessage("break-shop-use-supertool"));
                    return;
                }
                e.setCancelled(true);
                p.sendMessage(MsgUtil.getMessage("no-creative-break", MsgUtil.getItemi18n(Material.GOLDEN_AXE.name())));
                return;
            }
            if (e.isCancelled()) {
                p.sendMessage(MsgUtil.getMessage("no-permission"));
                return;
            }
            if (!shop.getModerator().isOwner(p.getUniqueId()) && !QuickShop.getPermissionManager().hasPermission(p, "quickshop.other.destroy")) {
                e.setCancelled(true);
                p.sendMessage(MsgUtil.getMessage("no-permission"));
                return;
            } else if (!shop.getModerator().isOwner(p.getUniqueId())) {
                e.setCancelled(true);
                p.sendMessage(MsgUtil.getMessage("no-permission"));
                return;
            }

            // Cancel their current menu... Doesnt cancel other's menu's.
            Info action = plugin.getShopManager().getActions().get(p.getUniqueId());
            if (action != null) {
                action.setAction(ShopAction.CANCELLED);
            }
            shop.onUnload();
            shop.delete();
            p.sendMessage(MsgUtil.getMessage("success-removed-shop"));
        } else if (Util.isWallSign(b.getType())) {
            if (b instanceof Sign) {
                Sign sign = (Sign) b;
                if (sign.getLine(0).equals(plugin.getConfig().getString("lockette.private")) || sign.getLine(0).equals(plugin
                        .getConfig().getString("lockette.more_users"))) {
                    //Ignore break lockette sign
                    return;
                }
            }
            Shop shop = getShopNextTo(b.getLocation());
            if (shop == null) {
                return;
            }
            // If they're in creative and not the owner, don't let them
            // (accidents happen)
            if (p.getGameMode() == GameMode.CREATIVE && !p.getUniqueId().equals(shop.getOwner())) {
                //Check SuperTool
                if (p.getInventory().getItemInMainHand().getType() == Material.GOLDEN_AXE) {
                    p.sendMessage(MsgUtil.getMessage("break-shop-use-supertool"));
                    shop.delete();
                    return;
                }
                e.setCancelled(true);
                p.sendMessage(MsgUtil.getMessage("no-creative-break", MsgUtil.getItemi18n(Material.GOLDEN_AXE.name())));
            }
            Util.debugLog("Cannot break the sign.");
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        Location loc = event.getDestination().getLocation();
        if (loc == null) {
            return;
        }
        Shop shop = plugin.getShopManager().getShop(loc);
        if (shop == null) {
            return;
        }
        shop.setSignText();
    }

    /*
     * Listens for chest placement, so a doublechest shop can't be created.
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (ListenerHelper.isDisabled(e.getClass())) {
            return;
        }
        BlockState bs = e.getBlock().getState();
        if (!(bs instanceof DoubleChest)) {
            return;
        }
        Block b = e.getBlock();
        Player p = e.getPlayer();
        Block chest = Util.getSecondHalf(b);
        if (chest != null && plugin.getShopManager().getShop(chest.getLocation()) != null && !QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.double")) {
            e.setCancelled(true);
            p.sendMessage(MsgUtil.getMessage("no-double-chests"));
        }
    }
}