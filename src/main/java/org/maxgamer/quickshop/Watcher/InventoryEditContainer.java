package org.maxgamer.quickshop.Watcher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
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
