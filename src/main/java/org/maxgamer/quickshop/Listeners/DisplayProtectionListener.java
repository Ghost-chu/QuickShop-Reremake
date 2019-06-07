package org.maxgamer.quickshop.Listeners;

import lombok.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.DisplayItem;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

@AllArgsConstructor
public class DisplayProtectionListener implements Listener {
    private QuickShop plugin;

    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory() == null)
            return;
        if (event.getInventory().getStorageContents() == null)
            return;
        for (ItemStack is : event.getInventory().getStorageContents()) {
            if (is == null)
                continue;
            if (DisplayItem.checkIsGuardItemStack(is)) {
                is.setType(Material.AIR);
                is.setAmount(1);
                event.getPlayer().closeInventory();
                //Util.inventoryCheck(event.getInventory());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void heldItem(PlayerItemHeldEvent e) {
        ItemStack stack = e.getPlayer().getInventory().getItemInMainHand();
        ItemStack stackOffHand = e.getPlayer().getInventory().getItemInOffHand();
        try {
            if (DisplayItem.checkIsGuardItemStack(stack)) {
                e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR, 0));
                // You shouldn't be able to pick up that...
                MsgUtil.sendExploitAlert(e.getPlayer(), "Player Inventory Scan", e.getPlayer().getLocation());
                Util.debugLog("Something trying collect QuickShop displayItem, already cancelled. (" + e.getPlayer().getLocation()
                        .toString() + ")");
                Util.inventoryCheck(e.getPlayer().getInventory());
            }
            if (DisplayItem.checkIsGuardItemStack(stackOffHand)) {
                e.getPlayer().getInventory().setItemInOffHand(new ItemStack(Material.AIR, 0));
                // You shouldn't be able to pick up that...
                MsgUtil.sendExploitAlert(e.getPlayer(), "Player Inventory Scan", e.getPlayer().getLocation());
                Util.debugLog("Something trying collect QuickShop displayItem, already cancelled. (" + e.getPlayer().getLocation()
                        .toString() + ")");
                Util.inventoryCheck(e.getPlayer().getInventory());
            }
        } catch (NullPointerException ex) {

        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickup(EntityPickupItemEvent e) {
        ItemStack stack = e.getItem().getItemStack();
        try {
            if (DisplayItem.checkIsGuardItemStack(stack)) {
                e.setCancelled(true);
                Util.debugLog("Something trying collect QuickShop displayItem, already cancelled. (" + e.getEntity().getLocation()
                        .toString() + ")");
                // You shouldn't be able to pick up that...
                e.getItem().remove();
                e.getEntity().setCanPickupItems(false);
                MsgUtil.sendExploitAlert(e.getEntity(), "Player Inventory Scan", e.getEntity().getLocation());
                if (e.getEntityType() != EntityType.PLAYER) {
                    Util.debugLog("A entity at " + e.getEntity().getLocation().toString() + " named " + e.getEntity()
                            .getCustomName() + "(" + e.getEntityType()
                            .name() + " trying pickup item, already banned this entity item pickup ability.");
                }

            }
        } catch (NullPointerException ex) {
        } // if meta/displayname/stack is null. We don't really care in that case.
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerClick(PlayerInteractEvent e) {
        ItemStack stack = e.getPlayer().getInventory().getItemInMainHand();
        ItemStack stackOffHand = e.getPlayer().getInventory().getItemInOffHand();
        boolean found = false;
        try {
            if (DisplayItem.checkIsGuardItemStack(stack)) {
                stack.setType(Material.AIR);
                found = true;
                // You shouldn't be able to pick up that...
            }
            if (DisplayItem.checkIsGuardItemStack(stackOffHand)) {
                stack.setType(Material.AIR);
                found = true;
            }
            if (found) {
                e.setCancelled(true);
                MsgUtil.sendExploitAlert(e.getPlayer(), "Player Interact", e.getPlayer().getLocation());
                Util.debugLog("Something trying collect QuickShop displayItem, already cancelled. (" + e.getPlayer()
                        .getInventory().getLocation().toString() + ")");
                Util.inventoryCheck(e.getPlayer().getInventory());
            }
        } catch (NullPointerException ex) {
        } // if meta/displayname/stack is null. We don't really care in that case.
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        try {
            if (DisplayItem.checkIsGuardItemStack(event.getCurrentItem())) {
                event.setCancelled(true);
                MsgUtil.sendExploitAlert(event.getClickedInventory(), "Click the DisplayItem in Inventory", event.getViewers()
                        .get(0).getLocation());
                event.getCurrentItem().setAmount(0);
                event.getCurrentItem().setType(Material.AIR);
                event.setResult(Result.DENY);
                Util.debugLog("Something trying collect QuickShop displayItem, already cancelled. (" + event.getInventory()
                        .getLocation().toString() + ")");
                Util.inventoryCheck(event.getInventory());
            }
            if (DisplayItem.checkIsGuardItemStack(event.getCursor())) {
                event.setCancelled(true);
                MsgUtil.sendExploitAlert(event.getClickedInventory(), "Click the DisplayItem in Inventory", event.getViewers()
                        .get(0).getLocation());
                event.getCursor().setAmount(0);
                event.getCursor().setType(Material.AIR);
                event.setResult(Result.DENY);
                Util.debugLog("Something trying collect QuickShop displayItem, already cancelled. (" + event.getInventory()
                        .getLocation().toString() + ")");
                Util.inventoryCheck(event.getInventory());
            }

        } catch (Exception e) {}
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        try {
            ItemStack is = event.getItem().getItemStack();
            if (DisplayItem.checkIsGuardItemStack(is)) {
                event.setCancelled(true);
//				plugin.getLogger().warning("[Exploit alert] Inventory "+event.getInventory().getName()+" at "+event.getItem().getLocation()+" picked up display item "+is);
//				Util.sendMessageToOps(ChatColor.RED+"[QuickShop][Exploit alert] Inventory "+event.getView().getTitle()+" at "+event.getItem().getLocation()+" picked up display item "+is);
                MsgUtil.sendExploitAlert(event.getInventory(), "Pickup DisplayItem", event.getInventory().getLocation());
                Util.debugLog("Something trying collect QuickShop displayItem, already cancelled. (" + event.getInventory()
                        .getLocation().toString() + ")");
                event.getItem().remove();
                Util.inventoryCheck(event.getInventory());
            }
        } catch (Exception e) {}
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        try {
            ItemStack is = event.getItem();
            if (DisplayItem.checkIsGuardItemStack(is)) {
                event.setCancelled(true);
                Util.debugLog("Some inventory trying move QuickShop displayItem to another container, already cancelled.");
                MsgUtil.sendExploitAlert(event.getInitiator(), "Move DisplayItem", event.getInitiator().getLocation());
                event.setItem(new ItemStack(Material.AIR));
                Util.inventoryCheck(event.getDestination());
                Util.inventoryCheck(event.getInitiator());
                Util.inventoryCheck(event.getSource());
            }
        } catch (Exception e) {}
    }

    @EventHandler(ignoreCancelled = true)
    public void onFishingItem(PlayerFishEvent event) {
        if (event.getState() != State.CAUGHT_ENTITY) {
            return;
        }
        if (event.getCaught().getType() != EntityType.DROPPED_ITEM)
            return;
        try {
            Item item = (Item) event.getCaught();
            ItemStack is = item.getItemStack();
            if (DisplayItem.checkIsGuardItemStack(is)) {
                //item.remove();
                event.getHook().remove();
                //event.getCaught().remove();
                event.setCancelled(true);
                MsgUtil.sendExploitAlert(event.getPlayer(), "Fish DisplayItem", event.getPlayer().getLocation());
                Util.debugLog("A player trying use fishrod hook the displayitem, already cancelled.");
                Util.inventoryCheck(event.getPlayer().getInventory());
            }
        } catch (Exception e) {
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLiuqidFlowing(BlockFromToEvent event) {
        Block targetBlock = event.getToBlock();
        Block shopBlock = targetBlock.getRelative(BlockFace.DOWN);
        Shop shop = plugin.getShopManager().getShop(shopBlock.getLocation());
        if (shop == null) {
            return;
        }
        event.setCancelled(true);
        MsgUtil.sendExploitAlert("Liuqid: " + event.getBlock().getType().name(), "Liquid transport DisplayItem", event.getBlock()
                .getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        Block block = event.getBlock().getRelative(event.getDirection()).getRelative(BlockFace.DOWN);
        Shop shop = plugin.getShopManager().getShop(block.getLocation());
        if (shop != null) {
            event.setCancelled(true);
            MsgUtil.sendExploitAlert("Block: " + event.getBlock().getType().name(), "Piston Extend to push DisplayItem", event
                    .getBlock().getLocation());
            return;
        }
        for (Block oBlock : event.getBlocks()) {
            Block otherBlock = oBlock.getRelative(event.getDirection()).getRelative(BlockFace.DOWN);
            if (Util.canBeShop(otherBlock)) {
                shop = plugin.getShopManager().getShop(otherBlock.getLocation());
                if (shop != null) {
                    event.setCancelled(true);
                    MsgUtil.sendExploitAlert("Block: " + event.getBlock().getType()
                            .name(), "Piston Extend to push DisplayItem", event.getBlock().getLocation());
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        Block block = event.getBlock().getRelative(event.getDirection()).getRelative(BlockFace.DOWN);
        Shop shop = plugin.getShopManager().getShop(block.getLocation());
        if (shop != null) {
            event.setCancelled(true);
            MsgUtil.sendExploitAlert("Block: " + event.getBlock().getType().name(), "Piston Retract to push DisplayItem", event
                    .getBlock().getLocation());
            return;
        }
        for (Block oBlock : event.getBlocks()) {
            Block otherBlock = oBlock.getRelative(event.getDirection()).getRelative(BlockFace.DOWN);
            if (Util.canBeShop(otherBlock)) {
                shop = plugin.getShopManager().getShop(otherBlock.getLocation());
                if (shop != null) {
                    event.setCancelled(true);
                    MsgUtil.sendExploitAlert("Block: " + event.getBlock().getType()
                            .name(), "Piston Retract to push DisplayItem", event.getBlock().getLocation());
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Block waterBlock = event.getBlockClicked().getRelative(event.getBlockFace());
        Shop shop = plugin.getShopManager().getShop(waterBlock.getRelative(BlockFace.DOWN).getLocation());
        if (shop == null)
            return;
        event.setCancelled(true);
        MsgUtil.sendExploitAlert(event.getPlayer(), "Place water and use water to push DisplayItem", waterBlock.getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onTryPickOrPlaceItemWithArmorStand(PlayerArmorStandManipulateEvent event) {
        if (!DisplayItem.checkIsGuardItemStack(event.getArmorStandItem()))
            return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onActionTheArmorStand(EntityInteractEvent event) {
        if (!(event.getEntity() instanceof ArmorStand))
            return;
        if (!DisplayItem.checkIsGuardItemStack(((ArmorStand) event.getEntity()).getItemInHand()))
            return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorStandWasDamageing(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof ArmorStand))
            return;
        if (!DisplayItem.checkIsGuardItemStack(((ArmorStand) event.getEntity()).getItemInHand()))
            return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorStandBreaked(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof ArmorStand))
            return;
        if (!DisplayItem.checkIsGuardItemStack(((ArmorStand) event.getEntity()).getItemInHand()))
            return;
        event.setDroppedExp(0);
    }
    
}
