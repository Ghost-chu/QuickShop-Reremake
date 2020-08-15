/*
 * This file is a part of project QuickShop, the name is GameLanguage.java
 *  Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
