/*
 * This file is a part of project QuickShop, the name is ReflectFactory.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandMap;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

/**
 * ReflectFactory is library builtin QuickShop to get/execute stuff that cannot be access with BukkitAPI with reflect way.
 *
 * @author Ghost_chu
 */
public class ReflectFactory {
    private static String cachedVersion = null;

    @NotNull
    public static String getServerVersion() {
        if (cachedVersion != null) {
            return cachedVersion;
        }
        try {
            Field consoleField = Bukkit.getServer().getClass().getDeclaredField("console");
            // protected
            consoleField.setAccessible(true);
            // dedicated server
            Object console = consoleField.get(Bukkit.getServer());
            cachedVersion = String.valueOf(
                    console.getClass().getSuperclass().getMethod("getVersion").invoke(console));
            return cachedVersion;
        } catch (Exception e) {
            cachedVersion = "Unknown";
            return cachedVersion;
        }
    }

    private static Method craftItemStack_asNMSCopyMethod;

    private static Method itemStack_saveMethod;

    private static Class<?> nbtTagCompoundClass;
    private static Class<?> craftServerClass;

    static {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String nmsVersion = name.substring(name.lastIndexOf('.') + 1);

        try {
            craftItemStack_asNMSCopyMethod =
                    Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".inventory.CraftItemStack")
                            .getDeclaredMethod("asNMSCopy", ItemStack.class);

            GameVersion gameVersion = GameVersion.get(nmsVersion);
            if (gameVersion.isNewNmsName()) {
                nbtTagCompoundClass = Class.forName("net.minecraft.nbt.NBTTagCompound");
                itemStack_saveMethod = Class.forName("net.minecraft.world.item.ItemStack").getDeclaredMethod("save", nbtTagCompoundClass);
            } else {
                nbtTagCompoundClass = Class.forName("net.minecraft.server." + nmsVersion + ".NBTTagCompound");
                itemStack_saveMethod = Class.forName("net.minecraft.server." + nmsVersion + ".ItemStack").getDeclaredMethod("save", nbtTagCompoundClass);
            }
            craftServerClass = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".CraftServer");

        } catch (Exception t) {
            QuickShop.getInstance().getLogger().log(Level.WARNING, "Failed to loading up net.minecraft.server support module, usually this caused by NMS changes but QuickShop not support yet, Did you have up-to-date?", t);
        }
    }

    /**
     * Save ItemStack to Json through the NMS.
     *
     * @param bStack ItemStack
     * @return The json for ItemStack.
     * @throws InvocationTargetException throws
     * @throws IllegalAccessException    throws
     * @throws NoSuchMethodException     throws
     * @throws InstantiationException    throws
     */
    @Nullable
    public static String convertBukkitItemStackToJson(@NotNull ItemStack bStack) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        if (bStack.getType() == Material.AIR) {
            return null;
        }
        Object mcStack = craftItemStack_asNMSCopyMethod.invoke(null, bStack);
        Object nbtTagCompound = nbtTagCompoundClass.getDeclaredConstructor().newInstance();

        itemStack_saveMethod.invoke(mcStack, nbtTagCompound);
        return nbtTagCompound.toString();
    }

    /**
     * Save ItemStack to Json through the NMS.
     *
     * @param bStack ItemStack
     * @return The json for ItemStack.
     * @throws InvocationTargetException throws
     * @throws IllegalAccessException    throws
     * @throws NoSuchMethodException     throws
     * @throws InstantiationException    throws
     */
    @Nullable
    public static Object convertBukkitItemStackToMojangItemStack(@NotNull ItemStack bStack) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        if (bStack.getType() == Material.AIR) {
            return null;
        }
        Object mcStack = craftItemStack_asNMSCopyMethod.invoke(null, bStack);
        Object nbtTagCompound = nbtTagCompoundClass.getDeclaredConstructor().newInstance();

        itemStack_saveMethod.invoke(mcStack, nbtTagCompound);
        return nbtTagCompound.toString();
    }

    public static CommandMap getCommandMap() throws NoSuchFieldException, IllegalAccessException {
        Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        commandMapField.setAccessible(true);
        return (CommandMap) commandMapField.get(Bukkit.getServer());
    }

    public static void syncCommands() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = craftServerClass.getDeclaredMethod("syncCommands");
        try {
            method.setAccessible(true);
        } catch (Exception ignored) {
        }
        method.invoke(Bukkit.getServer(), (Object[]) null);
    }


}
