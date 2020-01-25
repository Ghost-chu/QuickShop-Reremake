/*
 * This file is a part of project QuickShop, the name is ItemMatcher.java Copyright (C) Ghost_chu
 * <https://github.com/Ghost-chu> Copyright (C) Bukkit Commons Studio and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.maxgamer.quickshop.Util;

import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.Repairable;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;

/** A util allow quickshop check item matches easy and quick. */
public class ItemMatcher {
  private ItemMetaMatcher itemMetaMatcher;
  private QuickShop plugin;

  public ItemMatcher(QuickShop plugin) {
    this.plugin = plugin;
    itemMetaMatcher = new ItemMetaMatcher(
        Objects.requireNonNull(plugin.getConfig().getConfigurationSection("matcher.item")));
  }

  /**
   * Compares two items to each other. Returns true if they match. Rewrite it to use more faster
   * hashCode.
   *
   * @param requireStack The first item stack
   * @param givenStack The second item stack
   * @return true if the itemstacks match. (Material, durability, enchants, name)
   */
  public boolean matches(@Nullable ItemStack requireStack, @Nullable ItemStack givenStack) {
    if (requireStack == givenStack) {
      return true; // Referring to the same thing, or both are null.
    }

    if (requireStack == null || givenStack == null) {
      Util.debugLog("Match failed: A stack is null: " + "requireStack[" + requireStack
          + "] givenStack[" + givenStack + "]");
      return false; // One of them is null (Can't be both, see above)
    }

    //requireStack = requireStack.clone();
    //requireStack.setAmount(1);
    //givenStack = givenStack.clone();
    //givenStack.setAmount(1);

    // if (plugin.getConfig().getBoolean("shop.strict-matches-check")) {
    // Util.debugLog("Execute strict match check...");
    // return requireStack.equals(givenStack);
    // }
    // if(plugin.getConfig().getBoolean("matcher.use-bukkit-matcher")){
    // return givenStack.isSimilar(requireStack);
    // }
    switch (plugin.getConfig().getInt("matcher.work-type")) {
      case 1:
      case 2:
        return requireStack.isSimilar(givenStack);
      case 0:
      default:
        ;
    }

    if (requireStack.getType() != givenStack.getType()) {
      Util.debugLog("Type not match.");
      return false;
    }

    return itemMetaMatcher.matches(requireStack, givenStack);
  }
}

class ItemMetaMatcher {
  private boolean repaircost;
  private boolean attributes;
  private boolean custommodeldata;
  private boolean damage;
  private boolean displayname;
  private boolean enchs;
  private boolean itemflags;
  private boolean lores;
  private boolean potions;
  private boolean bukkit;

  public ItemMetaMatcher(ConfigurationSection itemMatcherConfig) {
    this.damage = itemMatcherConfig.getBoolean("damage");
    this.repaircost = itemMatcherConfig.getBoolean("repaircost");
    this.displayname = itemMatcherConfig.getBoolean("displayname");
    this.lores = itemMatcherConfig.getBoolean("lores");
    this.enchs = itemMatcherConfig.getBoolean("enchs");
    this.potions = itemMatcherConfig.getBoolean("potions");
    this.attributes = itemMatcherConfig.getBoolean("attributes");
    this.itemflags = itemMatcherConfig.getBoolean("itemflags");
    this.custommodeldata = itemMatcherConfig.getBoolean("custommodeldata");

    this.bukkit = damage && repaircost && displayname && lores && enchs && potions && attributes
        && itemflags && custommodeldata;
  }

  private boolean canMatches(boolean a, boolean b) {
    return a == b;
  }

  private boolean attributeModifiersMatches(ItemMeta required, ItemMeta test) {
    Util.debugLog("Matching: Attribute Modifiers");
    return attributes ?
    // For non-exists, the default is same as null.
        Objects.equals(required.getAttributeModifiers(), test.getAttributeModifiers()) : true;
  }

  private boolean customModelDataMatches(ItemMeta required, ItemMeta test) {
    Util.debugLog("Matching: Custom Model Data");
    boolean requiredHas = required.hasCustomModelData();
    return custommodeldata ? (canMatches(requiredHas, test.hasCustomModelData()) ?
            
        (requiredHas ?
            required.getCustomModelData() == test.getCustomModelData() : true) :
              
        false) : true;
  }

  private boolean damageMatches(ItemMeta required, ItemMeta test) {
    Util.debugLog("Matching: Damage");
    boolean requiredIs = required instanceof Damageable;
    return damage ? (canMatches(requiredIs, test instanceof Damageable) ?

        (requiredIs ? ((Damageable) required).getDamage() == ((Damageable) test).getDamage() : true)
        :

        false) : true;
  }

  private boolean displayNameMatches(ItemMeta required, ItemMeta test) {
    Util.debugLog("Matching: Display Name");
    boolean requiredHas = required.hasDisplayName();
    return displayname ? (canMatches(requiredHas, test.hasDisplayName()) ?

        (requiredHas ? required.getDisplayName().equals(test.getDisplayName()) : true) :

        false) : true;
  }

  private boolean enchantsMatches(ItemMeta required, ItemMeta test) {
    Util.debugLog("Matching: Enchants");
    boolean requiredHas = required.hasEnchants();
    return enchs ? (canMatches(requiredHas, test.hasEnchants()) ?

        (requiredHas ? Util.mapMatches(required.getEnchants(), test.getEnchants()) : true) :

        false) : true;
  }

  private boolean itemFlagsMatches(ItemMeta required, ItemMeta test) {
    Util.debugLog("Matching: Item Flags");
    return itemflags ? required.getItemFlags().equals(test.getItemFlags()) : true;
  }

  // We didn't touch the loresMatches because many plugin use this check item.
  // Re: Did you mean this method? I don't think there is actually any plugin use this as a helper.
  private boolean loreMatches(ItemMeta required, ItemMeta test) {
    Util.debugLog("Matching: Lore");
    boolean requiredHas = required.hasLore();
    return lores ? (canMatches(requiredHas, test.hasLore()) ?

        (requiredHas ? required.getLore().equals(test.getLore()) : true) :

        false) : true;
  }

  private boolean repairCostMatches(ItemMeta required, ItemMeta test) {
    Util.debugLog("Matching: Repair Cost");
    boolean requiredIs = required instanceof Repairable;
    if (!canMatches(requiredIs, test instanceof Repairable)) {
      return false;
    } else if (!repaircost || !requiredIs) {
      return true;
    }
    
    boolean requiredHas = requiredIs && ((Repairable) required).hasRepairCost();
    return canMatches(requiredHas, ((Repairable) test).hasRepairCost()) ?

            (requiredHas
                ? ((Repairable) required).getRepairCost() == ((Repairable) test).getRepairCost()
                : true)
            :

            false;
  }

  private boolean potionMatches(ItemMeta required, ItemMeta test) {
    Util.debugLog("Matching: Potion");
    boolean requiredIs = required instanceof PotionMeta;
    if (!canMatches(requiredIs, test instanceof PotionMeta)) {
      return false;
    } else if (!potions || !requiredIs) {
      return true;
    }
    
    boolean requiredHasColor = requiredIs && ((PotionMeta) required).hasColor();
    boolean requiredHasCustomEffects = requiredIs && ((PotionMeta) required).hasCustomEffects();

    return canMatches(requiredHasColor, ((PotionMeta) test).hasColor())
        && canMatches(requiredHasCustomEffects, ((PotionMeta) test).hasCustomEffects()) ?

            (requiredHasColor
                ? ((PotionMeta) required).getColor().equals(((PotionMeta) test).getColor())
                :

                (requiredHasCustomEffects
                    ? ((PotionMeta) required).getCustomEffects()
                        .equals(((PotionMeta) test).getCustomEffects())
                    : (

                    ((PotionMeta) required).getBasePotionData())
                        .equals(((PotionMeta) test).getBasePotionData())))
            :

            false;
  }

  boolean matches(ItemStack requiredStack, ItemStack testStack) {
    String method = bukkit ? "Bukkit" : "QuickShop";
    Util.debugLog("Matching item by method " + method + " @ " + requiredStack.getType() + ", "
        + testStack.getType());

    boolean result = matches0(requiredStack, testStack);
    Util.debugLog("Matches result (" + method + "): " + String.valueOf(result).toUpperCase());
    return result;
  }

  boolean matches0(ItemStack requiredStack, ItemStack testStack) {
    if (bukkit) {
      return requiredStack.isSimilar(testStack);
    }

    boolean requiredHas = requiredStack.hasItemMeta();
    if (!canMatches(requiredHas, testStack.hasItemMeta())) {
      return false;
    } else if (!requiredHas) {
      return true;
    }

    ItemMeta requiredMeta = requiredStack.getItemMeta();
    ItemMeta testMeta = testStack.getItemMeta();

    if (!damageMatches(requiredMeta, testMeta)) {
      return false;
    }
    if (!repairCostMatches(requiredMeta, testMeta)) {
      return false;
    }
    if (!displayNameMatches(requiredMeta, testMeta)) {
      return false;
    }
    if (!loreMatches(requiredMeta, testMeta)) {
      return false;
    }
    if (!enchantsMatches(requiredMeta, testMeta)) {
      return false;
    }
    if (!potionMatches(requiredMeta, testMeta)) {
      return false;
    }
    if (!attributeModifiersMatches(requiredMeta, testMeta)) {
      return false;
    }
    if (!itemFlagsMatches(requiredMeta, testMeta)) {
      return false;
    }

    try {
      if (!customModelDataMatches(requiredMeta, testMeta)) {
        return false;
      }
    } catch (NoSuchMethodError err) {
      Util.debugLog("Ignored custom model data.");
      // Ignore, for 1.13 compatibility
    }

    return true;
  }
}
