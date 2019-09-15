package org.maxgamer.quickshop.Watcher;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.maxgamer.quickshop.QuickShop;

import java.util.LinkedList;
import java.util.Queue;

public class SyncTaskWatcher {
    @Getter
    private Queue<Entity> entityRemoveQueue = new LinkedList<>();
    @Getter
    private Queue<InventoryEditContainer> inventoryEditQueue = new LinkedList<>();
    @Getter
    private Queue<ItemStack> itemStackRemoveQueue = new LinkedList<>();
    private QuickShop plugin;

    /**
     * SyncTaskWatcher is a loop task runner, it can be add from async thread and run in Bukkit main thread.
     *
     * @param plugin QuickShop instance
     */
    public SyncTaskWatcher(QuickShop plugin) {
        this.plugin = plugin;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (entityRemoveQueue.isEmpty()) {
                    return;
                }
                Entity entity = entityRemoveQueue.poll();
                while (entity != null) {
                    entity.remove();
                    entity = entityRemoveQueue.poll();
                }
            }
        }.runTaskTimer(plugin, 0, 5);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (itemStackRemoveQueue.isEmpty()) {
                    return;
                }
                ItemStack itemStack = itemStackRemoveQueue.poll();
                while (itemStack != null) {
                    itemStack.setAmount(0);
                    itemStack.setType(Material.AIR);
                    itemStack = itemStackRemoveQueue.poll();
                }

            }
        }.runTaskTimer(plugin, 0, 5);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (inventoryEditQueue.isEmpty()) {
                    return;
                }
                InventoryEditContainer container = inventoryEditQueue.poll();
                while (container != null) {
                    container.getInventory().setItem(container.getSlot(), container.getNewItemStack());
                    container = inventoryEditQueue.poll();
                }

            }
        }.runTaskTimer(plugin, 0, 5);
    }
}

