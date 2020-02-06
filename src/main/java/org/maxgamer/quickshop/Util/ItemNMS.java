/*
 * This file is a part of project QuickShop, the name is ItemNMS.java
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

import java.lang.reflect.Method;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;

@Getter
public abstract class ItemNMS {
  private static Method craftItemStack_asNMSCopyMethod;
  private static Method itemStack_saveMethod;
  private static Class<?> nbtTagCompoundClass;

  static {
    String name = Bukkit.getServer().getClass().getPackage().getName();
    String nmsVersion = name.substring(name.lastIndexOf('.') + 1);

    try {
      craftItemStack_asNMSCopyMethod =
          Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".inventory.CraftItemStack")
              .getDeclaredMethod("asNMSCopy", ItemStack.class);

      nbtTagCompoundClass = Class.forName("net.minecraft.server." + nmsVersion + ".NBTTagCompound");

      itemStack_saveMethod =
          Class.forName("net.minecraft.server." + nmsVersion + ".ItemStack")
              .getDeclaredMethod("save", nbtTagCompoundClass);

    } catch (Throwable t) {
      QuickShop.instance.getLogger().info("A error happend:");
      t.printStackTrace();
      QuickShop.instance
          .getLogger()
          .info(
              "Try to update QSRR and leave feedback about the bug on issue tracker if it continues.");
    }
  }

  /**
   * Save ItemStack to Json passthrough the NMS.
   *
   * @param bStack ItemStack
   * @return The json for ItemStack.
   * @throws Throwable throws
   */
  @Nullable
  public static String saveJsonfromNMS(@NotNull ItemStack bStack) throws Throwable {
    if (bStack.getType() == Material.AIR) {
      return null;
    }
    Object mcStack = craftItemStack_asNMSCopyMethod.invoke(null, bStack);
    Object nbtTagCompound = nbtTagCompoundClass.newInstance();

    itemStack_saveMethod.invoke(mcStack, nbtTagCompound);
    return nbtTagCompound.toString();
  }
}
