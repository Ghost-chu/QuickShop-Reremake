/*
 * This file is a part of project QuickShop, the name is ItemMatcher.java
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

package org.maxgamer.quickshop.Util;

import java.util.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionData;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;

/** A util allow quickshop check item matches easy and quick. */
public class ItemMatcher {
  private ItemMetaMatcher itemMetaMatcher;
  private QuickShop plugin;

  public ItemMatcher(QuickShop plugin) {
    this.plugin = plugin;
    itemMetaMatcher =
        new ItemMetaMatcher(
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
      Util.debugLog(
          "Match failed: A stack is null: "
              + "requireStack["
              + requireStack
              + "] givenStack["
              + givenStack
              + "]");
      return false; // One of them is null (Can't be both, see above)
    }

    requireStack = requireStack.clone();
    requireStack.setAmount(1);
    givenStack = givenStack.clone();
    givenStack.setAmount(1);
    switch (plugin.getConfig().getInt("matcher.work-type")) {
      case 1:
        return requireStack.isSimilar(givenStack);
      case 2:
        return requireStack.equals(givenStack);
    }

    if (!typeMatches(requireStack, givenStack)) {
      return false;
    }

    //        if (requireStack.hasItemMeta() != givenStack.hasItemMeta()) {
    //            Util.debugLog("Meta not matched");
    //            return false;

    if (requireStack.hasItemMeta()) {
      if (!givenStack.hasItemMeta()) {
        return false;
      }
      return itemMetaMatcher.matches(requireStack, givenStack);
    }

    return true;
  }

  private boolean typeMatches(ItemStack requireStack, ItemStack givenStack) {
    return requireStack.getType().equals(givenStack.getType());
  }
}

class ItemMetaMatcher {

  private boolean banner;
  private boolean repaircost;
  private boolean attributes;
  private boolean custommodeldata;
  private boolean damage;
  private boolean displayname;
  private boolean enchs;
  private boolean itemflags;
  private boolean lores;
  private boolean potions;
  private boolean book;

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
    this.book = itemMatcherConfig.getBoolean("book");
    this.banner = itemMatcherConfig.getBoolean("banner");
  }

  private boolean attributeModifiersMatches(ItemMeta meta1, ItemMeta meta2) {
    if (!this.attributes) {
      return true;
    }
    // requireStack doen't need require must have AM, skipping..
    if (!meta1.hasAttributeModifiers()) {
      return true;
    } else {
      // If require AM but hadn't, the item not matched.
      if (!meta2.hasAttributeModifiers()) {
        return false;
      }
      Set<Attribute> set1 = Objects.requireNonNull(meta1.getAttributeModifiers()).keySet();
      Set<Attribute> set2 = Objects.requireNonNull(meta2.getAttributeModifiers()).keySet();
      for (Attribute att : set1) {
        if (!set2.contains(att)) {
          return false;
        } else if (!meta1
            .getAttributeModifiers()
            .get(att)
            .equals(meta2.getAttributeModifiers().get(att))) {
          return false;
        }
      }
      return true;
    }
  }

  private boolean customModelDataMatches(ItemMeta meta1, ItemMeta meta2) {
    if (!this.custommodeldata) {
      return true;
    }
    if (!meta1.hasCustomModelData()) {
      return true;
    } else {
      if (!meta2.hasCustomModelData()) {
        return false;
      }
      return meta1.getCustomModelData() == meta2.getCustomModelData();
    }
  }

  private boolean damageMatches(ItemMeta meta1, ItemMeta meta2) {
    if (!this.damage) {
      return true;
    }
    //            if (!(meta1 instanceof Damageable)) {
    //                return true; //No damage need to check.
    //            }
    //            if(!(meta2 instanceof Damageable)){
    //                return false;
    //            }
    try {
      Damageable damage1 = (Damageable) meta1;
      Damageable damage2 = (Damageable) meta2;
      // Check them damages, if givenDamage >= requireDamage, allow it.
      return damage2.getDamage() <= damage1.getDamage();
    } catch (Throwable th) {
      th.printStackTrace();
      return true;
    }
  }

  private boolean bookMatches(ItemMeta meta1, ItemMeta meta2){
    if(!this.book){
      return true;
    }
    if(!(meta1 instanceof BookMeta)){
      return true;
    }
    if(!(meta2 instanceof BookMeta)){
      return false;
    }
    BookMeta book1 = (BookMeta)meta1;
    BookMeta book2 = (BookMeta)meta2;
    if(book1.hasTitle()){
      if(!book2.hasTitle()){
        return false;
      }
      if(!Objects.equals(book1.getTitle(), book2.getTitle())){
        return false;
      }
    }
    if(book1.hasPages()){
      if(!book2.hasPages()){
        return false;
      }
      if(!book1.getPages().equals(book2.getPages())){
        return false;
      }
    }
    if(book1.hasAuthor()){
      if(!book2.hasAuthor()){
        return false;
      }
      if(!Objects.equals(book1.getAuthor(), book2.getAuthor())){
        return false;
      }
    }
    if(book1.hasGeneration()){
      if(!book2.hasGeneration()){
        return false;
      }
      return Objects.equals(book1.getGeneration(), book2.getGeneration());
    }
    return true;
  }

  private boolean displayMatches(ItemMeta meta1, ItemMeta meta2) {
    if (!this.displayname) {
      return true;
    }
    if (!meta1.hasDisplayName()) {
      return true;
    } else {
      if (!meta2.hasDisplayName()) {
        return false;
      }
      return meta1.getDisplayName().equals(meta2.getDisplayName());
    }
  }

  private boolean enchMatches(ItemMeta meta1, ItemMeta meta2) {
    if (!this.enchs) {
      return true;
    }
    if (meta1.hasEnchants()) {
      if (!meta2.hasEnchants()) {
        return false;
      }
      Map<Enchantment, Integer> enchMap1 = meta1.getEnchants();
      Map<Enchantment, Integer> enchMap2 = meta2.getEnchants();
      if (!Util.mapMatches(enchMap1, enchMap2)) {
        return false;
      }
    }
    if ((meta1 instanceof EnchantmentStorageMeta)) {
      if (!(meta2 instanceof EnchantmentStorageMeta)) {
        return false;
      }
      Map<Enchantment, Integer> stor1 = ((EnchantmentStorageMeta) meta1).getStoredEnchants();
      Map<Enchantment, Integer> stor2 = ((EnchantmentStorageMeta) meta2).getStoredEnchants();
      return Util.mapMatches(stor1, stor2);
    }
    return true;
  }

  private boolean itemFlagsMatches(ItemMeta meta1, ItemMeta meta2) {
    if (!this.itemflags) {
      return true;
    }
    if (meta1.getItemFlags().isEmpty()) {
      return true;
    } else {
      if (meta2.getItemFlags().isEmpty()) {
        return false;
      }
      return Arrays.deepEquals(meta1.getItemFlags().toArray(), meta2.getItemFlags().toArray());
    }
  }

  // We didn't touch the loresMatches because many plugin use this check item.
  private boolean loresMatches(ItemMeta meta1, ItemMeta meta2) {
    if (!this.lores) {
      return true;
    }
    if (meta1.hasLore() != meta2.hasLore()) {
      return false;
    }
    if (!meta1.hasLore()) {
      return true; // No lores need to be checked.
    }
    List<String> lores1 = meta1.getLore();
    List<String> lores2 = meta2.getLore();
    return Arrays.deepEquals(
        Objects.requireNonNull(lores1).toArray(), Objects.requireNonNull(lores2).toArray());
  }

  boolean matches(ItemStack requireStack, ItemStack givenStack) {
    if (requireStack.hasItemMeta() != givenStack.hasItemMeta()) {
      return false;
    }
    if (!requireStack.hasItemMeta()) {
      return true; // Passed check. no meta need to check.
    }
    ItemMeta meta1 = requireStack.getItemMeta();
    ItemMeta meta2 = givenStack.getItemMeta();
    if ((meta1 == null) != (meta2 == null)) {
      return false;
    }
    if (meta1 == null) {
      return true; // Both null...
    }
    if (!damageMatches(meta1, meta2)) {
      return false;
    }
    if (!repaircostMatches(meta1, meta2)) {
      return false;
    }
    if (!displayMatches(meta1, meta2)) {
      return false;
    }
    if (!loresMatches(meta1, meta2)) {
      return false;
    }
    if (!enchMatches(meta1, meta2)) {
      return false;
    }
    if (!potionMatches(meta1, meta2)) {
      return false;
    }
    if (!attributeModifiersMatches(meta1, meta2)) {
      return false;
    }
    if (!itemFlagsMatches(meta1, meta2)) {
      return false;
    }
    if(!bookMatches(meta1,meta2)){
      return false;
    }
    if(!bannerMatches(meta1,meta2)){
      return false;
    }
    try {
      if (!customModelDataMatches(meta1, meta2)) {
        return false;
      }
    } catch (NoSuchMethodError err) {
      // Ignore, for 1.13 compatibility
    }
    return true;
  }

  private boolean repaircostMatches(ItemMeta meta1, ItemMeta meta2) {
    if (!this.repaircost) {
      return true;
    }
    if (!(meta1 instanceof Repairable)) {
      return true;
    }
    if (!(meta2 instanceof Repairable)) {
      return false;
    }
    Repairable repairable1 = (Repairable) meta1;
    Repairable repairable2 = (Repairable) meta2;
    if (repairable1.hasRepairCost() != repairable2.hasRepairCost()) {
      return false;
    }
    if (repairable1.hasRepairCost()) {
      return repairable2.getRepairCost() <= repairable1.getRepairCost();
    }
    return true;
  }

  private boolean potionMatches(ItemMeta meta1, ItemMeta meta2) {
    if (!this.potions) {
      return true;
    }
    if ((meta1 instanceof PotionMeta) != (meta2 instanceof PotionMeta)) {
      return false;
    }

    if (!(meta1 instanceof PotionMeta)) {
      return true; // No potion meta needs to be checked.
    }

    PotionMeta potion1 = (PotionMeta) meta1;
    PotionMeta potion2 = (PotionMeta) meta2;

    if (potion1.hasColor()) {
      if (!potion2.hasColor()) {
        return false;
      } else {
        if (!Objects.requireNonNull(potion1.getColor()).equals(potion2.getColor())) {
          return false;
        }
      }
    }
    if (potion1.hasCustomEffects()) {
      if (!potion2.hasCustomEffects()) {
        return false;
      }
      if (!Arrays.deepEquals(
          potion1.getCustomEffects().toArray(), potion2.getCustomEffects().toArray())) {
        return false;
      }
      //                if
      // (!Util.listMatches(potion1.getCustomEffects(),potion2.getCustomEffects())) {
      //                    return false;
      //                }
    }
    PotionData data1 = potion1.getBasePotionData();
    PotionData data2 = potion2.getBasePotionData();
    if (data2.equals(data1)) {
      return true;
    }
    if (!data2.getType().equals(data1.getType())) {
      return false;
    }
    if (data1.isExtended()) {
      if (!data2.isExtended()) {
        return false;
      }
    }
    if (data1.isUpgraded()) {
      //noinspection RedundantIfStatement
      if (!data2.isUpgraded()) {
        return false;
      }
    }
    return true;
  }
  private boolean bannerMatches(ItemMeta meta1, ItemMeta meta2) {
    if (!this.banner) {
      return true;
    }
    if((meta1 instanceof BannerMeta) != (meta2 instanceof BannerMeta)){
      return false;
    }
    if(!(meta1 instanceof BannerMeta)){
      return true;
    }
    BannerMeta bannerMeta1 = (BannerMeta)meta1;
    BannerMeta bannerMeta2 = (BannerMeta)meta2;
    if(bannerMeta1.numberOfPatterns() != bannerMeta2.numberOfPatterns()){
      return false;
    }
    return Util.listMatches(bannerMeta1.getPatterns(), bannerMeta2.getPatterns());
  }

  private boolean rootMatches(ItemMeta meta1, ItemMeta meta2) {
    return (meta1.hashCode() == meta2.hashCode());
  }
}
