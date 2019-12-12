package org.maxgamer.quickshop.Shop;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Netherfoam A display item, that spawns a block above the chest and
 * cannot be interacted with.
 */
public interface DisplayItem {

//    /**
//    * constant literal to check if item is quickshop display item
//    */
//    static final String QUICKSHOP_DISPLAY_ITEM = "quickshop display item";

    /**
     * Check the itemStack is contains protect flag.
     *
     * @param itemStack Target ItemStack
     * @return Contains protect flag.
     */
    static boolean checkIsGuardItemStack(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        itemStack = itemStack.clone();
        itemStack.setAmount(1);
        if (!itemStack.hasItemMeta()) {
            return false;
        }
        ItemMeta iMeta = itemStack.getItemMeta();
//        if (iMeta.hasDisplayName()) {
//            if (iMeta.getDisplayName().toLowerCase().contains(QUICKSHOP_DISPLAY_ITEM)) {
//                return true;
//            }
//        }
        if (!Objects.requireNonNull(iMeta).hasLore()) {
            return false;
        }
        List<String> lores = iMeta.getLore();
        Gson gson = new Gson();
        for (String lore : Objects.requireNonNull(lores)) {
            try {
                if (!lore.startsWith("{")) {
                    continue;
                }
                ShopProtectionFlag shopProtectionFlag = gson.fromJson(lore, ShopProtectionFlag.class);
                if (shopProtectionFlag == null) {
                    continue;
                }
                if (ShopProtectionFlag.getDefaultMark().equals(shopProtectionFlag.getMark())) {
                    return true;
                }
                if (shopProtectionFlag.getShopLocation() != null) {
                    return true;
                }
                if (shopProtectionFlag.getItemStackString() != null) {
                    return true;
                }
            } catch (JsonSyntaxException e) {
                //Ignore
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
    static boolean checkIsTargetShopDisplay(@NotNull ItemStack itemStack, @NotNull Shop shop) {
        itemStack = itemStack.clone();
        itemStack.setAmount(1);
        if (!itemStack.hasItemMeta()) {
            return false;
        }
        ItemMeta iMeta = itemStack.getItemMeta();
        if (iMeta == null) {
            return false;
        }
        if (!iMeta.hasLore()) {
            return false;
        }
        List<String> lores = iMeta.getLore();
        Gson gson = new Gson();
        for (String lore : Objects.requireNonNull(lores)) {
            try {
                if (!lore.startsWith("{")) {
                    continue;
                }
                ShopProtectionFlag shopProtectionFlag = gson.fromJson(lore, ShopProtectionFlag.class);
                if (shopProtectionFlag == null) {
                    continue;
                }
                if (!shopProtectionFlag.getMark().equals(ShopProtectionFlag.getDefaultMark())) {
                    continue;
                }
                if (shopProtectionFlag.getShopLocation().equals(shop.getLocation().toString())) {
                    return true;
                }
            } catch (JsonSyntaxException e) {
                //Ignore
            }
        }
        return false;
    }

    /**
     * Create a new itemStack with protect flag.
     *
     * @param itemStack Old itemStack
     * @param shop      The shop
     * @return New itemStack with protect flag.
     */
    static ItemStack createGuardItemStack(@NotNull ItemStack itemStack, @NotNull Shop shop) {
        itemStack = itemStack.clone();
        itemStack.setAmount(1);
        ItemMeta iMeta = itemStack.getItemMeta();
        if (QuickShop.instance.getConfig().getBoolean("shop.display-item-use-name")) {
			if (Objects.requireNonNull(iMeta).hasDisplayName()) {
				iMeta.setDisplayName(iMeta.getDisplayName());
			} else {
				iMeta.setDisplayName(Util.getItemStackName(itemStack));
			}
        } else {
            Objects.requireNonNull(iMeta).setDisplayName(null);
        }
        java.util.List<String> lore = new ArrayList<>();
        Gson gson = new Gson();
        ShopProtectionFlag shopProtectionFlag = createShopProtectionFlag(itemStack,shop);
        String protectFlag = gson.toJson(shopProtectionFlag);
        for (int i = 0; i < 21; i++) {
            lore.add(protectFlag); //Create 20 lines lore to make sure no stupid plugin accident remove mark.
        }
        iMeta.setLore(lore);
        itemStack.setItemMeta(iMeta);
        return itemStack;
    }

    /**
     * Create the shop protection flag for display item.
     * @param itemStack The item stack
     * @param shop The shop
     * @return ShopProtectionFlag obj
     */
    static ShopProtectionFlag createShopProtectionFlag(@NotNull ItemStack itemStack, @NotNull Shop shop){
        return new ShopProtectionFlag(shop.getLocation().toString(), Util.serialize(itemStack));
    }

    /**
     * Check the display is or not moved.
     *
     * @return Moved
     */
    boolean checkDisplayIsMoved();

    /**
     * Check the display is or not need respawn
     *
     * @return Need
     */
    boolean checkDisplayNeedRegen();

    /**
     * Check target Entity is or not a QuickShop display Entity.
     *
     * @param entity Target entity
     * @return Is or not
     */
    boolean checkIsShopEntity(Entity entity);

    /**
     * Fix the display moved issue.
     */
    void fixDisplayMoved();

    /**
     * Fix display need respawn issue.
     */
    void fixDisplayNeedRegen();

    /**
     * Remove the display entity.
     */
    void remove();

    /**
     * Remove this shop's display in the whole world.(Not whole server)
     *
     * @return Success
     */
    boolean removeDupe();

    /**
     * Respawn the displays, if it not exist, it will spawn new one.
     */
    void respawn();

    /**
     * Add the protect flags for entity or entity's hand item.
     * Target entity will got protect by QuickShop
     *
     * @param entity Target entity
     */
    void safeGuard(Entity entity);

    /**
     * Spawn new Displays
     */
    void spawn();

    /**
     * Get the display entity
     *
     * @return Target entity
     */
    Entity getDisplay();

    /**
     * Get display should at location.
     * Not display current location.
     *
     * @return Should at
     */
    Location getDisplayLocation();

    /**
     * Get plugin now is using which one DisplayType
     *
     * @return Using displayType.
     */
    static DisplayType getNowUsing() {
        return DisplayType.fromID(QuickShop.instance.getConfig().getInt("shop.display-type"));
    }

    /**
     * Check the display is or not already spawned
     *
     * @return Spawned
     */
    boolean isSpawned();

}
