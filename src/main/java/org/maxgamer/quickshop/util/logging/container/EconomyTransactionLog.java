package org.maxgamer.quickshop.util.logging.container;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.maxgamer.quickshop.economy.EconomyTransaction;

@AllArgsConstructor
@Builder
@Data
public class EconomyTransactionLog {
    private boolean success;
    private EconomyTransaction transaction;
}
