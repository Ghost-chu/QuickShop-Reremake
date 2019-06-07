package org.maxgamer.quickshop.Shop;

import lombok.*;
import org.jetbrains.annotations.*;

@Getter
public class ShopChunk {
    private String world;
    private int x;
    private int z;
    private int hash = 0;

    public ShopChunk(@NotNull String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
        this.hash = this.x * this.z;    // We don't need to use the world's hash,
        // as these are seperated by world in
        // memory
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        if (obj.getClass() != this.getClass()) {
            return false;
        } else {
            ShopChunk shopChunk = (ShopChunk) obj;
            return (this.getWorld().equals(shopChunk.getWorld()) && this.getX() == shopChunk.getX() && this.getZ() == shopChunk
                    .getZ());
        }
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
