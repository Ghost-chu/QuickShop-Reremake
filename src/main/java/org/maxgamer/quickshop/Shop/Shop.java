package org.maxgamer.quickshop.Shop;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Shop {
    /**
     * Clone new shop object.
     * Not a deep clone.
     *
     * @return New shop object
     */
    Shop clone();

    /**
     * Check the display location, and teleport, respawn if needs.
     */
    void checkDisplay();

    /**
     * Get shop remaining stock.
     * @return Remaining stock.
     */
    int getRemainingStock();

    /**
     * Get shop remaining space.
     * @return Remaining space.
     */
    int getRemainingSpace();

    /**
     * Check the target ItemStack is matches with this shop's item.
     * @param paramItemStack Target ItemStack.
     * @return Matches
     */
    boolean matches(ItemStack paramItemStack);

    /**
     * Get shop's location
     * @return Shop's location
     */
    Location getLocation();

    /**
     * Get shop's price
     * @return Price
     */
    double getPrice();

    /**
     * Set shop's new price
     * @param paramDouble New price
     */
    void setPrice(double paramDouble);

    /**
     * Update shop data to database
     */
    void update();

    /**
     * Get shop's item durability, if have.
     * @return Shop's item durability
     */
    short getDurability();

    /**
     * Get shop's owner UUID
     * @return Shop's owner UUID, can use Bukkit.getOfflinePlayer to convert to the OfflinePlayer.
     */
    UUID getOwner();

    /**
     * Get shop item's ItemStack
     * @return The shop's ItemStack
     */
    ItemStack getItem();

    /**
     * Remove x ItemStack from the shop inventory
     * @param paramItemStack Want removed ItemStack
     * @param paramInt Want remove how many
     */
    void remove(ItemStack paramItemStack, int paramInt);

    /**
     * Add x ItemStack to the shop inventory
     * @param paramItemStack The ItemStack you want add
     * @param paramInt How many you want add
     */
    void add(ItemStack paramItemStack, int paramInt);

    /**
     * Execute sell action for player with x items.
     * @param paramPlayer Target player
     * @param paramInt How many sold?
     */
    void sell(Player paramPlayer, int paramInt);

    /**
     * Execute buy action for player with x items.
     * @param paramPlayer Target player
     * @param paramInt How many buyed?
     */
    void buy(Player paramPlayer, int paramInt);

    /**
     * Set new owner to the shop's owner
     * @param paramString New owner UUID
     */
    void setOwner(UUID paramString);

    /**
     * Set shop is or not Unlimited Mode (Admin Shop)
     * @param paramBoolean status
     */
    void setUnlimited(boolean paramBoolean);

    /**
     * Get shop is or not in Unlimited Mode (Admin Shop)
     * @return yes or not
     */
    boolean isUnlimited();

    /**
     * Get shop type
     * @return shop type
     */
    ShopType getShopType();

    /**
     * Get shop is or not in buying mode
     * @return yes or no
     */
    boolean isBuying();

    /**
     * Get shop is or not in selling mode
     * @return yes or no
     */
    boolean isSelling();

    /**
     * Set new shop type for this shop
     * @param paramShopType New shop type
     */
    void setShopType(ShopType paramShopType);

    /**
     * Generate new sign texts on shop's sign.
     */
    void setSignText();

    /**
     * Set texts on shop's sign
     * @param paramArrayOfString The texts you want set
     */
    void setSignText(String[] paramArrayOfString);

    /**
     * Get shop signs, may have multi signs
     * @return Signs for the shop
     */
    List<Sign> getSigns();

    /**
     * Check shop is or not attacked the target block
     * @param paramBlock Target block
     * @return isAttached
     */
    boolean isAttached(Block paramBlock);

    /**
     * Delete shop from ram, and database.
     */
    void delete();

    /**
     * Delete shop from ram or ram and database
     * @param paramBoolean true = only delete from ram, false = delete from both ram and database
     */
    void delete(boolean paramBoolean);

    /**
     * Shop is valid
     * @return status
     */
    boolean isValid();

    /**
     * Unload shop from world
     */
    void onUnload();

    /**
     * Load shop to the world
     */
    void onLoad();

    /**
     * Execute codes when player click the shop will did things
     */
    void onClick();

    /**
     * Get shop's owner name, it will return owner name or Admin Shop(i18n) when it is unlimited
     * @return owner name
     */
    String ownerName();

    /**
     * Return this shop's moderators
     * @return Shop moderators
     */
    ShopModerator getModerator();

    /**
     * Set new shop's moderators
     * @param shopModerator New moderators team you want set
     */
    void setModerator(ShopModerator shopModerator);

    /**
     * Add new staff to the moderators
     * @param player New staff
     * @return Success
     */
    boolean addStaff(UUID player);

    /**
     * Remove a staff from moderators
     * @param player Staff
     * @return Success
     */
    boolean delStaff(UUID player);

    /**
     * Empty moderators team.
     */
    void clearStaffs();

    /**
     * Get this container shop is loaded or unloaded.
     * @return Loaded
     */
    boolean isLoaded();

     /**
     * Directly get all staffs.
     * @return staffs
     */
     ArrayList<UUID> getStaffs();


}