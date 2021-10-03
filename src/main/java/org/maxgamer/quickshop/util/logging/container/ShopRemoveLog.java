package org.maxgamer.quickshop.util.logging.container;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.maxgamer.quickshop.shop.ShopInfoStorage;

import java.util.UUID;

@AllArgsConstructor
@Data
public class ShopRemoveLog {
    private UUID player;
    private String reason;
    private ShopInfoStorage shop;
}
