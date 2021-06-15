package org.maxgamer.quickshop.command.processor;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

//For backward compatibility, implement CommandProcesser
public abstract class CommonCommandProcessor implements ICommandProcessor {
    /**
     * Accept the onCommand, it will call when have Command Event cmdArg not contains
     * CommandContainer's prefix. E.g: Register the CommandContainer with Prefix: unlimited
     * Permission: quickshop.unlimited
     *
     * <p>When player type /qs unlimited 123 the content of cmdArg is ["123"]
     *
     * @param sender       Sender
     * @param cmdArg       Args
     * @param commandLabel The command prefix /qs is qs
     */
    public void onCommand(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {

        throw new UnsupportedOperationException("Unimplemented command");
    }

    /**
     * Accept the onTabComplete, it will call when have Tab Event cmdArg not contains
     * CommandContainer's prefix. E.g: Register the CommandContainer with Prefix: unlimited
     * Permission: quickshop.unlimited
     *
     * <p>When player type /qs unlimited 123 cmdArg's content is 123
     *
     * @param sender       Sender
     * @param cmdArg       Args
     * @param commandLabel The command prefix /qs is qs
     * @return The result for tab-complete lists
     */
    @Nullable
    public List<String> onTabComplete(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return Collections.emptyList();
    }
}
