package org.maxgamer.quickshop.Shop;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract interface Shop {
    /**
     * Clone new shop object.
     * Not a deep clone.
     *
     * @return New shop object
     */
    public abstract Shop clone();

    /**
     * Check the display location, and teleport, respawn if needs.
     */
    public abstract void checkDisplay();

    /**
     * Get shop remaining stock.
     * @return Remaining stock.
     */
    public abstract int getRemainingStock();

    /**
     * Get shop remaining space.
     * @return Remaining space.
     */
    public abstract int getRemainingSpace();

    /**
     * Check the target ItemStack is matches with this shop's item.
     * @param paramItemStack Target ItemStack.
     * @return Matches
     */
    public abstract boolean matches(ItemStack paramItemStack);

    /**
     * Get shop's location
     * @return Shop's location
     */
    public abstract Location getLocation();

    /**
     * Get shop's price
     * @return Price
     */
    public abstract double getPrice();

    /**
     * Set shop's new price
     * @param paramDouble New price
     */
    public abstract void setPrice(double paramDouble);

    /**
     * Update shop data to database
     */
    public abstract void update();

    /**
     * Get shop's item durability, if have.
     * @return Shop's item durability
     */
    public abstract short getDurability();

    /**
     * Get shop's owner UUID
     * @return Shop's owner UUID, can use Bukkit.getOfflinePlayer to convert to the OfflinePlayer.
     */
    public abstract UUID getOwner();

    /**
     * Get shop item's ItemStack
     * @return The shop's ItemStack
     */
    public abstract ItemStack getItem();

    /**
     * Remove x ItemStack from the shop inventory
     * @param paramItemStack Want removed ItemStack
     * @param paramInt Want remove how many
     */
    public abstract void remove(ItemStack paramItemStack, int paramInt);

    /**
     * Add x ItemStack to the shop inventory
     * @param paramItemStack The ItemStack you want add
     * @param paramInt How many you want add
     */
    public abstract void add(ItemStack paramItemStack, int paramInt);

    /**
     * Execute sell action for player with x items.
     * @param paramPlayer Target player
     * @param paramInt How many sold?
     */
    public abstract void sell(Player paramPlayer, int paramInt);

    /**
     * Execute buy action for player with x items.
     * @param paramPlayer Target player
     * @param paramInt How many buyed?
     */
    public abstract void buy(Player paramPlayer, int paramInt);

    /**
     * Set new owner to the shop's owner
     * @param paramString New owner UUID
     */
    public abstract void setOwner(UUID paramString);

    /**
     * Set shop is or not Unlimited Mode (Admin Shop)
     * @param paramBoolean status
     */
    public abstract void setUnlimited(boolean paramBoolean);

    /**
     * Get shop is or not in Unlimited Mode (Admin Shop)
     * @return yes or not
     */
    public abstract boolean isUnlimited();

    /**
     * Get shop type
     * @return shop type
     */
    public abstract ShopType getShopType();

    /**
     * Get shop is or not in buying mode
     * @return yes or no
     */
    public abstract boolean isBuying();

    /**
     * Get shop is or not in selling mode
     * @return yes or no
     */
    public abstract boolean isSelling();

    /**
     * Set new shop type for this shop
     * @param paramShopType New shop type
     */
    public abstract void setShopType(ShopType paramShopType);

    /**
     * Generate new sign texts on shop's sign.
     */
    public abstract void setSignText();

    /**
     * Set texts on shop's sign
     * @param paramArrayOfString The texts you want set
     */
    public abstract void setSignText(String[] paramArrayOfString);

    /**
     * Get shop signs, may have multi signs
     * @return Signs for the shop
     */
    public abstract List<Sign> getSigns();

    /**
     * Check shop is or not attacked the target block
     * @param paramBlock Target block
     * @return isAttached
     */
    public abstract boolean isAttached(Block paramBlock);

    /**
     * Get item will show on the sign
     *
     * @return The text will show on the sign
     */
    @Deprecated
    public abstract String getDataName();

    /**
     * Delete shop from ram, and database.
     */
    public abstract void delete();

    /**
     * Delete shop from ram or ram and database
     * @param paramBoolean true = only delete from ram, false = delete from both ram and database
     */
    public abstract void delete(boolean paramBoolean);

    /**
     * Shop is valid
     * @return status
     */
    public abstract boolean isValid();

    /**
     * Unload shop from world
     */
    public abstract void onUnload();

    /**
     * Load shop to the world
     */
    public abstract void onLoad();

    /**
     * Execute codes when player click the shop will did things
     */
    public abstract void onClick();

    /**
     * Get shop's owner name, it will return owner name or Admin Shop(i18n) when it is unlimited
     * @return owner name
     */
    public abstract String ownerName();

    /**
     * Return this shop's moderators
     * @return Shop moderators
     */
    public abstract ShopModerator getModerator();

    /**
     * Set new shop's moderators
     * @param shopModerator New moderators team you want set
     */
    public abstract void setModerator(ShopModerator shopModerator);

    /**
     * Add new staff to the moderators
     * @param player New staff
     * @return Success
     */
    public abstract boolean addStaff(UUID player);

    /**
     * Remove a staff from moderators
     * @param player Staff
     * @return Success
     */
    public abstract boolean delStaff(UUID player);

    /**
     * Empty moderators team.
     */
    public abstract void clearStaffs();

    /**
     * Get this container shop is loaded or unloaded.
     */
    public abstract boolean isLoaded();

    /**
     /**
     * Directly get all staffs.
     * @return staffs
     */
    public abstract ArrayList<UUID> getStaffs();

    public static ItemStack createGuardItemStack(ItemStack itemStack) {

    }

}