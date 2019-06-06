package org.maxgamer.quickshop.Shop;

import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

public class ArmorStandDisplayItem implements DisplayItem {
    private QuickShop plugin = QuickShop.instance;
    private ItemStack iStack;
    private ArmorStand armorStand;
    private Shop shop;

    public ArmorStandDisplayItem(Shop shop, ItemStack iStack) {
        this.shop = shop;
        this.iStack = iStack.clone();
    }

    public void spawn() {
        if (shop.getLocation().getWorld() == null) {
            Util.debugLog("Cancelled the displayItem spawning cause location world is null.");
            return;
        }

        if (iStack == null) {
            Util.debugLog("Cancelled the displayItem spawning cause ItemStack is null.");
            return;
        }

        ShopDisplayItemSpawnEvent shopDisplayItemSpawnEvent = new ShopDisplayItemSpawnEvent(shop, iStack);
        Bukkit.getPluginManager().callEvent(shopDisplayItemSpawnEvent);
        if (shopDisplayItemSpawnEvent.isCancelled()) {
            Util.debugLog("Cancelled the displayItem spawning cause a plugin setCancelled the spawning event, usually is QuickShop Addon");
            return;
        }

        this.armorStand = (ArmorStand) this.shop.getLocation().getWorld().spawnEntity(this.shop
                .getLocation(), EntityType.ARMOR_STAND);
        //Set basic armorstand datas.
        this.armorStand.setArms(false);
        this.armorStand.setBasePlate(false);
        this.armorStand.setVisible(false);
        this.armorStand.setGravity(false);
        this.armorStand.setSilent(true);
        this.armorStand.setAI(false);
        //Set safeGuard

    }
}
