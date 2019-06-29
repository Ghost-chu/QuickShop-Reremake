package org.maxgamer.quickshop.Command;

import lombok.*;

@Data
@Builder
public class CommandContainer {
    private String prefix; // E.g /qs <prefix>
    private String permission; // E.g quickshop.unlimited
    private boolean hidden; // Hide from help, tabcomplete
    private CommandProcesser executor;
}
