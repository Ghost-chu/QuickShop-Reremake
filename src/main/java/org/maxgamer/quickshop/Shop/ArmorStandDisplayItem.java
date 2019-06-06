package org.maxgamer.quickshop.Shop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

public class ArmorStandDisplayItem implements DisplayItem {
    private QuickShop plugin = QuickShop.instance;
    private ItemStack iStack;
    private ItemStack guardedIstack;
    private ArmorStand armorStand;
    private Shop shop;

    public ArmorStandDisplayItem(Shop shop) {
        this.shop = shop;
        this.iStack = shop.getItem().clone();
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

        if (armorStand != null && armorStand.isValid() && !armorStand.isDead())
            Util.debugLog("Warning: Spawning the armorStand for DisplayItem when already have a exist one armorStand, This may cause dupe armorStand!");

        ShopDisplayItemSpawnEvent shopDisplayItemSpawnEvent = new ShopDisplayItemSpawnEvent(shop, iStack);
        Bukkit.getPluginManager().callEvent(shopDisplayItemSpawnEvent);
        if (shopDisplayItemSpawnEvent.isCancelled()) {
            Util.debugLog("Cancelled the displayItem spawning cause a plugin setCancelled the spawning event, usually is QuickShop Addon");
            return;
        }
        this.armorStand = (ArmorStand) this.shop.getLocation().getWorld()
                .spawnEntity(getDisplayLocation(), EntityType.ARMOR_STAND);
        //Set basic armorstand datas.
        this.armorStand.setArms(false);
        this.armorStand.setBasePlate(false);
        this.armorStand.setVisible(false);
        this.armorStand.setGravity(false);
        this.armorStand.setSilent(true);
        this.armorStand.setAI(false);
        //Set armorstand item in hand
        this.armorStand.setItemInHand(iStack);
        //Set safeGuard
        safeGuard(this.armorStand);
        //Set pose
        setPoseForArmorStand();
        Util.debugLog("Spawned new ArmorStand DisplayItem for shop " + shop.getLocation().toString());
    }

    public void safeGuard(Entity entity) {
        if (!(entity instanceof ArmorStand)) {
            Util.debugLog("Failed to safeGuard " + entity.getLocation().toString() + ", cause target not a ArmorStand");
            return;
        }
        ArmorStand armorStand = (ArmorStand) entity;
        //Set item protect in the armorstand's hand
        this.guardedIstack = DisplayItem.createGuardItemStack(this.iStack);
        armorStand.setItemInHand(guardedIstack);
        Util.debugLog("Successfully safeGuard ArmorStand: " + armorStand.getLocation().toString());
    }

    private void setPoseForArmorStand() {
        //TODO
    }

    public void remove() {
        if (this.armorStand == null || !this.armorStand.isValid() || this.armorStand.isDead()) {
            Util.debugLog("Ignore the armorStand removeing cause this armorStand already gone.");
            return;
        }
        this.armorStand.remove();
        this.armorStand = null;
        this.guardedIstack = null;

    }

    public void respawn() {
        remove();
        spawn();
    }

    public Entity getDisplay() {
        return this.armorStand;
    }

    @Override
    public Location getDisplayLocation() {
        return this.shop.getLocation().clone().add(0.5, 1.2, 0.5);
    }
}
