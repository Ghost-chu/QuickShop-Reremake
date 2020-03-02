/*
 * This file is a part of project QuickShop, the name is DisplayItemPersistentDataType.java
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

import com.google.gson.Gson;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Util.Util;

public class DisplayItemPersistentDataType
    implements PersistentDataType<String, ShopProtectionFlag> {
  static final DisplayItemPersistentDataType INSTANCE = new DisplayItemPersistentDataType();

  private static final Gson gson = new Gson();

  @Override
  public @NotNull Class<String> getPrimitiveType() {
    return String.class;
  }

  @Override
  public @NotNull Class<ShopProtectionFlag> getComplexType() {
    return ShopProtectionFlag.class;
  }

  @NotNull
  @Override
  public String toPrimitive(
      @NotNull ShopProtectionFlag complex, @NotNull PersistentDataAdapterContext context) {
    try {
      return gson.toJson(complex);
    } catch (Throwable th) {
      new RuntimeException("Cannot to toPrimitive the shop protection flag.").printStackTrace();
      return "";
    }
  }

  @NotNull
  @Override
  public ShopProtectionFlag fromPrimitive(
      @NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
    try {
      return gson.fromJson(primitive, ShopProtectionFlag.class);
    } catch (Throwable th) {
      new RuntimeException("Cannot to fromPrimitive the shop protection flag.").printStackTrace();
      return new ShopProtectionFlag("", Util.serialize(new ItemStack(Material.STONE)));
    }
  }
}
