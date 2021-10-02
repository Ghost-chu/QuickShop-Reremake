package org.maxgamer.quickshop.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.maxgamer.quickshop.shop.Shop;
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class ShopInventoryCalculate extends QSEvent {
    private Shop shop;
    private int space;
    private int stock;
}
