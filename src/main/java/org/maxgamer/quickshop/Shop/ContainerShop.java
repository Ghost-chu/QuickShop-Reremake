/*
 * This file is a part of project QuickShop, the name is ContainerShop.java
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

package org.maxgamer.quickshop.Shop;

import com.lishid.openinv.OpenInv;
import lombok.EqualsAndHashCode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Event.*;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.DisplayItem.ArmorStandDisplayItem;
import org.maxgamer.quickshop.Shop.DisplayItem.DisplayItem;
import org.maxgamer.quickshop.Shop.DisplayItem.RealDisplayItem;
import org.maxgamer.quickshop.Shop.DisplayItem.VirtualDisplayItem;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

/**
 * ChestShop core
 */
@EqualsAndHashCode
public class ContainerShop implements Shop {
    private DisplayItem displayItem;
    @EqualsAndHashCode.Exclude
    private boolean isLoaded = false;
    @EqualsAndHashCode.Exclude
    private boolean isDeleted = false;

    @NotNull
    private final ItemStack item;

    @NotNull
    private final Location location;

    private ShopModerator moderator;
    private QuickShop plugin;
    private double price;
    private ShopType shopType;
    private boolean unlimited;

    private ContainerShop(@NotNull ContainerShop s) {
        this.displayItem = s.displayItem;
        this.shopType = s.shopType;
        this.item = s.item;
        this.location = s.location;
        this.plugin = s.plugin;
        this.unlimited = s.unlimited;
        this.moderator = s.moderator;
        this.price = s.price;
        this.isLoaded = s.isLoaded;
    }

    /**
     * Adds a new shop.
     *
     * @param location       The location of the chest block
     * @param price     The cost per item
     * @param item      The itemstack with the properties we want. This is .cloned, no
     *                  need to worry about references
     * @param moderator The modertators
     * @param type      The shop type
     * @param unlimited The unlimited
     */
    public ContainerShop(@NotNull Location location, double price, @NotNull ItemStack item, @NotNull ShopModerator moderator, boolean unlimited, @NotNull ShopType type) {
        this.location = location;
        this.price = price;
        this.moderator = moderator;
        this.item = item.clone();
        this.plugin = QuickShop.instance;
        this.item.setAmount(1);
        this.shopType = type;
        this.unlimited = unlimited;

        if (plugin.isDisplay()) {
            switch (DisplayItem.getNowUsing()) {
                case UNKNOWN:
                    Util.debugLog("Failed to create a ContainerShop displayItem, the type is unknown, fallback to RealDisplayItem");
                    this.displayItem = new RealDisplayItem(this);
                    break;
                case REALITEM:
                    this.displayItem = new RealDisplayItem(this);
                    break;
                case ARMORSTAND:
                    this.displayItem = new ArmorStandDisplayItem(this);
                    break;
                case VIRTUALITEM:
                    this.displayItem = new VirtualDisplayItem(this);
                    break;
                default:
                    Util.debugLog("Warning: Failed to create a ContainerShop displayItem, the type we didn't know, fallback to RealDisplayItem");
                    this.displayItem = new RealDisplayItem(this);
                    break;
            }
        } else {
            Util.debugLog("The display was disabled.");
        }

    }

    /**
     * Add an item to shops chest.
     *
     * @param item   The itemstack. The amount does not matter, just everything
     *               else
     * @param amount The amount to add to the shop.
     */
    @Override
    public void add(@NotNull ItemStack item, int amount) {
        if (this.unlimited) {
            return;
        }
        Inventory inv = this.getInventory();
        int remains = amount;
        while (remains > 0) {
            int stackSize = Math.min(remains, item.getMaxStackSize());
            item.setAmount(stackSize);
            Objects.requireNonNull(inv).addItem(item);
            remains -= stackSize;
        }
        this.setSignText();
    }

    /**
     * Returns the number of items this shop has in stock.
     *
     * @return The number of items available for purchase.
     */
    @Override
    public int getRemainingStock() {
        if (this.unlimited) {
            return -1;
        }
        return Util.countItems(this.getInventory(), this.getItem());
    }

    /**
     * Returns the number of free spots in the chest for the particular item.
     *
     * @return remaining space
     */
    @Override
    public int getRemainingSpace() {
        if (this.unlimited) {
            return -1;
        }
        return Util.countSpace(this.getInventory(), this.getItem());
    }

    /**
     * Returns true if the ItemStack matches what this shop is selling/buying
     *
     * @param item The ItemStack
     * @return True if the ItemStack is the same (Excludes amounts)
     */
    @Override
    public boolean matches(@Nullable ItemStack item) {
        return plugin.getItemMatcher().matches(this.item, item);
    }

    /**
     * @return The location of the shops chest
     */
    @Override
    public @NotNull Location getLocation() {
        return this.location;
    }

    /**
     * @return The price per item this shop is selling
     */
    @Override
    public double getPrice() {
        return this.price;
    }

    /**
     * Sets the price of the shop.
     *
     * @param price The new price of the shop.
     */
    @Override
    public void setPrice(double price) {
        ShopPriceChangeEvent event = new ShopPriceChangeEvent(this, this.price, price);
        if (Util.fireCancellableEvent(event)) {
            Util.debugLog("A plugin cancelled the price change event.");
            return;
        }
        this.price = price;
        setSignText();
        update();
    }

    /**
     * Upates the shop into the database.
     */
    @Override
    public void update() {
        ShopUpdateEvent shopUpdateEvent = new ShopUpdateEvent(this);
        if (Util.fireCancellableEvent(shopUpdateEvent)) {
            Util.debugLog("The Shop update action was canceled by a plugin.");
            return;
        }

        int x = this.getLocation().getBlockX();
        int y = this.getLocation().getBlockY();
        int z = this.getLocation().getBlockZ();
        String world = Objects.requireNonNull(this.getLocation().getWorld()).getName();
        int unlimited = this.isUnlimited() ? 1 : 0;
        try {
            plugin.getDatabaseHelper().updateShop(ShopModerator.serialize(this.moderator.clone()), this
                    .getItem(), unlimited, shopType.toID(), this.getPrice(), x, y, z, world);
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().log(Level.WARNING, "Could not update a shop in the database! Changes will revert after a reboot!");
        }
    }

    /**
     * @return The durability of the item
     */
    @Override
    public short getDurability() {
        return (short) ((Damageable) Objects.requireNonNull(this.item.getItemMeta())).getDamage();
    }

    /**
     * @return The name of the player who owns the shop.
     */
    @Override
    public @NotNull UUID getOwner() {
        return this.moderator.getOwner();
    }

    /**
     * @return Returns a dummy itemstack of the item this shop is selling.
     */
    @Override
    public @NotNull ItemStack getItem() {
        return item;
    }

    @Override
    public boolean addStaff(@NotNull UUID player) {
        boolean result = this.moderator.addStaff(player);
        update();
        if (result) {
            Bukkit.getPluginManager().callEvent(new ShopModeratorChangedEvent(this, this.moderator));
        }
        return result;
    }

    /**
     * Buys amount of item from Player p. Does NOT check our inventory, or
     * balances
     *
     * @param p      The player to buy from
     * @param amount The amount to buy
     */
    @Override
    public void buy(@NotNull Player p, int amount) {
        int amount1 = amount;
        if (amount1 < 0) {
            this.sell(p, -amount1);
        }
        if (this.isUnlimited()) {
            ItemStack[] contents = p.getInventory().getContents();
            for (int i = 0; amount1 > 0 && i < contents.length; i++) {
                ItemStack stack = contents[i];
                if (stack == null) {
                    continue; // No item
                }
                if (matches(stack)) {
                    int stackSize = Math.min(amount1, stack.getAmount());
                    stack.setAmount(stack.getAmount() - stackSize);
                    amount1 -= stackSize;
                }
            }
            // Send the players new inventory to them
            p.getInventory().setContents(contents);
            this.setSignText();
            // This should not happen.
            if (amount1 > 0) {
                plugin.getLogger().log(Level.WARNING, "Could not take all items from a players inventory on purchase! " + p
                        .getName() + ", missing: " + amount1 + ", item: " + Util.getItemStackName(this.getItem()) + "!");
            }
        } else {
            ItemStack[] playerContents = p.getInventory().getContents();
            Inventory chestInv = this.getInventory();
            for (int i = 0; amount1 > 0 && i < playerContents.length; i++) {
                ItemStack item = playerContents[i];
                if (item != null && this.matches(item)) {
                    // Copy it, we don't want to interfere
                    item = item.clone();
                    // Amount = total, item.getAmount() = how many items in the
                    // stack
                    int stackSize = Math.min(amount1, item.getAmount());
                    // If Amount is item.getAmount(), then this sets the amount
                    // to 0
                    // Else it sets it to the remainder
                    playerContents[i].setAmount(playerContents[i].getAmount() - stackSize);
                    // We can modify this, it is a copy.
                    item.setAmount(stackSize);
                    // Add the items to the players inventory
                    Objects.requireNonNull(chestInv).addItem(item);
                    amount1 -= stackSize;
                }
            }
            // Now update the players inventory.
            p.getInventory().setContents(playerContents);
            this.setSignText();
        }
    }

    @Override
    public boolean delStaff(@NotNull UUID player) {
        boolean result = this.moderator.delStaff(player);
        update();
        if (result) {
            Bukkit.getPluginManager().callEvent(new ShopModeratorChangedEvent(this, this.moderator));
        }
        return result;
    }

    /**
     * Deletes the shop from the list of shops and queues it for database
     * deletion
     *
     * @param fromMemory True if you are *NOT* iterating over this currently, *false if
     *                   you are iterating*
     */
    @Override
    public void delete(boolean fromMemory) {
        ShopDeleteEvent shopDeleteEvent = new ShopDeleteEvent(this, fromMemory);
        if (Util.fireCancellableEvent(shopDeleteEvent)) {
            Util.debugLog("Shop deletion was canceled because a plugin canceled it.");
            return;
        }
        // Unload the shop
        if (isLoaded) {
            this.onUnload();
        }
        isDeleted = true;
        // Delete the display item
        if (this.getDisplayItem() != null) {
            this.getDisplayItem().remove();
        }
        // Delete the signs around it
        for (Sign s : this.getSigns()) {
            s.getBlock().setType(Material.AIR);
        }
        // Delete it from the database
        int x = this.getLocation().getBlockX();
        int y = this.getLocation().getBlockY();
        int z = this.getLocation().getBlockZ();
        String world = Objects.requireNonNull(this.getLocation().getWorld()).getName();
        // Refund if necessary
        if (plugin.getConfig().getBoolean("shop.refund")) {
            plugin.getEconomy().deposit(this.getOwner(), plugin.getConfig().getDouble("shop.cost"));
        }
        if (fromMemory) {
            // Delete it from memory
            plugin.getShopManager().removeShop(this);
        } else {
            try {
                plugin.getShopManager().removeShop(this);
                plugin.getDatabaseHelper().removeShop(x, y, z, world);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public boolean isDelete() {
        return false;
    }

    @Override
    public void checkDisplay() {
        Util.debugLog("Checking the display...");
        if (!plugin.isDisplay()) {
            return;
        }
        if (!this.isLoaded) {
            Util.debugLog("Shop not loaded, skipping...");
            return;
        }
        if (this.displayItem == null) {
            Util.debugLog("Warning: DisplayItem is null, this shouldn't happend...");
            Util.debugLog("Call from: " + Thread.currentThread().getStackTrace()[2].getClassName() + "#" + Thread.currentThread()
                    .getStackTrace()[2].getMethodName() + "%" + Thread.currentThread().getStackTrace()[2].getLineNumber());
            return;
        }
        if (!this.displayItem.isSpawned()) {
            /* Not spawned yet. */
            Util.debugLog("Target item not spawned, spawning...");
            this.displayItem.spawn();
        } else {
            /* If not spawned, we didn't need check these, only check them when we need. */
            if (this.displayItem.checkDisplayNeedRegen()) {
                this.displayItem.fixDisplayNeedRegen();
            } else {/* If display was regened, we didn't need check it moved, performance! */
                if (this.displayItem.checkDisplayIsMoved()) {
                    this.displayItem.fixDisplayMoved();
                }
            }
        }
        /* Dupe is always need check, if enabled display */
        if (plugin.isDisplay()) {
            this.displayItem.removeDupe();
        }
    }

    @Override
    public void clearStaffs() {
        this.moderator.clearStaffs();
        Bukkit.getPluginManager().callEvent(new ShopModeratorChangedEvent(this, this.moderator));
        update();
    }

    /**
     * Removes an item from the shop.
     *
     * @param item   The itemstack. The amount does not matter, just everything
     *               else
     * @param amount The amount to remove from the shop.
     */
    @Override
    public void remove(@NotNull ItemStack item, int amount) {
        if (this.unlimited) {
            return;
        }
        Inventory inv = this.getInventory();
        int remains = amount;
        while (remains > 0) {
            int stackSize = Math.min(remains, item.getMaxStackSize());
            item.setAmount(stackSize);
            Objects.requireNonNull(inv).removeItem(item);
            remains -= stackSize;
        }
        this.setSignText();
    }

    /**
     * Changes the owner of this shop to the given player.
     *
     * @param owner the new owner
     */
    @Override
    public void setOwner(@NotNull UUID owner) {
        this.moderator.setOwner(owner);
        Bukkit.getPluginManager().callEvent(new ShopModeratorChangedEvent(this, this.moderator));
        this.setSignText();
        update();
    }

    /**
     * @return The list of players who can manage the shop.
     */
    @NotNull
    @Override
    public ArrayList<UUID> getStaffs() {
        return this.moderator.getStaffs();
    }

    /**
     * Returns a clone of this shop. References to the same display item,
     * itemstack, location and owner as this shop does. Do not modify them or
     * you will modify this shop.
     * <p>
     * **NOT A DEEP CLONE**
     */
    @Override
    public @NotNull ContainerShop clone() {
        return new ContainerShop(this);
    }

    /**
     * Sells amount of item to Player p. Does NOT check our inventory, or
     * balances
     *
     * @param p      The player to sell to
     * @param amount The amount to sell
     */
    @Override
    public void sell(@NotNull Player p, int amount) {
        if (amount < 0) {
            this.buy(p, -amount);
        }
        // Items to drop on floor
        ArrayList<ItemStack> floor = new ArrayList<>(5);
        Inventory pInv = p.getInventory();
        if (this.isUnlimited()) {
            ItemStack item = this.item.clone();
            while (amount > 0) {
                int stackSize = Math.min(amount, this.item.getMaxStackSize());
                item.setAmount(stackSize);
                pInv.addItem(item);
                amount -= stackSize;
            }
        } else {
            ItemStack[] chestContents = Objects.requireNonNull(this.getInventory()).getContents();
            for (int i = 0; amount > 0 && i < chestContents.length; i++) {
                // Can't clone it here, it could be null
                ItemStack item = chestContents[i];
                if (item != null && this.matches(item)) {
                    // Copy it, we don't want to interfere
                    item = item.clone();
                    // Amount = total, item.getAmount() = how many items in the
                    // stack
                    int stackSize = Math.min(amount, item.getAmount());
                    // If Amount is item.getAmount(), then this sets the amount
                    // to 0
                    // Else it sets it to the remainder
                    chestContents[i].setAmount(chestContents[i].getAmount() - stackSize);
                    // We can modify this, it is a copy.
                    item.setAmount(stackSize);
                    // Add the items to the players inventory
                    floor.addAll(pInv.addItem(item).values());
                    amount -= stackSize;
                }
            }
            // We now have to update the chests inventory manually.
            this.getInventory().setContents(chestContents);
            this.setSignText();
        }
        for (ItemStack stack : floor) {
            p.getWorld().dropItem(p.getLocation(), stack);
        }
    }

    /**
     * Returns the shop that shares it's inventory with this one.
     *
     * @return the shop that shares it's inventory with this one. Will return
     * null if this shop is not attached to another.
     */
    public @Nullable ContainerShop getAttachedShop() {
        Block c = Util.getSecondHalf(this.getLocation().getBlock());
        if (c == null) {
            return null;
        }
        Shop shop = plugin.getShopManager().getShop(c.getLocation());
        return shop == null ? null : (ContainerShop) shop;
    }

    /**
     * Returns the display item associated with this shop.
     *
     * @return The display item associated with this shop.
     */
    public @Nullable DisplayItem getDisplayItem() {
        return this.displayItem;
    }

    /**
     * @return The enchantments the shop has on its items.
     */
    public @NotNull Map<Enchantment, Integer> getEnchants() {
        return Objects.requireNonNull(this.item.getItemMeta()).getEnchants();
    }

    /**
     * @return The chest this shop is based on.
     */
    public @Nullable Inventory getInventory() {
        try {
            if (location.getBlock().getState().getType() == Material.ENDER_CHEST && plugin.getOpenInvPlugin() != null) {
                OpenInv openInv = ((OpenInv) plugin.getOpenInvPlugin());
                return openInv.getSpecialEnderChest(Objects.requireNonNull(openInv.loadPlayer(Bukkit.getOfflinePlayer(this.moderator.getOwner()
                ))), Bukkit.getOfflinePlayer((this.moderator.getOwner())).isOnline()).getBukkitInventory();
            }
        } catch (Exception e) {
            Util.debugLog(e.getMessage());
            return null;
        }
        InventoryHolder container;
        try {
            container = (InventoryHolder) this.location.getBlock().getState();
            return container.getInventory();
        } catch (Exception e) {
            this.onUnload();
            this.delete();
            Util.debugLog("Inventory doesn't exist anymore: " + this + " shop was removed.");
            return null;
        }
    }

    /**
     * @return The ItemStack type of this shop
     */
    public @NotNull Material getMaterial() {
        return this.item.getType();
    }

    /**
     * Changes all lines of text on a sign near the shop
     *
     * @param lines The array of lines to change. Index is line number.
     */
    @Override
    public void setSignText(@NotNull String[] lines) {
        for (Sign sign : this.getSigns()) {
            if (Arrays.equals(sign.getLines(), lines)) {
                Util.debugLog("Skipped new sign text setup: Same content");
                continue;
            }
            for (int i = 0; i < lines.length; i++) {
                sign.setLine(i, lines[i]);
            }
            sign.update(true);
        }
    }

    @Override
    public void setUnlimited(boolean unlimited) {
        this.unlimited = unlimited;
        this.setSignText();
        update();
    }

    @Override
    public boolean isUnlimited() {
        return this.unlimited;
    }

    @Override
    public @NotNull ShopType getShopType() {
        return this.shopType;
    }

    @Override
    public boolean isBuying() {
        return this.shopType == ShopType.BUYING;
    }

    @Override
    public boolean isSelling() {
        return this.shopType == ShopType.SELLING;
    }

    /**
     * Changes a shop type to Buying or Selling. Also updates the signs nearby.
     *
     * @param shopType The new type (ShopType.BUYING or ShopType.SELLING)
     */
    @Override
    public void setShopType(@NotNull ShopType shopType) {
        this.shopType = shopType;
        this.setSignText();
        update();
    }

    /**
     * Updates signs attached to the shop
     */
    @Override
    public void setSignText() {
        if (!Util.isLoaded(this.location)) {
            return;
        }
        String[] lines = new String[4];
        OfflinePlayer player = Bukkit.getOfflinePlayer(this.getOwner());
        lines[0] = MsgUtil.getMessageOfflinePlayer("signs.header", player, this.ownerName());
        if (this.isSelling()) {
            if (this.getRemainingStock() == -1) {
                lines[1] = MsgUtil.getMessageOfflinePlayer("signs.selling", player, "" + MsgUtil.getMessageOfflinePlayer("signs.unlimited", player));
            } else {
                lines[1] = MsgUtil.getMessageOfflinePlayer("signs.selling", player, "" + this.getRemainingStock());
            }

        } else if (this.isBuying()) {
            if (this.getRemainingSpace() == -1) {
                lines[1] = MsgUtil.getMessageOfflinePlayer("signs.buying", player, "" + MsgUtil.getMessageOfflinePlayer("signs.unlimited", player));

            } else {
                lines[1] = MsgUtil.getMessageOfflinePlayer("signs.buying", player, "" + this.getRemainingSpace());
            }

        }
        lines[2] = MsgUtil.getMessageOfflinePlayer("signs.item", player, Util.getItemStackName(this.getItem()));
        lines[3] = MsgUtil.getMessageOfflinePlayer("signs.price", player, Util.format(this.getPrice()));
        this.setSignText(lines);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Shop " + (location.getWorld() == null ?
                "unloaded world" :
                location.getWorld().getName()) + "(" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")");
        sb.append(" Owner: ").append(this.ownerName()).append(" - ").append(getOwner());
        if (isUnlimited()) {
            sb.append(" Unlimited: true");
        }
        sb.append(" Price: ").append(getPrice());
        sb.append(" Item: ").append(getItem());
        return sb.toString();
    }

    /**
     * Returns a list of signs that are attached to this shop (QuickShop and
     * blank signs only)
     *
     * @return a list of signs that are attached to this shop (QuickShop and
     * blank signs only)
     */
    @Override
    public @NotNull List<Sign> getSigns() {
        List<Sign> signs = new ArrayList<>(1);
        if (this.getLocation().getWorld() == null) {
            return signs;
        }
        Block[] blocks = new Block[4];
        blocks[0] = location.getBlock().getRelative(BlockFace.EAST);
        blocks[1] = location.getBlock().getRelative(BlockFace.NORTH);
        blocks[2] = location.getBlock().getRelative(BlockFace.SOUTH);
        blocks[3] = location.getBlock().getRelative(BlockFace.WEST);
        OfflinePlayer player = Bukkit.getOfflinePlayer(this.getOwner());
        final String signHeader = MsgUtil.getMessageOfflinePlayer("sign.header", player, this.ownerName());

        for (Block b : blocks) {
            if (b == null) {
                plugin.getLogger().warning("Null signs in the queue, skipping");
                continue;
            }
            Material mat = b.getType();
            if (!Util.isWallSign(mat)) {
                continue;
            }
            if (!isAttached(b)) {
                continue;
            }
            if (!(b.getState() instanceof Sign)) {
                continue;
            }
            org.bukkit.block.Sign sign = (org.bukkit.block.Sign) b.getState();
            String[] lines = sign.getLines();
            for (int i = 0; i < lines.length; i++) {
                if (i == 0) {
                    if (lines[i].contains(signHeader)) {
                        signs.add(sign);
                        break;
                    }
                } else {
                    if (Arrays.stream(lines).anyMatch((str) -> !str.isEmpty())) {
                        break;
                    }
                }
            }
            signs.add(sign);
        }
//            if (currentLine.contains(signHeader) || currentLine.isEmpty()) {
//                signs.add(sign);
//            } else {
//                boolean text = false;
//                for (String s : sign.getLines()) {
//                    if (!s.isEmpty()) {
//                        text = true;
//                        break;
//                    }
//                }
//                if (!text) {
//                    signs.add(sign);
//                }
//            }
//        }
        return signs;
    }

    @Override
    public boolean isAttached(@NotNull Block b) {
        return this.getLocation().getBlock().equals(Util.getAttached(b));
    }

    /**
     * Deletes the shop from the list of shops and queues it for database
     */
    @Override
    public void delete() {
        delete(false);
    }

    /**
     * Returns true if this shop is a double chest, and the other half is
     * selling/buying the same as this is buying/selling.
     *
     * @return true if this shop is a double chest, and the other half is
     * selling/buying the same as this is buying/selling.
     */
    public boolean isDoubleShop() {
        ContainerShop nextTo = this.getAttachedShop();
        if (nextTo == null) {
            return false;
        }
        if (nextTo.matches(this.getItem())) {
            // They're both trading the same item
            // They're both buying or both selling => Not a double shop,
            // just two shops.
            // One is buying, one is selling.
            return this.getShopType() != nextTo.getShopType();
        } else {
            return false;
        }
    }

    /**
     * Different with isDoubleShop, this method only check the shop is created on the double chest.
     *
     * @return true if create on double chest.
     */
    public boolean isDoubleChestShop() {
        return Util.isDoubleChest(this.getLocation().getBlock());
    }

    /**
     * Check shop is or not still Valid.
     *
     * @return isValid
     */
    @Override
    public boolean isValid() {
        checkDisplay();
        return Util.canBeShop(this.getLocation().getBlock());
    }

    @Override
    public @Nullable DisplayItem getDisplay() {
        return this.displayItem;
    }

    /**
     * Check the container still there and we can keep use it.
     */
    public void checkContainer() {
        if(!this.isLoaded){
            return;
        }
        if (!Util.canBeShop(this.getLocation().getBlock())) {
            Util.debugLog("Shop at " + this.getLocation() + " container was missing, remove...");
            this.onUnload();
            this.delete();
        }
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    /**
     * Load ContainerShop.
     */
    @Override
    public void onLoad() {
        if (this.isLoaded) {
            Util.debugLog("Dupe load request, canceled.");
            return;
        }
        ShopLoadEvent shopLoadEvent = new ShopLoadEvent(this);
        if (Util.fireCancellableEvent(shopLoadEvent)) {
            return;
        }

        this.isLoaded = true;
        Objects.requireNonNull(plugin.getShopManager().getLoadedShops()).add(this);

        checkContainer();

        // check price restriction
        Entry<Double, Double> priceRestriction = Util.getPriceRestriction(this.getMaterial());

        if (priceRestriction != null) {
            if (price < priceRestriction.getKey()) {
                price = priceRestriction.getKey();
                this.update();
            } else if (price > priceRestriction.getValue()) {
                price = priceRestriction.getValue();
                this.update();
            }
        }
        checkDisplay();
    }

    /**
     * Unload ContainerShop.
     */
    @Override
    public void onUnload() {
        if (!this.isLoaded) {
            Util.debugLog("Dupe unload request, canceled.");
            return;
        }
        if (this.getDisplayItem() != null) {
            this.getDisplayItem().remove();
        }
        update();
        this.isLoaded = false;
        Objects.requireNonNull(plugin.getShopManager().getLoadedShops()).remove(this);
        ShopUnloadEvent shopUnloadEvent = new ShopUnloadEvent(this);
        Bukkit.getPluginManager().callEvent(shopUnloadEvent);
    }

    @Override
    public void onClick() {
        ShopClickEvent event = new ShopClickEvent(this);
        if (Util.fireCancellableEvent(event)) {
            Util.debugLog("Ignore shop click, because some plugin cancel it.");
            return;
        }
        this.setSignText();
        this.checkDisplay();
    }

    @Override
    public @NotNull String ownerName() {
        if (this.isUnlimited()) {
            return MsgUtil.getMessageOfflinePlayer("admin-shop", Bukkit.getOfflinePlayer(this.getOwner()));
        }
        String name = Bukkit.getOfflinePlayer(this.getOwner()).getName();
        if (name == null || name.isEmpty()) {
            return MsgUtil.getMessageOfflinePlayer("unknown-owner", Bukkit.getOfflinePlayer(this.getOwner()));
        }
        return name;
    }

    @Override
    public @NotNull ShopModerator getModerator() {
        return this.moderator;
    }

    @Override
    public void setModerator(ShopModerator shopModerator) {
        this.moderator = shopModerator;
        update();
        Bukkit.getPluginManager().callEvent(new ShopModeratorChangedEvent(this, this.moderator));
    }

    @Override
    public boolean isLoaded() {
        return this.isLoaded;
    }

}
