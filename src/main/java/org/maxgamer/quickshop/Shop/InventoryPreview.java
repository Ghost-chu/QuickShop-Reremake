package org.maxgamer.quickshop.Shop;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Event.ShopInventoryPreviewEvent;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to create a GUI item preview quickly
 */
@EqualsAndHashCode
@ToString
public class InventoryPreview implements Listener {

    public static boolean isPreviewItem(@Nullable ItemStack stack) {
        if (stack == null) {
            return false;
        }
        if (!stack.hasItemMeta() || !stack.getItemMeta().hasLore()) {
            return false;
        }
        List<String> lores = stack.getItemMeta().getLore();
        for (String string : lores) {
            if ("QuickShop GUI preview item".equals(string)) {
                return true;
            }
        }
        return false;
    }

    private Inventory inventory;
    private ItemStack itemStack;
    private Player player;

    /**
     * Create a preview item GUI for a player.
     *
     * @param itemStack The item you want create.
     * @param player    Target player.
     */
    public InventoryPreview(@NotNull ItemStack itemStack, @NotNull Player player) {
        this.itemStack = itemStack.clone();
        this.player = player;
        if (this.itemStack.getItemMeta().hasLore()) {
            ItemMeta itemMeta = this.itemStack.getItemMeta();
            List<String> lores = itemMeta.getLore();
            lores.add("QuickShop GUI preview item");
            itemMeta.setLore(lores);
            this.itemStack.setItemMeta(itemMeta);
        } else {
            ItemMeta itemMeta = this.itemStack.getItemMeta();
            List<String> lores = new ArrayList<>();
            lores.add("QuickShop GUI preview item");
            itemMeta.setLore(lores);
            this.itemStack.setItemMeta(itemMeta);
        }
    }

    public void close() {
        if (inventory == null) {
            return;
        }
        for (HumanEntity player : inventory.getViewers()) {
            player.closeInventory();
        }
        inventory = null; // Destory
    }

    /**
     * Open the preview GUI for player.
     */
    public void show() {
        if (inventory != null) // Not inited
        {
            close();
        }
        if (itemStack == null) // Null pointer exception
        {
            return;
        }
        if (player == null) // Null pointer exception
        {
            return;
        }
        if (player.isSleeping()) // Bed bug
        {
            return;
        }
        ShopInventoryPreviewEvent shopInventoryPreview = new ShopInventoryPreviewEvent(player, itemStack);
        Bukkit.getPluginManager().callEvent(shopInventoryPreview);
        if (shopInventoryPreview.isCancelled()) {
            Util.debugLog("Inventory preview was canceled by a plugin.");
            return;
        }
        final int size = 9;
        inventory = Bukkit.createInventory(null, size, MsgUtil.getMessage("menu.preview", player));
        for (int i = 0; i < size; i++) {
            inventory.setItem(i, itemStack);
        }
        player.openInventory(inventory);
    }
}
