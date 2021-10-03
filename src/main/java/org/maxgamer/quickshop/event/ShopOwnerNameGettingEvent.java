package org.maxgamer.quickshop.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.maxgamer.quickshop.shop.Shop;

import java.util.UUID;

@AllArgsConstructor
@Data
public class ShopOwnerNameGettingEvent extends AbstractQSEvent {
    private Shop shop;
    private UUID owner;
    private String name;
}
