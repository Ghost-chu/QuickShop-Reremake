package org.maxgamer.quickshop.util.record.log;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.shop.Shop;
@Data
@Builder
public class ShopDeletionRecord implements Recordable {
    private Shop shop;
    private Reason reason;
    @Nullable
    private String extra;
    public enum Reason{
        BLOCK_BREAK,
        CLEAN,
        REMOVE,
    }
}
