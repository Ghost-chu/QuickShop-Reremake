package org.maxgamer.quickshop.Util;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
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
     * @param stack1 The first item stack
     * @param stack2 The second item stack
     * @return true if the itemstacks match. (Material, durability, enchants, name)
     */
    public boolean matches(@Nullable ItemStack stack1, @Nullable ItemStack stack2) {

        if (stack1 == stack2) {
            return true; // Referring to the same thing, or both are null.
        }

        if (stack1 == null || stack2 == null) {
            return false; // One of them is null (Can't be both, see above)
        }

        if (plugin.getConfig().getBoolean("shop.strict-matches-check")) {
            return stack1.equals(stack2);
        }

        if (!typeMatches(stack1, stack2)) {
            return false;
        }

        if (stack1.hasItemMeta() != stack2.hasItemMeta()) {
            return false;
        }

        if (stack1.hasItemMeta()) {
            return itemMetaMatcher.matches(stack1, stack2);
        }

        return true;
    }

    private boolean typeMatches(ItemStack stack1, ItemStack stack2) {
        return (stack1.getType().equals(stack2.getType()));
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
            if (meta1.hasAttributeModifiers() != meta2.hasAttributeModifiers()) {
                return false;
            }

            if (!meta1.hasAttributeModifiers()) {
                return true; //No attributeModifiers need to be checked.
            }

            return (meta1.getAttributeModifiers().hashCode() == meta2.getAttributeModifiers().hashCode());
        }

        private boolean customModelDataMatches(ItemMeta meta1, ItemMeta meta2) {
            if (!this.custommodeldata) {
                return true;
            }
            if (meta1.hasCustomModelData() != meta2.hasCustomModelData()) {
                return false;
            }
            if (!meta1.hasCustomModelData()) {
                return true; //No customModelData needs to be checked.
            }
            return (meta1.getCustomModelData() == meta2.getCustomModelData());
        }

        private boolean damageMatches(ItemMeta meta1, ItemMeta meta2) {
            if (!this.damage) {
                return true;
            }
            if ((meta1 instanceof Damageable) != (meta2 instanceof Damageable)) {
                return false;
            }

            if (!(meta1 instanceof Damageable)) {
                return true; //No damage need to check.
            }

            Damageable damage1 = (Damageable) meta1;
            Damageable damage2 = (Damageable) meta2;

            if (damage1.hasDamage() != damage2.hasDamage()) {
                return false;
            }

            if (!damage1.hasDamage()) {
                return true; //No damage need to check.
            }

            return damage1.getDamage() == damage2.getDamage();

        }

        private boolean displayMatches(ItemMeta meta1, ItemMeta meta2) {
            if (!this.displayname) {
                return true;
            }
            if (meta1.hasDisplayName() != meta2.hasDisplayName()) {
                return false;
            }
            if (!meta1.hasDisplayName()) {
                return true; //Passed check. no display need to check
            }
            return (meta1.getDisplayName().equals(meta2.getDisplayName()));
        }

        private boolean enchMatches(ItemMeta meta1, ItemMeta meta2) {
            if (!this.enchs) {
                return true;
            }
            if (meta1.hasEnchants() != meta2.hasEnchants()) {
                return false;
            }

            if (meta1.hasEnchants()) {
                Map<Enchantment, Integer> enchMap1 = meta1.getEnchants();
                Map<Enchantment, Integer> enchMap2 = meta2.getEnchants();
                if (!Util.mapDuoMatches(enchMap1, enchMap2)) {
                    return false;
                }
            }
            if (meta1 instanceof EnchantmentStorageMeta != meta2 instanceof EnchantmentStorageMeta) {
                return false;
            }
            if (meta1 instanceof EnchantmentStorageMeta) {
                Map<Enchantment, Integer> stor1 = ((EnchantmentStorageMeta) meta1).getStoredEnchants();
                Map<Enchantment, Integer> stor2 = ((EnchantmentStorageMeta) meta2).getStoredEnchants();
                return Util.mapDuoMatches(stor1, stor2);
            }
            return true;
        }

        private boolean itemFlagsMatches(ItemMeta meta1, ItemMeta meta2) {
            if (!this.itemflags) {
                return true;
            }
            return (meta1.getItemFlags().hashCode() == meta2.getItemFlags().hashCode());
        }

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
            return (lores2.hashCode() == lores2.hashCode());
        }

        boolean matches(ItemStack stack1, ItemStack stack2) {
            Util.debugLog("Begin the item matches checking...");
            if (stack1.hasItemMeta() != stack2.hasItemMeta()) {
                return false;
            }
            if (!stack1.hasItemMeta()) {
                return true; //Passed check. no meta need to check.
            }
            ItemMeta meta1 = stack1.getItemMeta();
            ItemMeta meta2 = stack2.getItemMeta();
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

            if (potion1.hasColor() != potion2.hasColor()) {
                return false;
            }
            if (potion1.hasColor()) {
                if (!potion1.getColor().equals(potion2.getColor())) {
                    return false;
                }
            }
            if (potion1.hasCustomEffects() != potion2.hasCustomEffects()) {
                return false;
            }

            if (potion1.hasCustomEffects()) {
                if (potion1.getCustomEffects().hashCode() != potion2.getCustomEffects().hashCode()) {
                    return false;
                }
            }

            if (potion1.getBasePotionData().hashCode() != potion2.getBasePotionData().hashCode()) {
                return false;
            }

            return true;
        }

        private boolean rootMatches(ItemMeta meta1, ItemMeta meta2) {
            return (meta1.hashCode() == meta2.hashCode());
        }

    }
}
