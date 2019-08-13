package org.maxgamer.quickshop.Shop;

import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

import com.lishid.openinv.OpenInv;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Event.*;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

/**
 * ChestShop core
 */
@EqualsAndHashCode
public class ContainerShop implements Shop {
    private DisplayItem displayItem;
    @EqualsAndHashCode.Exclude private boolean isLoaded = false;
    private ItemStack item;
    private Location loc;
    private ShopModerator moderator;
    private QuickShop plugin;
    private double price;
    private ShopType shopType;
    private boolean unlimited;

    private ContainerShop(@NotNull ContainerShop s) {
        this.displayItem = s.displayItem;
        this.shopType = s.shopType;
        this.item = s.item;
        this.loc = s.loc;
        this.plugin = s.plugin;
        this.unlimited = s.unlimited;
        this.moderator = s.moderator;
        this.price = s.price;
        this.isLoaded = s.isLoaded;
    }

    /**
     * Adds a new shop.
     *
     * @param loc       The location of the chest block
     * @param price     The cost per item
     * @param item      The itemstack with the properties we want. This is .cloned, no
     *                  need to worry about references
     * @param moderator The modertators
     * @param type      The shop type
     * @param unlimited The unlimited
     */
    public ContainerShop(@NotNull Location loc, double price, @NotNull ItemStack item, @NotNull ShopModerator moderator, boolean unlimited, @NotNull ShopType type) {
        this.loc = loc;
        this.price = price;
        this.moderator = moderator;
        this.item = item.clone();
        this.plugin = (QuickShop) Bukkit.getPluginManager().getPlugin("QuickShop");
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
    public void add(@NotNull ItemStack item, int amount) {
        if (this.unlimited)
            return;
        Inventory inv = this.getInventory();
        int remains = amount;
        while (remains > 0) {
            int stackSize = Math.min(remains, item.getMaxStackSize());
            item.setAmount(stackSize);
            inv.addItem(item);
            remains = remains - stackSize;
        }
        this.setSignText();
    }

    /**
     * Returns the number of items this shop has in stock.
     *
     * @return The number of items available for purchase.
     */
    public int getRemainingStock() {
        if (this.unlimited)
            return -1;
        return Util.countItems(this.getInventory(), this.getItem());
    }

    /**
     * Returns the number of free spots in the chest for the particular item.
     *
     * @return remaining space
     */
    public int getRemainingSpace() {
        if (this.unlimited)
            return -1;
        return Util.countSpace(this.getInventory(), this.getItem());
    }

    /**
     * Returns true if the ItemStack matches what this shop is selling/buying
     *
     * @param item The ItemStack
     * @return True if the ItemStack is the same (Excludes amounts)
     */
    public boolean matches(@Nullable ItemStack item) {
        return plugin.getItemMatcher().matches(this.item, item);
    }

    /**
     * @return The location of the shops chest
     */
    public Location getLocation() {
        return this.loc;
    }

    /**
     * @return The price per item this shop is selling
     */
    public double getPrice() {
        return this.price;
    }

    /**
     * Sets the price of the shop.
     *
     * @param price The new price of the shop.
     */
    public void setPrice(double price) {
        Bukkit.getPluginManager().callEvent(new ShopPriceChangedEvent(this, this.price, price));
        this.price = price;
        setSignText();
        update();
    }

    /**
     * Upates the shop into the database.
     */
    public void update() {
        ShopUpdateEvent shopUpdateEvent = new ShopUpdateEvent(this);
        if (shopUpdateEvent.isCancelled()) {
            Util.debugLog("The Shop update action was canceled by a plugin.");
            return;
        }

        int x = this.getLocation().getBlockX();
        int y = this.getLocation().getBlockY();
        int z = this.getLocation().getBlockZ();
        String world = this.getLocation().getWorld().getName();
        int unlimited = this.isUnlimited() ? 1 : 0;
        try {
            plugin.getDatabaseHelper().updateShop(plugin.getDatabase(), ShopModerator.serialize(this.moderator.clone()), this
                    .getItem(), unlimited, shopType.toID(), this.getPrice(), x, y, z, world);
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().log(Level.WARNING, "Could not update a shop in the database! Changes will revert after a reboot!");
        }
    }

    /**
     * @return The durability of the item
     */
    public short getDurability() {
        return (short) ((Damageable) this.item.getItemMeta()).getDamage();
    }

    /**
     * @return The name of the player who owns the shop.
     */
    public UUID getOwner() {
        return this.moderator.getOwner();
    }

    /**
     * @return Returns a dummy itemstack of the item this shop is selling.
     */
    public ItemStack getItem() {
        return item;
    }

    @Override
    public boolean addStaff(@NotNull UUID player) {
        boolean result = this.moderator.addStaff(player);
        update();
        if (result)
            Bukkit.getPluginManager().callEvent(new ShopModeratorChangedEvent(this, this.moderator));
        return result;
    }

    /**
     * Buys amount of item from Player p. Does NOT check our inventory, or
     * balances
     *
     * @param p      The player to buy from
     * @param amount The amount to buy
     */
    public void buy(@NotNull Player p, int amount) {
        if (amount < 0)
            this.sell(p, -amount);
        if (this.isUnlimited()) {
            ItemStack[] contents = p.getInventory().getContents();
            for (int i = 0; amount > 0 && i < contents.length; i++) {
                ItemStack stack = contents[i];
                if (stack == null)
                    continue; // No item
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
                plugin.getLogger().log(Level.WARNING, "Could not take all items from a players inventory on purchase! " + p
                        .getName() + ", missing: " + amount + ", item: " + Util.getItemStackName(this.getItem()) + "!");
            }
        } else {
            ItemStack[] playerContents = p.getInventory().getContents();
            Inventory chestInv = this.getInventory();
            for (int i = 0; amount > 0 && i < playerContents.length; i++) {
                ItemStack item = playerContents[i];
                if (item != null && this.matches(item)) {
                    // Copy it, we don't want to interfere
                    item = item.clone();
                    // Amount = total, item.getAmount() = how many items in the
                    // stack
                    int stackSize = Math.min(amount, item.getAmount());
                    // If Amount is item.getAmount(), then this sets the amount
                    // to 0
                    // Else it sets it to the remainder
                    playerContents[i].setAmount(playerContents[i].getAmount() - stackSize);
                    // We can modify this, it is a copy.
                    item.setAmount(stackSize);
                    // Add the items to the players inventory
                    chestInv.addItem(item);
                    amount -= stackSize;
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
        if (result)
            Bukkit.getPluginManager().callEvent(new ShopModeratorChangedEvent(this, this.moderator));
        return result;
    }

    /**
     * Deletes the shop from the list of shops and queues it for database
     * deletion
     *
     * @param fromMemory True if you are *NOT* iterating over this currently, *false if
     *                   you are iterating*
     */
    public void delete(boolean fromMemory) {
        ShopDeleteEvent shopDeleteEvent = new ShopDeleteEvent(this, fromMemory);
        Bukkit.getPluginManager().callEvent(shopDeleteEvent);
        if (shopDeleteEvent.isCancelled()) {
            Util.debugLog("Shop deletion was canceled because a plugin canceled it.");
            return;
        }
        // Unload the shop
        if (isLoaded)
            this.onUnload();
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
        String world = this.getLocation().getWorld().getName();
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
                plugin.getDatabaseHelper().removeShop(plugin.getDatabase(), x, y, z, world);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void checkDisplay() {
        Util.debugLog("Checking the display...");
        if (!plugin.isDisplay())
            return;
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
                if (this.displayItem.checkDisplayIsMoved())
                    this.displayItem.fixDisplayMoved();
            }
        }
        /* Dupe is always need check, if enabled display */
        if (plugin.isDisplay())
            this.displayItem.removeDupe();
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
    public void remove(@NotNull ItemStack item, int amount) {
        if (this.unlimited)
            return;
        Inventory inv = this.getInventory();
        int remains = amount;
        while (remains > 0) {
            int stackSize = Math.min(remains, item.getMaxStackSize());
            item.setAmount(stackSize);
            inv.removeItem(item);
            remains = remains - stackSize;
        }
        this.setSignText();
    }

    /**
     * Changes the owner of this shop to the given player.
     *
     * @param owner the new owner
     */
    public void setOwner(@NotNull UUID owner) {
        this.moderator.setOwner(owner);
        Bukkit.getPluginManager().callEvent(new ShopModeratorChangedEvent(this, this.moderator));
        this.setSignText();
        update();
    }

    /**
     * @return The list of players who can manage the shop.
     */
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
    public ContainerShop clone() {
        return new ContainerShop(this);
    }

    /**
     * Sells amount of item to Player p. Does NOT check our inventory, or
     * balances
     *
     * @param p      The player to sell to
     * @param amount The amount to sell
     */
    public void sell(@NotNull Player p, int amount) {
        if (amount < 0)
            this.buy(p, -amount);
        // Items to drop on floor
        ArrayList<ItemStack> floor = new ArrayList<ItemStack>(5);
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
            ItemStack[] chestContents = this.getInventory().getContents();
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
    public ContainerShop getAttachedShop() {
        Block c = Util.getSecondHalf(this.getLocation().getBlock());
        if (c == null)
            return null;
        Shop shop = plugin.getShopManager().getShop(c.getLocation());
        return shop == null ? null : (ContainerShop) shop;
    }

    /**
     * Returns the display item associated with this shop.
     *
     * @return The display item associated with this shop.
     */
    public DisplayItem getDisplayItem() {
        return this.displayItem;
    }

    /**
     * @return The enchantments the shop has on its items.
     */
    public Map<Enchantment, Integer> getEnchants() {
        return this.item.getItemMeta().getEnchants();
    }

    /**
     * @return The chest this shop is based on.
     */
    public Inventory getInventory() {
        try {
            if (loc.getBlock().getState().getType() == Material.ENDER_CHEST && plugin.getOpenInvPlugin() != null) {
                OpenInv openInv = ((OpenInv) plugin.getOpenInvPlugin());
                return openInv.getSpecialEnderChest(openInv.loadPlayer(Bukkit.getOfflinePlayer(this.moderator.getOwner()
                )), Bukkit.getOfflinePlayer((this.moderator.getOwner())).isOnline()).getBukkitInventory();
            }
        } catch (Exception e) {
            Util.debugLog(e.getMessage());
            return null;
        }
        InventoryHolder container;
        try {
            container = (InventoryHolder) this.loc.getBlock().getState();
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
    public Material getMaterial() {
        return this.item.getType();
    }

    /**
     * Changes all lines of text on a sign near the shop
     *
     * @param lines The array of lines to change. Index is line number.
     */
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





    public void setUnlimited(boolean unlimited) {
        this.unlimited = unlimited;
        this.setSignText();
        update();
    }

    public boolean isUnlimited() {
        return this.unlimited;
    }

    public ShopType getShopType() {
        return this.shopType;
    }

    public boolean isBuying() {
        return this.shopType == ShopType.BUYING;
    }

    public boolean isSelling() {
        return this.shopType == ShopType.SELLING;
    }

    /**
     * Changes a shop type to Buying or Selling. Also updates the signs nearby.
     *
     * @param shopType The new type (ShopType.BUYING or ShopType.SELLING)
     */
    public void setShopType(@NotNull ShopType shopType) {
        this.shopType = shopType;
        this.setSignText();
        update();
    }

    /**
     * Updates signs attached to the shop
     */
    public void setSignText() {
        if (!Util.isLoaded(this.getLocation()))
            return;
        String[] lines = new String[4];
        lines[0] = MsgUtil.getMessage("signs.header", this.ownerName());
        if (this.isSelling()) {
            if (this.getRemainingStock() == -1) {
                lines[1] = MsgUtil.getMessage("signs.selling", "" + MsgUtil.getMessage("signs.unlimited"));
            } else {
                lines[1] = MsgUtil.getMessage("signs.selling", "" + this.getRemainingStock());
            }

        } else if (this.isBuying()) {
            if (this.getRemainingSpace() == -1) {
                lines[1] = MsgUtil.getMessage("signs.buying", "" + MsgUtil.getMessage("signs.unlimited"));

            } else {
                lines[1] = MsgUtil.getMessage("signs.buying", "" + this.getRemainingSpace());
            }

        }
        lines[2] = MsgUtil.getMessage("signs.item", Util.getItemStackName(this.getItem()));
        lines[3] = MsgUtil.getMessage("signs.price", Util.format(this.getPrice()));
        this.setSignText(lines);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Shop " + (loc.getWorld() == null ?
                "unloaded world" :
                loc.getWorld().getName()) + "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
        sb.append(" Owner: ").append(this.ownerName()).append(" - ").append(getOwner().toString());
        if (isUnlimited())
            sb.append(" Unlimited: true");
        sb.append(" Price: ").append(getPrice());
        sb.append(" Item: ").append(getItem().toString());
        return sb.toString();
    }

    /**
     * Returns a list of signs that are attached to this shop (QuickShop and
     * blank signs only)
     *
     * @return a list of signs that are attached to this shop (QuickShop and
     * blank signs only)
     */
    public List<Sign> getSigns() {
        List<Sign> signs = new ArrayList<Sign>(1);
        if (this.getLocation().getWorld() == null)
            return signs;
        Block[] blocks = new Block[4];
        blocks[0] = loc.getBlock().getRelative(BlockFace.EAST);
        blocks[1] = loc.getBlock().getRelative(BlockFace.NORTH);
        blocks[2] = loc.getBlock().getRelative(BlockFace.SOUTH);
        blocks[3] = loc.getBlock().getRelative(BlockFace.WEST);
        final String signHeader = MsgUtil.getMessage("signs.header", "");
        final String signHeader2 = MsgUtil.getMessage("sign.header", this.ownerName());

        for (Block b : blocks) {
            if (b == null) {
                plugin.getLogger().warning("Null signs in the queue, skipping");
                continue;
            }
            Material mat = b.getType();
            if (!Util.isWallSign(mat))
                continue;
            if (!isAttached(b))
                continue;
            if (!(b.getState() instanceof Sign))
                continue;
            org.bukkit.block.Sign sign = (org.bukkit.block.Sign) b.getState();
            String currentLine = sign.getLine(0);
            if (currentLine.contains(signHeader) || currentLine.contains(signHeader2)) {
                signs.add(sign);
            } else {
                boolean text = false;
                for (String s : sign.getLines()) {
                    if (!s.isEmpty()) {
                        text = true;
                        break;
                    }
                }
                if (!text)
                    signs.add(sign);
            }
        }
        return signs;
    }

    public boolean isAttached(@NotNull Block b) {
        return this.getLocation().getBlock().equals(Util.getAttached(b));
    }

    /**
     * Deletes the shop from the list of shops and queues it for database
     */
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
     * Check shop is or not still Valid.
     *
     * @return isValid
     */
    public boolean isValid() {
        checkDisplay();
        return Util.canBeShop(this.getLocation().getBlock());
    }

    /**
     * Load ContainerShop.
     */
    public void onLoad() {
        if (this.isLoaded) {
            Util.debugLog("Dupe load request, canceled.");
            return;
        }
        ShopLoadEvent shopLoadEvent = new ShopLoadEvent(this);
        Bukkit.getPluginManager().callEvent(shopLoadEvent);
        if (shopLoadEvent.isCancelled())
            return;

        this.isLoaded = true;
        plugin.getShopManager().getLoadedShops().add(this);

        if (!Util.canBeShop(this.getLocation().getBlock())) {
            this.onUnload();
            this.delete();
            return;
        }

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
    public void onUnload() {
        if (!this.isLoaded) {
            Util.debugLog("Dupe unload request, canceled.");
            return;
        }
        if (this.getDisplayItem() != null) {
            this.getDisplayItem().remove();
        }
        this.isLoaded = false;
        plugin.getShopManager().getLoadedShops().remove(this);
        ShopUnloadEvent shopUnloadEvent = new ShopUnloadEvent(this);
        Bukkit.getPluginManager().callEvent(shopUnloadEvent);
    }

    public void onClick() {
        this.setSignText();
        this.checkDisplay();
    }

    public String ownerName() {
        if (this.isUnlimited())
            return MsgUtil.getMessage("admin-shop");

        if (this.getOwner() == null)
            return MsgUtil.getMessage("unknown-owner");
        String name = Bukkit.getOfflinePlayer(this.getOwner()).getName();
        if (name == null || name.isEmpty())
            return MsgUtil.getMessage("unknown-owner");
        return name;
    }

    @Override
    public ShopModerator getModerator() {
        return this.moderator.clone();
    }

    @Override
    public void setModerator(ShopModerator shopModerator) {
        this.moderator = shopModerator.clone();
        update();
        Bukkit.getPluginManager().callEvent(new ShopModeratorChangedEvent(this, this.moderator));
    }

    @Override
    public boolean isLoaded() {
        return this.isLoaded;
    }

}
