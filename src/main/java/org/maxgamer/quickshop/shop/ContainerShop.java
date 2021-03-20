/*
 * This file is a part of project QuickShop, the name is ContainerShop.java
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

import com.lishid.openinv.OpenInv;
import io.papermc.lib.PaperLib;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.event.*;
import org.maxgamer.quickshop.util.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * ChestShop core
 */
@EqualsAndHashCode
public class ContainerShop implements Shop {

    private static final String shopSignPrefix = "§d§o §r";
    private static final String shopSignPattern = "§d§o ";
    @NotNull
    private final Location location;
    @EqualsAndHashCode.Exclude
    private final QuickShop plugin;
    private final Map<String, Map<String, Object>> extra;
    @EqualsAndHashCode.Exclude
    private final UUID runtimeRandomUniqueId = UUID.randomUUID();
    @NotNull
    private ItemStack item;
    @Nullable
    private DisplayItem displayItem;
    @EqualsAndHashCode.Exclude
    private volatile boolean isLoaded = false;
    @EqualsAndHashCode.Exclude
    private volatile boolean isDeleted = false;
    @EqualsAndHashCode.Exclude
    private boolean isLeftShop = false;
    @EqualsAndHashCode.Exclude
    private volatile boolean createBackup = false;
    @EqualsAndHashCode.Exclude
    private InventoryPreview inventoryPreview = null;
    private ShopModerator moderator;
    private double price;
    private ShopType shopType;
    private boolean unlimited;
    @EqualsAndHashCode.Exclude
    private ContainerShop attachedShop;
    @EqualsAndHashCode.Exclude
    private boolean dirty;


    private ContainerShop(@NotNull ContainerShop s) {
        Util.ensureThread(false);
        this.shopType = s.shopType;
        this.item = s.item.clone();
        this.location = s.location.clone();
        this.plugin = s.plugin;
        this.unlimited = s.unlimited;
        this.moderator = s.moderator.clone();
        this.price = s.price;
        this.isLoaded = s.isLoaded;
        this.isDeleted = s.isDeleted;
        this.createBackup = s.createBackup;
        this.extra = s.extra;
        this.dirty = true;
        this.inventoryPreview = null;
        initDisplayItem();
    }

    /**
     * Adds a new shop. You need call ShopManager#loadShop if you create from outside of
     * ShopLoader.
     *
     * @param location  The location of the chest block
     * @param price     The cost per item
     * @param item      The itemstack with the properties we want. This is .cloned, no need to worry
     *                  about references
     * @param moderator The modertators
     * @param type      The shop type
     * @param unlimited The unlimited
     * @param plugin    The plugin instance
     * @param extra     The extra data saved by addon
     */
    public ContainerShop(
        @NotNull QuickShop plugin,
        @NotNull Location location,
        double price,
        @NotNull ItemStack item,
        @NotNull ShopModerator moderator,
        boolean unlimited,
        @NotNull ShopType type,
        @NotNull Map<String, Map<String, Object>> extra) {
        Util.ensureThread(false);
        this.location = location;
        this.price = price;
        this.moderator = moderator;
        this.item = item.clone();
        this.plugin = plugin;
        if (!plugin.isAllowStack()) {
            this.item.setAmount(1);
        }
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName()) {
                Util.debugLog("Shop item display is: " + meta.getDisplayName());
                //https://hub.spigotmc.org/jira/browse/SPIGOT-5964
                if (meta.getDisplayName().matches("\\{.*\\}")) {
                    meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(
                        GsonComponentSerializer.gson().deserialize(meta.getDisplayName())));
                    //Correct both items
                    item.setItemMeta(meta);
                    this.item.setItemMeta(meta);
                }
            }
        }
        this.shopType = type;
        this.unlimited = unlimited;
        this.extra = extra;
        initDisplayItem();
        this.dirty = false;
        //version = dataMap != null ? Integer.parseInt(String.valueOf(dataMap.getOrDefault("version", 0))) : 0;
    }

    private void initDisplayItem() {
        Util.ensureThread(false);
        if (plugin.isDisplay()) {
            if (displayItem != null) {
                displayItem.remove();
            }
            // Update double shop status, is left status, and the attachedShop
            updateAttachedShop();
            // Don't make an item for this chest if it's a left shop.
            if (isLeftShop) {
                if (attachedShop != null && attachedShop.getDisplayItem() != null) {
                    attachedShop.refresh();
                }
                return;
            }

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
                        if (!GameVersion.get(ReflectFactory.getServerVersion())
                            .isVirtualDisplaySupports()) {
                            throw new IllegalStateException(
                                "Version not supports Virtual DisplayItem.");
                        }
                        this.displayItem = new VirtualDisplayItem(this);

                        //Catch everything
                    } catch (Throwable e) {
                        Util.debugLog(e.getMessage());
                        MsgUtil.debugStackTrace(e.getStackTrace());
                        plugin.getConfig().set("shop.display-type", 0);
                        plugin.saveConfig();
                        this.displayItem = new RealDisplayItem(this);
                        //do not throw
                        plugin.getLogger().log(Level.SEVERE,
                            "Failed to initialize VirtualDisplayItem, fallback to RealDisplayItem, are you using the latest version of ProtocolLib?",
                            e);
                    }
                    break;
                default:
                    Util.debugLog(
                        "Warning: Failed to create a ContainerShop displayItem, the type we didn't know, fallback to RealDisplayItem");
                    this.displayItem = new RealDisplayItem(this);
                    break;
            }
        }
//        } else {
//            Util.debugLog("The display was disabled.");
//        }
    }

    /**
     * Add an item to shops chest.
     *
     * @param item   The itemstack. The amount does not matter, just everything else
     * @param amount The amount to add to the shop.
     */
    @Override
    public void add(@NotNull ItemStack item, int amount) {
        Util.ensureThread(false);
        if (this.unlimited) {
            return;
        }
        item = item.clone();
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
        Util.ensureThread(false);
        setDirty();
        boolean result = this.moderator.addStaff(player);
        update();
        if (result) {
            Util.mainThreadRun(() -> plugin.getServer().getPluginManager()
                .callEvent(new ShopModeratorChangedEvent(this, this.moderator)));
        }
        return result;
    }

    /**
     * Buys amount of item from Player p. Does NOT check our inventory, or balances
     *
     * @param buyer          The player to buy from
     * @param buyerInventory The buyer's inventory
     * @param loc2Drop       The location to drop items if inventory are full
     * @param amount         The amount to buy
     */
    @Override
    public void buy(@NotNull UUID buyer, @NotNull Inventory buyerInventory,
        @NotNull Location loc2Drop, int amount) {
        Util.ensureThread(false);
        amount = amount * item.getAmount();
        if (amount < 0) {
            this.sell(buyer, buyerInventory, loc2Drop, -amount);
        }
        ItemStack[] contents = buyerInventory.getContents();
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
            buyerInventory.setContents(contents);
            this.setSignText();
            // This should not happen.
            if (amount > 0) {
                plugin
                    .getLogger()
                    .log(
                        Level.WARNING,
                        "Could not take all items from a players inventory on purchase! "
                            + buyer
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
            buyerInventory.setContents(contents);
            this.setSignText();
        }
    }

    @Override
    public void checkDisplay() {
        Util.ensureThread(false);
        if (!plugin.isDisplay() || !this.isLoaded || this
            .isDeleted()) { // FIXME: Reinit scheduler on reloading config
            return;
        }

        updateAttachedShop();

        if (isLeftShop) {
            if (displayItem != null) {
                displayItem.remove();
            }
            if (attachedShop != null) {
                attachedShop.refresh();
            }
            return;
        }

        if (this.displayItem == null) {
            Util.debugLog("Warning: DisplayItem is null, this shouldn't happened...");
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
            //Util.debugLog("Target item not spawned, spawning for shop " + this.getLocation());
            displayItem.spawn();
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
        setDirty();
        this.moderator.clearStaffs();
        Util.mainThreadRun(() -> plugin.getServer().getPluginManager()
            .callEvent(new ShopModeratorChangedEvent(this, this.moderator)));
        update();
    }

    @Override
    public boolean delStaff(@NotNull UUID player) {
        Util.ensureThread(false);
        setDirty();
        boolean result = this.moderator.delStaff(player);
        update();
        if (result) {
            Util.mainThreadRun(() -> plugin.getServer().getPluginManager()
                .callEvent(new ShopModeratorChangedEvent(this, this.moderator)));
        }
        return result;
    }

    /**
     * Deletes the shop from the list of shops and queues it for database
     */
    @Override
    public void delete() {
        Util.ensureThread(false);
        delete(false);
    }

    /**
     * Deletes the shop from the list of shops and queues it for database deletion
     *
     * @param memoryOnly whether to delete from database
     */
    @Override
    public void delete(boolean memoryOnly) {
        Util.ensureThread(false);
        // Get a copy of the attached shop to save it from deletion
        ContainerShop neighbor = getAttachedShop();
        setDirty();
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
        if (memoryOnly) {
            // Delete it from memory
            plugin.getShopManager().removeShop(this);
        } else {
            // Delete the signs around it
            for (Sign s : this.getSigns()) {
                s.getBlock().setType(Material.AIR);
            }
            // Delete it from the database
            // Refund if necessary
            if (plugin.getConfig().getBoolean("shop.refund")) {
                plugin.getEconomy()
                    .deposit(this.getOwner(), plugin.getConfig().getDouble("shop.cost"),
                        Objects.requireNonNull(getLocation().getWorld()), getCurrency());
            }
            plugin.getShopManager().removeShop(this);
            plugin.getDatabaseHelper().removeShop(this);
        }
        // Use that copy we saved earlier (which is now deleted) to refresh it's now alone neighbor
        if (neighbor != null) {
            neighbor.refresh();
        }
    }

    @Override
    public boolean isAttached(@NotNull Block b) {
        Util.ensureThread(false);
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
        Util.ensureThread(false);
        ShopClickEvent event = new ShopClickEvent(this);
        if (Util.fireCancellableEvent(event)) {
            Util.debugLog("Ignore shop click, because some plugin cancel it.");
            return;
        }
        refresh();
        setSignText();
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
        Util.ensureThread(false);
        if (!this.isLoaded) {
            Util.debugLog("Dupe unload request, canceled.");
            return;
        }
        if (inventoryPreview != null) {
            inventoryPreview.close();
        }
        if (this.getDisplayItem() != null) {
            this.getDisplayItem().remove();
        }
        update();
        this.isLoaded = false;
        plugin.getShopManager().getLoadedShops().remove(this);
        ShopUnloadEvent shopUnloadEvent = new ShopUnloadEvent(this);
        plugin.getServer().getPluginManager().callEvent(shopUnloadEvent);
    }

    @Override
    public @NotNull String ownerName(boolean forceUsername) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(this.getOwner());
        String name = player.getName();
        if (name == null || name.isEmpty()) {
            name = MsgUtil.getMessageOfflinePlayer(
                "unknown-owner", player);
        }
        if (forceUsername) {
            return name;
        }
        if (isUnlimited()) {
            return MsgUtil.getMessageOfflinePlayer("admin-shop", player);
        }
        return name;
    }

    @Override
    public @NotNull String ownerName() {
        return ownerName(false);
    }

    /**
     * Removes an item from the shop.
     *
     * @param item   The itemstack. The amount does not matter, just everything else
     * @param amount The amount to remove from the shop.
     */
    @Override
    public void remove(@NotNull ItemStack item, int amount) {
        Util.ensureThread(false);
        if (this.unlimited) {
            return;
        }
        item = item.clone();
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
     * @param seller          The seller
     * @param sellerInventory The seller's inventory
     * @param loc2Drop        Location to drop items if inventory are full
     * @param amount          The amount to sell
     */
    @Override
    public void sell(@NotNull UUID seller, @NotNull Inventory sellerInventory,
        @NotNull Location loc2Drop, int amount) {
        Util.ensureThread(false);
        amount = item.getAmount() * amount;
        if (amount < 0) {
            this.buy(seller, sellerInventory, loc2Drop, -amount);
        }
        // Items to drop on floor
        ArrayList<ItemStack> floor = new ArrayList<>(5);
        int itemMaxStackSize = Util.getItemMaxStackSize(this.item.getType());
        if (this.isUnlimited()) {
            ItemStack item = this.item.clone();
            while (amount > 0) {
                int stackSize = Math.min(amount, itemMaxStackSize);
                item.setAmount(stackSize);
                sellerInventory.addItem(item);
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
                    floor.addAll(sellerInventory.addItem(item).values());
                    amount -= stackSize;
                }
            }
            // We now have to update the chests inventory manually.
            this.getInventory().setContents(chestContents);
            this.setSignText();
        }
        if (loc2Drop != null) {
            for (ItemStack stack : floor) {
                loc2Drop.getWorld().dropItem(loc2Drop, stack);
            }
        }
    }

    @Override
    public String[] getSignText() {
        Util.ensureThread(false);
        String[] lines = new String[4];
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(this.getOwner());
        lines[0] = MsgUtil.getMessageOfflinePlayer("signs.header", null, this.ownerName(false));
        if (this.isSelling()) {
            if (this.getItem().getAmount() > 1) {
                if (this.getRemainingStock() == -1) {
                    lines[1] = MsgUtil.getMessageOfflinePlayer("signs.stack-selling", player,
                        MsgUtil.getMessageOfflinePlayer("signs.unlimited", player));
                } else {
                    lines[1] =
                        MsgUtil.getMessageOfflinePlayer("signs.stack-selling", player,
                            Integer.toString(getRemainingStock()));
                }
            } else {
                if (this.getRemainingStock() == -1) {
                    lines[1] = MsgUtil.getMessageOfflinePlayer("signs.selling", player,
                        MsgUtil.getMessageOfflinePlayer("signs.unlimited", player));
                } else {
                    lines[1] =
                        MsgUtil.getMessageOfflinePlayer("signs.selling", player,
                            Integer.toString(this.getRemainingStock()));
                }
            }


        } else if (this.isBuying()) {
            if (this.getItem().getAmount() > 1) {
                if (this.getRemainingSpace() == -1) {
                    lines[1] = MsgUtil.getMessageOfflinePlayer("signs.stack-buying", player,
                        MsgUtil.getMessageOfflinePlayer("signs.unlimited", player));
                } else {
                    lines[1] =
                        MsgUtil.getMessageOfflinePlayer("signs.stack-buying", player,
                            Integer.toString(getRemainingSpace()));
                }
            } else {
                if (this.getRemainingSpace() == -1) {
                    lines[1] = MsgUtil.getMessageOfflinePlayer("signs.buying", player,
                        MsgUtil.getMessageOfflinePlayer("signs.unlimited", player));
                } else {
                    lines[1] =
                        MsgUtil.getMessageOfflinePlayer("signs.buying", player,
                            Integer.toString(this.getRemainingSpace()));
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
        if (this.isStackingShop()) {
            lines[3] = MsgUtil.getMessageOfflinePlayer("signs.stack-price", player,
                Util.format(this.getPrice(), this), Integer.toString(item.getAmount()),
                Util.getItemStackName(item));
        } else {
            lines[3] = MsgUtil
                .getMessageOfflinePlayer("signs.price", player, Util.format(this.getPrice(), this));

        }
        //new pattern
        lines[1] = shopSignPrefix + lines[1] + " ";

        return lines;
    }

    /**
     * Changes all lines of text on a sign near the shop
     *
     * @param lines The array of lines to change. Index is line number.
     */
    @Override
    public void setSignText(@NotNull String[] lines) {
        Util.ensureThread(false);
        List<Sign> signs = this.getSigns();
        for (Sign sign : signs) {
            if (Arrays.equals(sign.getLines(), lines)) {
                Util.debugLog("Skipped new sign text setup: Same content");
                continue;
            }
            for (int i = 0; i < lines.length; i++) {
                sign.setLine(i, lines[i]);
            }
            sign.update(true);
            plugin.getServer().getPluginManager().callEvent(new ShopSignUpdateEvent(this, sign));
        }
    }

    /**
     * Updates signs attached to the shop
     */
    @Override
    public void setSignText() {
        Util.ensureThread(false);
        if (!Util.isLoaded(this.location)) {
            return;
        }
        this.setSignText(getSignText());
    }

    /**
     * Upates the shop into the database.
     */
    @Override
    public synchronized void update() {
        Util.mainThreadRun(this::update0);
    }

    public synchronized void update0() {
        Util.ensureThread(false);
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
            plugin.getDatabaseHelper()
                .updateShop(ShopModerator.serialize(this.moderator.clone()), this.getItem(),
                    unlimited, shopType.toID(), this.getPrice(), x, y, z, world,
                    this.saveExtraToJson());
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING,
                "Could not update a shop in the database! Changes will revert after a reboot!", e);
        }
        this.dirty = false;
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
        Util.ensureThread(false);
        ShopItemChangeEvent event = new ShopItemChangeEvent(this, this.item, item);
        if (Util.fireCancellableEvent(event)) {
            Util.debugLog("A plugin cancelled the item change event.");
            return;
        }
        this.item = item;
        update();
        refresh();
    }

    @Override
    public void refresh() {
        Util.ensureThread(false);
        if (inventoryPreview != null) {
            inventoryPreview.close();
            inventoryPreview = null;
        }
        if (displayItem != null) {
            displayItem.remove();
        }

        checkDisplay();

        if (!isLeftShop) {
            initDisplayItem();
            displayItem.spawn();
        }

        setSignText();
    }

    /**
     * Load ContainerShop.
     */
    @Override
    public void onLoad() {
        Util.ensureThread(false);
        if (this.isLoaded) {
            Util.debugLog("Dupe load request, canceled.");
            return;
        }
        ShopLoadEvent shopLoadEvent = new ShopLoadEvent(this);
        if (Util.fireCancellableEvent(shopLoadEvent)) {
            return;
        }
        this.isLoaded = true;
        plugin.getShopManager().loadShop(this.getLocation().getWorld().getName(), this);
        plugin.getShopManager().getLoadedShops().add(this);
        plugin.getShopContainerWatcher().scheduleCheck(this);

        // check price restriction
        if (plugin.getShopManager().getPriceLimiter().check(item, price)
            != PriceLimiter.Status.PASS) {
            Entry<Double, Double> priceRestriction = Util.getPriceRestriction(
                this.getMaterial()); //TODO Adapt priceLimiter, also improve priceLimiter return a container
            if (priceRestriction != null) {
                if (price < priceRestriction.getKey()) {
                    setDirty();
                    price = priceRestriction.getKey();
                    this.update();
                } else if (price > priceRestriction.getValue()) {
                    setDirty();
                    price = priceRestriction.getValue();
                    this.update();
                }
            }
        }
        checkDisplay();
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
        setDirty();
        this.moderator = shopModerator;
        update();
        Util.mainThreadRun(() -> plugin.getServer().getPluginManager()
            .callEvent(new ShopModeratorChangedEvent(this, this.moderator)));
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
        OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(owner);
        //Get the sign at first
        List<Sign> signs = this.getSigns();
        //then setOwner
        this.moderator.setOwner(owner);
        //then change the sign
        Util.mainThreadRun(() -> {
            for (Sign shopSign : signs) {
                shopSign.setLine(0, MsgUtil
                    .getMessageOfflinePlayer("signs.header", offlinePlayer, ownerName(false)));
                //Don't forgot update it
                shopSign.update(true);
            }
            //Event
            plugin.getServer().getPluginManager()
                .callEvent(new ShopModeratorChangedEvent(this, this.moderator));
        });
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
        setDirty();
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
        Util.ensureThread(false);
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
        Util.ensureThread(false);
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
     * Changes a shop type to Bu ying or Selling. Also updates the signs nearby.
     *
     * @param newShopType The new type (ShopType.BUYING or ShopType.SELLING)
     */
    @Override
    public void setShopType(@NotNull ShopType newShopType) {
        Util.ensureThread(false);
        if (this.shopType == newShopType) {
            return; //Ignore if there actually no changes
        }
        setDirty();
        if (Util.fireCancellableEvent(new ShopTypeChangeEvent(this, this.shopType, newShopType))) {
            Util.debugLog(
                "Some addon cancelled shop type changes, target shop: " + this.toString());
            return;
        }
        this.shopType = newShopType;
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
        Util.ensureThread(false);
        List<Sign> signs = new ArrayList<>(4);
        if (this.getLocation().getWorld() == null) {
            return Collections.emptyList();
        }
        Block[] blocks = new Block[4];
        blocks[0] = location.getBlock().getRelative(BlockFace.EAST);
        blocks[1] = location.getBlock().getRelative(BlockFace.NORTH);
        blocks[2] = location.getBlock().getRelative(BlockFace.SOUTH);
        blocks[3] = location.getBlock().getRelative(BlockFace.WEST);
        for (Block b : blocks) {
            if (b == null) {
                continue;
            }

            BlockState state = PaperLib.getBlockState(b, false).getState();
            if (!(state instanceof Sign)) {
                continue;
            }
            if (!isAttached(b)) {
                continue;
            }
            Sign sign = (Sign) state;
            String[] lines = sign.getLines();
            if (lines[0].isEmpty() && lines[1].isEmpty() && lines[2].isEmpty() && lines[3]
                .isEmpty()) {
                signs.add(sign); //NEW SIGN
                continue;
            }
            String header = lines[0];

            if (lines[1].startsWith(shopSignPattern)) {
                signs.add(sign);
            } else {
                String adminShopHeader = MsgUtil
                    .getMessageOfflinePlayer("signs.header", null, MsgUtil.getMessageOfflinePlayer(
                        "admin-shop", plugin.getServer().getOfflinePlayer(this.getOwner())));
                String signHeaderUsername =
                    MsgUtil.getMessageOfflinePlayer("signs.header", null, this.ownerName(true));
                if (header.contains(adminShopHeader) || header.contains(signHeaderUsername)) {
                    signs.add(sign);
                    //TEXT SIGN
                    //continue
                }
            }
        }

        return signs;
    }

    /**
     * @return The list of players who can manage the shop.
     */
    @NotNull
    @Override
    public List<UUID> getStaffs() {
        return new ArrayList<>(this.moderator
            .getStaffs()); //Clone only, so make sure external calling will use addStaff
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
        this.setOwner(UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"));
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
        Util.ensureThread(false);
        //this.checkDisplay();
        if (this.isDeleted) {
            return false;
        }
        return Util.canBeShop(this.getLocation().getBlock());
    }

    @Override
    public boolean isDeleted() {
        return this.isDeleted;
    }

    @Override
    public @Nullable DisplayItem getDisplay() {
        return this.displayItem;
    }

    @Override
    public void setDirty() {
        this.dirty = true;
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public void setDirty(boolean isDirty) {
        this.dirty = isDirty;
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
                    + (location.getWorld() == null ? "unloaded world"
                    : location.getWorld().getName())
                    + "("
                    + location.getBlockX()
                    + ", "
                    + location.getBlockY()
                    + ", "
                    + location.getBlockZ()
                    + ")");
        sb.append(" Owner: ").append(this.ownerName(false)).append(" - ").append(getOwner());
        if (isUnlimited()) {
            sb.append(" Unlimited: true");
        }
        sb.append(" Price: ").append(getPrice());
        //sb.append(" Item: ").append(getItem());
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
        if (this.item.hasItemMeta() && this.item.getItemMeta().hasEnchants()) {
            return Objects.requireNonNull(this.item.getItemMeta()).getEnchants();
        }
        return Collections.emptyMap();
    }

    /**
     * @return The chest this shop is based on.
     */
    public @Nullable Inventory getInventory() {
        Util.ensureThread(false);
        BlockState state = PaperLib.getBlockState(location.getBlock(), false).getState();
        try {
            if (state.getType() == Material.ENDER_CHEST
                && plugin.getOpenInvPlugin() != null) { //FIXME: Need better impl
                OpenInv openInv = ((OpenInv) plugin.getOpenInvPlugin());
                return openInv.getSpecialEnderChest(
                    Objects.requireNonNull(
                        openInv.loadPlayer(
                            plugin.getServer().getOfflinePlayer(this.moderator.getOwner()))),
                    plugin.getServer().getOfflinePlayer((this.moderator.getOwner())).isOnline())
                    .getBukkitInventory();
            }
        } catch (Exception e) {
            Util.debugLog(e.getMessage());
            return null;
        }
        InventoryHolder container;
        try {
            container = (InventoryHolder) state;
            return container.getInventory();
        } catch (Exception e) {
            if (!createBackup) {
                createBackup = Util.backupDatabase();
                if (createBackup) {
                    plugin.log("Deleting shop " + this + " request by invalid inventory.");
                    this.delete();
                    Util.debugLog(
                        "Inventory doesn't exist anymore: " + this + " shop was removed.");
                }
            } else {
                plugin.log("Deleting shop " + this + " request by invalid inventory.");
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
        Util.ensureThread(false);
        if (attachedShop == null) {
            return false;
        }
        if (attachedShop.matches(this.getItem())) {
            // They're both trading the same item
            // They're both buying or both selling => Not a double shop,
            // just two shops.
            // One is buying, one is selling.
            return this.getShopType() != attachedShop.getShopType();
        } else {
            return false;
        }
    }

    /**
     * Updates the attachedShop variable to reflect the currently attached shop, if any.
     * Also updates the left shop status.
     */
    public void updateAttachedShop() {
        Util.ensureThread(false);
        Block attachedChest = Util
            .getSecondHalf(PaperLib.getBlockState(this.getLocation().getBlock(), false).getState());
        if (attachedChest == null) {
            return;
        }
        Shop shop = plugin.getShopManager().getShop(attachedChest.getLocation());
        attachedShop = shop == null ? null : (ContainerShop) shop;

        if (attachedShop != null && attachedShop.matches(this.getItem())) {
            updateLeftShop();
        } else {
            attachedShop = null;
            isLeftShop = false;
        }
    }

    /**
     * This function calculates which block of a double chest is the left block,
     * relative to the direction the chest is facing. Left shops don't spawn items since
     * they merge items with the right shop.
     * It also updates the isLeftShop status of this class to reflect the changes.
     */
    private void updateLeftShop() {
        if (attachedShop == null) {
            return;
        }

        switch (((Chest) getLocation().getBlock().getBlockData()).getFacing()) {
            case WEST:
                // left block has a smaller z value
                isLeftShop = getLocation().getZ() < attachedShop.getLocation().getZ();
                break;
            case EAST:
                // left block has a greater z value
                isLeftShop = getLocation().getZ() > attachedShop.getLocation().getZ();
                break;
            case NORTH:
                // left block has greater x value
                isLeftShop = getLocation().getX() > attachedShop.getLocation().getX();
                break;
            case SOUTH:
                // left block has a smaller x value
                isLeftShop = getLocation().getX() < attachedShop.getLocation().getX();
                break;
            default:
                isLeftShop = false;
        }
    }

    /**
     * Checks to see if it is a real double without updating anything.
     * @return If the chest is a real double chest, as in it is a double and it has the same item.
     */
    public boolean isRealDouble() {
        Util.ensureThread(false);
        if (attachedShop == null) {
            return false;
        }
        return attachedShop.matches(this.getItem());
    }

    @Override
    public boolean isLeftShop() {
        return isLeftShop;
    }

    @Override
    public ContainerShop getAttachedShop() {
        return attachedShop;
    }

    /**
     * Different with isDoubleShop, this method only check the shop is created on the double chest.
     *
     * @return true if create on double chest.
     */
    public boolean isDoubleChestShop() {
        Util.ensureThread(false);
        return Util
            .isDoubleChest(PaperLib.getBlockState(this.getLocation().getBlock(), false).getState());
    }

    /**
     * Check the container still there and we can keep use it.
     */
    public void checkContainer() {
        Util.ensureThread(false);
        if (!this.isLoaded) {
            return;
        }
        if (!Util.isLoaded(this.getLocation())) {
            return;
        }
        if (!Util.canBeShop(this.getLocation().getBlock())) {
            Util.debugLog("Shop at " + this.getLocation() + "@" + this.getLocation().getBlock()
                + " container was missing, unload from memory...");
            this.onUnload();
            this.delete(true);
//            if (!createBackup) {
//                this.createBackup = Util.backupDatabase();
//            }
//            if (createBackup) {
//                plugin.log("Deleting shop " + this + " request by non-shopable container.");
//                this.delete();
//            } else {
//                Util.debugLog("Failed to create backup, shop at " + this.toString() + " won't to delete.");
//            }
        }
    }

    @Override
    public @NotNull String saveExtraToJson() {
        return JsonUtil.getGson().toJson(this.extra);
    }

    /**
     * Gets the plugin's k-v map to storage the data. It is spilt by plugin name, different name
     * have different map, the data won't conflict. But if you plugin name is too common, add a
     * prefix will be a good idea.
     *
     * @param plugin Plugin instance
     * @return The data table
     */
    @Override
    public @NotNull Map<String, Object> getExtra(@NotNull Plugin plugin) {
        Map<String, Object> extraMap = this.extra.get(plugin.getName());
        if (extraMap == null) {
            extraMap = new ConcurrentHashMap<>();
            this.extra.put(plugin.getName(), extraMap);
        }

        return extraMap;
    }

    /**
     * Gets ExtraManager to quick access extra data
     *
     * @param plugin Plugin instance
     * @return The Extra data manager
     */
    @Override
    public @NotNull ShopExtraManager getExtraManager(@NotNull Plugin plugin) {
        return new ShopExtraManager(this, plugin);
    }

    /**
     * Save the extra data to the shop.
     *
     * @param plugin Plugin instace
     * @param data   The data table
     * @deprecated Extra Map doen't need set to save it.
     */
    @Override
    public void setExtra(@NotNull Plugin plugin, @NotNull Map<String, Object> data) {
        setDirty();
        update();
    }

    /**
     * Gets shop status is stacking shop
     *
     * @return The shop stacking status
     */
    @Override
    public boolean isStackingShop() {
        return plugin.isAllowStack() && this.item.getAmount() > 1;
    }

    /**
     * WARNING: This UUID will changed after plugin reload, shop reload or server restart DO NOT USE
     * IT TO STORE DATA!
     *
     * @return Random UUID
     */
    @Override
    public @NotNull UUID getRuntimeRandomUniqueId() {
        return this.runtimeRandomUniqueId;
    }

    /**
     * Gets the currency that shop use
     *
     * @return The currency name
     */
    @Override
    public @Nullable String getCurrency() {
        Map<String, Object> extraMap = getExtra(plugin);
        return (String) extraMap.get("currency");
    }

    /**
     * Sets the currency that shop use
     *
     * @param currency The currency name; null to use default currency
     */
    @Override
    public void setCurrency(@Nullable String currency) {
        Map<String, Object> extraMap = getExtra(plugin);
        if (currency == null) {
            extraMap.remove("currency");
        } else {
            extraMap.put("currency", currency);
        }
        extra.put(plugin.getName(), extraMap);

        setDirty();
        this.update();
    }

    @Override
    public void openPreview(@NotNull Player player) {
        if (inventoryPreview == null) {
            inventoryPreview = new InventoryPreview(plugin, getItem(), player);
        }
        inventoryPreview.show();
    }


}
