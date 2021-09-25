/*
 * This file is a part of project QuickShop, the name is InventoryPreview.java
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

package org.maxgamer.quickshop.shop;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.NamespacedKey;
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
import org.maxgamer.quickshop.util.holder.QuickShopPreviewGUIHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

/**
 * A class to create a GUI item preview quickly
 */
@EqualsAndHashCode
@ToString
public class InventoryPreview implements Listener {

    private final ItemStack itemStack;
    private final Player player;
    private final QuickShop plugin = QuickShop.getInstance();
    @Nullable
    private Inventory inventory;
    private final String previewStr;
    private static final NamespacedKey key = new NamespacedKey(QuickShop.getInstance(), "preview-item");
    ;

    /**
     * Create a preview item GUI for a player.
     *
     * @param itemStack The item you want create.
     * @param player    Target player.
     * @param plugin    The plugin instance.
     */
    public InventoryPreview(@NotNull QuickShop plugin, @NotNull ItemStack itemStack, @NotNull Player player) {
        Util.ensureThread(false);
        this.itemStack = itemStack.clone();
        this.player = player;
        ItemMeta itemMeta;
        if (itemStack.hasItemMeta()) {
            itemMeta = this.itemStack.getItemMeta();
        } else {
            itemMeta = plugin.getServer().getItemFactory().getItemMeta(itemStack.getType());
        }
        previewStr = plugin.text().of(player, "quickshop-gui-preview").forLocale();
        if (itemMeta != null) {
            if (itemMeta.hasLore()) {
                itemMeta.getLore().add(previewStr);
            } else {
                itemMeta.setLore(Collections.singletonList(previewStr));
            }

            itemMeta.getPersistentDataContainer().set(key, PreviewGuiPersistentDataType.INSTANCE, UUID.randomUUID());
            this.itemStack.setItemMeta(itemMeta);
        }
    }

    @Deprecated
    public static boolean isPreviewItem(@Nullable ItemStack stack) {
        if (stack == null) {
            return false;
        }
        if (!stack.hasItemMeta() || !stack.getItemMeta().hasLore()) {
            return false;
        }
        return stack.getItemMeta().getPersistentDataContainer().get(key, PreviewGuiPersistentDataType.INSTANCE) != null;
    }

    /**
     * Open the preview GUI for player.
     */
    public void show() {
        Util.ensureThread(false);
        if (itemStack == null || player == null || player.isSleeping()) // Null pointer exception
        {
            return;
        }
        ShopInventoryPreviewEvent shopInventoryPreview = new ShopInventoryPreviewEvent(player, itemStack);
        if (Util.fireCancellableEvent(shopInventoryPreview)) {
            Util.debugLog("Inventory preview was canceled by a plugin.");
            return;
        }
        final int size = 9;

        inventory = plugin.getServer().createInventory(new QuickShopPreviewGUIHolder(), size, MsgUtil.getMessage("menu.preview", player));
        for (int i = 0; i < size; i++) {
            inventory.setItem(i, itemStack);
        }
        player.openInventory(inventory);
    }

    public void close() {
        Util.ensureThread(false);
        if (inventory == null) {
            return;
        }

        for (HumanEntity player : new ArrayList<>(inventory.getViewers())) {
            player.closeInventory();
        }
        inventory = null; // Destroy
    }

}
