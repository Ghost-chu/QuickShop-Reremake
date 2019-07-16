package org.maxgamer.quickshop.Listeners;

import lombok.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.entity.*;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.DisplayItem;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

@AllArgsConstructor
public class DisplayProtectionListener implements Listener {
    private QuickShop plugin;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void block(BlockFromToEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        Block targetBlock = event.getToBlock();
        Block shopBlock = targetBlock.getRelative(BlockFace.DOWN);
        Shop shop = plugin.getShopManager().getShop(shopBlock.getLocation());
        if (shop == null)
            return;
        Block anotherBlock = Util.getAttached(shopBlock);
        if (anotherBlock == null)
            return;
        shop = plugin.getShopManager().getShop(anotherBlock.getLocation());
        if (shop == null)
            return;
        event.setCancelled(true);
        MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Liuqid " + targetBlock
                .toString() + " trying flow to top of shop, QuickShop already cancel it.");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void block(BlockPistonExtendEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        Block block = event.getBlock().getRelative(event.getDirection()).getRelative(BlockFace.DOWN);
        Shop shop = plugin.getShopManager().getShop(block.getLocation());
        if (shop != null) {
            event.setCancelled(true);
            MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Piston  " + event.getBlock()
                    .toString() + " trying push somethings on the shop top, QuickShop already cancel it.");
            return;
        }
        for (Block oBlock : event.getBlocks()) {
            Block otherBlock = oBlock.getRelative(event.getDirection()).getRelative(BlockFace.DOWN);
            if (Util.canBeShop(otherBlock)) {
                shop = plugin.getShopManager().getShop(otherBlock.getLocation());
                if (shop != null) {
                    event.setCancelled(true);
                    MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Piston  " + event.getBlock()
                            .toString() + " trying push somethings on the shop top, QuickShop already cancel it.");
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void block(BlockPistonRetractEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        Block block = event.getBlock().getRelative(event.getDirection()).getRelative(BlockFace.DOWN);
        Shop shop = plugin.getShopManager().getShop(block.getLocation());
        if (shop != null) {
            event.setCancelled(true);
            MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Piston  " + event.getBlock()
                    .toString() + " trying pull somethings on the shop top, QuickShop already cancel it.");
            return;
        }
        for (Block oBlock : event.getBlocks()) {
            Block otherBlock = oBlock.getRelative(event.getDirection()).getRelative(BlockFace.DOWN);
            if (Util.canBeShop(otherBlock)) {
                shop = plugin.getShopManager().getShop(otherBlock.getLocation());
                if (shop != null) {
                    event.setCancelled(true);
                    MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Piston  " + event.getBlock()
                            .toString() + " trying push somethings on the shop top, QuickShop already cancel it.");
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void block(BrewingStandFuelEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        ItemStack itemStack = event.getFuel();
        if (DisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Block  " + event.getBlock().getLocation().toString()
                    + " trying fuel the BrewingStand with DisplayItem.");
            return;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void block(FurnaceBurnEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        ItemStack itemStack = event.getFuel();
        if (DisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Block furnace = event.getBlock();
            if (furnace.getState() instanceof Furnace) {
                Furnace furnace1 = (Furnace) furnace.getState();
                MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Block  " + event.getBlock().getLocation().toString()
                        + " trying burn with DisplayItem.");
                Util.inventoryCheck(furnace1.getInventory());
            }
            return;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void block(FurnaceSmeltEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        ItemStack itemStack = event.getSource();
        if (DisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Block furnace = event.getBlock();
            if (furnace.getState() instanceof Furnace) {
                Furnace furnace1 = (Furnace) furnace.getState();
                MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Block  " + event.getBlock().getLocation().toString()
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
                MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Block  " + event.getBlock().getLocation().toString()
                        + " trying smelt with DisplayItem.");
                Util.inventoryCheck(furnace1.getInventory());
            }
            return;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void entity(EntityPickupItemEvent e) {
        if (ListenerHelper.isDisabled(e.getClass()))
            return;
        ItemStack stack = e.getItem().getItemStack();
        if (!DisplayItem.checkIsGuardItemStack(stack))
            return;
        e.setCancelled(true);
        // You shouldn't be able to pick up that...
        e.getItem().remove();
        if (e.getEntityType() != EntityType.PLAYER) {
            e.getEntity().setCanPickupItems(false);
            MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Entity " + e.getEntity()
                    .getType().name() + " pickedup the displayItem, QuickShop already removed it.");
        } else {
            MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Player " + e
                    .getEntity().getType().name() + " pickedup the displayItem, QuickShop already removed it.");
        }
        Entity entity = e.getEntity();
        if (entity instanceof InventoryHolder)
            Util.inventoryCheck(((InventoryHolder) entity).getInventory());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void entity(EntityDamageEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        if (!(event.getEntity() instanceof ArmorStand))
            return;
        if (!DisplayItem.checkIsGuardItemStack(((ArmorStand) event.getEntity()).getItemInHand()))
            return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void entity(EntityDeathEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        if (!(event.getEntity() instanceof ArmorStand))
            return;
        if (!DisplayItem.checkIsGuardItemStack(((ArmorStand) event.getEntity()).getItemInHand()))
            return;
        event.setDroppedExp(0);
        event.getDrops().clear();
    }

    @EventHandler(ignoreCancelled =, priority = EventPriority.HIGHEST)
    public void entity(EntityInteractEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        if (!(event.getEntity() instanceof ArmorStand))
            return;
        if (!DisplayItem.checkIsGuardItemStack(((ArmorStand) event.getEntity()).getItemInHand()))
            return;
        event.setCancelled(true);
        Entity entity = event.getEntity();
        if (entity instanceof InventoryHolder)
            Util.inventoryCheck(((InventoryHolder) entity).getInventory());
        MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Entity  " + event.getEntityType()
                .name() + " trying interact the hold displayItem's entity.");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void inventory(InventoryOpenEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        Util.inventoryCheck(event.getInventory());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void inventory(InventoryClickEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        if (!DisplayItem.checkIsGuardItemStack(event.getCurrentItem()))
            return;
        event.setCancelled(true);
        MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Inventory " + event.getClickedInventory()
                .toString() + " was clicked the displayItem, QuickShop already removed it.");
        event.getCurrentItem().setAmount(0);
        event.getCurrentItem().setType(Material.AIR);
        event.setResult(Result.DENY);
        Util.inventoryCheck(event.getInventory());

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void inventory(InventoryMoveItemEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        try {
            ItemStack is = event.getItem();
            if (DisplayItem.checkIsGuardItemStack(is)) {
                event.setCancelled(true); ;
                MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Inventory " + event.getInitiator()
                        .toString() + " trying moving displayItem, QuickShop already removed it.");
                event.setItem(new ItemStack(Material.AIR));
                Util.inventoryCheck(event.getDestination());
                Util.inventoryCheck(event.getInitiator());
                Util.inventoryCheck(event.getSource());
            }
        } catch (Exception e) {}
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void inventory(InventoryPickupItemEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        ItemStack itemStack = event.getItem().getItemStack();
        if (!DisplayItem.checkIsGuardItemStack(itemStack))
            return; //We didn't care that
        event.setCancelled(true);
        MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Something  " + event.getInventory().getHolder()
                .toString() + " trying pickup the DisplayItem.");
        Util.inventoryCheck(event.getInventory());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void inventory(InventoryDragEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        ItemStack itemStack = event.getCursor();
        if (DisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Util.inventoryCheck(event.getInventory());
            MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Player  " + event.getWhoClicked()
                    .getName() + " trying use DisplayItem crafting.");
            return;
        }
        itemStack = event.getOldCursor();
        if (DisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Util.inventoryCheck(event.getInventory());
            MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Player  " + event.getWhoClicked()
                    .getName() + " trying use DisplayItem crafting.");
            return;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void inventory(InventoryCreativeEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        ItemStack itemStack = event.getCursor();
        if (DisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Util.inventoryCheck(event.getInventory());
            MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Player  " + event.getWhoClicked()
                    .getName() + " trying use DisplayItem crafting.");
            return;
        }
        itemStack = event.getCurrentItem();
        if (DisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Util.inventoryCheck(event.getInventory());
            MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Player  " + event.getWhoClicked()
                    .getName() + " trying use DisplayItem crafting.");
            return;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void item(PlayerItemHeldEvent e) {
        if (ListenerHelper.isDisabled(e.getClass()))
            return;
        ItemStack stack = e.getPlayer().getInventory().getItemInMainHand();
        ItemStack stackOffHand = e.getPlayer().getInventory().getItemInOffHand();
        if (DisplayItem.checkIsGuardItemStack(stack)) {
            e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR, 0));
            // You shouldn't be able to pick up that...
            MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Player " + e.getPlayer()
                    .getName() + " helded the displayItem, QuickShop already cancelled and removed it.");
            e.setCancelled(true);
            Util.inventoryCheck(e.getPlayer().getInventory());
        }
        if (DisplayItem.checkIsGuardItemStack(stackOffHand)) {
            e.getPlayer().getInventory().setItemInOffHand(new ItemStack(Material.AIR, 0));
            // You shouldn't be able to pick up that...
            MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Player " + e.getPlayer()
                    .getName() + " helded the displayItem, QuickShop already cancelled and removed it.");
            e.setCancelled(true);
            Util.inventoryCheck(e.getPlayer().getInventory());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void item(ItemDespawnEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        ItemStack itemStack = event.getEntity().getItemStack();
        if (!DisplayItem.checkIsGuardItemStack(itemStack))
            return; //We didn't care that
        event.setCancelled(true);
        //Util.debugLog("We canceled an Item from despawning because they are our display item.");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void player(CraftItemEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        ItemStack itemStack = event.getRecipe().getResult();
        itemStack = event.getCurrentItem();
        if (DisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Util.inventoryCheck(event.getInventory());
            MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Player  " + event.getWhoClicked()
                    .getName() + " trying use DisplayItem crafting.");
            return;
        }
        itemStack = event.getCursor();
        if (DisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Util.inventoryCheck(event.getInventory());
            MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Player  " + event.getWhoClicked()
                    .getName() + " trying use DisplayItem crafting.");
            return;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void player(PlayerInteractEvent e) {
        if (ListenerHelper.isDisabled(e.getClass()))
            return;
        ItemStack stack = e.getItem();
        if (!DisplayItem.checkIsGuardItemStack(stack))
            return;
        stack.setType(Material.AIR);
        stack.setAmount(0);
        // You shouldn't be able to pick up that...
        e.setCancelled(true);
        MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Player " + ((Player) e)
                .getName() + " using the displayItem, QuickShop already removed it.");
        Util.inventoryCheck(e.getPlayer().getInventory());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void player(PlayerFishEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        if (event.getState() != State.CAUGHT_ENTITY)
            return;
        if (event.getCaught().getType() != EntityType.DROPPED_ITEM)
            return;
        Item item = (Item) event.getCaught();
        ItemStack is = item.getItemStack();
        if (!DisplayItem.checkIsGuardItemStack(is))
            return;
        //item.remove();
        event.getHook().remove();
        //event.getCaught().remove();
        event.setCancelled(true);
        MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Player " + event.getPlayer()
                .getName() + " trying hook item use Fishing Rod, QuickShop already removed it.");
        Util.inventoryCheck(event.getPlayer().getInventory());

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void player(PlayerBucketEmptyEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        Block waterBlock = event.getBlockClicked().getRelative(event.getBlockFace());
        Shop shop = plugin.getShopManager().getShop(waterBlock.getRelative(BlockFace.DOWN).getLocation());
        if (shop == null)
            return;
        event.setCancelled(true);
        MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Player  " + event.getPlayer()
                .getName() + " trying use water to move somethings on the shop top, QuickShop already remove it.");

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void player(PlayerArmorStandManipulateEvent event) {
        if (ListenerHelper.isDisabled(event.getClass()))
            return;
        if (!DisplayItem.checkIsGuardItemStack(event.getArmorStandItem()))
            return;
        event.setCancelled(true);
        Util.inventoryCheck(event.getPlayer().getInventory());
        MsgUtil.sendGlobalAlert(Util.getClassPrefix() + "Player  " + event.getPlayer()
                .getName() + " trying mainipulate armorstand contains displayItem.");
    }
}

