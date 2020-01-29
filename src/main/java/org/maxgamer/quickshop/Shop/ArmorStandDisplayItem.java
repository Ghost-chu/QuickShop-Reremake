/*
 * This file is a part of project QuickShop, the name is ArmorStandDisplayItem.java
 * Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 * Copyright (C) Bukkit Commons Studio and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.maxgamer.quickshop.Shop;

import java.util.Objects;
import lombok.ToString;
import org.apache.commons.lang.ObjectUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Event.ShopDisplayItemDespawnEvent;
import org.maxgamer.quickshop.Event.ShopDisplayItemSpawnEvent;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;
import com.bekvon.bukkit.residence.commands.contract;

@ToString
public class ArmorStandDisplayItem implements DisplayItem {

  boolean pendingRemoval;
  @Nullable
  private volatile ArmorStand armorStand;
  @Nullable
  private ItemStack guardedIstack;
  private ItemStack originalItemStack;
  private QuickShop plugin = QuickShop.instance;
  private Shop shop;
  @NotNull
  private DisplayData data;

  ArmorStandDisplayItem(@NotNull Shop shop) {
    this(shop, new DisplayData(DisplayType.ARMORSTAND, false, false));
  }
    
  ArmorStandDisplayItem(@NotNull Shop shop, @NotNull DisplayData data) {
    this.shop = shop;
    this.originalItemStack = new ItemStack(shop.getItem());
    this.originalItemStack.setAmount(1);
    this.data = data;
  }

  private static boolean isTool(Material material) {
    String nlc = material.name().toLowerCase();
    return nlc.contains("sword") || nlc.contains("shovel") || nlc.contains("axe");
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

    synchronized (this) {
      if (armorStand != null && armorStand.isValid() && !armorStand.isDead()) {
        Util.debugLog(
            "Warning: Spawning the armorStand for DisplayItem when there is already an existing armorStand may cause a duplicated armorStand!");
        StackTraceElement[] traces = Thread.currentThread().getStackTrace();
        for (StackTraceElement trace : traces) {
          Util.debugLog(
              trace.getClassName() + "#" + trace.getMethodName() + "#" + trace.getLineNumber());
        }
      }
      
      ShopDisplayItemSpawnEvent shopDisplayItemSpawnEvent =
          new ShopDisplayItemSpawnEvent(shop, originalItemStack, DisplayType.ARMORSTAND);
      Bukkit.getPluginManager().callEvent(shopDisplayItemSpawnEvent);
      if (shopDisplayItemSpawnEvent.isCancelled()) {
        Util.debugLog(
            "Canceled the displayItem from spawning because a plugin setCancelled the spawning event, usually it is a QuickShop Add on");
        return;
      }
      
      Location location = getDisplayLocation();
      this.armorStand =
          (ArmorStand)
              this.shop
                  .getLocation()
                  .getWorld().spawn(location, ArmorStand.class, armorStand -> {
                    // Set basic armorstand datas.
                    armorStand.setGravity(false);
                    armorStand.setVisible(false);
                    armorStand.setMarker(true);
                    armorStand.setCollidable(false);
                    armorStand.setSmall(getAttribute(DisplayAttribute.SMALL, true));
                    armorStand.setArms(false);
                    armorStand.setBasePlate(false);
                    armorStand.setSilent(true);
                    armorStand.setAI(false);
                    armorStand.setCanMove(false);
                    armorStand.setCanPickupItems(false);
                    // Set pose
                    setPoseForArmorStand(armorStand);
                  });
      // Set safeGuard
      Util.debugLog("Spawned armor stand @ " + this.armorStand.getLocation() + " with UUID " + this.armorStand.getUniqueId());
      safeGuard(this.armorStand); // Helmet must be set after spawning
    }
  }
  
  @SuppressWarnings("unchecked")
  private <T> T getAttribute(DisplayAttribute attr, T defaultValue) {
    Object value = data.attribute.get(attr);
    if (value == ObjectUtils.NULL || value == null)
      return defaultValue;
    Util.debugLog("Attribute to cast: " + value);
    
    try {
      if (defaultValue instanceof EquipmentSlot) {
        return (T) EquipmentSlot.valueOf(String.class.cast(value));
      }
      
      if (value instanceof Integer) {
        if (defaultValue instanceof Double)
          return (T) Double.valueOf((int) value);
        
        if (defaultValue instanceof Float)
          return (T) Float.valueOf((int) value);
      }
      
      return (T) value;
    } catch (Throwable t) {
      plugin.getLogger().warning("Error when processing attribute for " +
          attr.name() + " with unexpected value " +
          value.toString() + ", please check your config before reporting!");
      t.printStackTrace();
      return defaultValue;
    }
  }

  @Override
  public boolean removeDupe() {
    if (this.armorStand == null) {
      Util.debugLog("Warning: Trying to removeDupe for a null display shop.");
      return false;
    }
    boolean removed = false;
    for (Entity entity : armorStand.getNearbyEntities(1.5, 1.5, 1.5)) {
      if (entity.getType() != EntityType.ARMOR_STAND) {
        continue;
      }
      ArmorStand eArmorStand = (ArmorStand) entity;

      if (!eArmorStand.getUniqueId().equals(this.armorStand.getUniqueId())) {
        if (DisplayItem.checkIsTargetShopDisplay(
            eArmorStand.getItem(EquipmentSlot.HAND), this.shop)) {
          Util.debugLog(
              "Removing dupes ArmorEntity "
                  + eArmorStand.getUniqueId()
                  + " at "
                  + eArmorStand.getLocation());
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
      Util.debugLog(
          "Failed to safeGuard " + entity.getLocation() + ", cause target not a ArmorStand");
      return;
    }
    ArmorStand armorStand = (ArmorStand) entity;
    // Set item protect in the armorstand's hand
    this.guardedIstack = DisplayItem.createGuardItemStack(this.originalItemStack, this.shop);
    armorStand.setItem(getAttribute(DisplayAttribute.SLOT, EquipmentSlot.HEAD), guardedIstack);
    try {
      armorStand
          .getPersistentDataContainer()
          .set(
              new NamespacedKey(plugin, "displayMark"),
              DisplayItemPersistentDataType.INSTANCE,
              DisplayItem.createShopProtectionFlag(this.originalItemStack, shop));
    } catch (Throwable ignored) {
    }
  }

  @Override
  public void respawn() {
    remove();
    spawn();
  }

  @Override
  public @Nullable Entity getDisplay() {
    return this.armorStand;
  }

  @Override
  public void remove() {
    if (this.armorStand == null) {
      Util.debugLog("Ignore the armorStand removing because the armorStand not spawned.");
      return;
    }
    this.armorStand.remove();
    this.armorStand = null;
    this.guardedIstack = null;
    ShopDisplayItemDespawnEvent shopDisplayItemDespawnEvent =
        new ShopDisplayItemDespawnEvent(this.shop, this.originalItemStack, DisplayType.ARMORSTAND);
    Bukkit.getPluginManager().callEvent(shopDisplayItemDespawnEvent);
  }

  @Override
  public Location getDisplayLocation() {
    BlockFace containerBlockFace = BlockFace.NORTH; // Set default vaule
    if (this.shop.getLocation().getBlock().getBlockData() instanceof Directional) {
      containerBlockFace =
          ((Directional) this.shop.getLocation().getBlock().getBlockData())
              .getFacing(); // Replace by container face.
    }
    
    // Fix specific block facing
    Material type = this.shop.getLocation().getBlock().getType();
    if (type.name().contains("ANVIL") || type.name().contains("FENCE") || type.name().contains("WALL") ) {
      switch (containerBlockFace) {
        case SOUTH:
          containerBlockFace = BlockFace.WEST;
          break;
        case NORTH:
          containerBlockFace = BlockFace.EAST;
        case EAST:
          containerBlockFace = BlockFace.NORTH;
        case WEST:
          containerBlockFace = BlockFace.SOUTH;
        default:
          break;
      }
    }
    
    Location asloc = getCenter(this.shop.getLocation());
    Util.debugLog("containerBlockFace " + containerBlockFace);
    
    if (this.originalItemStack.getType().isBlock()) {
      asloc.add(0, 0.5, 0);
    }
    
    switch (containerBlockFace) {
      case SOUTH:
        asloc.add(0, -0.5, 0);
        asloc.setYaw(0);
        Util.debugLog("Block face as SOUTH");
        break;
      case WEST:
        asloc.add(0, -0.5, 0);
        asloc.setYaw(90);
        Util.debugLog("Block face as WEST");
        break;
      case EAST:
        asloc.add(0, -0.5, 0);
        asloc.setYaw(-90);
        Util.debugLog("Block face as EAST");
        break;
      case NORTH:
        asloc.add(0, -0.5, 0);
        asloc.setYaw(180);
        Util.debugLog("Block face as NORTH");
        break;
      default:
        break;
    }
    
    asloc.setYaw(asloc.getYaw() + getAttribute(DisplayAttribute.OFFSET_YAW, 0f));
    asloc.setPitch(asloc.getYaw() + getAttribute(DisplayAttribute.OFFSET_PITCH, 0f));
    asloc.add(
        getAttribute(DisplayAttribute.OFFSET_X, 0d),
        getAttribute(DisplayAttribute.OFFSET_Y, 0d),
        getAttribute(DisplayAttribute.OFFSET_Z, 0d));
    
    return asloc;
  }
  
  public Location getCenter(Location loc) {
    // This is always '+' instead of '-' even in negative pos
    return new Location(loc.getWorld(),
        loc.getBlockX() + .5,
        loc.getBlockY() + .5,
        loc.getBlockZ() + .5);
  }

  private void setPoseForArmorStand(ArmorStand armorStand) {
    armorStand.setBodyPose(new EulerAngle(
        getAttribute(DisplayAttribute.POSE_BODY_X, 0d),
        getAttribute(DisplayAttribute.POSE_BODY_Y, 0d),
        getAttribute(DisplayAttribute.POSE_BODY_Z, 0d)));
    
    armorStand.setHeadPose(new EulerAngle(
        getAttribute(DisplayAttribute.POSE_HEAD_X, 0d),
        getAttribute(DisplayAttribute.POSE_HEAD_Y, 0d),
        getAttribute(DisplayAttribute.POSE_HEAD_Z, 0d)));
    
    armorStand.setRightArmPose(new EulerAngle(
        getAttribute(DisplayAttribute.POSE_ARM_RIGHT_X, 0d),
        getAttribute(DisplayAttribute.POSE_ARM_RIGHT_Y, 0d),
        getAttribute(DisplayAttribute.POSE_ARM_RIGHT_Z, 0d)));
    
    armorStand.setLeftArmPose(new EulerAngle(
        getAttribute(DisplayAttribute.POSE_ARM_LEFT_X, 0d),
        getAttribute(DisplayAttribute.POSE_ARM_LEFT_Y, 0d),
        getAttribute(DisplayAttribute.POSE_ARM_LEFT_Z, 0d)));
    
    armorStand.setRightLegPose(new EulerAngle(
        getAttribute(DisplayAttribute.POSE_LEG_RIGHT_X, 0d),
        getAttribute(DisplayAttribute.POSE_LEG_RIGHT_Y, 0d),
        getAttribute(DisplayAttribute.POSE_LEG_RIGHT_Z, 0d)));
    
    armorStand.setLeftLegPose(new EulerAngle(
        getAttribute(DisplayAttribute.POSE_LEG_LEFT_X, 0d),
        getAttribute(DisplayAttribute.POSE_LEG_LEFT_Y, 0d),
        getAttribute(DisplayAttribute.POSE_LEG_LEFT_Z, 0d)));
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
    Location location = this.getDisplayLocation();
    if (this.armorStand != null) {
      if (location != null) {
        this.armorStand.teleport(location);
      } else {
        fixDisplayMovedOld();
      }
    } else {
      fixDisplayMovedOld();
    }
  }

  public void fixDisplayMovedOld() {
    for (Entity entity : Objects.requireNonNull(this.shop.getLocation().getWorld()).getEntities()) {
      if (!(entity instanceof ArmorStand)) {
        continue;
      }
      ArmorStand eArmorStand = (ArmorStand) entity;
      if (eArmorStand.getUniqueId().equals(Objects.requireNonNull(this.armorStand).getUniqueId())) {
        Util.debugLog(
            "Fixing moved ArmorStand displayItem "
                + eArmorStand.getUniqueId()
                + " at "
                + eArmorStand.getLocation());
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
  public synchronized boolean isSpawned() {
    return this.armorStand == null ? false : this.armorStand.isValid();
  }

  @Override
  public boolean pendingRemoval() {
    return pendingRemoval = true;
  }

  @Override
  public boolean isPendingRemoval() {
    return pendingRemoval;
  }
}
