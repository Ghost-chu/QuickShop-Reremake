/*
 * This file is a part of project QuickShop, the name is DisplayItem.java
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

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.JsonUtil;
import org.maxgamer.quickshop.util.Util;

/**
 * @author Netherfoam A display item, that spawns a block above the chest and cannot be interacted
 * with.
 */
public abstract class DisplayItem {

    protected static final QuickShop plugin = QuickShop.getInstance();

    private static final Gson gson = JsonUtil.getGson();

    protected final ItemStack originalItemStack;

    protected final Shop shop;

    @Nullable
    protected ItemStack guardedIstack;

    private boolean pendingRemoval;

    protected DisplayItem(Shop shop) {
        this.shop = shop;
        this.originalItemStack = shop.getItem().clone();
        //this.originalItemStack.setAmount(1);
    }

    /**
     * Check the itemStack is contains protect flag.
     *
     * @param itemStack Target ItemStack
     * @return Contains protect flag.
     */
    public static boolean checkIsGuardItemStack(@Nullable final ItemStack itemStack) {
        Util.ensureThread(false);
        if (!plugin.isDisplay()) {
            return false;
        }
        if (getNowUsing() == DisplayType.VIRTUALITEM) {
            return false;
        }

        if (itemStack == null) {
            return false;
        }
        //    itemStack = itemStack.clone();
        //    itemStack.setAmount(1);
        if (!itemStack.hasItemMeta()) {
            return false;
        }
        ItemMeta iMeta = itemStack.getItemMeta();
        if (!iMeta.hasLore()) {
            return false;
        }
        String defaultMark = ShopProtectionFlag.getDefaultMark();
        //noinspection ConstantConditions
        for (String lore : iMeta.getLore()) {
            try {
                if (!lore.startsWith("{")) {
                    continue;
                }
                ShopProtectionFlag shopProtectionFlag = gson.fromJson(lore, ShopProtectionFlag.class);
                if (shopProtectionFlag == null) {
                    continue;
                }
                if (defaultMark.equals(ShopProtectionFlag.getMark())) {
                    return true;
                }
                if (shopProtectionFlag.getShopLocation() != null) {
                    return true;
                }
                if (shopProtectionFlag.getItemStackString() != null) {
                    return true;
                }
            } catch (JsonSyntaxException e) {
                // Ignore
            }
        }

        return false;
    }

    /**
     * Check the itemStack is target shop's display
     *
     * @param itemStack Target ItemStack
     * @param shop      Target shop
     * @return Is target shop's display
     */
    public static boolean checkIsTargetShopDisplay(@NotNull final ItemStack itemStack, @NotNull Shop shop) {
        if (!plugin.isDisplay()) {
            return false;
        }
        if (getNowUsing() == DisplayType.VIRTUALITEM) {
            return false;
        }

        if (!itemStack.hasItemMeta()) {
            return false;
        }
        ItemMeta iMeta = itemStack.getItemMeta();
        if (!iMeta.hasLore()) {
            return false;
        }
        String defaultMark = ShopProtectionFlag.getDefaultMark();
        String shopLocation = shop.getLocation().toString();
        String attachedShopLocation = (shop.getAttachedShop() == null ? "null" : shop.getAttachedShop().getLocation().toString());
        //noinspection ConstantConditions
        for (String lore : iMeta.getLore()) {
            try {
                if (!lore.startsWith("{")) {
                    continue;
                }
                ShopProtectionFlag shopProtectionFlag = gson.fromJson(lore, ShopProtectionFlag.class);
                if (shopProtectionFlag == null) {
                    continue;
                }
                if (!ShopProtectionFlag.getMark().equals(defaultMark)) {
                    continue;
                }
                if (shopProtectionFlag.getShopLocation().equals(shopLocation)
                || shopProtectionFlag.getShopLocation().equals(attachedShopLocation)) {
                    return true;
                }
            } catch (JsonSyntaxException e) {
                // Ignore
            }
        }
        return false;
    }

    /**
     * Get plugin now is using which one DisplayType
     *
     * @return Using displayType.
     */
    @NotNull
    public static DisplayType getNowUsing() {
        return DisplayType.fromID(plugin.getConfig().getInt("shop.display-type"));
    }

    /**
     * Create a new itemStack with protect flag.
     *
     * @param itemStack Old itemStack
     * @param shop      The shop
     * @return New itemStack with protect flag.
     */
    @NotNull
    public static ItemStack createGuardItemStack(@NotNull ItemStack itemStack, @NotNull Shop shop) {
        itemStack = itemStack.clone();
        //itemStack.setAmount(1);
        ItemMeta iMeta = itemStack.getItemMeta();
        if (iMeta == null) {
            iMeta = plugin.getServer().getItemFactory().getItemMeta(itemStack.getType());
        }
        if (iMeta == null) {
            Util.debugLog("ItemStack " + itemStack + " cannot getting or creating ItemMeta, failed to create guarded ItemStack.");
            return itemStack;
        }
        if (plugin.getConfig().getBoolean("shop.display-item-use-name")) {
            if (iMeta.hasDisplayName()) {
                iMeta.setDisplayName(iMeta.getDisplayName());
            } else {
                iMeta.setDisplayName(Util.getItemStackName(itemStack));
            }
        } else {
            iMeta.setDisplayName(null);
        }
        ShopProtectionFlag shopProtectionFlag = createShopProtectionFlag(itemStack, shop);
        String protectFlag = gson.toJson(shopProtectionFlag);
        iMeta.setLore(Lists.newArrayList(protectFlag));
        itemStack.setItemMeta(iMeta);
        return itemStack;
    }

    /**
     * Create the shop protection flag for display item.
     *
     * @param itemStack The item stack
     * @param shop      The shop
     * @return ShopProtectionFlag obj
     */
    @NotNull
    public static ShopProtectionFlag createShopProtectionFlag(
            @NotNull ItemStack itemStack, @NotNull Shop shop) {
        return new ShopProtectionFlag(shop.getLocation().toString(), Util.serialize(itemStack));
    }

    /**
     * Gets the original ItemStack (without protection mark, should same with shop trading item.
     *
     * @return ItemStack
     */
    @NotNull
    public ItemStack getOriginalItemStack() {
        return originalItemStack;
    }

    /**
     * Check the display is or not moved.
     *
     * @return Moved
     */
    public abstract boolean checkDisplayIsMoved();

    /**
     * Check the display is or not need respawn
     *
     * @return Need
     */
    public abstract boolean checkDisplayNeedRegen();

    /**
     * Check target Entity is or not a QuickShop display Entity.
     *
     * @param entity Target entity
     * @return Is or not
     */
    public abstract boolean checkIsShopEntity(Entity entity);

    /**
     * Fix the display moved issue.
     */
    public abstract void fixDisplayMoved();

    /**
     * Fix display need respawn issue.
     */
    public abstract void fixDisplayNeedRegen();

    /**
     * Remove the display entity.
     */
    public abstract void remove();

    /**
     * Remove this shop's display in the whole world.(Not whole server)
     *
     * @return Success
     */
    public abstract boolean removeDupe();

    /**
     * Respawn the displays, if it not exist, it will spawn new one.
     */
    public abstract void respawn();

    /**
     * Add the protect flags for entity or entity's hand item. Target entity will got protect by
     * QuickShop
     *
     * @param entity Target entity
     */
    public abstract void safeGuard(@NotNull Entity entity);

    /**
     * Spawn new Displays
     */
    public abstract void spawn();

    /**
     * Get the display entity
     *
     * @return Target entity
     */
    public abstract Entity getDisplay();

    /**
     * Get display should at location. Not display current location.
     *
     * @return Should at
     */
    public abstract Location getDisplayLocation();

    /**
     * Check the display is or not already spawned
     *
     * @return Spawned
     */
    public abstract boolean isSpawned();

    /**
     * Sets this display item should be remove
     */
    public void pendingRemoval() {
        pendingRemoval = true;
    }

    /**
     * Gets this display item should be remove
     *
     * @return the status
     */
    public boolean isPendingRemoval() {
        return pendingRemoval;
    }

}
