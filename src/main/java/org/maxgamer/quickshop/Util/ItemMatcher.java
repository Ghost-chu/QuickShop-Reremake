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

import org.bukkit.attribute.Attribute;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionData;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;

import java.util.*;

/**
 * A util allow quickshop check item matches easy and quick.
 */
public class ItemMatcher {
    private final ItemMetaMatcher itemMetaMatcher;

    private final QuickShop plugin;

    public ItemMatcher(QuickShop plugin) {
        this.plugin = plugin;
        itemMetaMatcher =
            new ItemMetaMatcher(
                Objects.requireNonNull(plugin.getConfig().getConfigurationSection("matcher.item")));
    }
    /**
     * Compares two items array to each other. Returns true if they match. Rewrite it to use more faster
     * hashCode.
     *
     * @param requireStack The first item stack array
     * @param givenStack The second item stack array
     * @return true if the itemstacks match. (Material, durability, enchants, name)
     */
    public boolean matches(@Nullable ItemStack[] requireStack, @Nullable ItemStack[] givenStack) {
        if(requireStack==null&&givenStack==null){
            return true;
        }

        if(requireStack==null||givenStack==null){
            return false;
        }

        if(requireStack.length!=givenStack.length){
            return false;
        }
        //For performance, we just check really equals in each index,check isn't contain or match will cost n^n time in most
        for(int i=0;i<requireStack.length;i++){
            //IDEA bug, ignore NPE tips
            //noinspection ConstantConditions
            if((requireStack[i] != null) && (givenStack[i] != null) &&
                    (requireStack[i].getAmount() != givenStack[i].getAmount())){
                return false;
            }

            if(!matches(requireStack[i],givenStack[i])){
                return false;
            }
        }
        return true;
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
        if (requireStack == null && givenStack == null) {
            return true;
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

    interface Matcher{

        boolean match(ItemMeta meta1, ItemMeta meta2);

    }

    private final List<Matcher> matcherList=new LinkedList<>();



    private void addIfEnable(ConfigurationSection itemMatcherConfig,String path,Matcher matcher){
        if(itemMatcherConfig.getBoolean(path)){
            matcherList.add(matcher);
        }
    }
    public ItemMetaMatcher(ConfigurationSection itemMatcherConfig) {

        addIfEnable(itemMatcherConfig,"damage",(meta1, meta2) -> {
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
            });
        addIfEnable(itemMatcherConfig,"repaircost", (meta1, meta2) -> {
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
            });
        addIfEnable(itemMatcherConfig,"displayname",((meta1, meta2) -> {
            if (!meta1.hasDisplayName()) {
            return true;
        } else {
            if (!meta2.hasDisplayName()) {
                return false;
            }
            return meta1.getDisplayName().equals(meta2.getDisplayName());
        }}));
        // We didn't touch the loresMatches because many plugin use this check item.
        addIfEnable(itemMatcherConfig,"lores",((meta1, meta2) -> {
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

        }));
        addIfEnable(itemMatcherConfig,"enchs",((meta1, meta2) -> {
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
        }));
        addIfEnable(itemMatcherConfig,"potions",((meta1, meta2) -> {
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
            return true;}));
        addIfEnable(itemMatcherConfig,"attributes",(meta1, meta2) -> { // requireStack doen't need require must have AM, skipping..
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
            }});
        addIfEnable(itemMatcherConfig,"itemflags",((meta1, meta2) -> {
            if (meta1.getItemFlags().isEmpty()) {
                return true;
            } else {
                if (meta2.getItemFlags().isEmpty()) {
                    return false;
                }
                return Arrays.deepEquals(meta1.getItemFlags().toArray(), meta2.getItemFlags().toArray());
            }
        }));
        addIfEnable(itemMatcherConfig,"book",((meta1, meta2) -> {
            if (!(meta1 instanceof BookMeta)) {
                return true;
            }
            if (!(meta2 instanceof BookMeta)) {
                return false;
            }
            BookMeta book1 = (BookMeta) meta1;
            BookMeta book2 = (BookMeta) meta2;
            if (book1.hasTitle()) {
                if (!book2.hasTitle()) {
                    return false;
                }
                if (!Objects.equals(book1.getTitle(), book2.getTitle())) {
                    return false;
                }
            }
            if (book1.hasPages()) {
                if (!book2.hasPages()) {
                    return false;
                }
                if (!book1.getPages().equals(book2.getPages())) {
                    return false;
                }
            }
            if (book1.hasAuthor()) {
                if (!book2.hasAuthor()) {
                    return false;
                }
                if (!Objects.equals(book1.getAuthor(), book2.getAuthor())) {
                    return false;
                }
            }
            if (book1.hasGeneration()) {
                if (!book2.hasGeneration()) {
                    return false;
                }
                return Objects.equals(book1.getGeneration(), book2.getGeneration());
            }
            return true;
        }));
        addIfEnable(itemMatcherConfig,"banner",((meta1, meta2) -> {
            if ((meta1 instanceof BannerMeta) != (meta2 instanceof BannerMeta)) {
                return false;
            }
            if (!(meta1 instanceof BannerMeta)) {
                return true;
            }
            BannerMeta bannerMeta1 = (BannerMeta) meta1;
            BannerMeta bannerMeta2 = (BannerMeta) meta2;
            if (bannerMeta1.numberOfPatterns() != bannerMeta2.numberOfPatterns()) {
                return false;
            }
            return Util.listMatches(bannerMeta1.getPatterns(), bannerMeta2.getPatterns());
        }));
        addIfEnable(itemMatcherConfig,"skull",(meta1, meta2) -> {
            if ((meta1 instanceof SkullMeta) != (meta2 instanceof SkullMeta)) {
                return false;
            }
            if (!(meta1 instanceof SkullMeta)) {
                return true;
            }
            //getOwningPlayer will let server query playerProfile in server thread
            //Causing huge lag, so using String instead
            String player1= ((SkullMeta) meta1).getOwner();
            String player2= ((SkullMeta) meta2).getOwner();
            if(player1==null){
                return true;
            }
            return player1.equalsIgnoreCase(player2);
        });
        addIfEnable(itemMatcherConfig,"map",(meta1, meta2) -> {
            if ((meta1 instanceof MapMeta) != (meta2 instanceof MapMeta)) {
                return false;
            }
            if (!(meta1 instanceof MapMeta)) {
                return true;
            }
            MapMeta mapMeta1= ((MapMeta) meta1);
            MapMeta mapMeta2= ((MapMeta) meta2);
            if(!mapMeta1.hasMapView()||mapMeta1.getMapView()==null){
                return true;
            }
            if(!mapMeta1.getMapView().equals(mapMeta2.getMapView())) {
                return false;
            }

            if(!mapMeta1.hasColor()||mapMeta1.getColor()==null){
                return true;
            }
            if(!mapMeta1.getColor().equals(mapMeta2.getColor())) {
                return false;
            }

            if(!mapMeta1.hasLocationName()||mapMeta1.getLocationName()==null){
                return true;
            }
            return mapMeta1.getLocationName().equals(mapMeta2.getLocationName());
        });
        addIfEnable(itemMatcherConfig,"firework",(meta1, meta2) -> {
            if ((meta1 instanceof FireworkMeta) != (meta2 instanceof FireworkMeta)) {
                return false;
            }
            if (!(meta1 instanceof FireworkMeta)) {
                return true;
            }
            FireworkMeta fireworkMeta1= ((FireworkMeta) meta1);
            FireworkMeta fireworkMeta2= ((FireworkMeta) meta2);
            if(!fireworkMeta1.hasEffects()){
                return true;
            }
            if(!fireworkMeta1.getEffects().equals(fireworkMeta2.getEffects())){
                return false;
            }
            return fireworkMeta1.getPower()==fireworkMeta2.getPower();
        });
        addIfEnable(itemMatcherConfig,"leatherArmor",((meta1, meta2) -> {
            if ((meta1 instanceof LeatherArmorMeta) != (meta2 instanceof LeatherArmorMeta)) {
                return false;
            }
            if (!(meta1 instanceof LeatherArmorMeta)) {
                return true;
            }
            return ((LeatherArmorMeta) meta1).getColor().equals(((LeatherArmorMeta) meta2).getColor());
        }));
        addIfEnable(itemMatcherConfig,"fishBucket",(meta1, meta2) -> {
            if ((meta1 instanceof TropicalFishBucketMeta) != (meta2 instanceof TropicalFishBucketMeta)) {
                return false;
            }
            if (!(meta1 instanceof TropicalFishBucketMeta)) {
                return true;
            }
            TropicalFishBucketMeta fishBucketMeta1=((TropicalFishBucketMeta) meta1);
            TropicalFishBucketMeta fishBucketMeta2=((TropicalFishBucketMeta) meta2);
            if(!fishBucketMeta1.hasVariant()){
                return true;
            }

            return fishBucketMeta2.hasVariant()&&fishBucketMeta1.getPattern()==fishBucketMeta2.getPattern()
                    &&fishBucketMeta1.getBodyColor().equals(fishBucketMeta2.getBodyColor())
                    &&fishBucketMeta1.getPatternColor().equals(fishBucketMeta2.getPatternColor());
        });
        addIfEnable(itemMatcherConfig,"shulkerBox",((meta1, meta2) -> {
            //https://www.spigotmc.org/threads/getting-the-inventory-of-a-shulker-box-itemstack.212369
            if ((meta1 instanceof BlockStateMeta) != (meta2 instanceof BlockStateMeta)) {
                return false;
            }
            if (!(meta1 instanceof BlockStateMeta)) {
                return true;
            }

            if ((((BlockStateMeta) meta1).getBlockState() instanceof ShulkerBox) != ((BlockStateMeta) meta2).getBlockState() instanceof ShulkerBox) {
                return false;
            }
            if(!(((BlockStateMeta) meta1).getBlockState() instanceof ShulkerBox)){
                return true;
            }
            return QuickShop.getInstance().getItemMatcher().matches(((ShulkerBox) ((BlockStateMeta) meta1).getBlockState()).getInventory().getContents(), ((ShulkerBox) ((BlockStateMeta) meta2).getBlockState()).getInventory().getContents());
        }));
        if(!Util.getNMSVersion().equals("v1_13_R1")&&!Util.getNMSVersion().equals("v1_13_R2")){
            addIfEnable(itemMatcherConfig,"custommodeldata",((meta1, meta2) -> {
                if (!meta1.hasCustomModelData()) {
                    return true;
                } else {
                    if (!meta2.hasCustomModelData()) {
                        return false;
                    }
                    return meta1.getCustomModelData() == meta2.getCustomModelData();
                }
            }));
            addIfEnable(itemMatcherConfig,"suspiciousStew",((meta1, meta2) -> {
                if ((meta1 instanceof SuspiciousStewMeta) != (meta2 instanceof SuspiciousStewMeta)) {
                    return false;
                }
                if (!(meta1 instanceof SuspiciousStewMeta)) {
                    return true;
                }
                SuspiciousStewMeta stewMeta1= ((SuspiciousStewMeta) meta1);
                SuspiciousStewMeta stewMeta2= ((SuspiciousStewMeta) meta2);
                if(!stewMeta1.hasCustomEffects()){
                    return true;
                }
                return stewMeta1.getCustomEffects().equals(stewMeta2.getCustomEffects());
            }));
        }
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
        for(Matcher matcher:matcherList){
            if(!matcher.match(meta1,meta2)){
                return false;
            }
        }
        return true;
    }


    private boolean rootMatches(ItemMeta meta1, ItemMeta meta2) {
        return (meta1.hashCode() == meta2.hashCode());
    }

}
