/*
 * This file is a part of project QuickShop, the name is DisplayItem.java
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

import com.bekvon.bukkit.residence.commands.contract;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

/**
 * @author Netherfoam A display item, that spawns a block above the chest and cannot be interacted
 *     with.
 */
public interface DisplayItem {
  Gson gson = new Gson();

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
    //    itemStack = itemStack.clone();
    //    itemStack.setAmount(1);
    if (!itemStack.hasItemMeta()) {
      return false;
    }
    ItemMeta iMeta = itemStack.getItemMeta();
    if (!iMeta.hasLore()) {
      return false;
    }
    String defaultMark = ShopProtectionFlag.getDefaultMark();
    //noinspection ConstantConditions
    for (String lore : iMeta.getLore()) {
      try {
        if (!lore.startsWith("{")) {
          continue;
        }
        ShopProtectionFlag shopProtectionFlag = gson.fromJson(lore, ShopProtectionFlag.class);
        if (shopProtectionFlag == null) {
          continue;
        }
        if (defaultMark.equals(shopProtectionFlag.getMark())) {
          return true;
        }
        if (shopProtectionFlag.getShopLocation() != null) {
          return true;
        }
        if (shopProtectionFlag.getItemStackString() != null) {
          return true;
        }
      } catch (JsonSyntaxException e) {
        // Ignore
      }
    }

    return false;
  }

  /**
   * Check the itemStack is target shop's display
   *
   * @param itemStack Target ItemStack
   * @param shop Target shop
   * @return Is target shop's display
   */
  static boolean checkIsTargetShopDisplay(@NotNull ItemStack itemStack, @NotNull Shop shop) {
    if (!itemStack.hasItemMeta()) {
      return false;
    }
    ItemMeta iMeta = itemStack.getItemMeta();
    if (!iMeta.hasLore()) {
      return false;
    }
    String defaultMark = ShopProtectionFlag.getDefaultMark();
    String shopLocation = shop.getLocation().toString();
    //noinspection ConstantConditions
    for (String lore : iMeta.getLore()) {
      try {
        if (!lore.startsWith("{")) {
          continue;
        }
        ShopProtectionFlag shopProtectionFlag = gson.fromJson(lore, ShopProtectionFlag.class);
        if (shopProtectionFlag == null) {
          continue;
        }
        if (!shopProtectionFlag.getMark().equals(defaultMark)) {
          continue;
        }
        if (shopProtectionFlag.getShopLocation().equals(shopLocation)) {
          return true;
        }
      } catch (JsonSyntaxException e) {
        // Ignore
      }
    }
    return false;
  }

  /**
   * Create a new itemStack with protect flag.
   *
   * @param itemStack Old itemStack
   * @param shop The shop
   * @return New itemStack with protect flag.
   */
  static ItemStack createGuardItemStack(@NotNull ItemStack itemStack, @NotNull Shop shop) {
    itemStack = new ItemStack(itemStack);
    itemStack.setAmount(1);
    ItemMeta iMeta = itemStack.getItemMeta();
    if (QuickShop.instance.getConfig().getBoolean("shop.display-item-use-name")) {
      if (iMeta.hasDisplayName()) {
        iMeta.setDisplayName(iMeta.getDisplayName());
      } else {
        iMeta.setDisplayName(Util.getItemStackName(itemStack));
      }
    } else {
      iMeta.setDisplayName(null);
    }
    java.util.List<String> lore = new ArrayList<>();
    ShopProtectionFlag shopProtectionFlag = createShopProtectionFlag(itemStack, shop);
    String protectFlag = gson.toJson(shopProtectionFlag);
    for (int i = 0; i < 21; i++) {
      lore.add(
          protectFlag); // Create 20 lines lore to make sure no stupid plugin accident remove mark.
    }
    iMeta.setLore(lore);
    itemStack.setItemMeta(iMeta);
    return itemStack;
  }

  /**
   * Create the shop protection flag for display item.
   *
   * @param itemStack The item stack
   * @param shop The shop
   * @return ShopProtectionFlag obj
   */
  static ShopProtectionFlag createShopProtectionFlag(
      @NotNull ItemStack itemStack, @NotNull Shop shop) {
    return new ShopProtectionFlag(shop.getLocation().toString(), Util.serialize(itemStack));
  }

  /**
   * Get plugin now is using which one DisplayType
   *
   * @return Using displayType.
   */
  static DisplayType getNowUsing(@Nullable ItemStack item) {
    try {
    if (item != null) {
      /*
       * The below codes are massy like a shit (but not bad at function), need to cleanup
       * 
       * Nest structure as:
       *   1:
       *     small_:
       *       specific:
       *         type: DEBUG_STICK
       *         lore:
       *           - mysterious item
       *           - gift
       *         strict: true
       *       yaw: 180
       *       pitch: 0
       *       small: false
       *       item-slot: HELMET
       *       offset:
       *         x: 1
       *         y: 0.1
       *         z: -1.1
       *       pose-head:
       *         x: 1
       *       pose-body:
       *         y: 0.2
       *       pose-arm:
       *         left:
       *           z: 0.5
       *       pose-leg:
       *         left:
       *           x: 0.14
       *         right:
       *           x: 0.3
       *           y: 0.1
       *           z: 0.3
       *     small2:
       *       specific:
       *       type:
       *         - GLASS
       *         - GRASS
       *         - GRASS_BLOCK
       *   0:
       *     small:
       *       specific:
       *         type: BOW
       *         lore: common mark
        */
      // type id such as 0, 1, 2, 3.
      List<?> typeSections = 
          QuickShop.instance.getConfig().getList("shop.display-type-specifics", Lists.newArrayList());
      if (typeSections.isEmpty()) {
        return DisplayType.fromID(QuickShop.instance.getConfig().getInt("shop.display-type"));
      }
      for (Object typeSection : typeSections) {
        if (typeSection == null) {
          continue;
        }
        // id as key
        Util.debugLog(typeSection.toString() + " @ LEVEL 1, " + typeSection.getClass().getName());
        if (typeSection instanceof Map) {
          Map<?, ?> itemSections = Map.class.cast(typeSection);
          for (Entry<?, ?> itemSection : itemSections.entrySet()) {
            if (itemSection == null) {
              continue;
            }
            // list
            Util.debugLog(itemSection.toString() + " @ LEVEL 2, " + itemSection.getClass().getName());
            if (itemSection.getValue() instanceof List) {
              List<?> infoSections = List.class.cast(itemSection.getValue());
              if (infoSections == null) {
                continue;
              }
              for (Object $infoSection : infoSections) {
                if ($infoSection == null) {
                  continue;
                }
                // custom name as key
                Util.debugLog($infoSection.toString() + " @ LEVEL 3, " + $infoSection.getClass().getName());
                if ($infoSection instanceof Map) {
                  for (Object infoSectiont : Map.class.cast($infoSection).values()) {
                    if (infoSectiont == null) {
                      continue;
                    }
                    Map<?, Object> infoSection = Map.class.cast(infoSectiont);
                    Util.debugLog(infoSection.toString() + " @ LEVEL 4, " + infoSection.getClass().getName());
                    List<Material> type = Lists.newArrayList();
                    boolean strictMeta = (boolean) infoSection.getOrDefault("strict", false);
                    String name = "";
                    List<String> lore = Lists.newArrayList();
                    int customModelData = -1;
                    
                    Object strOrListType = infoSection.getOrDefault("type", Lists.newArrayList());
                    if (strOrListType instanceof List) {
                      for (Object s : (List<?>) strOrListType) {
                        if (s instanceof String) {
                          type.add(Material.valueOf((String) s));
                        }
                      }
                    } else {
                      type = Collections.singletonList(Material.valueOf((String) strOrListType));
                    }
                    
                    name = (String) infoSection.getOrDefault("name", "");
                    
                    Object strOrListLore = infoSection.getOrDefault("lore", Lists.newArrayList());
                    if (strOrListLore instanceof List) {
                      for (Object s : (List<?>) strOrListLore) {
                        if (s instanceof String) {
                          lore.add((String) s);
                        }
                      }
                    } else {
                      lore = Collections.singletonList((String) strOrListLore);
                    }
                    
                    customModelData = (int) infoSection.getOrDefault("custom-model-data", -1);
                    
                    boolean meta = !name.isEmpty() || !lore.isEmpty() || customModelData != -1;
                    if (type.isEmpty() || type.contains(item.getType())) {
                      if (meta) {
                        if (item.hasItemMeta()) {
                          ItemMeta itemMeta = item.getItemMeta();
                          
                          if (!name.isEmpty()) {
                            if (!(itemMeta.hasDisplayName() && name.equals(itemMeta.getDisplayName()))) {
                              Util.debugLog("getNowUsing failed in name");
                              return
                                  DisplayType.fromID(QuickShop.instance.getConfig().getInt("shop.display-type"));
                            }
                          }
                          
                          if (!lore.isEmpty()) {
                            boolean loreCheck = strictMeta ? lore.equals(itemMeta.getLore()) :
                              lore.containsAll(itemMeta.getLore());
                            if (!(itemMeta.hasLore() && loreCheck)) {
                              Util.debugLog("getNowUsing failed in lore");
                              return
                                  DisplayType.fromID(QuickShop.instance.getConfig().getInt("shop.display-type"));
                            }
                          }
                          
                          try {
                            if (customModelData != -1) {
                              if (!(itemMeta.hasCustomModelData() &&
                                  customModelData == itemMeta.getCustomModelData())) {
                                Util.debugLog("getNowUsing failed in CMD");
                                return
                                    DisplayType.fromID(QuickShop.instance.getConfig().getInt("shop.display-type"));
                              }
                            }
                          } catch (Exception e) {
                            ;
                          }
                          
                          Util.debugLog("getNowUsing passed thorough meta");
                          return DisplayType.fromID((Integer) itemSection.getKey());
                        } else {
                          ;
                        }
                        
                      } else {
                        Util.debugLog("getNowUsing passed without meta");
                        return DisplayType.fromID((Integer) itemSection.getKey());
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    
    return DisplayType.fromID(QuickShop.instance.getConfig().getInt("shop.display-type"));
    } catch (Throwable e) {
      e.printStackTrace();
      return DisplayType.REALITEM;
    }
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

  /** Fix the display moved issue. */
  void fixDisplayMoved();

  /** Fix display need respawn issue. */
  void fixDisplayNeedRegen();

  /** Remove the display entity. */
  void remove();

  /**
   * Remove this shop's display in the whole world.(Not whole server)
   *
   * @return Success
   */
  boolean removeDupe();

  /** Respawn the displays, if it not exist, it will spawn new one. */
  void respawn();

  /**
   * Add the protect flags for entity or entity's hand item. Target entity will got protect by
   * QuickShop
   *
   * @param entity Target entity
   */
  void safeGuard(Entity entity);

  /** Spawn new Displays */
  void spawn();

  /**
   * Get the display entity
   *
   * @return Target entity
   */
  Entity getDisplay();

  /**
   * Get display should at location. Not display current location.
   *
   * @return Should at
   */
  Location getDisplayLocation();

  /**
   * Check the display is or not already spawned
   *
   * @return Spawned
   */
  boolean isSpawned();

  boolean pendingRemoval();

  boolean isPendingRemoval();
}
