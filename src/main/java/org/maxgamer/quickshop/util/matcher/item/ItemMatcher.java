package org.maxgamer.quickshop.util.matcher.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface ItemMatcher {
    /**
     * Gets the ItemMatcher provider name
     * @return Provider name
     */
    @NotNull String getName();

    /**
     * Gets the ItemMatcher provider plugin instance
     * @return Provider Plugin instance
     */
    @NotNull Plugin getPlugin();

    /**
     * Tests ItemStacks is matches
     * BEWARE: Different order of itemstacks you might will got different results
     * @param original The original ItemStack
     * @param tester The ItemStack will test matches with original itemstack.
     * @return The result of tests
     */
    boolean matches(@NotNull ItemStack original, @NotNull ItemStack tester);
}
