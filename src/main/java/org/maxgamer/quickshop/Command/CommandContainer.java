package org.maxgamer.quickshop.Command;

import java.util.List;

import lombok.*;

@Data
@Builder
public class CommandContainer {
    private CommandProcesser executor;
    private boolean hidden; // Hide from help, tabcomplete
    @Singular private List<String> permissions; // E.g quickshop.unlimited
    private String prefix; // E.g /qs <prefix>
}
