/*
 * This file is a part of project QuickShop, the name is PlayerListener.java
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

import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.economy.Economy;
import org.maxgamer.quickshop.shop.Info;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.shop.ShopAction;
import org.maxgamer.quickshop.util.InteractUtil;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerListener extends QSListener {
    //    private final Cache<Player, Long> dupeClickPrevent = CacheBuilder
//            .newBuilder()
//            .initialCapacity(10)
//            .expireAfterAccess(1, TimeUnit.SECONDS)
//            .weakKeys()
//            .build();
    private final CooldownMap<Player> cooldownMap = CooldownMap.create(Cooldown.of(1, TimeUnit.SECONDS));
    private boolean swapBehavior = false;

    public PlayerListener(QuickShop plugin) {
        super(plugin);
        swapBehavior = plugin.getConfig().getBoolean("shop.interact.swap-click-behavior");
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onClick(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }
        final Block b = e.getClickedBlock();

        // ----Adventure dupe click workaround start----
        if (e.getPlayer().getGameMode() == GameMode.ADVENTURE) {
            cooldownMap.test(e.getPlayer());
        }
        // ----Adventure dupe click workaround end----
        if (!e.getAction().equals(Action.LEFT_CLICK_BLOCK) && b != null) {
            if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                if (!swapBehavior) {
                    postControlPanel(e);
                } else {
                    postTrade(e);
                }
                return;
            }
        }
        if (!swapBehavior) {
            postTrade(e);
        } else {
            postControlPanel(e);
        }

    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onAdventureClick(PlayerAnimationEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.ADVENTURE) {
            return;
        }
        // ----Adventure dupe click workaround start----
        if (!cooldownMap.test(event.getPlayer())) {
            return;
        }
        // ----Adventure dupe click workaround end----
        Block focused = event.getPlayer().getTargetBlock(null, 5);
        PlayerInteractEvent interactEvent
                = new PlayerInteractEvent(event.getPlayer(),
                focused.getType() == Material.AIR ? Action.LEFT_CLICK_AIR : Action.LEFT_CLICK_BLOCK,
                event.getPlayer().getInventory().getItemInMainHand(),
                focused,
                event.getPlayer().getFacing().getOppositeFace());

        if (!swapBehavior) {
            postTrade(interactEvent);
        } else {
            postControlPanel(interactEvent);
        }

    }

    private void playClickSound(@NotNull Player player) {
        if (plugin.getConfig().getBoolean("effect.sound.onclick")) {
            player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 80.f, 1.0f);
        }
    }

    private void postControlPanel(PlayerInteractEvent e) {
        final Block b = e.getClickedBlock();
        final Player p = e.getPlayer();
        if (Util.isWallSign(b.getType())) {
            final Block block;
            if (Util.isWallSign(b.getType())) {
                block = Util.getAttached(b);
            } else {
                block = e.getClickedBlock();
            }
            Shop controlPanelShop = plugin.getShopManager().getShop(Objects.requireNonNull(block).getLocation());
            if (controlPanelShop != null && (controlPanelShop.getOwner().equals(p.getUniqueId()) || QuickShop.getPermissionManager().hasPermission(p, "quickshop.other.control"))) {
                MsgUtil.sendControlPanelInfo(p, Objects.requireNonNull(plugin.getShopManager().getShop(block.getLocation())));
                this.playClickSound(e.getPlayer());
                Objects.requireNonNull(plugin.getShopManager().getShop(block.getLocation())).setSignText();
            }
        }
    }

    private void postTrade(PlayerInteractEvent e) {
        final Block b = e.getClickedBlock();
        final Player p = e.getPlayer();
        if (b == null) {
            return;
        }
        if (!Util.canBeShop(b) && !Util.isWallSign(b.getType())) {
            return;
        }

        final Location loc = b.getLocation();
        final ItemStack item = e.getItem();
        // Get the shop
        Shop shop = plugin.getShopManager().getShop(loc);
        // If that wasn't a shop, search nearby shops
        if (shop == null) {
            final Block attached;
            if (Util.isWallSign(b.getType())) {
                attached = Util.getAttached(b);
                if (attached != null) {
                    shop = plugin.getShopManager().getShop(attached.getLocation());
                }
            } else if (Util.isDoubleChest(b.getBlockData())) {
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
            if (!InteractUtil.check(InteractUtil.Action.TRADE, p.isSneaking())) {
                return;
            }
            shop.onClick();
            this.playClickSound(e.getPlayer());
            // Text menu
            MsgUtil.sendShopInfo(p, shop);
            shop.setSignText();

            final Economy eco = plugin.getEconomy();
            final double price = shop.getPrice();
            final double money = plugin.getEconomy().getBalance(p.getUniqueId(), shop.getLocation().getWorld(), shop.getCurrency());

            if (shop.isSelling()) {
                int itemAmount = Math.min(Util.countSpace(p.getInventory(), shop.getItem()), (int) Math.floor(money / price));
                if (!shop.isUnlimited()) {
                    itemAmount = Math.min(itemAmount, shop.getRemainingStock());
                }
                if (itemAmount < 0) {
                    itemAmount = 0;
                }
                if (shop.isStackingShop()) {
                    MsgUtil.sendMessage(p, MsgUtil.getMessage("how-many-buy-stack", p, Integer.toString(shop.getItem().getAmount()), Integer.toString(itemAmount)));
                } else {
                    MsgUtil.sendMessage(p, MsgUtil.getMessage("how-many-buy", p, Integer.toString(itemAmount)));
                }
            } else {
                final double ownerBalance = eco.getBalance(shop.getOwner(), shop.getLocation().getWorld(), shop.getCurrency());
                int items = Util.countItems(p.getInventory(), shop.getItem());
                final int ownerCanAfford = (int) (ownerBalance / shop.getPrice());

                if (!shop.isUnlimited()) {
                    // Amount check player amount and shop empty slot
                    items = Math.min(items, shop.getRemainingSpace());
                    // Amount check player selling item total cost and the shop owner's balance
                    items = Math.min(items, ownerCanAfford);
                } else if (plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners")) {
                    // even if the shop is unlimited, the config option pay-unlimited-shop-owners is set to
                    // true,
                    // the unlimited shop owner should have enough money.
                    items = Math.min(items, ownerCanAfford);
                }
                if (items < 0) {
                    items = 0;
                }
                if (shop.isStackingShop()) {
                    MsgUtil.sendMessage(p, MsgUtil.getMessage("how-many-sell-stack", p, Integer.toString(shop.getItem().getAmount()), Integer.toString(items)));
                } else {
                    MsgUtil.sendMessage(p, MsgUtil.getMessage("how-many-sell", p, Integer.toString(items)));
                }
            }
            // Add the new action
            Map<UUID, Info> actions = plugin.getShopManager().getActions();
            Info info = new Info(shop.getLocation(), ShopAction.BUY, null, null, shop);
            actions.put(p.getUniqueId(), info);
        }
        // Handles creating shops
        else if (e.useInteractedBlock() == Event.Result.ALLOW
                && shop == null
                && item != null
                && item.getType() != Material.AIR
                && QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.sell")
                && p.getGameMode() != GameMode.CREATIVE) {
            if (e.useInteractedBlock() == Event.Result.DENY
                    || !InteractUtil.check(InteractUtil.Action.CREATE, p.isSneaking())
                    || plugin.getConfig().getBoolean("shop.disable-quick-create")
                    || !plugin.getShopManager().canBuildShop(p, b, e.getBlockFace())) {
                // As of the new checking system, most plugins will tell the
                // player why they can't create a shop there.
                // So telling them a message would cause spam etc.
                return;
            }
            if (Util.isDoubleChest(b.getBlockData())
                    && !QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.double")) {
                MsgUtil.sendMessage(p, MsgUtil.getMessage("no-double-chests", p));
                return;
            }
            if (Util.isBlacklisted(item)
                    && !QuickShop.getPermissionManager()
                    .hasPermission(p, "quickshop.bypass." + item.getType().name())) {
                MsgUtil.sendMessage(p, MsgUtil.getMessage("blacklisted-item", p));
                return;
            }
            if (b.getType() == Material.ENDER_CHEST //FIXME: Need a better impl
                    && !QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.enderchest")) {
                return;
            }
            if (Util.isWallSign(b.getType())) {
                return;
            }
            // Finds out where the sign should be placed for the shop
            Block last = null;
            final Location from = p.getLocation().clone();

            from.setY(b.getY());
            from.setPitch(0);
            final BlockIterator bIt = new BlockIterator(from, 0, 7);

            while (bIt.hasNext()) {
                final Block n = bIt.next();
                if (n.equals(b)) {
                    break;
                }
                last = n;
            }
            // Send creation menu.
            final Info info = new Info(b.getLocation(), ShopAction.CREATE, e.getItem(), last);

            plugin.getShopManager().getActions().put(p.getUniqueId(), info);
            MsgUtil.sendMessage(p,
                    MsgUtil.getMessage(
                            "how-much-to-trade-for",
                            p,
                            Util.getItemStackName(Objects.requireNonNull(e.getItem())), Integer.toString(plugin.isAllowStack() && QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.stacks") ? item.getAmount() : 1)));
        }
    }

    enum InteractAction {
        TRADE,
        CONTROL_PANEL
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent e) {

        try {
            Location location;
            //for strange NPE from spigot API fix
            //noinspection ConstantConditions
            if (e.getInventory() == null) {
                return;
            }
            if (e.getInventory().getLocation() == null) {
                return;
            }
            location = e.getInventory().getLocation();

            if (location == null) {
                return; /// ignored as workaround, GH-303
            }
            final Shop shop = plugin.getShopManager().getShopIncludeAttached(location);
            if (shop != null) {
                shop.setSignText();
            }
        } catch (NullPointerException ignored) {
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        // Notify the player any messages they were sent
        if (plugin.getConfig().getBoolean("shop.auto-fetch-shop-messages")) {
            MsgUtil.flush(e.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e) {
        // Remove them from the menu
        plugin.getShopManager().getActions().remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        onMove(new PlayerMoveEvent(e.getPlayer(), e.getFrom(), e.getTo()));
    }

    /*
     * Waits for a player to move too far from a shop, then cancels the menu.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        final Info info = plugin.getShopManager().getActions().get(e.getPlayer().getUniqueId());
        if (info == null) {
            return;
        }
        final Player p = e.getPlayer();
        final Location loc1 = info.getLocation();
        final Location loc2 = p.getLocation();
        if (loc1.getWorld() != loc2.getWorld() || loc1.distanceSquared(loc2) > 25) {
            if (info.getAction() == ShopAction.BUY) {
                MsgUtil.sendMessage(p, MsgUtil.getMessage("shop-purchase-cancelled", p));
                Util.debugLog(p.getName() + " too far with the shop location.");
            } else if (info.getAction() == ShopAction.CREATE) {
                MsgUtil.sendMessage(p, MsgUtil.getMessage("shop-creation-cancelled", p));
                Util.debugLog(p.getName() + " too far with the shop location.");
            }
            plugin.getShopManager().getActions().remove(p.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDyeing(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null || !Util.isDyes(e.getItem().getType())) {
            return;
        }
        final Block block = e.getClickedBlock();
        if (block == null || !Util.isWallSign(block.getType())) {
            return;
        }
        final Block attachedBlock = Util.getAttached(block);
        if (attachedBlock == null || plugin.getShopManager().getShopIncludeAttached(attachedBlock.getLocation()) == null) {
            return;
        }
        e.setCancelled(true);
        Util.debugLog("Disallow " + e.getPlayer().getName() + " dye the shop sign.");
    }
}
