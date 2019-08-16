package org.maxgamer.quickshop.Listeners;

import lombok.*;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.InventoryPreview;

@AllArgsConstructor
public class CustomInventoryListener implements Listener {
    private QuickShop plugin;

    @EventHandler(ignoreCancelled = true)
    public void invEvent(InventoryInteractEvent e) {
        if (ListenerHelper.isDisabled(e.getClass())) {
            return;
        }
        Inventory inventory = e.getInventory();
        ItemStack[] stacks = inventory.getContents();
        for (ItemStack itemStack : stacks) {
            if (itemStack == null) {
                continue;
            }
            if (InventoryPreview.isPreviewItem(itemStack)) {
                e.setCancelled(true);
                e.setResult(Result.DENY);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void invEvent(InventoryMoveItemEvent e) {
        if (ListenerHelper.isDisabled(e.getClass())) {
            return;
        }
        if (InventoryPreview.isPreviewItem(e.getItem())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void invEvent(InventoryClickEvent e) {
        if (ListenerHelper.isDisabled(e.getClass())) {
            return;
        }
        if (InventoryPreview.isPreviewItem(e.getCursor())) {
            e.setCancelled(true);
            e.setResult(Result.DENY);
        }
        if (InventoryPreview.isPreviewItem(e.getCurrentItem())) {
            e.setCancelled(true);
            e.setResult(Result.DENY);
        }
    }

    @EventHandler
    public void invEvent(InventoryDragEvent e) {
        if (ListenerHelper.isDisabled(e.getClass())) {
            return;
        }
        if (InventoryPreview.isPreviewItem(e.getCursor())) {
            e.setCancelled(true);
            e.setResult(Result.DENY);
        }
        if (InventoryPreview.isPreviewItem(e.getOldCursor())) {
            e.setCancelled(true);
            e.setResult(Result.DENY);
        }

    }

    @EventHandler
    public void invEvent(InventoryPickupItemEvent e) {
        if (ListenerHelper.isDisabled(e.getClass())) {
            return;
        }
        Inventory inventory = e.getInventory();
        ItemStack[] stacks = inventory.getContents();
        for (ItemStack itemStack : stacks) {
            if (itemStack == null) {
                continue;
            }
            if (InventoryPreview.isPreviewItem(itemStack)) {
                e.setCancelled(true);
            }
        }
    }
}
