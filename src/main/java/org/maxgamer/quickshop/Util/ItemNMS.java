package org.maxgamer.quickshop.Util;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.QuickShop;

public abstract class ItemNMS {
    private static Method craftItemStack_asNMSCopyMethod;
    private static Class<?> nbtTagCompoundClass;
    private static Method itemStack_saveMethod;

    static {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String nmsVersion = name.substring(name.lastIndexOf('.') + 1);

        try {
            craftItemStack_asNMSCopyMethod = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".inventory.CraftItemStack")
                    .getDeclaredMethod("asNMSCopy", ItemStack.class);

            nbtTagCompoundClass = Class.forName("net.minecraft.server." + nmsVersion + ".NBTTagCompound");

            itemStack_saveMethod = Class.forName("net.minecraft.server." + nmsVersion + ".ItemStack")
                    .getDeclaredMethod("save", nbtTagCompoundClass);

        } catch (Throwable t) {
            QuickShop.instance.getLogger().info("A error happend:");
            t.printStackTrace();
            QuickShop.instance.getLogger().info("Try update QSRR. And feedback this bug on issue tracker.");
        }
    }

    /**
     * Save ItemStack to Json passthrough the NMS.
     *
     * @param bStack ItemStack
     * @return The json for ItemStack.
     * @throws Throwable throws
     */
    public static String saveJsonfromNMS(@NotNull ItemStack bStack) throws Throwable {
        if (bStack.getType() == Material.AIR)
            return null;
        Object mcStack = craftItemStack_asNMSCopyMethod.invoke(null, bStack);
        Object nbtTagCompound = nbtTagCompoundClass.newInstance();

        itemStack_saveMethod.invoke(mcStack, nbtTagCompound);
        return nbtTagCompound.toString();
    }
}
