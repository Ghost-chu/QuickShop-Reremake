package org.maxgamer.quickshop.Shop.DisplayItem;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Shop.Shop;

public class VirtualDisplayItem extends DisplayItem {


    public VirtualDisplayItem(@NotNull Shop shop) {
        super(shop);
    }

    @Override
    public boolean checkDisplayIsMoved() {
        return false;
    }

    @Override
    public boolean checkDisplayNeedRegen() {
        return false;
    }

    @Override
    public boolean checkIsShopEntity(Entity entity) {
        return false;
    }

    @Override
    public void fixDisplayMoved() {

    }

    @Override
    public void fixDisplayNeedRegen() {

    }

    @Override
    public void remove() {

    }

    @Override
    public boolean removeDupe() {
        return false;
    }

    @Override
    public void respawn() {

    }

    @Override
    public void safeGuard(Entity entity) {

    }

    @Override
    public void spawn() {

    }

    @Override
    public Entity getDisplay() {
        return null;
    }

    @Override
    public Location getDisplayLocation() {
        return null;
    }

    @Override
    public boolean isSpawned() {
        return false;
    }
}
