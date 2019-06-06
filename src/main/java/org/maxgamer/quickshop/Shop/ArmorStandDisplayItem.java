package org.maxgamer.quickshop.Shop;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

public class ArmorStandDisplayItem implements DisplayItem {
    private QuickShop plugin = QuickShop.instance;
    private ItemStack originalItemStack;
    private ItemStack guardedIstack;
    private ArmorStand armorStand;
    private Shop shop;

    public ArmorStandDisplayItem(Shop shop) {
        this.shop = shop;
        this.originalItemStack = shop.getItem().clone();
    }

    @Override
    public void spawn() {
        if (shop.getLocation().getWorld() == null) {
            Util.debugLog("Cancelled the displayItem spawning cause location world is null.");
            return;
        }

        if (originalItemStack == null) {
            Util.debugLog("Cancelled the displayItem spawning cause ItemStack is null.");
            return;
        }

        if (armorStand != null && armorStand.isValid() && !armorStand.isDead()) {
            Util.debugLog("Warning: Spawning the armorStand for DisplayItem when already have a exist one armorStand, This may cause dupe armorStand!");
            StackTraceElement[] traces = Thread.currentThread().getStackTrace();
            for (StackTraceElement trace : traces) {
                Util.debugLog(trace.getClassName() + "#" + trace.getMethodName() + "#" + trace.getLineNumber());
            }
        }
        ShopDisplayItemSpawnEvent shopDisplayItemSpawnEvent = new ShopDisplayItemSpawnEvent(shop, originalItemStack);
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
        this.armorStand.setCollidable(false);
        this.armorStand.setCanPickupItems(false);
        //this.armorStand.setSmall(true);

        //Set armorstand item in hand
        this.armorStand.setItemInHand(originalItemStack);
        //Set safeGuard
        safeGuard(this.armorStand);
        //Set pose
        setPoseForArmorStand();
        Util.debugLog("Spawned new ArmorStand DisplayItem for shop " + shop.getLocation().toString());
    }

    @Override
    public void safeGuard(Entity entity) {
        if (entity == null) {
            Util.debugLog("Failed to safeGuard the NULL, somethings given a wrong args.");
        }
        if (!(entity instanceof ArmorStand)) {
            Util.debugLog("Failed to safeGuard " + entity.getLocation().toString() + ", cause target not a ArmorStand");
            return;
        }
        ArmorStand armorStand = (ArmorStand) entity;
        //Set item protect in the armorstand's hand
        this.guardedIstack = DisplayItem.createGuardItemStack(this.originalItemStack);
        armorStand.setItemInHand(guardedIstack);
        Util.debugLog("Successfully safeGuard ArmorStand: " + armorStand.getLocation().toString());
    }
    private void setPoseForArmorStand() {
        //TODO
        BlockFace containerBlockFace = BlockFace.NORTH; //Set default vaule

        if (this.shop.getLocation().getBlock().getState() instanceof Directional)
            containerBlockFace = ((Directional) this.shop.getLocation().getBlock().getState())
                    .getFacing(); //Replace by container face.

        //EulerAngle = pitch, yaw, roll.
        this.armorStand.setRightArmPose(new EulerAngle(0, 0, 0));
    }

    @Override
    public void remove() {
        if (this.armorStand == null || !this.armorStand.isValid() || this.armorStand.isDead()) {
            Util.debugLog("Ignore the armorStand removeing cause this armorStand already gone.");
            return;
        }
        this.armorStand.remove();
        this.armorStand = null;
        this.guardedIstack = null;
    }

    @Override
    public void respawn() {
        remove();
        spawn();
    }

    @Override
    public Entity getDisplay() {
        return this.armorStand;
    }
    @Override
    public Location getDisplayLocation() {
        return this.shop.getLocation().clone().add(0.5, 1.2, 0.5);
    }

    @Override
    public boolean checkDisplayIsMoved() {
        return !this.armorStand.getLocation().equals(getDisplayLocation());
    }

    @Override
    public boolean checkDisplayNeedRegen() {
        return !this.armorStand.isValid() || this.armorStand.isDead();
    }

    @Override
    public boolean checkIsShopEntity(Entity entity) {
        if (!(entity instanceof ArmorStand))
            return false;
        return DisplayItem.checkIsGuardItemStack(((ArmorStand) entity).getItemInHand());
    }

    @Override
    public boolean removeDupe() {
        if (this.armorStand == null) {
            Util.debugLog("Warning: Trying removeDupe for no display shop.");
            return false;
        }
        boolean removed = false;
        Chunk chunk = shop.getLocation().getChunk();
        for (Entity entity : chunk.getEntities()) {
            if (!(entity instanceof ArmorStand))
                continue;
            ArmorStand eArmorStand = (ArmorStand) entity;
            if (eArmorStand.getItemInHand().equals(this.guardedIstack)) {
                if (!eArmorStand.getUniqueId().equals(this.armorStand.getUniqueId())) {
                    Util.debugLog("Removing dupes ArmorEntity " + eArmorStand.getUniqueId().toString() + " at " + eArmorStand
                            .getLocation().toString());
                    entity.remove();
                    removed = true;
                }
            }
        }
        return removed;
    }

    @Override
    public void fixDisplayMoved() {
        for (Entity entity : this.shop.getLocation().getWorld().getEntities()) {
            if (!(entity instanceof ArmorStand))
                continue;
            ArmorStand eArmorStand = (ArmorStand) entity;
            if (eArmorStand.getUniqueId().equals(this.armorStand.getUniqueId())) {
                Util.debugLog("Fixing moved ArmorStand displayItem " + eArmorStand.getUniqueId().toString() + " at " + eArmorStand
                        .getLocation().toString());
                eArmorStand.teleport(getDisplayLocation());
                return;
            }
        }
    }

    @Override
    public void fixDisplayNeedRegen() {
        respawn();
    }
}
