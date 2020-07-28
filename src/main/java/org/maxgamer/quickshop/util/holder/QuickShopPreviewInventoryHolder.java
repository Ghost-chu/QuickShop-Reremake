package org.maxgamer.quickshop.util.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class QuickShopPreviewInventoryHolder implements InventoryHolder {
    private final UUID random = UUID.randomUUID(); //To let java know this is different with InventoryHolder (regular) in some stupid JDK/JRE
    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }


}
