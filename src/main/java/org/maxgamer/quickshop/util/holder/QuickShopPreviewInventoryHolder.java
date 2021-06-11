/*
 * This file is a part of project QuickShop, the name is QuickShopPreviewInventoryHolder.java
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

package org.maxgamer.quickshop.util.holder;

import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

@AllArgsConstructor
public class QuickShopPreviewInventoryHolder implements InventoryHolder, Inventory {
    private final ItemStack stack;
    private final int size;

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public int getMaxStackSize() {
        return 0;
    }

    @Override
    public void setMaxStackSize(int i) {

    }

    @Nullable
    @Override
    public ItemStack getItem(int i) {
        return null;
    }

    @Override
    public void setItem(int i, @Nullable ItemStack itemStack) {

    }

    @NotNull
    @Override
    public HashMap<Integer, ItemStack> addItem(@NotNull ItemStack... itemStacks) throws IllegalArgumentException {
        return new HashMap<>();
    }

    @NotNull
    @Override
    public HashMap<Integer, ItemStack> removeItem(@NotNull ItemStack... itemStacks) throws IllegalArgumentException {
        return new HashMap<>();
    }

    @NotNull
    @Override
    public ItemStack[] getContents() {
        return new ItemStack[]{stack};
    }

    @Override
    public void setContents(@NotNull ItemStack[] itemStacks) throws IllegalArgumentException {

    }

    @NotNull
    @Override
    public ItemStack[] getStorageContents() {
        return new ItemStack[]{stack};
    }

    @Override
    public void setStorageContents(@NotNull ItemStack[] itemStacks) throws IllegalArgumentException {

    }

    @Override
    public boolean contains(@NotNull Material material) throws IllegalArgumentException {
        return stack.getType().equals(material);
    }

    @Override
    public boolean contains(@Nullable ItemStack itemStack) {
        return stack.equals(itemStack);
    }

    @Override
    public boolean contains(@NotNull Material material, int i) throws IllegalArgumentException {
        return contains(material);
    }

    @Override
    public boolean contains(@Nullable ItemStack itemStack, int i) {
        return contains(itemStack);
    }

    @Override
    public boolean containsAtLeast(@Nullable ItemStack itemStack, int i) {
        return contains(itemStack);
    }

    @NotNull
    @Override
    public HashMap<Integer, ? extends ItemStack> all(@NotNull Material material) throws IllegalArgumentException {
        return new HashMap<>();
    }

    @NotNull
    @Override
    public HashMap<Integer, ? extends ItemStack> all(@Nullable ItemStack itemStack) {
        return new HashMap<>();
    }

    @Override
    public int first(@NotNull Material material) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public int first(@NotNull ItemStack itemStack) {
        return 0;
    }

    @Override
    public int firstEmpty() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void remove(@NotNull Material material) throws IllegalArgumentException {

    }

    @Override
    public void remove(@NotNull ItemStack itemStack) {

    }

    @Override
    public void clear(int i) {

    }

    @Override
    public void clear() {

    }

    @NotNull
    @Override
    public List<HumanEntity> getViewers() {
        return new ArrayList<>();
    }

    @NotNull
    @Override
    public InventoryType getType() {
        return InventoryType.HOPPER;
    }

    @Nullable
    @Override
    public InventoryHolder getHolder() {
        return null;
    }

    @NotNull
    @Override
    public ListIterator<ItemStack> iterator() {
        return iterator(0);
    }

    @NotNull
    @Override
    public ListIterator<ItemStack> iterator(int i) {
        return new ListIterator<ItemStack>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public ItemStack next() {
                return null;
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }

            @Override
            public ItemStack previous() {
                return null;
            }

            @Override
            public int nextIndex() {
                return 0;
            }

            @Override
            public int previousIndex() {
                return 0;
            }

            @Override
            public void remove() {

            }

            @Override
            public void set(ItemStack itemStack) {

            }

            @Override
            public void add(ItemStack itemStack) {

            }
        };
    }

    @Nullable
    @Override
    public Location getLocation() {
        return null;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this;
    }

    //private final UUID random = UUID.randomUUID(); //To let java know this is different with InventoryHolder (regular) in some stupid JDK/JRE


}
