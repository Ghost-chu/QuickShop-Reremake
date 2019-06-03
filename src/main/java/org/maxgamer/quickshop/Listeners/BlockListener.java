package org.maxgamer.quickshop.Listeners;

import lombok.*;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.*;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

@AllArgsConstructor
public class BlockListener implements Listener {
    private QuickShop plugin;
    /**
     * Listens for chest placement, so a doublechest shop can't be created.
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        BlockState bs = e.getBlock().getState();
        if (bs instanceof DoubleChest == false)
            return;
        Block b = e.getBlock();
        Player p = e.getPlayer();
        Block chest = Util.getSecondHalf(b);
        if (chest != null && plugin.getShopManager().getShop(chest.getLocation()) != null && !p
                .hasPermission("quickshop.create.double")) {
            e.setCancelled(true);
            p.sendMessage(MsgUtil.getMessage("no-double-chests"));
        }
    }

    /**
     * Removes chests when they're destroyed.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
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
            if (shop == null)
                return;
            // If they're either survival or the owner, they can break it
            if (p.getGameMode() == GameMode.CREATIVE && !p.getUniqueId().equals(shop.getOwner())) {
                e.setCancelled(true);
                p.sendMessage(MsgUtil.getMessage("no-creative-break"));
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
                    plugin.getLogger().info("Skipped a dead-lock shop sign.(Lockette)");
                    return;
                }
            }
            Shop shop = getShopNextTo(b.getLocation());
            if (shop == null)
                return;
            // If they're in creative and not the owner, don't let them
            // (accidents happen)
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
                return;
            }
            e.setCancelled(true);
            // Cancel the event so that the sign does not
            // drop.. TODO: Find a better way.
            //b.setType(Material.AIR);
        }
    }

    //Protect Minecart steal shop
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (plugin.getConfig().getBoolean("protect.minecart")) {
            // Additional Hopper Minecart Check
            if (event.getDestination().getHolder() instanceof HopperMinecart) {
                HopperMinecart hm = (HopperMinecart) event.getDestination().getHolder();
                Location minecartLoc = new Location(hm.getWorld(), hm.getLocation().getBlockX(), hm.getLocation()
                        .getBlockY() + 1, hm.getLocation().getBlockZ());
                Shop shop = plugin.getShopManager().getShop(minecartLoc);
                if (shop == null) {
                    return;
                }
                event.setCancelled(true);
                hm.remove();
                plugin.getLogger().warning("[Exploit Alert] a HopperMinecart tried to move the item of " + shop);
                Util.sendMessageToOps(ChatColor.RED + "[QuickShop][Exploit alert] A HopperMinecart tried to move the item of " + shop);
            }
        }
        if (plugin.getConfig().getBoolean("protect.hopper")) {
            if (event.getDestination().getHolder() instanceof Hopper) {
                Hopper h = (Hopper) event.getDestination().getHolder();
                Location minecartLoc = h.getBlock().getLocation();
                Shop shop = plugin.getShopManager().getShop(minecartLoc);
                if (shop == null) {
                    return;
                }
                event.setCancelled(true);
            }
        }
    }

    //Protect Entity pickup shop
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobChangeBlock(EntityChangeBlockEvent event) {
        Shop shop = plugin.getShopManager().getShop(event.getBlock().getLocation());
        if (shop == null) {
            return;
        }
        if (plugin.getConfig().getBoolean("protect.entity")) {
            event.setCancelled(true);
            //event.getEntity().remove();
            //plugin.getLogger().warning("[Exploit Alert] a Entity tried to break the shop of " + shop);
            Util.sendMessageToOps(ChatColor.RED + "[QuickShop][Exploit alert] A Entity tried to break the shop of " + shop);
        } else {
            plugin.getQueuedShopManager().add(new QueueShopObject(shop, QueueAction.DELETE));
        }

    }

    //Protect Redstone active shop
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        if (!plugin.getConfig().getBoolean("protect.redstone")) {
            return;
        }
        Shop shop = plugin.getShopManager().getShop(event.getBlock().getLocation());
        if (shop == null) {
            return;
        }
            event.setNewCurrent(event.getOldCurrent());
        //plugin.getLogger().warning("[Exploit Alert] a Redstone tried to active of " + shop);
            Util.sendMessageToOps(ChatColor.RED + "[QuickShop][Exploit alert] A Redstone tried to active of " + shop);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        if (!plugin.getConfig().getBoolean("protect.structuregrow")) {
            return;
        }
        for (BlockState blockstate : event.getBlocks()) {
            Shop shop = plugin.getShopManager().getShop(blockstate.getLocation());
            if (shop == null) {
                return;
            }
            event.setCancelled(true);
            plugin.getLogger().warning("[Exploit Alert] a StructureGrowing tried to break the shop of " + shop);
            Util.sendMessageToOps(ChatColor.RED + "[QuickShop][Exploit alert] A StructureGrowing tried to break the shop of " + shop);
        }
    }

    /**
     * Handles shops breaking through explosions
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        for (int i = 0; i < e.blockList().size(); i++) {
            Block b = e.blockList().get(i);
            Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
                if (plugin.getConfig().getBoolean("protect.explode")) {
                    e.setCancelled(true);
                    plugin.getLogger().warning("[Exploit Alert] a EntityExplode tried to break the shop of " + shop);
                    Util.sendMessageToOps(ChatColor.RED + "[QuickShop][Exploit alert] A EntityExplode tried to break the shop of " + shop);
                } else {
                    plugin.getQueuedShopManager().add(new QueueShopObject(shop, QueueAction.DELETE));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        for (int i = 0; i < e.blockList().size(); i++) {
            Block b = e.blockList().get(i);
            Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
                if (plugin.getConfig().getBoolean("protect.explode")) {
                    e.setCancelled(true);
                    plugin.getLogger().warning("[Exploit Alert] a BlockExplode tried to break the shop of " + shop);
                    Util.sendMessageToOps(ChatColor.RED + "[QuickShop][Exploit alert] A BlockExplode tried to break the shop of " + shop);
                } else {
                    plugin.getQueuedShopManager().add(new QueueShopObject(shop, QueueAction.DELETE));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent e) {
        Block newBlock = e.getNewState().getBlock();
        Shop thisBlockShop = plugin.getShopManager().getShop(newBlock.getLocation());
        Shop underBlockShop = plugin.getShopManager().getShop(newBlock.getRelative(BlockFace.DOWN).getLocation());
        if (thisBlockShop == null && underBlockShop == null)
            return;
        e.setCancelled(true);
    }

    /**
     * Gets the shop a sign is attached to
     *
     * @param loc The location of the sign
     * @return The shop
     */
    private Shop getShopNextTo(Location loc) {
        Block b = Util.getAttached(loc.getBlock());
        // Util.getAttached(b)
        if (b == null)
            return null;
        return plugin.getShopManager().getShop(b.getLocation());
    }
}