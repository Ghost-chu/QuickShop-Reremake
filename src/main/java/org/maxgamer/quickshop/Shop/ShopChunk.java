package org.maxgamer.quickshop.Shop;

import lombok.*;
import org.jetbrains.annotations.*;

@Getter
@EqualsAndHashCode
public class ShopChunk {
    private String world;
    private int x;
    private int z;

    public ShopChunk(@NotNull String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }
}
