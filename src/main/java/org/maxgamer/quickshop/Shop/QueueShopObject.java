package org.maxgamer.quickshop.Shop;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class QueueShopObject {
    private Shop shop;
    private QueueAction[] action;
}
