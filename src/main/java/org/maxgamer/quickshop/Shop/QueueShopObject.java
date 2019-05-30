package org.maxgamer.quickshop.Shop;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class QueueShopObject {
    /**
     * The shop you want to do actions
     */
    private Shop shop;
    /**
     * The actions you want to do
     */
    private QueueAction[] action;
}
