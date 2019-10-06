package org.maxgamer.quickshop.Shop;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

import java.util.ArrayList;

/**
 * @author Netherfoam A display item, that spawns a block above the chest and
 * cannot be interacted with.
 */
public interface DisplayItem {

    /**
     * Check the itemStack is contains protect flag.
     *
     * @param itemStack Target ItemStack
     * @param shop The shop you want check the this guarded item is that shop or not.
     * @return Contains protect flag.
     */
    static boolean checkIsGuardItemStack(@Nullable ItemStack itemStack, @Nullable Shop shop) {
        if (itemStack == null) {
            return false;
        }
        itemStack = itemStack.clone();
        itemStack.setAmount(1);
        if (!itemStack.hasItemMeta()) {
            return false;
        }
        ItemMeta iMeta = itemStack.getItemMeta();
        if (iMeta.hasDisplayName()) {
            if (iMeta.getDisplayName().toLowerCase().contains("quickshop displayitem")) {
                return true;
            }
        }
        try{
            DisplayItemMarker marker = iMeta.getPersistentDataContainer().get(new NamespacedKey(QuickShop.instance, "QuickShopDisplay"),DisplayItemMarkerDataType.INSTANCE);
            if(marker == null){
                return false;
            }
            if(shop == null){
                return true;
            }else{
                return marker.getShop().equals(shop);
            }
        }catch (Throwable ignore){} //1.14 new api
        if (!iMeta.hasLore()) {
            return false;
        }
        for (String lore :  iMeta.getLore()) {
            try {
                lore = ChatColor.stripColor(lore);
                if (!lore.startsWith("{")) {
                    continue;
                }
                Gson gson = new Gson();
                ShopProtectionFlag shopProtectionFlag = gson.fromJson(lore, ShopProtectionFlag.class);
                if (shopProtectionFlag == null) {
                    continue;
                }
                if (ShopProtectionFlag.getDefaultMark().equals(shopProtectionFlag.getMark())) {
                    return true;
                }
                if(shop == null){
                    if (shopProtectionFlag.getShopLocation() != null) {
                        return true;
                    }
                }else{
                    if (shopProtectionFlag.getShopLocation() != null&&shopProtectionFlag.getShopLocation().equals(shop.getLocation().toString())) {
                        return true;
                    }
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
     * Check the itemStack is contains protect flag.
     *
     * @param itemStack Target ItemStack
     * @return Contains protect flag.
     */
    static boolean checkIsGuardItemStack(@Nullable ItemStack itemStack) {
        return checkIsGuardItemStack(itemStack,null);
    }
    /**
     * Check the itemStack is target shop's display
     *
     * @param itemStack Target ItemStack
     * @param shop      Target shop
     * @return Is target shop's display
     */
    static boolean checkIsTargetShopDisplay(@NotNull ItemStack itemStack, @NotNull Shop shop) {
        return checkIsGuardItemStack(itemStack,shop);
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
            iMeta.setDisplayName("QuickShop DisplayItem");
        } else {
            iMeta.setDisplayName(null);
        }
        java.util.List<String> lore = new ArrayList<String>();
        Gson gson = new Gson();
        ShopProtectionFlag shopProtectionFlag = new ShopProtectionFlag(shop.getLocation().toString(), Util.serialize(itemStack));
        String protectFlag = gson.toJson(shopProtectionFlag);
        for (int i = 0; i < 21; i++) {
            lore.add(protectFlag); //Create 20 lines lore to make sure no stupid plugin accident remove mark.
        }
        iMeta.setLore(lore);
        try{
            PersistentDataContainer container = iMeta.getPersistentDataContainer();
            container.set(new NamespacedKey(QuickShop.instance, "QuickShopDisplay"),DisplayItemMarkerDataType.INSTANCE,new DisplayItemMarker(shop));
        }catch (Throwable ignore){}//1.14 data api
        itemStack.setItemMeta(iMeta);
        return itemStack;
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
