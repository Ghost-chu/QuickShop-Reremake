package org.maxgamer.quickshop.Command;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.*;

public interface CommandProcesser {
    /**
     * Accept the onCommand, it will call when have Command Event
     * cmdArg not contains CommandContainer's prefix.
     * E.g:
     * Register the CommandContainer with
     * Prefix: unlimited
     * Permission: quickshop.unlimited
     * <p>
     * When player type /qs unlimited 123
     * cmdArg's content is 123
     *
     * @param sender Sender
     * @param cmdArg Args
     * @param commandLabel The command prefix /qs is qs
     */
    void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg);

    /**
     * Accept the onTabComplete, it will call when have Tab Event
     * cmdArg not contains CommandContainer's prefix.
     * E.g:
     * Register the CommandContainer with
     * Prefix: unlimited
     * Permission: quickshop.unlimited
     * <p>
     * When player type /qs unlimited 123
     * cmdArg's content is 123
     *
     * @param sender Sender
     * @param cmdArg Args
     * @param commandLabel The command prefix /qs is qs
     * @return The result for tab-complete lists
     */
    @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg);
}
