package org.maxgamer.quickshop.Permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public class PermissionInfomationContainer {
    @NotNull
    private CommandSender sender;
    @NotNull
    private String permission;
    @Nullable
    private String groupName;
    @Nullable
    private String otherInfos;

    /**
     * Get sender is console
     *
     * @return yes or no
     */
    public boolean isConsole() {
        return sender instanceof Server;
    }
}
