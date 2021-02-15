/*
 * This file is a part of project QuickShop, the name is Shop.java
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

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface Shop {
    /**
     * Add x ItemStack to the shop inventory
     *
     * @param paramItemStack The ItemStack you want add
     * @param paramInt       How many you want add
     */
    void add(@NotNull ItemStack paramItemStack, int paramInt);

    /**
     * Add new staff to the moderators
     *
     * @param player New staff
     * @return Success
     */
    boolean addStaff(@NotNull UUID player);

    /**
     * Execute buy action for player with x items.
     *
     * @param buyer          The player buying
     * @param buyerInventory The buyer inventory ( may not a player inventory )
     * @param paramInt       How many buyed?
     */
    void buy(@NotNull UUID buyer, @NotNull Inventory buyerInventory, @NotNull Location loc2Drop, int paramInt);

    /**
     * Check the display location, and teleport, respawn if needs.
     */
    void checkDisplay();

    /**
     * Empty moderators team.
     */
    void clearStaffs();

    /**
     * Clone new shop object. Not a deep clone.
     *
     * @return New shop object
     */
    @NotNull
    Shop clone();

    /**
     * Remove a staff from moderators
     *
     * @param player Staff
     * @return Success
     */
    boolean delStaff(@NotNull UUID player);

    /**
     * Delete shop from ram, and database.
     */
    void delete();

    /**
     * Delete shop from ram or ram and database
     *
     * @param memoryOnly true = only delete from ram, false = delete from both ram and database
     */
    void delete(boolean memoryOnly);

    /**
     * Check shop is or not attacked the target block
     *
     * @param paramBlock Target block
     * @return isAttached
     */
    boolean isAttached(@NotNull Block paramBlock);

    /**
     * Check the target ItemStack is matches with this shop's item.
     *
     * @param paramItemStack Target ItemStack.
     * @return Matches
     */
    boolean matches(@NotNull ItemStack paramItemStack);

    /**
     * Execute codes when player click the shop will did things
     */
    void onClick();

    /**
     * Load shop to the world
     */
    void onLoad();

    /**
     * Unload shop from world
     */
    void onUnload();

    /**
     * Get shop's owner name, it will return owner name or Admin Shop(i18n) when it is unlimited
     *
     * @param forceUsername Force returns username of shop
     * @return owner name
     */
    @NotNull
    String ownerName(boolean forceUsername);

    /**
     * Get shop's owner name, it will return owner name or Admin Shop(i18n) when it is unlimited
     *
     * @return owner name
     */
    @NotNull
    String ownerName();

    /**
     * Remove x ItemStack from the shop inventory
     *
     * @param paramItemStack Want removed ItemStack
     * @param paramInt       Want remove how many
     */
    void remove(@NotNull ItemStack paramItemStack, int paramInt);

    /**
     * Execute sell action for player with x items.
     *
     * @param seller          Seller
     * @param sellerInventory Seller's inventory ( may not a player inventory )
     * @param paramInt        How many sold?
     */
    void sell(@NotNull UUID seller, @NotNull Inventory sellerInventory, @NotNull Location loc2Drop, int paramInt);

    /**
     * Generate new sign texts on shop's sign.
     */
    void setSignText();

    /**
     * Get sign texts on shop's sign.
     *
     * @return String arrays represents sign texts:
     * Index | Content
     * Line 0: Header
     * Line 1: Shop Type
     * Line 2: Shop Item Name
     * Line 3: Price
     */
    @NotNull
    default String[] getSignText() {
        //backward support
        throw new UnsupportedOperationException();
    }

    /**
     * Set texts on shop's sign
     *
     * @param paramArrayOfString The texts you want set
     */
    void setSignText(@NotNull String[] paramArrayOfString);

    /**
     * Update shop data to database
     */
    void update();

    /**
     * Get shop's item durability, if have.
     *
     * @return Shop's item durability
     */
    short getDurability();

    /**
     * Get shop item's ItemStack
     *
     * @return The shop's ItemStack
     */
    @NotNull
    ItemStack getItem();

    /**
     * Set shop item's ItemStack
     *
     * @param item ItemStack to set
     */
    void setItem(@NotNull ItemStack item);

    /**
     * Refresh shop sign and display item
     */
    void refresh();

    /**
     * Get shop's location
     *
     * @return Shop's location
     */
    @NotNull
    Location getLocation();

    /**
     * Return this shop's moderators
     *
     * @return Shop moderators
     */
    @NotNull
    ShopModerator getModerator();

    /**
     * Set new shop's moderators
     *
     * @param shopModerator New moderators team you want set
     */
    void setModerator(@NotNull ShopModerator shopModerator);

    /**
     * Get shop's owner UUID
     *
     * @return Shop's owner UUID, can use Bukkit.getOfflinePlayer to convert to the OfflinePlayer.
     */
    @NotNull
    UUID getOwner();

    /**
     * Set new owner to the shop's owner
     *
     * @param paramString New owner UUID
     */
    void setOwner(@NotNull UUID paramString);

    /**
     * Get shop's price
     *
     * @return Price
     */
    double getPrice();

    /**
     * Set shop's new price
     *
     * @param paramDouble New price
     */
    void setPrice(double paramDouble);

    /**
     * Get shop remaining space.
     *
     * @return Remaining space.
     */
    int getRemainingSpace();

    /**
     * Get shop remaining stock.
     *
     * @return Remaining stock.
     */
    int getRemainingStock();

    /**
     * Get shop type
     *
     * @return shop type
     */
    @NotNull
    ShopType getShopType();

    /**
     * Set new shop type for this shop
     *
     * @param paramShopType New shop type
     */
    void setShopType(@NotNull ShopType paramShopType);

    /**
     * Get shop signs, may have multi signs
     *
     * @return Signs for the shop
     */
    @NotNull
    List<Sign> getSigns();

    /**
     * Directly get all staffs.
     *
     * @return staffs
     */
    @NotNull
    List<UUID> getStaffs();

    /**
     * Get shop is or not in buying mode
     *
     * @return yes or no
     */
    boolean isBuying();

    /**
     * Get this container shop is loaded or unloaded.
     *
     * @return Loaded
     */
    boolean isLoaded();

    /**
     * Get shop is or not in selling mode
     *
     * @return yes or no
     */
    boolean isSelling();

    /**
     * Get shop is or not in Unlimited Mode (Admin Shop)
     *
     * @return yes or not
     */
    boolean isUnlimited();

    /**
     * Set shop is or not Unlimited Mode (Admin Shop)
     *
     * @param paramBoolean status
     */
    void setUnlimited(boolean paramBoolean);

    /**
     * Whether Shop is valid
     *
     * @return status
     */
    boolean isValid();

    /**
     * Whether Shop is deleted
     *
     * @return status
     */
    boolean isDeleted();

    /**
     * Get the shop display entity
     *
     * @return The entity for shop display.
     */
    @Nullable
    DisplayItem getDisplay();

    /**
     * Gets if shop is dirty
     * (so shop will be save)
     *
     * @return Is dirty
     */
    boolean isDirty();

    /**
     * Sets dirty status
     *
     * @param isDirty Shop is dirty
     */
    void setDirty(boolean isDirty);

    /**
     * Sets shop is dirty
     */
    void setDirty();


    /**
     * Save the plugin extra data to Json format
     *
     * @return The json string
     */
    @NotNull
    String saveExtraToJson();

    /**
     * Gets the plugin's k-v map to storage the data.
     * It is spilt by plugin name, different name have different map, the data won't conflict.
     * But if you plugin name is too common, add a prefix will be a good idea.
     *
     * @param plugin Plugin instance
     * @return The data table
     */
    @NotNull
    Map<String, Object> getExtra(@NotNull Plugin plugin);

    /**
     * Gets ExtraManager to quick access extra data
     *
     * @param plugin Plugin instance
     * @return The Extra data manager
     */
    @NotNull
    ShopExtraManager getExtraManager(@NotNull Plugin plugin);

    /**
     * Save the extra data to the shop.
     *
     * @param plugin Plugin instace
     * @param data   The data table
     */
    void setExtra(@NotNull Plugin plugin, @NotNull Map<String, Object> data);

    /**
     * Gets shop status is stacking shop
     *
     * @return The shop stacking status
     */
    boolean isStackingShop();

    /**
     * WARNING: This UUID will changed after plugin reload, shop reload or server restart
     * DO NOT USE IT TO STORE DATA!
     *
     * @return Random UUID
     */
    @NotNull UUID getRuntimeRandomUniqueId();

    /**
     * Gets the currency that shop use
     *
     * @return The currency name
     */
    @Nullable
    String getCurrency();

    /**
     * Sets the currency that shop use
     *
     * @param currency The currency name; null to use default currency
     */
    void setCurrency(@Nullable String currency);

}
