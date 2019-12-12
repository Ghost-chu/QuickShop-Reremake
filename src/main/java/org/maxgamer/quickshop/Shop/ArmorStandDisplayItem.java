package org.maxgamer.quickshop.Shop;

import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Event.ShopDisplayItemDespawnEvent;
import org.maxgamer.quickshop.Event.ShopDisplayItemSpawnEvent;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

import java.util.Objects;

@ToString
public class ArmorStandDisplayItem implements DisplayItem {

    private static boolean isTool(Material material) {
        String nlc = material.name().toLowerCase();
        return nlc.contains("sword") || nlc.contains("shovel") || nlc.contains("axe");
    }

    @Nullable
    private ArmorStand armorStand;
    @Nullable
    private ItemStack guardedIstack;
    private ItemStack originalItemStack;
    private QuickShop plugin = QuickShop.instance;
    private Shop shop;

    ArmorStandDisplayItem(@NotNull Shop shop) {
        this.shop = shop;
        this.originalItemStack = shop.getItem().clone();
        this.originalItemStack.setAmount(1);
    }

    @Override
    public boolean checkIsShopEntity(@NotNull Entity entity) {
        if (!(entity instanceof ArmorStand)) {
            return false;
        }
        return DisplayItem.checkIsGuardItemStack(((ArmorStand) entity).getItemInHand());
    }

    @Override
    public void spawn() {
        if (shop.getLocation().getWorld() == null) {
            Util.debugLog("Canceled the displayItem spawning because the location in the world is null.");
            return;
        }

        if (originalItemStack == null) {
            Util.debugLog("Canceled the displayItem spawning because the ItemStack is null.");
            return;
        }

        if (armorStand != null && armorStand.isValid() && !armorStand.isDead()) {
            Util.debugLog("Warning: Spawning the armorStand for DisplayItem when there is already an existing armorStand may cause a duplicated armorStand!");
            StackTraceElement[] traces = Thread.currentThread().getStackTrace();
            for (StackTraceElement trace : traces) {
                Util.debugLog(trace.getClassName() + "#" + trace.getMethodName() + "#" + trace.getLineNumber());
            }
        }
        ShopDisplayItemSpawnEvent shopDisplayItemSpawnEvent = new ShopDisplayItemSpawnEvent(shop, originalItemStack, DisplayType.ARMORSTAND);
        Bukkit.getPluginManager().callEvent(shopDisplayItemSpawnEvent);
        if (shopDisplayItemSpawnEvent.isCancelled()) {
            Util.debugLog("Canceled the displayItem from spawning because a plugin setCancelled the spawning event, usually it is a QuickShop Add on");
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
        //Set safeGuard
        safeGuard(this.armorStand);
        //Set pose
        setPoseForArmorStand();
        Util.debugLog("Spawned a new ArmorStand DisplayItem for a shop " + shop.getLocation());
    }

    @Override
    public boolean removeDupe() {
        if (this.armorStand == null) {
            Util.debugLog("Warning: Trying to removeDupe for a null display shop.");
            return false;
        }
        boolean removed = false;
        for (Entity entity : armorStand.getNearbyEntities(1, 1, 1)) {
            if (!(entity instanceof ArmorStand)) {
                continue;
            }
            ArmorStand eArmorStand = (ArmorStand) entity;

            if (!eArmorStand.getUniqueId().equals(this.armorStand.getUniqueId())) {
                if (DisplayItem.checkIsTargetShopDisplay(eArmorStand.getItemInHand(), this.shop)) {
                    Util.debugLog("Removing dupes ArmorEntity " + eArmorStand.getUniqueId() + " at " + eArmorStand
                            .getLocation());
                    entity.remove();
                    removed = true;
                }
            }
        }
        return removed;
    }

    @Override
    public void safeGuard(@NotNull Entity entity) {
        if (!(entity instanceof ArmorStand)) {
            Util.debugLog("Failed to safeGuard " + entity.getLocation() + ", cause target not a ArmorStand");
            return;
        }
        ArmorStand armorStand = (ArmorStand) entity;
        //Set item protect in the armorstand's hand
        this.guardedIstack = DisplayItem.createGuardItemStack(this.originalItemStack, this.shop);
        armorStand.setItemInHand(guardedIstack);
        armorStand.getPersistentDataContainer().set(new NamespacedKey(plugin,"displayMark"),DisplayItemPersistentDataType.INSTANCE,DisplayItem.createShopProtectionFlag(this.originalItemStack,shop));
        Util.debugLog("Successfully safeGuard ArmorStand: " + armorStand.getLocation());
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
    public void remove() {
        if (this.armorStand == null || !this.armorStand.isValid() || this.armorStand.isDead()) {
            Util.debugLog("Ignore the armorStand removing because the armorStand is already gone.");
            return;
        }
        this.armorStand.remove();
        this.armorStand = null;
        this.guardedIstack = null;
        ShopDisplayItemDespawnEvent shopDisplayItemDespawnEvent = new ShopDisplayItemDespawnEvent(this.shop, this.originalItemStack, DisplayType.ARMORSTAND);
        Bukkit.getPluginManager().callEvent(shopDisplayItemDespawnEvent);
    }

    @Override
    public Location getDisplayLocation() {
        BlockFace containerBlockFace = BlockFace.NORTH; //Set default vaule
        if (this.shop.getLocation().getBlock().getBlockData() instanceof Directional) {
            containerBlockFace = ((Directional) this.shop.getLocation().getBlock().getBlockData())
                    .getFacing(); //Replace by container face.
        }
        Location asloc = this.shop.getLocation().clone();
        Util.debugLog("containerBlockFace " + containerBlockFace);
        if (this.originalItemStack.getType().isBlock()) {
            asloc.add(0, 1, 0);
        }
        switch (containerBlockFace) {
            case SOUTH:
                if (isTool(this.originalItemStack.getType())) {
                    asloc.setYaw(90);
                    asloc.add(0.9, -0.4, 1);
                } else if (originalItemStack.getType().isBlock()) {
                    asloc.add(0.87, -0.4, 0.2);
                } else {
                    asloc.setYaw(0);
                    asloc.add(0.9, -0.4, 0);
                }
                break;
            case WEST:
                if (isTool(this.originalItemStack.getType())) {
                    asloc.add(0.9, -0.4, 0);
                } else if (originalItemStack.getType().isBlock()) {
                    asloc.add(0.85, -0.4, 0.15);
                } else {
                    asloc.setYaw(-90);
                    asloc.add(-0.1, -0.4, 0.15);
                }
                break;
            case EAST:
                if (isTool(this.originalItemStack.getType())) {
                    asloc.add(0.9, -0.4, 0);
                } else if (originalItemStack.getType().isBlock()) {
                    asloc.setYaw(-90);
                    asloc.add(0.15, -0.4, 0.1);
                } else {
                    asloc.setYaw(-90);
                    asloc.add(0, -0.4, 0.1);
                }
                break;
            case NORTH:
                if (isTool(this.originalItemStack.getType())) {
                    asloc.setYaw(90);
                    asloc.add(1, -0.4, 1);
                } else if (originalItemStack.getType().isBlock()) {
                    asloc.add(0.85, -0.4, 0.1);
                } else {
                    asloc.add(0.9, -0.4, 0);
                }
                break;
            default:
                break;
        }
        return asloc;
    }

    private void setPoseForArmorStand() {
        if (this.originalItemStack.getType().isBlock()) {
            Objects.requireNonNull(this.armorStand).setRightArmPose(new EulerAngle(-0.2, 0, 0));
        } else {
            Objects.requireNonNull(this.armorStand).setRightArmPose(new EulerAngle(-89.5, 0, 0));
        }
    }

    @Override
    public boolean checkDisplayIsMoved() {
        if (this.armorStand == null) {
            return false;
        }
        return !this.armorStand.getLocation().equals(getDisplayLocation());
    }

    @Override
    public boolean checkDisplayNeedRegen() {
        if (this.armorStand == null) {
            return false;
        }
        return !this.armorStand.isValid() || this.armorStand.isDead();
    }

    @Override
    public void fixDisplayMoved() {
        for (Entity entity : Objects.requireNonNull(this.shop.getLocation().getWorld()).getEntities()) {
            if (!(entity instanceof ArmorStand)) {
                continue;
            }
            ArmorStand eArmorStand = (ArmorStand) entity;
            if (eArmorStand.getUniqueId().equals(Objects.requireNonNull(this.armorStand).getUniqueId())) {
                Util.debugLog("Fixing moved ArmorStand displayItem " + eArmorStand.getUniqueId() + " at " + eArmorStand
                        .getLocation());
                eArmorStand.teleport(getDisplayLocation());
                return;
            }
        }
    }

    @Override
    public void fixDisplayNeedRegen() {
        respawn();
    }

    @Override
    public boolean isSpawned() {
        if (this.armorStand == null) {
            return false;
        }
        return this.armorStand.isValid();
    }
}
