package org.maxgamer.quickshop.util.logging.container;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.maxgamer.quickshop.api.shop.ShopInfoStorage;
import org.maxgamer.quickshop.api.shop.ShopType;

import java.util.UUID;

@AllArgsConstructor
@Data
public class ShopPurchaseLog {
    private ShopInfoStorage shop;
    private ShopType type;
    private UUID trader;
    private String itemName;
    private String itemStack;
    private int amount;
    private double balance;
    private double tax;

}
