package org.maxgamer.quickshop.Listeners;

import lombok.AllArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Info;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopAction;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

import java.util.HashMap;
import java.util.UUID;

//import com.griefcraft.lwc.LWC;
//import com.griefcraft.lwc.LWCPlugin;
@AllArgsConstructor
public class PlayerListener implements Listener {
    private QuickShop plugin;

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onClick(PlayerInteractEvent e) {
        if (ListenerHelper.isDisabled(e.getClass())) {
            return;
        }
        if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            Block b = e.getClickedBlock();
            if (b == null) {
                return;
            }
            if (!Util.canBeShop(b) && !Util.isWallSign(b.getType())) {
                return;
            }
            Player p = e.getPlayer();
            Location loc = b.getLocation();
            ItemStack item = e.getItem();
            // Get the shop
            Shop shop = plugin.getShopManager().getShop(loc);
            // If that wasn't a shop, search nearby shops
            if (shop == null) {
                Block attached;
                if (Util.isWallSign(b.getType())) {
                    attached = Util.getAttached(b);
                    if (attached != null) {
                        shop = plugin.getShopManager().getShop(attached.getLocation());
                    }
                } else if (Util.isDoubleChest(b)) {
                    attached = Util.getSecondHalf(b);
                    if (attached != null) {
                        Shop secondHalfShop = plugin.getShopManager().getShop(attached.getLocation());
                        if (secondHalfShop != null && !p.getUniqueId().equals(secondHalfShop.getOwner())) {
                            // If player not the owner of the shop, make him select the second half of the
                            // shop
                            // Otherwise owner will be able to create new double chest shop
                            shop = secondHalfShop;
                        }
                    }
                }
            }
            // Purchase handling
            if (shop != null && QuickShop.getPermissionManager().hasPermission(p, "quickshop.use")) {
                if (plugin.getConfig().getBoolean("shop.sneak-to-trade") && !p.isSneaking()) {
                    return;
                }
                shop.onClick();
                if (plugin.getConfig().getBoolean("effect.sound.onclick")) {
                    e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_DISPENSER_FAIL, 80.f, 1.0f);
                }
                // Text menu
                MsgUtil.sendShopInfo(p, shop);
                shop.setSignText();
                if (shop.isSelling()) {
                    double price = shop.getPrice();
                    double money = plugin.getEconomy().getBalance(p.getUniqueId());
                    int itemAmount = Math.min(Util.countSpace(p.getInventory(), shop.getItem()), (int) Math.floor(money / price));
                    if (!shop.isUnlimited()) {
                        itemAmount = Math.min(itemAmount, shop.getRemainingStock());
                    }
                    p.sendMessage(MsgUtil.getMessage("how-many-buy", p, "" + itemAmount));
                } else {
                    int items = Util.countItems(p.getInventory(), shop.getItem());
                    p.sendMessage(MsgUtil.getMessage("how-many-sell", p, "" + items));
                }
                // Add the new action
                HashMap<UUID, Info> actions = plugin.getShopManager().getActions();
                Info info = new Info(shop.getLocation(), ShopAction.BUY, null, null, shop);
                actions.put(p.getUniqueId(), info);
                return;
            }
            // Handles creating shops

            else if (e.useInteractedBlock() == Result.ALLOW && shop == null && item != null && item.getType() != Material.AIR
                    && QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.sell") && p.getGameMode() != GameMode.CREATIVE) {
                if (e.useInteractedBlock() == Result.DENY) {
                    return;
                }
                if (plugin.getConfig().getBoolean("shop.sneak-to-create") && !p.isSneaking()) {
                    return;
                }
                if (!plugin.getShopManager().canBuildShop(p, b, e.getBlockFace())) {
                    // As of the new checking system, most plugins will tell the
                    // player why they can't create a shop there.
                    // So telling them a message would cause spam etc.
                    return;
                }
                if (Util.getSecondHalf(b) != null && !QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.double")) {
                    p.sendMessage(MsgUtil.getMessage("no-double-chests", p));
                    return;
                }
                if (Util.isBlacklisted(item.getType())
                        && !QuickShop.getPermissionManager().hasPermission(p, "quickshop.bypass." + item.getType().name())) {
                    p.sendMessage(MsgUtil.getMessage("blacklisted-item", p));
                    return;
                }
                if (b.getType() == Material.ENDER_CHEST) {
                    if (!QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.enderchest")) {
                        return;
                    }
                }
                // if (!Util.canBeShop(b)) {
                //     Util.debugLog("Can be shop check failed.");
                //     return;
                // }
                // Already checked above
                if (Util.isWallSign(b.getType())) {
                    Util.debugLog("WallSign check failed.");
                    return;
                }
                // Finds out where the sign should be placed for the shop
                Block last = null;
                Location from = p.getLocation().clone();
                from.setY(b.getY());
                from.setPitch(0);
                BlockIterator bIt = new BlockIterator(from, 0, 7);
                while (bIt.hasNext()) {
                    Block n = bIt.next();
                    if (n.equals(b)) {
                        break;
                    }
                    last = n;
                }
                // Send creation menu.
                Info info = new Info(b.getLocation(), ShopAction.CREATE, e.getItem(), last);
                plugin.getShopManager().getActions().put(p.getUniqueId(), info);
                p.sendMessage(
                        MsgUtil.getMessage("how-much-to-trade-for", p, Util.getItemStackName(e.getItem())));
            }
        } else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && Util.isWallSign(e.getClickedBlock().getType())) {
            Block block;
            if (Util.isWallSign(e.getClickedBlock().getType())) {
                block = Util.getAttached(e.getClickedBlock());
            } else {
                block = e.getClickedBlock();
            }
            if (plugin.getShopManager().getShop(block.getLocation()) != null && plugin.getShopManager().getShop(block
                    .getLocation()).getOwner().equals(e.getPlayer().getUniqueId())) {
                if (plugin.getConfig().getBoolean("shop.sneak-to-control") && !e.getPlayer().isSneaking()) {
                    return;
                }
                if (plugin.getConfig().getBoolean("effect.sound.onclick")) {
                    e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_DISPENSER_FAIL, 80.f, 1.0f);
                }
                MsgUtil.sendControlPanelInfo(e.getPlayer(),
                        plugin.getShopManager().getShop(block.getLocation()));
                plugin.getShopManager().getShop(block.getLocation()).setSignText();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent e) {
        if (ListenerHelper.isDisabled(e.getClass())) {
            return;
        }
        try {
            Inventory inventory = e.getInventory();
            if (inventory == null) {
                return;
            }
            Location location = inventory.getLocation();
            if (location == null) {
                return;
            }
            Shop shop = plugin.getShopManager().getShop(location);
            if (shop == null) {
                return;
            }
            shop.setSignText();
        } catch (Throwable t) {
            //Ignore
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        if (ListenerHelper.isDisabled(e.getClass())) {
            return;
        }
        // Notify the player any messages they were sent
        if (plugin.getConfig().getBoolean("shop.auto-fetch-shop-messages")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    MsgUtil.flush(e.getPlayer());
                }
            }.runTaskAsynchronously(plugin);
        }

    }


    /*
     * Waits for a player to move too far from a shop, then cancels the menu.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (ListenerHelper.isDisabled(e.getClass())) {
            return;
        }
        Info info = plugin.getShopManager().getActions().get(e.getPlayer().getUniqueId());
        if (info == null) {
            return;
        }
        Player p = e.getPlayer();
        Location loc1 = info.getLocation();
        Location loc2 = p.getLocation();
        if (loc1.getWorld() != loc2.getWorld() || loc1.distanceSquared(loc2) > 25) {
            if (info.getAction() == ShopAction.CREATE) {
                p.sendMessage(MsgUtil.getMessage("shop-creation-cancelled", p));
                Util.debugLog(p.getName() + " too far with the shop location.");
            } else if (info.getAction() == ShopAction.BUY) {
                p.sendMessage(MsgUtil.getMessage("shop-purchase-cancelled", p));
                Util.debugLog(p.getName() + " too far with the shop location.");
            }
            plugin.getShopManager().getActions().remove(p.getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (ListenerHelper.isDisabled(e.getClass())) {
            return;
        }
        // Remove them from the menu
        plugin.getShopManager().getActions().remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (ListenerHelper.isDisabled(e.getClass())) {
            return;
        }
        PlayerMoveEvent me = new PlayerMoveEvent(e.getPlayer(), e.getFrom(), e.getTo());
        onMove(me);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDyeing(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (e.getItem() == null) {
            return;
        }
        if (!Util.isDyes(e.getItem().getType())) {
            return;
        }
        Block block = e.getClickedBlock();
        if (block == null) {
            return;
        }
        if (!Util.isWallSign(block.getType())) {
            return;
        }
        Block attachedBlock = Util.getAttached(block);
        if (attachedBlock == null) {
            return;
        }
        if (plugin.getShopManager().getShopIncludeAttached(attachedBlock.getLocation()) == null) {
            return;
        }
        e.setCancelled(true);
        Util.debugLog("Disallow " + e.getPlayer().getName() + " dye the shop sign.");
    }
}
