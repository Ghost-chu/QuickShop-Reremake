package org.maxgamer.quickshop.util.matcher.item;

import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;

@AllArgsConstructor
public class BukkitItemMatcherImpl implements ItemMatcher {
    private QuickShop plugin;
    /**
     * Gets the ItemMatcher provider name
     *
     * @return Provider name
     */
    @Override
    public @NotNull String getName() {
        return plugin.getName();
    }

    /**
     * Gets the ItemMatcher provider plugin instance
     *
     * @return Provider Plugin instance
     */
    @Override
    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    /**
     * Tests ItemStacks is matches
     * BEWARE: Different order of itemstacks you might will got different results
     *
     * @param original The original ItemStack
     * @param tester   The ItemStack will test matches with original itemstack.
     * @return The result of tests
     */
    @Override
    public boolean matches(@Nullable ItemStack original, @Nullable ItemStack tester) {
        if(original == null && tester == null){
            return true;
        }
        if((original == null) != (tester == null)){
            return false;
        }
        return tester.isSimilar(original);
    }
}
