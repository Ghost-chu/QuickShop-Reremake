/*
 * This file is a part of project QuickShop, the name is InventoryPreview.java
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

package org.maxgamer.quickshop.shop;

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
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.event.ShopInventoryPreviewEvent;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.holder.QuickShopPreviewInventoryHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A class to create a GUI item preview quickly
 */
@EqualsAndHashCode
@ToString
public class InventoryPreview implements Listener {

    @Nullable
    private Inventory inventory;

    private ItemStack itemStack;

    private Player player;

    /**
     * Create a preview item GUI for a player.
     *
     * @param itemStack The item you want create.
     * @param player Target player.
     */
    public InventoryPreview(@NotNull ItemStack itemStack, @NotNull Player player) {
        this.itemStack = itemStack.clone();
        this.player = player;
        if (Objects.requireNonNull(this.itemStack.getItemMeta()).hasLore()) {
            ItemMeta itemMeta = this.itemStack.getItemMeta();
            List<String> lores = itemMeta.getLore();
            Objects.requireNonNull(lores).add(QuickShop.instance.getPreviewProtectionLore());
            itemMeta.setLore(lores);
            this.itemStack.setItemMeta(itemMeta);
        } else {
            ItemMeta itemMeta = this.itemStack.getItemMeta();
            List<String> lores = new ArrayList<>();
            lores.add(QuickShop.instance.getPreviewProtectionLore());
            itemMeta.setLore(lores);
            this.itemStack.setItemMeta(itemMeta);
        }
    }

    public static boolean isPreviewItem(@Nullable ItemStack stack) {
        if (stack == null) {
            return false;
        }
        if (!stack.hasItemMeta() || !stack.getItemMeta().hasLore()) {
            return false;
        }
        List<String> lores = stack.getItemMeta().getLore();
        for (String string : lores) {
            if (QuickShop.instance.getPreviewProtectionLore().equals(string)) {
                return true;
            }
        }
        return false;
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
        ShopInventoryPreviewEvent shopInventoryPreview =
            new ShopInventoryPreviewEvent(player, itemStack);
        Bukkit.getPluginManager().callEvent(shopInventoryPreview);
        if (shopInventoryPreview.isCancelled()) {
            Util.debugLog("Inventory preview was canceled by a plugin.");
            return;
        }
        final int size = 9;
        inventory = Bukkit.createInventory(new QuickShopPreviewInventoryHolder(),size, MsgUtil.getMessage("menu.preview", player));
        for (int i = 0; i < size; i++) {
            inventory.setItem(i, itemStack);
        }
        player.openInventory(inventory);
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

}
