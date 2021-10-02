package org.maxgamer.quickshop.util.logging.container;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;
@AllArgsConstructor
@Data
public class PlayerEconomyPreCheckLog {
    private boolean beforeTrading;
    private UUID player;
    private double holding;
}
