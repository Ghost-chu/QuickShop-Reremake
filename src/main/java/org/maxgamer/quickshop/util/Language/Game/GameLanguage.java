package org.maxgamer.quickshop.util.language.game;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public interface GameLanguage {
    @NotNull String getName();
    @NotNull Plugin getPlugin();
    @NotNull String getItem(@NotNull ItemStack itemStack);
    @NotNull String getItem(@NotNull Material material);
    @NotNull String getPotion(@NotNull PotionEffectType potionEffectType);
    @NotNull String getEnchantment(@NotNull Enchantment enchantment);
    @NotNull String getEntity(@NotNull EntityType entityType);
}
