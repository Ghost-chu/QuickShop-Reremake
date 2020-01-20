/*
 * This file is a part of project QuickShop, the name is RealDisplayItem.java
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
import java.util.UUID;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Event.ShopDisplayItemDespawnEvent;
import org.maxgamer.quickshop.Event.ShopDisplayItemSpawnEvent;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

@ToString
public class RealDisplayItem implements DisplayItem {

  private static QuickShop plugin = QuickShop.instance;
  boolean pendingRemoval;
  @Nullable private ItemStack guardedIstack;
  @Nullable private Item item;
  private ItemStack originalItemStack;
  private Shop shop;

  /**
   * ZZ Creates a new display item.
   *
   * @param shop The shop (See Shop)
   */
  RealDisplayItem(@NotNull Shop shop) {
    this.shop = shop;
    this.originalItemStack = shop.getItem().clone();
    this.originalItemStack.setAmount(1);

    // this.displayLoc = shop.getLocation().clone().add(0.5, 1.2, 0.5);
  }

  @Override
  public boolean checkDisplayIsMoved() {
    if (this.item == null) {
      return false;
    }
    // return !this.item.getLocation().equals(getDisplayLocation());
    /* We give 0.6 block to allow item drop on the chest, not floating on the air. */
    if (!Objects.requireNonNull(this.item.getLocation().getWorld())
        .equals(Objects.requireNonNull(getDisplayLocation()).getWorld())) {
      return true;
    }
    return this.item.getLocation().distance(getDisplayLocation()) > 0.6;
  }

  @Override
  public boolean checkDisplayNeedRegen() {
    if (this.item == null) {
      return false;
    }
    return !this.item.isValid() || this.item.isDead();
  }

  @Override
  public boolean checkIsShopEntity(@NotNull Entity entity) {
    if (!(entity instanceof Item)) {
      return false;
    }
    return DisplayItem.checkIsGuardItemStack(((Item) entity).getItemStack());
  }

  @Override
  public void fixDisplayMoved() {
    Location location = this.getDisplayLocation();
    if (this.item != null) {
      if (location != null) {
        this.item.teleport(location);
      } else {
        fixDisplayMovedOld();
      }
    } else {
      fixDisplayMovedOld();
    }
  }

  public void fixDisplayMovedOld() {
    for (Entity entity : Objects.requireNonNull(this.shop.getLocation().getWorld()).getEntities()) {
      if (!(entity instanceof Item)) {
        continue;
      }
      Item eItem = (Item) entity;
      if (eItem.getUniqueId().equals(Objects.requireNonNull(this.item).getUniqueId())) {
        Util.debugLog(
            "Fixing moved Item displayItem " + eItem.getUniqueId() + " at " + eItem.getLocation());
        plugin
            .getBukkitAPIWrapper()
            .teleportEntity(
                eItem,
                Objects.requireNonNull(getDisplayLocation()),
                PlayerTeleportEvent.TeleportCause.UNKNOWN);
        return;
      }
    }
  }

  @Override
  public void fixDisplayNeedRegen() {
    respawn();
  }

  @Override
  public void remove() {
    if (this.item == null) {
      Util.debugLog("Ignore the Item removing because the Item is already gone.");
      return;
    }
    this.item.remove();
    this.item = null;
    this.guardedIstack = null;
    ShopDisplayItemDespawnEvent shopDisplayItemDepawnEvent =
        new ShopDisplayItemDespawnEvent(shop, originalItemStack, DisplayType.REALITEM);
    Bukkit.getPluginManager().callEvent(shopDisplayItemDepawnEvent);
  }

  @Override
  public boolean removeDupe() {
    if (this.item == null) {
      Util.debugLog("Warning: Trying to removeDupe for a null display shop.");
      return false;
    }

    boolean removed = false;
    // Chunk chunk = shop.getLocation().getChunk();
    for (Entity entity : item.getNearbyEntities(1, 1, 1)) {
      if (!(entity instanceof Item)) {
        continue;
      }
      Item eItem = (Item) entity;
      UUID displayUUID = this.item.getUniqueId();
      if (!eItem.getUniqueId().equals(displayUUID)) {
        if (DisplayItem.checkIsTargetShopDisplay(eItem.getItemStack(), this.shop)) {
          Util.debugLog(
              "Removing a duped ItemEntity " + eItem.getUniqueId() + " at " + eItem.getLocation());
          entity.remove();
          removed = true;
        }
      }
    }
    return removed;
  }

  @Override
  public void respawn() {
    remove();
    spawn();
  }

  @Override
  public void safeGuard(@NotNull Entity entity) {
    if (!(entity instanceof Item)) {
      Util.debugLog("Failed to safeGuard " + entity.getLocation() + ", cause target not a Item");
      return;
    }
    Item item = (Item) entity;
    // Set item protect in the armorstand's hand

    if (plugin.getConfig().getBoolean("shop.display-item-use-name")) {
      item.setCustomName(Util.getItemStackName(this.originalItemStack));
      item.setCustomNameVisible(true);
    }
    item.setPickupDelay(Integer.MAX_VALUE);
    item.setSilent(true);
    item.setPortalCooldown(Integer.MAX_VALUE);
    item.setVelocity(new Vector(0, 0.1, 0));
    item.setCustomNameVisible(false);
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
    if (item != null && item.isValid() && !item.isDead()) {
      Util.debugLog(
          "Warning: Spawning the Dropped Item for DisplayItem when there is already an existing Dropped Item, May cause a duplicated Dropped Item!");
      StackTraceElement[] traces = Thread.currentThread().getStackTrace();
      for (StackTraceElement trace : traces) {
        Util.debugLog(
            trace.getClassName() + "#" + trace.getMethodName() + "#" + trace.getLineNumber());
      }
    }
    if (!Util.isDisplayAllowBlock(
        Objects.requireNonNull(getDisplayLocation()).getBlock().getType())) {
      Util.debugLog(
          "Can't spawn the displayItem because there is not an AIR block above the shopblock.");
      return;
    }

    ShopDisplayItemSpawnEvent shopDisplayItemSpawnEvent =
        new ShopDisplayItemSpawnEvent(shop, originalItemStack, DisplayType.REALITEM);
    Bukkit.getPluginManager().callEvent(shopDisplayItemSpawnEvent);
    if (shopDisplayItemSpawnEvent.isCancelled()) {
      Util.debugLog(
          "Canceled the displayItem spawning because a plugin setCancelled the spawning event, usually this is a QuickShop Add on");
      return;
    }
    this.guardedIstack = DisplayItem.createGuardItemStack(this.originalItemStack, this.shop);
    this.item =
        this.shop.getLocation().getWorld().dropItem(getDisplayLocation(), this.guardedIstack);
    this.item.setItemStack(this.guardedIstack);
    safeGuard(this.item);
  }

  @Override
  public @Nullable Entity getDisplay() {
    return this.item;
  }

  @Override
  public @Nullable Location getDisplayLocation() {
    return this.shop.getLocation().clone().add(0.5, 1.2, 0.5);
  }

  @Override
  public boolean isSpawned() {
    if (this.item == null) {
      return false;
    }
    return this.item.isValid();
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
