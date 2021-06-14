package org.maxgamer.quickshop.command.processor;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.command.CommandProcesser;

import java.util.Collections;
import java.util.List;

//For backward compatibility, extended CommandProcesser
interface ICommandProcessor extends CommandProcesser {
    void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg);

    @Nullable
    default List<String> onTabComplete(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return Collections.emptyList();
    }
}
