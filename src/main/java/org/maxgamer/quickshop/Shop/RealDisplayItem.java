package org.maxgamer.quickshop.Shop;

import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.maxgamer.quickshop.QuickShop;
//import org.maxgamer.quickshop.Util.NMS;

/**
 * @author Netherfoam A display item, that spawns a block above the chest and
 * cannot be interacted with.
 */
public class RealDisplayItem implements DisplayItem {
    private Shop shop;
    private ItemStack iStack;
    private Item item;
    static QuickShop plugin = QuickShop.instance;
    // private Location displayLoc;

    /**
     * ZZ
     * Creates a new display item.
     *
     * @param shop   The shop (See Shop)
     * @param iStack The item stack to clone properties of the display item from.
     */
    public RealDisplayItem(Shop shop, ItemStack iStack) {
        this.shop = shop;
        this.iStack = iStack.clone();

        // this.displayLoc = shop.getLocation().clone().add(0.5, 1.2, 0.5);
    }

    /**
     * Spawns the dummy item on top of the shop.
     */
    @Override
    public void spawn() {
        if (shop.getLocation().getWorld() == null)
            return;
        Location dispLoc = this.getDisplayLocation();
        //Call Event for QSAPI

        ShopDisplayItemSpawnEvent shopDisplayItemSpawnEvent = new ShopDisplayItemSpawnEvent(shop, iStack);
        Bukkit.getPluginManager().callEvent(shopDisplayItemSpawnEvent);
        if (shopDisplayItemSpawnEvent.isCancelled()) {
            return;
        }
        this.item = shop.getLocation().getWorld().dropItem(dispLoc, this.iStack);
        this.item.setVelocity(new Vector(0, 0.1, 0));
        try {
            this.safeGuard(this.item);
            ShopDisplayItemSpawnedEvent shopDisplayItemSpawnedEvent = new ShopDisplayItemSpawnedEvent(shop, this.item);
            Bukkit.getPluginManager().callEvent(shopDisplayItemSpawnedEvent);
            // NMS.safeGuard
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().log(Level.WARNING,
                    "QuickShop version mismatch! This version of QuickShop is incompatible with this version of bukkit! Try update?");
        }
    }

    /**
     * Spawns the new display item. Does not remove duplicate items.
     */
    @Override
    public void respawn() {
        remove();
        spawn();
    }

    /**
     * Set item is QuickShop's DisplayItem and prevent them.
     *
     * @param item
     */
    @Override
    public void safeGuard(Item item) {
        item.setPickupDelay(Integer.MAX_VALUE);
        ItemMeta iMeta = item.getItemStack().getItemMeta();

        if (plugin.getConfig().getBoolean("shop.display-item-use-name")) {
            item.setCustomName("QuickShop");
            iMeta.setDisplayName("QuickShop");
        }
        item.setPortalCooldown(Integer.MAX_VALUE);
        item.setSilent(true);
        item.setInvulnerable(true);
        java.util.List<String> lore = new ArrayList<String>();
        for (int i = 0; i < 11; i++) {
            lore.add("QuickShop DisplayItem"); //Create 10 lines lore to make sure no stupid plugin accident remove mark.
        }
        iMeta.setLore(lore);
        item.getItemStack().setItemMeta(iMeta);
    }

    /**
     * Removes all items floating ontop of the chest that aren't the display
     * item.
     */
    @Override
    public boolean removeDupe() {
        if (shop.getLocation().getWorld() == null)
            return false;
        Location displayLoc = shop.getLocation().getBlock().getRelative(0, 1, 0).getLocation();
        boolean removed = false;
        Chunk c = displayLoc.getChunk();
        for (Entity e : c.getEntities()) {
            if (!(e instanceof Item))
                continue;
            if (this.item == null)
                continue;
            if (this.item != null && e.getEntityId() == this.item.getEntityId())
                continue;
            Location eLoc = e.getLocation().getBlock().getLocation();
            if (eLoc.equals(shop.getLocation()) || eLoc.equals(displayLoc)) {
                ItemStack near = ((Item) e).getItemStack();
                // if its the same its a dupe
                if (this.shop.matches(near)) {
                    e.remove();
                    removed = true;
                }
            }
        }
        return removed;

    }

    /**
     * Removes the display item.
     */
    @Override
    public void remove() {
        if (this.item == null)
            return;
        this.item.remove();
        this.item = null;
    }

    /**
     * @return Returns the exact location of the display item. (1 above shop
     * block, in the center)
     */
    @Override
    public Location getDisplayLocation() {
        return this.shop.getLocation().clone().add(0.5, 1.2, 0.5);
    }

    /**
     * Returns the reference to this shops item. Do not modify.
     */
    @Override
    public Item getItem() {
        return this.item;
    }

}