package org.maxgamer.quickshop.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.QuickShop;

/**
 * A util allow quickshop check item matches easy and quick.
 */
public class ItemMatcher {
    private ItemMetaMatcher itemMetaMatcher;
    private QuickShop plugin;

    public ItemMatcher(QuickShop plugin) {
        this.plugin = plugin;
        itemMetaMatcher = new ItemMetaMatcher(plugin.getConfig().getConfigurationSection("matcher.item"));
    }

    /**
     * Compares two items to each other. Returns true if they match.
     * Rewrite it to use more faster hashCode.
     *
     * @param requireStack The first item stack
     * @param givenStack   The second item stack
     * @return true if the itemstacks match. (Material, durability, enchants, name)
     */
    public boolean matches(@Nullable ItemStack requireStack, @Nullable ItemStack givenStack) {

        if (requireStack == givenStack) {
            return true; // Referring to the same thing, or both are null.
        }

        if (requireStack == null || givenStack == null) {
            return false; // One of them is null (Can't be both, see above)
        }

        if (plugin.getConfig().getBoolean("shop.strict-matches-check")) {
            return requireStack.equals(givenStack);
        }

        if (!typeMatches(requireStack, givenStack)) {
            return false;
        }

        if (requireStack.hasItemMeta() != givenStack.hasItemMeta()) {
            return false;
        }

        if (requireStack.hasItemMeta()) {
            return itemMetaMatcher.matches(requireStack, givenStack);
        }

        return true;
    }

    private boolean typeMatches(ItemStack requireStack, ItemStack givenStack) {
        return requireStack.getType().equals(givenStack.getType());
    }

    class ItemMetaMatcher {
        private boolean attributes;
        private boolean custommodeldata;
        private boolean damage;
        private boolean displayname;
        private boolean enchs;
        private boolean itemflags;
        private boolean lores;
        private boolean potions;

        public ItemMetaMatcher(ConfigurationSection itemMatcherConfig) {
            this.damage = itemMatcherConfig.getBoolean("damage");
            this.displayname = itemMatcherConfig.getBoolean("displayname");
            this.lores = itemMatcherConfig.getBoolean("lores");
            this.enchs = itemMatcherConfig.getBoolean("enchs");
            this.potions = itemMatcherConfig.getBoolean("potions");
            this.attributes = itemMatcherConfig.getBoolean("attributes");
            this.itemflags = itemMatcherConfig.getBoolean("itemflags");
            this.custommodeldata = itemMatcherConfig.getBoolean("custommodeldata");
        }

        private boolean attributeModifiersMatches(ItemMeta meta1, ItemMeta meta2) {
            if (!this.attributes) {
                return true;
            }
            //requireStack doen't need require must have AM, skipping..
            if (!meta1.hasAttributeModifiers()) {
                return true;
            } else {
                //If require AM but hadn't, the item not matched.
                if (!meta2.hasAttributeModifiers()) {
                    return false;
                }
                return Util.mapMatches(meta1.getAttributeModifiers().asMap(), meta2.getAttributeModifiers().asMap());
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
            //This is special, we must check them damageable.
            if ((meta1 instanceof Damageable) != (meta2 instanceof Damageable)) {
                return false;
            }

            if (!(meta1 instanceof Damageable)) {
                return true; //No damage need to check.
            }

            Damageable damage1 = (Damageable) meta1;
            Damageable damage2 = (Damageable) meta2;
            //Check them damages, if givenDamage >= requireDamage, allow it.
            return damage2.getDamage() >= damage1.getDamage();

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
            if (!meta1.hasEnchants()) {
                return true;
            } else {
                if (!meta2.hasEnchants()) {
                    return false;
                }
                Map<Enchantment, Integer> enchMap1 = meta1.getEnchants();
                Map<Enchantment, Integer> enchMap2 = meta2.getEnchants();
                if (!Util.mapMatches(enchMap1, enchMap2)) {
                    return false;
                }
                if (!(meta1 instanceof EnchantmentStorageMeta)) {
                    return true;
                } else {
                    if (!(meta2 instanceof EnchantmentStorageMeta)) {
                        return false;
                    }
                    Map<Enchantment, Integer> stor1 = ((EnchantmentStorageMeta) meta1).getStoredEnchants();
                    Map<Enchantment, Integer> stor2 = ((EnchantmentStorageMeta) meta2).getStoredEnchants();
                    return Util.mapMatches(stor1, stor2);
                }
            }
        }

        private boolean itemFlagsMatches(ItemMeta meta1, ItemMeta meta2) {
            if (!this.itemflags) {
                return true;
            }
            if(meta1.getItemFlags().isEmpty()){
                return true;
            }else{
                if(meta2.getItemFlags().isEmpty()){
                    return false;
                }
                return Util.listMatches(new ArrayList<>(meta1.getItemFlags()),new ArrayList<>(meta2.getItemFlags()));
            }
        }
        //We didn't touch the loresMatches because many plugin use this check item.
        private boolean loresMatches(ItemMeta meta1, ItemMeta meta2) {
            Util.debugLog("Lores checker");
            if (!this.lores) {
                return true;
            }
            Util.debugLog("Lores checking");
            if (meta1.hasLore() != meta2.hasLore()) {
                return false;
            }
            if (!meta1.hasLore()) {
                return true; // No lores need to be checked.
            }
            List<String> lores1 = meta1.getLore();
            List<String> lores2 = meta2.getLore();
            if (lores1.size() != lores2.size()) {
                return false;
            }
            for (int i = 0; i < lores1.size(); i++) {
                if (!lores1.get(i).equals(lores2.get(i))) {
                    return false;
                }
            }
            return true;
        }

        boolean matches(ItemStack requireStack, ItemStack givenStack) {
            Util.debugLog("Begin the item matches checking...");
            if (requireStack.hasItemMeta() != givenStack.hasItemMeta()) {
                return false;
            }
            if (!requireStack.hasItemMeta()) {
                return true; //Passed check. no meta need to check.
            }
            ItemMeta meta1 = requireStack.getItemMeta();
            ItemMeta meta2 = givenStack.getItemMeta();
            if ((meta1 == null) != (meta2 == null)) {
                return false;
            }
            if (meta1 == null) {
                Util.debugLog("Pass");
                return true; //Both null...
            }
            if (!damageMatches(meta1, meta2)) {
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
            try {
                if (!customModelDataMatches(meta1, meta2)) {
                    return false;
                }
            } catch (NoSuchMethodError err) {
                //Ignore, for 1.13 compatibility
            }
            Util.debugLog("Pass");
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
                return true; //No potion meta needs to be checked.
            }

            PotionMeta potion1 = (PotionMeta) meta1;
            PotionMeta potion2 = (PotionMeta) meta2;

            if (potion1.hasColor()) {
                if(!potion2.hasColor()){
                    return false;
                }else{
                    if (!potion1.getColor().equals(potion2.getColor())) {
                        return false;
                    }
                }
            }
            if (potion1.hasCustomEffects()) {
               if(!potion2.hasCustomEffects()){
                   return false;
               }
                if (!Util.listMatches(potion1.getCustomEffects(),potion2.getCustomEffects())) {
                    return false;
                }
            }
            PotionData data1 = potion1.getBasePotionData();
            PotionData data2 = potion2.getBasePotionData();
            if(data2.equals(data1)){
                return true;
            }
            if(!data2.getType().equals(data1.getType())){
                return false;
            }
            if(data1.isExtended()){
                if(!data2.isExtended()){
                    return false;
                }
            }
            if(data1.isUpgraded()){
                //noinspection RedundantIfStatement
                if(!data2.isUpgraded()){
                    return false;
                }
            }
            return true;
        }

        private boolean rootMatches(ItemMeta meta1, ItemMeta meta2) {
            return (meta1.hashCode() == meta2.hashCode());
        }

    }
}
