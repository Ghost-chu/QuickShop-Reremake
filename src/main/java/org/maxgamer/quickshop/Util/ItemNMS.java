package org.maxgamer.quickshop.Util;

import java.lang.reflect.Method;

import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.*;
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
            craftItemStack_asNMSCopyMethod = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".inventory.CraftItemStack")
                    .getDeclaredMethod("asNMSCopy", ItemStack.class);

            nbtTagCompoundClass = Class.forName("net.minecraft.server." + nmsVersion + ".NBTTagCompound");

            itemStack_saveMethod = Class.forName("net.minecraft.server." + nmsVersion + ".ItemStack")
                    .getDeclaredMethod("save", nbtTagCompoundClass);

        } catch (Throwable t) {
            QuickShop.instance.getLogger().info("A error happend:");
            t.printStackTrace();
            QuickShop.instance.getLogger().info("Try to update QSRR and leave feedback about the bug on issue tracker if it continues.");
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
