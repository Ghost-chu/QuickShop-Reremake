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
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.ObjectUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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
  static DisplayData getNowUsing(@Nullable ItemStack item) {
    try {
      /*
       * Nest structure as:
       *   armor-stand:
       *     - myStick:
       *       type: DEBUG_STICK
       *       lore:
       *         - mysterious item
       *         - gift
       *       strict: true
       *       attribute:
       *         yaw: 180
       *         pitch: 0
       *         small: false
       *         item-slot: HELMET
       *         offset:
       *           x: 1
       *           y: 0.1
       *           z: -1.1
       *         pose-head:
       *           x: 1
       *         pose-body:
       *           y: 0.2
       *         pose-arm:
       *           left:
       *             z: 0.5
       *         pose-leg:
       *           left:
       *             x: 0.14
       *           right:
       *             x: 0.3
       *             y: 0.1
       *             z: 0.3
       *     - grasses:
       *       type:
       *         - GLASS
       *         - GRASS
       *         - GRASS_BLOCK
       *   real-item:
       *     - 0:
       *       type: BOW
       *       lore: common mark
       */
      
      DisplayData def = new DisplayData(
          DisplayType.fromID(QuickShop.instance.getConfig().getInt("shop.display-type")), false, false);
      
      if (item != null) {
        ConfigurationSection conf =
            QuickShop.instance.getConfig().getConfigurationSection("shop.display-type-specifics");
        
        if (conf != null) {
          DisplayData dataArmourStand = matchData(conf, "armor-stand", item, true);
          if (dataArmourStand != null && !dataArmourStand.needVaildate)
            return dataArmourStand.fixer ?
                (def.type == dataArmourStand.type ?
                    dataArmourStand : def) : dataArmourStand;
          
          DisplayData dataDroppedItem = matchData(conf, "dropped-item", item, false);
          if (dataDroppedItem != null)
            return dataDroppedItem.fixer ?
                (def.type == dataDroppedItem.type ?
                    dataDroppedItem : def) : dataDroppedItem;
          else if (dataArmourStand != null)
            return dataArmourStand.fixer ?
                (def.type == dataArmourStand.type ?
                    dataArmourStand : def) : dataArmourStand;
        }
      }
      
      return def;
    } catch (Throwable e) {
      e.printStackTrace();
      return new DisplayData(DisplayType.REALITEM, false, false);
    }
  }

  @Nullable
  static DisplayData matchData(@NotNull ConfigurationSection conf, @NotNull String rootType, @NotNull ItemStack item, boolean armorStand) {
    List<?> specifics = conf.getList(rootType);
    //  <Map>
    // Value: Specific Map
    
    if (specifics instanceof List) {
      return matchData0(specifics, item, armorStand);
    }
    Util.debugLog("Specifics Is Not A List: " + specifics);
    return null;
  }
  
  @Nullable
  static DisplayData matchData0(@NotNull List<?> specifics, @NotNull ItemStack item, boolean armorStand) {
    DisplayData vaildateData = null;
    boolean needVaildate = false;
    for (Object o : specifics) {
      Util.debugLog("Specific: " + o);
      if (o instanceof Map) {
        Map<?, ?> specificMap = Map.class.cast(o);
        // <String, Map<String, ?>>
        // Key:   Custom name of Specific
        // Value: Attribute Map
        
        for (Object o_ : specificMap.values()) {
          Map<?, ?> attrMap = Map.class.cast(o_);
          
          // Mode
          Object temp = attrMap.get("fixer");
          Object fixer = temp == null ? false : true;
          
          // Type matcher
          temp = attrMap.get("type");
          Object type = temp == null ? "TAG:ANY" : temp;

          if (!type.equals("TAG:ANY")) {
            if (type instanceof Collection) {
              Collection<?> c = Collection.class.cast(type);
              boolean containsType = c.contains(item.getType().name());
              
              if (c.contains("TYPE:!".concat(item.getType().name()))) {
                continue;
              }
              if (c.contains("TAG:BLOCK") && !containsType) {
                if (!item.getType().isBlock()) {
                  continue;
                }
                needVaildate = true;
                break;
              }
              if (c.contains("TAG:!BLOCK") && !containsType) {
                if (item.getType().isBlock()) {
                  continue;
                }
                needVaildate = true;
                break;
              }

              if (!containsType) {
                continue;
              }
            } else if (type instanceof String) {
              if (type.equals("TYPE:!".concat(item.getType().name()))) {
                continue;
              }
              if (type.equals("TAG:BLOCK")) {
                if (!item.getType().isBlock()) {
                  continue;
                }
                needVaildate = true;
                break;
              }
              if (type.equals("TAG:!BLOCK")) {
                if (item.getType().isBlock()) {
                  continue;
                }
                needVaildate = true;
                break;
              }
              
              if (!item.getType().name().equals((type))) {
                continue;
              }
            }
          } else {
            needVaildate = true;
          }
          
          // Custom Model Data matcher
          temp = attrMap.get("strict");
          boolean strict = temp == null ? false : (boolean) temp;
          
          temp = attrMap.get("model-data");
          if (temp != null) {
            if (item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
              if ((int) temp != item.getItemMeta().getCustomModelData()) {
                continue;
              }
            } else {
              continue;
            }
          } else {
            needVaildate = true;
          }

          // Lore matcher
          temp = attrMap.get("lore");
          Object lore = temp == null ? "" : temp;

          if (!lore.equals("") && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            if (lore instanceof Collection) {
              Collection<?> specificLore = Collection.class.cast(lore);
              if (strict) {
                if (!specificLore.equals(item.getLore())) {
                  continue;
                }
              } else {
                boolean hasAny = false;

                LOOP_LORE:
                  for (String s : item.getLore()) {
                    if (specificLore.contains(s)) {
                      hasAny = true;
                      break LOOP_LORE;
                    }
                  }

                if (!hasAny) {
                  continue;
                } else {
                  needVaildate = true;
                }
              }
            } else if (lore instanceof String) {
              if (strict) {
                if (!item.getLore().equals(lore)) {
                  continue;
                }
              } else {
                if (!item.getLore().contains((lore))) {
                  continue;
                } else {
                  needVaildate = true;
                }
              }
            }
          } else {
            needVaildate = true;
          }

          DisplayData data = new DisplayData(
              armorStand ? DisplayType.ARMORSTAND : DisplayType.REALITEM, needVaildate, (boolean) fixer);
          if (armorStand) {
            Map<?, ?> attributes = Map.class.cast(attrMap.get("attribute"));
            
            if (attributes instanceof Map) {
              for (DisplayAttribute attr : DisplayAttribute.values()) {
                String[] attrKeys = attr.name().split("_");
                String rootKey = attrKeys[0].toLowerCase(Locale.ROOT);
                
                if (attrKeys.length == 1) {
                  temp = Map.class.cast(attributes).get(rootKey);
                  data.attribute.put(attr,
                      temp == null ? ObjectUtils.NULL : temp);
                } else {
                  // Nested Map
                  if ((temp = Map.class.cast(attributes).get(rootKey)) instanceof Map) {
                    String subKey = attrKeys[1].toLowerCase(Locale.ROOT);
                    Object value = Map.class.cast(temp).get(subKey);

                    data.attribute.put(attr,
                        value == null ? ObjectUtils.NULL : value);
                  }
                }
              }
            }
          }
          
          if (needVaildate)
            vaildateData = data;
          else
            return data;
        }
      }
    }
    
    return vaildateData;
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
