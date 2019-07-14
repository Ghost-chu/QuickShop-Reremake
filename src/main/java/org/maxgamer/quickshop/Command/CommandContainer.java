package org.maxgamer.quickshop.Command;

import java.util.List;

import lombok.*;

@Data
@Builder
public class CommandContainer {
    private String prefix; // E.g /qs <prefix>
    @Singular private List<String> permissions; // E.g quickshop.unlimited
    private boolean hidden; // Hide from help, tabcomplete
    private CommandProcesser executor;
}
