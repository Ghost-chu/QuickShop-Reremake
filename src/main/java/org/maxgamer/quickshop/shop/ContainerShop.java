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

package org.maxgamer.quickshop.shop;

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
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.event.*;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.PriceLimiter;
import org.maxgamer.quickshop.util.Util;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

/**
 * ChestShop core
 */
@EqualsAndHashCode
public class ContainerShop implements Shop {
    @NotNull
    private ItemStack item;

    @NotNull
    private final Location location;
    private final QuickShop plugin;
    @Nullable
    private DisplayItem displayItem;
    @EqualsAndHashCode.Exclude
    private volatile boolean isLoaded = false;
    @EqualsAndHashCode.Exclude
    private volatile boolean isDeleted = false;
    @EqualsAndHashCode.Exclude
    private volatile boolean createBackup = false;
    private ShopModerator moderator;
    private double price;

    private ShopType shopType;

    private boolean unlimited;

    private long lastChangedAt;

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
        this.lastChangedAt = System.currentTimeMillis();
        this.isDeleted = s.isDeleted;
        this.createBackup = s.createBackup;
    }

    /**
     * Adds a new shop.
     *
     * @param location  The location of the chest block
     * @param price     The cost per item
     * @param item      The itemstack with the properties we want. This is .cloned, no need to worry about
     *                  references
     * @param moderator The modertators
     * @param type      The shop type
     * @param unlimited The unlimited
     */
    public ContainerShop(
            @NotNull QuickShop plugin,
            @NotNull Location location,
            double price,
            @NotNull ItemStack item,
            @NotNull ShopModerator moderator,
            boolean unlimited,
            @NotNull ShopType type) {
        this.location = location;
        this.price = price;
        this.moderator = moderator;
        this.item = item.clone();
        this.plugin = plugin;
        if (!plugin.isAllowStack()) {
            this.item.setAmount(1);
        }
        this.shopType = type;
        this.unlimited = unlimited;
        initDisplayItem();
        this.lastChangedAt = System.currentTimeMillis();
    }

    private void initDisplayItem() {
        if (plugin.isDisplay()) {
            switch (DisplayItem.getNowUsing()) {
                case UNKNOWN:
                    Util.debugLog(
                            "Failed to create a ContainerShop displayItem, the type is unknown, fallback to RealDisplayItem");
                    this.displayItem = new RealDisplayItem(this);
                    break;
                case REALITEM:
                    this.displayItem = new RealDisplayItem(this);
                    break;
                case ARMORSTAND:
                    this.displayItem = new ArmorStandDisplayItem(this);
                    break;
                case VIRTUALITEM:
                    try {
                        this.displayItem = new VirtualDisplayItem(this);
                    } catch (Throwable e) {
                        Util.debugLog(e.getMessage());
                        Arrays.stream(e.getStackTrace()).forEach(ex -> Util.debugLog(ex.getClassName() + "#" + ex.getMethodName() + "#" + ex.getLineNumber()));
                        plugin.getConfig().set("shop.display-type", 0);
                        plugin.saveConfig();
                        this.displayItem = new RealDisplayItem(this);
                        //do not throw
                        plugin.getLogger().log(Level.SEVERE, "Failed to initialize VirtualDisplayItem, fallback to RealDisplayItem, are you using the latest version of ProtocolLib?", e);
                    }
                    break;
                default:
                    Util.debugLog(
                            "Warning: Failed to create a ContainerShop displayItem, the type we didn't know, fallback to RealDisplayItem");
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
     * @param item   The itemstack. The amount does not matter, just everything else
     * @param amount The amount to add to the shop.
     */
    @Override
    public void add(@NotNull ItemStack item, int amount) {
        if (this.unlimited) {
            return;
        }
        int itemMaxStackSize = Util.getItemMaxStackSize(item.getType());
        Inventory inv = this.getInventory();
        int remains = amount;
        while (remains > 0) {
            int stackSize = Math.min(remains, itemMaxStackSize);
            item.setAmount(stackSize);
            Objects.requireNonNull(inv).addItem(item);
            remains -= stackSize;
        }
        this.setSignText();
    }

    @Override
    public boolean addStaff(@NotNull UUID player) {
        this.lastChangedAt = System.currentTimeMillis();
        boolean result = this.moderator.addStaff(player);
        update();
        if (result) {
            Bukkit.getPluginManager().callEvent(new ShopModeratorChangedEvent(this, this.moderator));
        }
        return result;
    }

    /**
     * Buys amount of item from Player p. Does NOT check our inventory, or balances
     *
     * @param p      The player to buy from
     * @param amount The amount to buy
     */
    @Override
    public void buy(@NotNull Player p, int amount) {
        amount = amount * item.getAmount();
        if (amount < 0) {
            this.sell(p, -amount);
        }
        ItemStack[] contents = p.getInventory().getContents();
        if (this.isUnlimited()) {
            for (int i = 0; amount > 0 && i < contents.length; i++) {
                ItemStack stack = contents[i];
                if (stack == null || stack.getType() == Material.AIR) {
                    continue; // No item
                }
                if (matches(stack)) {
                    int stackSize = Math.min(amount, stack.getAmount());
                    stack.setAmount(stack.getAmount() - stackSize);
                    amount -= stackSize;
                }
            }
            // Send the players new inventory to them
            p.getInventory().setContents(contents);
            this.setSignText();
            // This should not happen.
            if (amount > 0) {
                plugin
                        .getLogger()
                        .log(
                                Level.WARNING,
                                "Could not take all items from a players inventory on purchase! "
                                        + p.getName()
                                        + ", missing: "
                                        + amount
                                        + ", item: "
                                        + Util.getItemStackName(this.getItem())
                                        + "!");
            }
        } else {
            Inventory chestInv = this.getInventory();
            for (int i = 0; amount > 0 && i < contents.length; i++) {
                ItemStack item = contents[i];
                if (item != null && this.matches(item)) {
                    // Copy it, we don't want to interfere
                    item = item.clone();
                    // Amount = total, item.getAmount() = how many items in the
                    // stack
                    int stackSize = Math.min(amount, item.getAmount());
                    // If Amount is item.getAmount(), then this sets the amount
                    // to 0
                    // Else it sets it to the remainder
                    contents[i].setAmount(contents[i].getAmount() - stackSize);
                    // We can modify this, it is a copy.
                    item.setAmount(stackSize);
                    // Add the items to the players inventory
                    Objects.requireNonNull(chestInv).addItem(item);
                    amount -= stackSize;
                }
            }
            // Now update the players inventory.
            p.getInventory().setContents(contents);
            this.setSignText();
        }
    }

    @Override
    public void checkDisplay() {
        if (!plugin.isDisplay() || !this.isLoaded) { // FIXME: Reinit scheduler on reloading config
            return;
        }

        if (this.displayItem == null) {
            Util.debugLog("Warning: DisplayItem is null, this shouldn't happend...");
            StackTraceElement traceElements = Thread.currentThread().getStackTrace()[2];
            Util.debugLog(
                    "Call from: "
                            + traceElements.getClassName()
                            + "#"
                            + traceElements.getMethodName()
                            + "%"
                            + traceElements.getLineNumber());
            return;
        }

        if (!this.displayItem.isSpawned()) {
            /* Not spawned yet. */
            Util.debugLog("Target item not spawned, spawning for shop " + this.getLocation());
            this.displayItem.spawn();
        } else {
            /* If not spawned, we didn't need check these, only check them when we need. */
            if (this.displayItem.checkDisplayNeedRegen()) {
                this.displayItem.fixDisplayNeedRegen();
            } else {
                /* If display was regened, we didn't need check it moved, performance! */
                if (this.displayItem.checkDisplayIsMoved()) {
                    this.displayItem.fixDisplayMoved();
                }
            }
        }

        /* Dupe is always need check, if enabled display */
        this.displayItem.removeDupe();
        // plugin.getDisplayDupeRemoverWatcher().add(this.displayItem);
    }

    @Override
    public void clearStaffs() {
        this.lastChangedAt = System.currentTimeMillis();
        this.moderator.clearStaffs();
        Bukkit.getPluginManager().callEvent(new ShopModeratorChangedEvent(this, this.moderator));
        update();
    }

    @Override
    public boolean delStaff(@NotNull UUID player) {
        this.lastChangedAt = System.currentTimeMillis();
        boolean result = this.moderator.delStaff(player);
        update();
        if (result) {
            Bukkit.getPluginManager().callEvent(new ShopModeratorChangedEvent(this, this.moderator));
        }
        return result;
    }

    /**
     * Deletes the shop from the list of shops and queues it for database
     */
    @Override
    public void delete() {
        delete(false);
    }

    /**
     * Deletes the shop from the list of shops and queues it for database deletion
     *
     * @param memoryOnly whether to delete from database
     */
    @Override
    public void delete(boolean memoryOnly) {
        this.lastChangedAt = System.currentTimeMillis();
        ShopDeleteEvent shopDeleteEvent = new ShopDeleteEvent(this, memoryOnly);
        if (Util.fireCancellableEvent(shopDeleteEvent)) {
            Util.debugLog("Shop deletion was canceled because a plugin canceled it.");
            return;
        }
        isDeleted = true;
        // Unload the shop
        if (isLoaded) {
            this.onUnload();
        }
        // Delete the signs around it
        for (Sign s : this.getSigns()) {
            s.getBlock().setType(Material.AIR);
        }
        // Delete it from the database
        // Refund if necessary
        if (plugin.getConfig().getBoolean("shop.refund")) {
            plugin.getEconomy().deposit(this.getOwner(), plugin.getConfig().getDouble("shop.cost"));
        }
        if (memoryOnly) {
            // Delete it from memory
            plugin.getShopManager().removeShop(this);
        } else {
            plugin.getShopManager().removeShop(this);
            plugin.getDatabaseHelper().removeShop(this);
        }
    }

    @Override
    public boolean isAttached(@NotNull Block b) {
        return this.getLocation().getBlock().equals(Util.getAttached(b));
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
        plugin.getShopContainerWatcher().scheduleCheck(this);
        // check price restriction


        if(plugin.getShopManager().getPriceLimiter().check(item,price) != PriceLimiter.Status.PASS){
            Entry<Double, Double> priceRestriction = Util.getPriceRestriction(this.getMaterial()); //TODO Adapt priceLimiter, also improve priceLimiter return a container
            if (priceRestriction != null) {
                if (price < priceRestriction.getKey()) {
                    this.lastChangedAt = System.currentTimeMillis();
                    price = priceRestriction.getKey();
                    this.update();
                } else if (price > priceRestriction.getValue()) {
                    this.lastChangedAt = System.currentTimeMillis();
                    price = priceRestriction.getValue();
                    this.update();
                }
            }
        }
        this.checkDisplay();
    }

    /**
     * @return The ItemStack type of this shop
     */
    public @NotNull Material getMaterial() {
        return this.item.getType();
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
        plugin.getShopManager().getLoadedShops().remove(this);
        ShopUnloadEvent shopUnloadEvent = new ShopUnloadEvent(this);
        Bukkit.getPluginManager().callEvent(shopUnloadEvent);
    }

    @Override
    public @NotNull String ownerName() {
        if (this.isUnlimited()) {
            return MsgUtil.getMessageOfflinePlayer(
                    "admin-shop", Bukkit.getOfflinePlayer(this.getOwner()));
        }
        String name = Bukkit.getOfflinePlayer(this.getOwner()).getName();
        if (name == null || name.isEmpty()) {
            return MsgUtil.getMessageOfflinePlayer(
                    "unknown-owner", Bukkit.getOfflinePlayer(this.getOwner()));
        }
        return name;
    }

    /**
     * Removes an item from the shop.
     *
     * @param item   The itemstack. The amount does not matter, just everything else
     * @param amount The amount to remove from the shop.
     */
    @Override
    public void remove(@NotNull ItemStack item, int amount) {
        if (this.unlimited) {
            return;
        }
        int itemMaxStackSize = Util.getItemMaxStackSize(item.getType());
        Inventory inv = this.getInventory();
        int remains = amount;
        while (remains > 0) {
            int stackSize = Math.min(remains, itemMaxStackSize);
            item.setAmount(stackSize);
            Objects.requireNonNull(inv).removeItem(item);
            remains -= stackSize;
        }
        this.setSignText();
    }

    /**
     * Sells amount of item to Player p. Does NOT check our inventory, or balances
     *
     * @param p      The player to sell to
     * @param amount The amount to sell
     */
    @Override
    public void sell(@NotNull Player p, int amount) {
        amount = item.getAmount() * amount;
        if (amount < 0) {
            this.buy(p, -amount);
        }
        // Items to drop on floor
        ArrayList<ItemStack> floor = new ArrayList<>(5);
        Inventory pInv = p.getInventory();
        int itemMaxStackSize = Util.getItemMaxStackSize(this.item.getType());
        if (this.isUnlimited()) {
            ItemStack item = this.item.clone();
            while (amount > 0) {
                int stackSize = Math.min(amount, itemMaxStackSize);
                item.setAmount(stackSize);
                pInv.addItem(item);
                amount -= stackSize;
            }
        } else {
            ItemStack[] chestContents = Objects.requireNonNull(this.getInventory()).getContents();
            for (int i = 0; amount > 0 && i < chestContents.length; i++) {
                // Can't clone it here, it could be null
                ItemStack item = chestContents[i];
                if (item != null && item.getType() != Material.AIR && this.matches(item)) {
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
            if (this.getItem().getAmount() > 1) {
                if (this.getRemainingStock() == -1) {
                    lines[1] = MsgUtil.getMessageOfflinePlayer("signs.stack-selling", player, MsgUtil.getMessageOfflinePlayer("signs.unlimited", player));
                } else {
                    lines[1] =
                            MsgUtil.getMessageOfflinePlayer("signs.stack-selling", player, Integer.toString(getRemainingStock()));
                }
            } else {
                if (this.getRemainingStock() == -1) {
                    lines[1] = MsgUtil.getMessageOfflinePlayer("signs.selling", player, MsgUtil.getMessageOfflinePlayer("signs.unlimited", player));
                } else {
                    lines[1] =
                            MsgUtil.getMessageOfflinePlayer("signs.selling", player, Integer.toString(this.getRemainingStock()));
                }
            }


        } else if (this.isBuying()) {
            if (this.getItem().getAmount() > 1) {
                if (this.getRemainingSpace() == -1) {
                    lines[1] = MsgUtil.getMessageOfflinePlayer("signs.stack-buying", player, MsgUtil.getMessageOfflinePlayer("signs.unlimited", player));
                } else {
                    lines[1] =
                            MsgUtil.getMessageOfflinePlayer("signs.stack-buying", player, Integer.toString(getRemainingSpace()));
                }
            } else {
                if (this.getRemainingSpace() == -1) {
                    lines[1] = MsgUtil.getMessageOfflinePlayer("signs.buying", player, MsgUtil.getMessageOfflinePlayer("signs.unlimited", player));
                } else {
                    lines[1] =
                            MsgUtil.getMessageOfflinePlayer("signs.buying", player, Integer.toString(this.getRemainingSpace()));
                }
            }
//            if (this.getRemainingSpace() == -1) {
//                lines[1] =
//                        MsgUtil.getMessageOfflinePlayer(
//                                "signs.buying",
//                                player,
//                                "" + MsgUtil.getMessageOfflinePlayer("signs.unlimited", player));
//            } else {
//                lines[1] =
//                        MsgUtil.getMessageOfflinePlayer("signs.buying", player, "" + this.getRemainingSpace());
//            }
        }
        lines[2] =
                MsgUtil.getMessageOfflinePlayer(
                        "signs.item", player, Util.getItemStackName(this.getItem()));
        if (plugin.isAllowStack() && this.getItem().getAmount() > 1) { //FIXME: A trash impl, need use a better way
            lines[3] = MsgUtil.getMessageOfflinePlayer("signs.stack-price", player, Util.format(this.getPrice()), Integer.toString(item.getAmount()), Util.getItemStackName(item));
        } else {
            lines[3] = MsgUtil.getMessageOfflinePlayer("signs.price", player, Util.format(this.getPrice()));

        }
        this.setSignText(lines);
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
            plugin
                    .getDatabaseHelper()
                    .updateShop(
                            ShopModerator.serialize(this.moderator.clone()),
                            this.getItem(),
                            unlimited,
                            shopType.toID(),
                            this.getPrice(),
                            x,
                            y,
                            z,
                            world);
        } catch (Exception e) {
            e.printStackTrace();
            plugin
                    .getLogger()
                    .log(
                            Level.WARNING,
                            "Could not update a shop in the database! Changes will revert after a reboot!");
        }
    }

    /**
     * @return The durability of the item
     */
    @Override
    public short getDurability() {
        return (short) ((Damageable) this.item.getItemMeta()).getDamage();
    }

    /**
     * @return Returns a dummy itemstack of the item this shop is selling.
     */
    @Override
    public @NotNull ItemStack getItem() {
        return item;
    }

    @Override
    public void setItem(@NotNull ItemStack item) {
        this.item = item;
        update();
        refresh();
    }

    @Override
    public void refresh() {
        if (displayItem != null) {
            displayItem.remove();
            initDisplayItem();
            displayItem.spawn();
        }
        setSignText();
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

    /**
     * @return The location of the shops chest
     */
    @Override
    public @NotNull Location getLocation() {
        return this.location;
    }

    @Override
    public @NotNull ShopModerator getModerator() {
        return this.moderator;
    }

    @Override
    public void setModerator(@NotNull ShopModerator shopModerator) {
        this.lastChangedAt = System.currentTimeMillis();
        this.moderator = shopModerator;
        update();
        Bukkit.getPluginManager().callEvent(new ShopModeratorChangedEvent(this, this.moderator));
    }

    /**
     * @return The name of the player who owns the shop.
     */
    @Override
    public @NotNull UUID getOwner() {
        return this.moderator.getOwner();
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
        this.lastChangedAt = System.currentTimeMillis();
        this.price = price;
        setSignText();
        update();
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

    @Override
    public @NotNull ShopType getShopType() {
        return this.shopType;
    }

    /**
     * Changes a shop type to Buying or Selling. Also updates the signs nearby.
     *
     * @param shopType The new type (ShopType.BUYING or ShopType.SELLING)
     */
    @Override
    public void setShopType(@NotNull ShopType shopType) {
        this.lastChangedAt = System.currentTimeMillis();
        this.shopType = shopType;
        this.setSignText();
        update();
    }

    /**
     * Returns a list of signs that are attached to this shop (QuickShop and blank signs only)
     *
     * @return a list of signs that are attached to this shop (QuickShop and blank signs only)
     */
    @Override
    public @NotNull List<Sign> getSigns() {
        List<Sign> signs = new ArrayList<>(4);
        if (this.getLocation().getWorld() == null) {
            return signs;
        }
        Block[] blocks = new Block[4];
        blocks[0] = location.getBlock().getRelative(BlockFace.EAST);
        blocks[1] = location.getBlock().getRelative(BlockFace.NORTH);
        blocks[2] = location.getBlock().getRelative(BlockFace.SOUTH);
        blocks[3] = location.getBlock().getRelative(BlockFace.WEST);
        OfflinePlayer player = Bukkit.getOfflinePlayer(this.getOwner());
        final String signHeader =
                MsgUtil.getMessageOfflinePlayer("sign.header", player, this.ownerName());

        next:
        for (Block b : blocks) {
            if (b == null) {
                plugin.getLogger().warning("Null signs in the queue, skipping");
                continue;
            }

            if (!(b.getState() instanceof Sign)) {
                continue;
            }
            if (!isAttached(b)) {
                continue;
            }
            Sign sign = (Sign) b.getState();
            String[] lines = sign.getLines();
            if (lines.length >= 1) {
                if (!lines[0].contains(signHeader)) {
                    for (String line : lines) {
                        if (!line.isEmpty()) {
                            break next;
                        }
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

    /**
     * @return The list of players who can manage the shop.
     */
    @NotNull
    @Override
    public List<UUID> getStaffs() {
        return this.moderator.getStaffs();
    }

    @Override
    public boolean isBuying() {
        return this.shopType == ShopType.BUYING;
    }

    @Override
    public boolean isLoaded() {
        return this.isLoaded;
    }

    @Override
    public boolean isSelling() {
        return this.shopType == ShopType.SELLING;
    }

    @Override
    public boolean isUnlimited() {
        return this.unlimited;
    }

    @Override
    public void setUnlimited(boolean unlimited) {
        this.unlimited = unlimited;
        this.setSignText();
        update();
    }

    /**
     * Check shop is or not still Valid.
     *
     * @return isValid
     */
    @Override
    public boolean isValid() {
        this.checkDisplay();
        return Util.canBeShop(this.getLocation().getBlock());
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public @Nullable DisplayItem getDisplay() {
        return this.displayItem;
    }

    /**
     * Gets the shop last changes timestamp
     *
     * @return The time stamp
     */
    @Override
    public long getLastChangedAt() {
        return this.lastChangedAt;
    }

    /**
     * Returns a clone of this shop. References to the same display item, itemstack, location and
     * owner as this shop does. Do not modify them or you will modify this shop.
     *
     * <p>**NOT A DEEP CLONE**
     */
    @Override
    public @NotNull ContainerShop clone() {
        return new ContainerShop(this);
    }

    @Override
    public String toString() {
        StringBuilder sb =
                new StringBuilder(
                        "Shop "
                                + (location.getWorld() == null ? "unloaded world" : location.getWorld().getName())
                                + "("
                                + location.getBlockX()
                                + ", "
                                + location.getBlockY()
                                + ", "
                                + location.getBlockZ()
                                + ")");
        sb.append(" Owner: ").append(this.ownerName()).append(" - ").append(getOwner());
        if (isUnlimited()) {
            sb.append(" Unlimited: true");
        }
        sb.append(" Price: ").append(getPrice());
        sb.append(" Item: ").append(getItem());
        return sb.toString();
    }

    /**
     * Returns the display item associated with this shop.
     *
     * @return The display item associated with this shop.
     */
    @Nullable
    public DisplayItem getDisplayItem() {
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
            if (location.getBlock().getState().getType() == Material.ENDER_CHEST
                    && plugin.getOpenInvPlugin() != null) { //FIXME: Need better impl
                OpenInv openInv = ((OpenInv) plugin.getOpenInvPlugin());
                return openInv
                        .getSpecialEnderChest(
                                Objects.requireNonNull(
                                        openInv.loadPlayer(Bukkit.getOfflinePlayer(this.moderator.getOwner()))),
                                Bukkit.getOfflinePlayer((this.moderator.getOwner())).isOnline())
                        .getBukkitInventory();
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
            if (!createBackup) {
                createBackup = Util.backupDatabase();
                if (createBackup) {
                    this.delete();
                    Util.debugLog("Inventory doesn't exist anymore: " + this + " shop was removed.");
                }
            } else {
                this.delete();
                Util.debugLog("Inventory doesn't exist anymore: " + this + " shop was removed.");
            }
            return null;
        }
    }

    /**
     * Returns true if this shop is a double chest, and the other half is selling/buying the same as
     * this is buying/selling.
     *
     * @return true if this shop is a double chest, and the other half is selling/buying the same as
     * this is buying/selling.
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
     * Returns the shop that shares it's inventory with this one.
     *
     * @return the shop that shares it's inventory with this one. Will return null if this shop is not
     * attached to another.
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
     * Different with isDoubleShop, this method only check the shop is created on the double chest.
     *
     * @return true if create on double chest.
     */
    public boolean isDoubleChestShop() {
        return Util.isDoubleChest(this.getLocation().getBlock());
    }

    /**
     * Check the container still there and we can keep use it.
     */
    public void checkContainer() {
        if (!this.isLoaded) {
            return;
        }
        if (!Util.isLoaded(this.getLocation())) {
            return;
        }
        if (!Util.canBeShop(this.getLocation().getBlock())) {
            Util.debugLog("Shop at " + this.getLocation() + "@" + this.getLocation().getBlock() + " container was missing, remove...");
            this.onUnload();
            if (!createBackup) {
                this.createBackup = Util.backupDatabase();
            }
            if (createBackup) {
                this.delete();
            } else {
                Util.debugLog("Failed to create backup, shop at " + this.toString() + " won't to delete.");
            }
        }
    }

}
