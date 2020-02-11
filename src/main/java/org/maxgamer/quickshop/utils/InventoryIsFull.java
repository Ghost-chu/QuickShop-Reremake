package org.maxgamer.quickshop.utils;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Predicate;

@RequiredArgsConstructor
public final class InventoryIsFull implements Predicate<ItemStack> {

    @NotNull
    private final Player player;

    /**
     * checks if player has enough space for the itemStack
     *
     * @param itemStack the item to check
     * @return returns true if player has not enough space for the itemStack.
     */
    @Override
    public boolean test(@NotNull ItemStack itemStack) {
        if (itemStack.getType() == Material.AIR) {
            return false;
        }

        if (itemStack.getAmount() > 5000) {
            return true;
        }

        if (player.getInventory().firstEmpty() >= 0 && itemStack.getAmount() <= itemStack.getMaxStackSize()) {
            return false;
        }

        if (itemStack.getAmount() > itemStack.getMaxStackSize()) {
            final ItemStack clone = itemStack.clone();

            clone.setAmount(itemStack.getMaxStackSize());

            final boolean check = test(clone);

            if (check) {
                return true;
            }

            clone.setAmount(itemStack.getAmount() - itemStack.getMaxStackSize());

            return test(clone);
        }

        final Map<Integer, ? extends ItemStack> all = player.getInventory().all(itemStack.getType());
        int amount = itemStack.getAmount();

        for (ItemStack element : all.values()) {
            amount = amount - (element.getMaxStackSize() - element.getAmount());
        }

        return amount > 0;
    }

}
