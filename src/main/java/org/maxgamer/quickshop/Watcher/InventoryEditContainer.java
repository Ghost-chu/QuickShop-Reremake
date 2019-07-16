package org.maxgamer.quickshop.Watcher;

import lombok.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Data
@AllArgsConstructor
@Builder
@NonNull
public class InventoryEditContainer {
    private Inventory inventory;
    private int slot;
    private ItemStack newItemStack;
}
