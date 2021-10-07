/*
 * This file is a part of project QuickShop, the name is Info.java
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
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.api.shop.Info;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.api.shop.ShopAction;

/**
 * A class contains shop's infomations
 */
@EqualsAndHashCode
@ToString
public class JavaInfo implements Info {
    private final Block last;
    private final Location loc;
    private final boolean dirty;
    private ShopAction action;
    private ItemStack item;
    private Shop shop;

    public JavaInfo(
            @NotNull Location loc,
            @NotNull ShopAction action,
            @Nullable ItemStack item,
            @Nullable Block last) {
        this.loc = loc;
        this.action = action;
        this.last = last;
        if (item != null) {
            this.item = item.clone();
        }
        this.dirty = true;
    }

    public JavaInfo(
            @NotNull Location loc,
            @NotNull ShopAction action,
            @Nullable ItemStack item,
            @Nullable Block last,
            @Nullable Shop shop) {
        this.loc = loc;
        this.action = action;
        this.last = last;
        if (item != null) {
            this.item = item.clone();
        }
        if (shop != null) {
            this.shop = shop.clone();
            this.dirty = shop.isDirty();
        } else {
            this.dirty = true;
        }
    }

    /**
     * @return ShopAction action, Get shop action.
     */
    public @NotNull ShopAction getAction() {
        return this.action;
    }

    public void setAction(@NotNull ShopAction action) {
        this.action = action;
    }

    /**
     * @return ItemStack iStack, Get Shop's selling/buying item's ItemStack.
     */
    public @NotNull ItemStack getItem() {
        return this.item;
    }

    /*
     * public Material getMaterial(){ return this.item.getType(); } public byte
     * getData(){ return this.getData(); }
     */

    /**
     * @return Location loc, Get shop's location,
     */
    public @NotNull Location getLocation() {
        return this.loc;
    }

    /**
     * @return Block signBlock, Get block of shop's sign, may return the null.
     */
    public @Nullable Block getSignBlock() {
        return this.last;
    }

    /**
     * Get shop is or not has changed.
     *
     * @param shop, The need checked with this shop.
     * @return hasChanged
     */
    public boolean hasChanged(@NotNull Shop shop) {
        if (this.shop.isUnlimited() != shop.isUnlimited()) {
            return true;
        }
        if (this.shop.getShopType() != shop.getShopType()) {
            return true;
        }
        if (!this.shop.getOwner().equals(shop.getOwner())) {
            return true;
        }
        if (this.shop.getPrice() != shop.getPrice()) {
            return true;
        }
        if (!this.shop.getLocation().equals(shop.getLocation())) {
            return true;
        }
        if (this.dirty != shop.isDirty()) {
            return false;
        }
        return !this.shop.matches(shop.getItem());
    }

}
