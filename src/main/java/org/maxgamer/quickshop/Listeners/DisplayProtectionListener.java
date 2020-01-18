/*
 * This file is a part of project QuickShop, the name is DisplayProtectionListener.java
 * Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 * Copyright (C) Bukkit Commons Studio and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.maxgamer.quickshop.Listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.DisplayItem;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;


@SuppressWarnings("DuplicatedCode")
public class DisplayProtectionListener implements Listener {
    private QuickShop plugin;
    private boolean useEnhanceProtection;

    public DisplayProtectionListener(QuickShop plugin) {
        this.plugin = plugin;
        useEnhanceProtection = plugin.getConfig().getBoolean("shop.enchance-display-protect");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void block(BlockFromToEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        if (!useEnhanceProtection) {
            return;
        }
        Block targetBlock = event.getToBlock();
        Block shopBlock = targetBlock.getRelative(BlockFace.DOWN);
        Shop shop = plugin.getShopManager().getShopIncludeAttached(shopBlock.getLocation());
        if (shop == null) {
            return;
        }
        event.setCancelled(true);
        if (shop.getDisplay() != null) {
            shop.getDisplay().remove();
        }
        sendAlert("[DisplayGuard] Liuqid " + targetBlock.getLocation() + " trying flow to top of shop, QuickShop already cancel it.");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void portal(EntityPortalEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        if (!(event.getEntity() instanceof Item)) {
            return;
        }
        if (DisplayItem.checkIsGuardItemStack(((Item) event.getEntity()).getItemStack())) {
            event.setCancelled(true);
            event.getEntity().remove();
            sendAlert("[DisplayGuard] Somebody want dupe the display by Portal at " + event.getFrom() + " , QuickShop already cancel it.");
        }

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void block(BlockPistonExtendEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        if (!useEnhanceProtection) {
            return;
        }
        Block block = event.getBlock().getRelative(event.getDirection()).getRelative(BlockFace.DOWN);
        Shop shop = plugin.getShopManager().getShopIncludeAttached(block.getLocation());
        if (shop != null) {
            event.setCancelled(true);
            sendAlert("[DisplayGuard] Piston  " + event.getBlock().getLocation() + " trying push somethings on the shop top, QuickShop already cancel it.");
            if (shop.getDisplay() != null) {
                shop.getDisplay().remove();
            }
            return;
        }
        for (Block oBlock : event.getBlocks()) {
            Block otherBlock = oBlock.getRelative(event.getDirection()).getRelative(BlockFace.DOWN);
            if (Util.canBeShop(otherBlock)) {
                shop = plugin.getShopManager().getShopIncludeAttached(otherBlock.getLocation());
                if (shop != null) {
                    event.setCancelled(true);
                    sendAlert("[DisplayGuard] Piston  " + event.getBlock().getLocation() + " trying push somethings on the shop top, QuickShop already cancel it.");
                    if (shop.getDisplay() != null) {
                        shop.getDisplay().remove();
                    }
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void block(BlockPistonRetractEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        if (!useEnhanceProtection) {
            return;
        }
        Block block = event.getBlock().getRelative(event.getDirection()).getRelative(BlockFace.DOWN);
        Shop shop = plugin.getShopManager().getShopIncludeAttached(block.getLocation());
        if (shop != null) {
            event.setCancelled(true);
            sendAlert("[DisplayGuard] Piston  " + event.getBlock().getLocation() + " trying pull somethings on the shop top, QuickShop already cancel it.");
            if (shop.getDisplay() != null) {
                shop.getDisplay().remove();
            }
            return;
        }
        for (Block oBlock : event.getBlocks()) {
            Block otherBlock = oBlock.getRelative(event.getDirection()).getRelative(BlockFace.DOWN);
            if (Util.canBeShop(otherBlock)) {
                shop = plugin.getShopManager().getShopIncludeAttached(otherBlock.getLocation());
                if (shop != null) {
                    event.setCancelled(true);
                    sendAlert("[DisplayGuard] Piston  " + event.getBlock().getLocation() + " trying push somethings on the shop top, QuickShop already cancel it.");
                    if (shop.getDisplay() != null) {
                        shop.getDisplay().remove();
                    }
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void block(BrewingStandFuelEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        if (!useEnhanceProtection) {
            return;
        }
        ItemStack itemStack = event.getFuel();
        if (DisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            sendAlert("[DisplayGuard] Block  " + event.getBlock().getLocation()
                    + " trying fuel the BrewingStand with DisplayItem.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void block(FurnaceBurnEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        if (!useEnhanceProtection) {
            return;
        }
        ItemStack itemStack = event.getFuel();
        if (DisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Block furnace = event.getBlock();
            if (furnace.getState() instanceof Furnace) {
                Furnace furnace1 = (Furnace) furnace.getState();
                sendAlert("[DisplayGuard] Block  " + event.getBlock().getLocation()
                        + " trying burn with DisplayItem.");
                Util.inventoryCheck(furnace1.getInventory());

            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void block(FurnaceSmeltEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        if (!useEnhanceProtection) {
            return;
        }
        ItemStack itemStack = event.getSource();
        if (DisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Block furnace = event.getBlock();
            if (furnace.getState() instanceof Furnace) {
                Furnace furnace1 = (Furnace) furnace.getState();
                sendAlert("[DisplayGuard] Block  " + event.getBlock().getLocation()
                        + " trying smelt with DisplayItem.");
                Util.inventoryCheck(furnace1.getInventory());
            }
            return;
        }
        itemStack = event.getResult();
        if (DisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Block furnace = event.getBlock();
            if (furnace.getState() instanceof Furnace) {
                Furnace furnace1 = (Furnace) furnace.getState();
                sendAlert("[DisplayGuard] Block  " + event.getBlock().getLocation()
                        + " trying smelt with DisplayItem.");
                Util.inventoryCheck(furnace1.getInventory());
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void entity(EntityPickupItemEvent e) {
        if (ListenerHelper.isDisabled(e.getClass())) {
            return;
        }
        if (!useEnhanceProtection) {
            return;
        }
        ItemStack stack = e.getItem().getItemStack();
        if (!DisplayItem.checkIsGuardItemStack(stack)) {
            return;
        }
        e.setCancelled(true);
        // You shouldn't be able to pick up that...
        e.getItem().remove();
        sendAlert("[DisplayGuard] Entity " + e
                .getEntity().getName() + " # " + e.getEntity().getLocation() + " pickedup the displayItem, QuickShop already removed it.");

        Entity entity = e.getEntity();
        if (entity instanceof InventoryHolder) {
            Util.inventoryCheck(((InventoryHolder) entity).getInventory());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void entity(EntityDamageEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        if (!(event.getEntity() instanceof ArmorStand)) {
            return;
        }
        if (!DisplayItem.checkIsGuardItemStack(((ArmorStand) event.getEntity()).getItemInHand())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void entity(EntityDeathEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        if (!useEnhanceProtection) {
            return;
        }
        if (!(event.getEntity() instanceof ArmorStand)) {
            return;
        }
        if (!DisplayItem.checkIsGuardItemStack(((ArmorStand) event.getEntity()).getItemInHand())) {
            return;
        }
        event.setDroppedExp(0);
        event.getDrops().clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void entity(EntityInteractEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        if (!useEnhanceProtection) {
            return;
        }
        if (!(event.getEntity() instanceof ArmorStand)) {
            return;
        }
        if (!DisplayItem.checkIsGuardItemStack(((ArmorStand) event.getEntity()).getItemInHand())) {
            return;
        }
        event.setCancelled(true);
        Entity entity = event.getEntity();
        if (entity instanceof InventoryHolder) {
            Util.inventoryCheck(((InventoryHolder) entity).getInventory());
        }
        sendAlert("[DisplayGuard] Entity  " + event.getEntityType()
                .name() + " # " + event.getEntity().getLocation() + " trying interact the hold displayItem's entity.");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void inventory(InventoryOpenEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        Util.inventoryCheck(event.getInventory());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void inventory(InventoryClickEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        if (!useEnhanceProtection) {
            return;
        }
        if (!DisplayItem.checkIsGuardItemStack(event.getCurrentItem())) {
            return;
        }
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getClickedInventory().getLocation() == null) {
            return;
        }
        event.setCancelled(true);

        sendAlert("[DisplayGuard] Inventory " + event.getClickedInventory().getHolder() + " at" + event.getClickedInventory().getLocation() + " was clicked the displayItem, QuickShop already removed it.");
        event.getCurrentItem().setAmount(0);
        event.getCurrentItem().setType(Material.AIR);
        event.setResult(Result.DENY);
        Util.inventoryCheck(event.getInventory());

    }

//    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
//    public void inventory(InventoryMoveItemEvent event) {
//        if (ListenerHelper.isDisabled(event.getClass())) {
//            return;
//        }
//        try {
//            ItemStack is = event.getItem();
//            if (DisplayItem.checkIsGuardItemStack(is)) {
//                event.setCancelled(true); ;
//                sendAlert("[DisplayGuard] Inventory " + event.getInitiator()
//                        .getLocation().toString() + " trying moving displayItem, QuickShop already removed it.");
//                event.setItem(new ItemStack(Material.AIR));
//                Util.inventoryCheck(event.getDestination());
//                Util.inventoryCheck(event.getInitiator());
//                Util.inventoryCheck(event.getSource());
//            }
//        } catch (Exception e) {}
//    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void inventory(InventoryPickupItemEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        ItemStack itemStack = event.getItem().getItemStack();
        if (!DisplayItem.checkIsGuardItemStack(itemStack)) {
            return; //We didn't care that
        }
        @Nullable Location loc = event.getInventory().getLocation();
        @Nullable InventoryHolder holder = event.getInventory().getHolder();
        event.setCancelled(true);
        sendAlert("[DisplayGuard] Something  " + holder + " at " + loc + " trying pickup the DisplayItem,  you should teleport to that location and to check detail..");
        Util.inventoryCheck(event.getInventory());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void inventory(InventoryDragEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        if (!useEnhanceProtection) {
            return;
        }
        ItemStack itemStack = event.getCursor();
        if (DisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Util.inventoryCheck(event.getInventory());
            sendAlert("[DisplayGuard] Player  " + event.getWhoClicked()
                    .getName() + " trying use DisplayItem crafting.");
            return;
        }
        itemStack = event.getOldCursor();
        if (DisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Util.inventoryCheck(event.getInventory());
            sendAlert("[DisplayGuard] Player  " + event.getWhoClicked()
                    .getName() + " trying use DisplayItem crafting.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void inventory(InventoryCreativeEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        if (!useEnhanceProtection) {
            return;
        }
        ItemStack itemStack = event.getCursor();
        if (DisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Util.inventoryCheck(event.getInventory());
            sendAlert("[DisplayGuard] Player  " + event.getWhoClicked()
                    .getName() + " trying use DisplayItem crafting.");
            return;
        }
        itemStack = event.getCurrentItem();
        if (DisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Util.inventoryCheck(event.getInventory());
            sendAlert("[DisplayGuard] Player  " + event.getWhoClicked()
                    .getName() + " trying use DisplayItem crafting.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void item(PlayerItemHeldEvent e) {
        if (ListenerHelper.isDisabled(e.getClass())) {
            return;
        }
        if (!useEnhanceProtection) {
            return;
        }
        ItemStack stack = e.getPlayer().getInventory().getItemInMainHand();
        ItemStack stackOffHand = e.getPlayer().getInventory().getItemInOffHand();
        if (DisplayItem.checkIsGuardItemStack(stack)) {
            e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR, 0));
            // You shouldn't be able to pick up that...
            sendAlert("[DisplayGuard] Player " + e.getPlayer()
                    .getName() + " helded the displayItem, QuickShop already cancelled and removed it.");
            e.setCancelled(true);
            Util.inventoryCheck(e.getPlayer().getInventory());
        }
        if (DisplayItem.checkIsGuardItemStack(stackOffHand)) {
            e.getPlayer().getInventory().setItemInOffHand(new ItemStack(Material.AIR, 0));
            // You shouldn't be able to pick up that...
            sendAlert("[DisplayGuard] Player " + e.getPlayer()
                    .getName() + " helded the displayItem, QuickShop already cancelled and removed it.");
            e.setCancelled(true);
            Util.inventoryCheck(e.getPlayer().getInventory());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void item(ItemDespawnEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        ItemStack itemStack = event.getEntity().getItemStack();
        if (!DisplayItem.checkIsGuardItemStack(itemStack)) {
            return; //We didn't care that
        }
        event.setCancelled(true);
        //Util.debugLog("We canceled an Item from despawning because they are our display item.");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void player(CraftItemEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        if (!useEnhanceProtection) {
            return;
        }
        ItemStack itemStack;
        itemStack = event.getCurrentItem();
        if (DisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Util.inventoryCheck(event.getInventory());
            sendAlert("[DisplayGuard] Player  " + event.getWhoClicked()
                    .getName() + " trying use DisplayItem crafting.");
            return;
        }
        itemStack = event.getCursor();
        if (DisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Util.inventoryCheck(event.getInventory());
            sendAlert("[DisplayGuard] Player  " + event.getWhoClicked()
                    .getName() + " trying use DisplayItem crafting.");
        }
    }
//Player can't interact the item entity... of course
//    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
//    public void player(PlayerInteractEvent e) {
//        if (ListenerHelper.isDisabled(e.getClass())) {
//            return;
//        }
//        ItemStack stack = e.getItem();
//        if (!DisplayItem.checkIsGuardItemStack(stack)) {
//            return;
//        }
//        stack.setType(Material.AIR);
//        stack.setAmount(0);
//        // You shouldn't be able to pick up that...
//        e.setCancelled(true);
//        sendAlert("[DisplayGuard] Player " + ((Player) e)
//                .getName() + " using the displayItem, QuickShop already removed it.");
//        Util.inventoryCheck(e.getPlayer().getInventory());
//    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void player(PlayerFishEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        if (event.getState() != State.CAUGHT_ENTITY) {
            return;
        }
        if (event.getCaught() == null) {
            return;
        }
        if (event.getCaught().getType() != EntityType.DROPPED_ITEM) {
            return;
        }
        Item item = (Item) event.getCaught();
        ItemStack is = item.getItemStack();
        if (!DisplayItem.checkIsGuardItemStack(is)) {
            return;
        }
        //item.remove();
        event.getHook().remove();
        //event.getCaught().remove();
        event.setCancelled(true);
        sendAlert("[DisplayGuard] Player " + event.getPlayer()
                .getName() + " trying hook item use Fishing Rod, QuickShop already removed it.");
        Util.inventoryCheck(event.getPlayer().getInventory());


    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void player(PlayerBucketEmptyEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        if (!useEnhanceProtection) {
            return;
        }
        Block waterBlock = event.getBlockClicked().getRelative(event.getBlockFace());
        Shop shop = plugin.getShopManager().getShop(waterBlock.getRelative(BlockFace.DOWN).getLocation());
        if (shop == null) {
            return;
        }
        event.setCancelled(true);
        sendAlert("[DisplayGuard] Player  " + event.getPlayer()
                .getName() + " trying use water to move somethings on the shop top, QuickShop already remove it.");

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void player(PlayerArmorStandManipulateEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        if (!DisplayItem.checkIsGuardItemStack(event.getArmorStandItem())) {
            return;
        }
        event.setCancelled(true);
        Util.inventoryCheck(event.getPlayer().getInventory());
        sendAlert("[DisplayGuard] Player  " + event.getPlayer()
                .getName() + " trying mainipulate armorstand contains displayItem.");
    }

    private void sendAlert(@NotNull String msg) {
        if (!plugin.getConfig().getBoolean("send-display-item-protection-alert")) {
            return;
        }
        MsgUtil.sendGlobalAlert(msg);
    }

}

